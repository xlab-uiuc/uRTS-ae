/*
 * Copyright 2014-present Milos Gligoric
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.urts.maven;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import org.urts.Config;
import org.urts.Names;
import org.urts.check.AffectedChecker;
import org.urts.log.Log;
import org.urts.util.FileUtil;

/**
 * Monitor invoked before Surefire. The purpose of monitoring is to
 * adjust the arguments for Surefire to integrate Ekstazi at
 * appropriate places.
 */
public final class SurefireMojoInterceptor extends AbstractMojoInterceptor {

    // Methods in Surefire Mojo.
    private static final String GET_FORK_COUNT = "getForkCount";
    private static final String IS_REUSE_FORKS = "isReuseForks";
    // Fields in Surefire Mojo.
    private static final String EXCLUDES_FIELD = "excludes";
    private static final String PARALLEL_FIELD = "parallel";
    private static final String FORK_MODE_FIELD = "forkMode";

    // Used to check Surefire version (2.7 or higher).
    @SuppressWarnings("unused")
    private static final String GET_RUN_ORDER = "getRunOrder";
    // Used to check Surefire version (2.4 or higher).
    private static final String SKIP_TESTS_FIELD = "skipTests";

    /**
     * This method is invoked before SurefirePlugin execute method.
     *
     * @param mojo
     *            Surefire plugin.
     * @throws Exception
     *             Always MojoExecutionException.
     */
    public static void execute(Object mojo) throws Exception {
        // Note that the object can be an instance of
        // AbstractSurefireMojo.
        if (!(isSurefirePlugin(mojo) || isFailsafePlugin(mojo))) {
            return;
        }
        // Check if the same object is already invoked.  This may
        // happen (in the future) if execute method is in both
        // AbstractSurefire and SurefirePlugin classes.
        if (isAlreadyInvoked(mojo)) {
            return;
        }

        // Check if surefire version is supported.
        checkSurefireVersion(mojo);
        // Check surefire configuration.
        checkSurefireConfiguration(mojo);

        try {
            // Update argLine.
            updateArgLine(mojo);
            // Update excludes.
            if (!Boolean.valueOf(System.getProperty("retestall"))) {
                updateExcludes(mojo);
            }
            updateParallel(mojo);
        } catch (Exception ex) {
            // This exception should not happen in theory.
            throwMojoExecutionException(mojo, "Unsupported surefire version", ex);
        }
    }

    // INTERNAL

    private static boolean isSurefirePlugin(Object mojo) throws Exception {
        return mojo.getClass().getName().equals(MavenNames.SUREFIRE_PLUGIN_BIN);
    }

    private static boolean isFailsafePlugin(Object mojo) throws Exception {
        return mojo.getClass().getName().equals(MavenNames.FAILSAFE_PLUGIN_BIN);
    }

    private static boolean isAlreadyInvoked(Object mojo) throws Exception {
        String key = Names.TOOL_NAME + System.identityHashCode(mojo);
        String value = System.getProperty(key);
        System.setProperty(key, "seen");
        return value != null;
    }

    /**
     * Checks that Surefire has all the metohds that are needed, i.e., check
     * Surefire version. Currently we support 2.7 and newer.
     *
     * @param mojo
     *            Surefire plugin
     * @throws Exception
     *             MojoExecutionException if Surefire version is not supported
     */
    private static void checkSurefireVersion(Object mojo) throws Exception {
        try {
            // Check version specific methods/fields.
            // mojo.getClass().getMethod(GET_RUN_ORDER);
            getField(SKIP_TESTS_FIELD, mojo);
            getField(FORK_MODE_FIELD, mojo);

            // We will always require the following.
            getField(ARGLINE_FIELD, mojo);
            getField(EXCLUDES_FIELD, mojo);
        } catch (NoSuchMethodException ex) {
            throwMojoExecutionException(mojo, "Unsupported surefire version.  An alternative is to use select/restore goals.", ex);
        }
    }

    /**
     * Checks that surefire configuration allows integration with
     * Ekstazi.  At the moment, we check that forking is enabled.
     */
    private static void checkSurefireConfiguration(Object mojo) throws Exception {
        String forkCount = null;
        try {
            forkCount = invokeAndGetString(GET_FORK_COUNT, mojo);
        } catch (NoSuchMethodException ex) {
            // Nothing: earlier versions (before 2.14) of surefire did
            // not have forkCount.
        }
        String forkMode = null;
        try {
            forkMode = getStringField(FORK_MODE_FIELD, mojo);
        } catch (NoSuchMethodException ex) {
            // Nothing: earlier versions (before 2.1) of surefire did
            // not have forkMode.
        }
        if ((forkCount != null && forkCount.equals("0"))
                || (forkMode != null && (forkMode.equals("never") || forkMode.equals("none")))) {
            throwMojoExecutionException(mojo,
                    "Fork has to be enabled when running tests with Ekstazi; check forkMode and forkCount parameters.",
                    null);
        }
    }

    private static void updateArgLine(Object mojo) throws Exception {
        Config.AgentMode junitMode = isJupiterInPom() ? (isOneVMPerClass(mojo) ? Config.AgentMode.JUNIT5FORK : Config.AgentMode.JUNIT5EXTENSION) :
                (isOneVMPerClass(mojo) ? Config.AgentMode.JUNITFORK : Config.AgentMode.JUNIT);
        if (Boolean.valueOf(System.getProperty("retestall"))) {
            junitMode = isJupiterInPom()? Config.AgentMode.JUNIT5RETESTALL : Config.AgentMode.JUNIT4RETESTALL;
        }
        String currentArgLine = (String) getField(ARGLINE_FIELD, mojo);
        String newArgLine = makeArgLine(mojo, junitMode, currentArgLine);
        setField(ARGLINE_FIELD, mojo, newArgLine);
    }

    private static void updateExcludes(Object mojo) throws Exception {
        // Get excludes set by the user (in pom.xml in Surefire).
        List<String> currentExcludes = getListField(EXCLUDES_FIELD, mojo);
        List<String> ekstaziExcludesFromPrev = new ArrayList<String>(Arrays.asList(System.getProperty(EXCLUDES_INTERNAL_PREV_PROP)
                .replace("[", "").replace("]", "").split(",")));
        List<String> ekstaziExcludesFromCurRound = new ArrayList<String>(Arrays.asList(System.getProperty(EXCLUDES_INTERNAL_CURROUND_PROP)
                .replace("[", "").replace("]", "").split(",")));
        List<String> ekstaziExcludesFromCurRoundWithoutRound = new ArrayList<>();
        for(String classNameWithRound : ekstaziExcludesFromCurRound) {
            String className = classNameWithRound.split(AffectedChecker.ROUND_SEPARATOR)[0];
            ekstaziExcludesFromCurRoundWithoutRound.add(className);
        }

        List<String> newExcludes = new ArrayList<String>();
        if (!ekstaziExcludesFromPrev.isEmpty()) {
            newExcludes.addAll(ekstaziExcludesFromPrev);
        }

        if (!ekstaziExcludesFromCurRoundWithoutRound.isEmpty()) {
            newExcludes.addAll(ekstaziExcludesFromCurRoundWithoutRound);
        }

        if (currentExcludes != null) {
            newExcludes.addAll(currentExcludes);
        } else {
            // Add default excludes as specified by Surefire if excludes is not provided by the user.
            newExcludes.add("**/*$*");
        }
        //Log.d2f("[INFO] Print Exclude Test Name:");
        //Log.d2f(newExcludes);
        setField(EXCLUDES_FIELD, mojo, newExcludes);

        List<String> copyDependencyFromPrevList = new ArrayList<>();
        List<String> copyDependencyFromCurRoundList = new ArrayList<>();
        copyDependencyFromPrevList.addAll(ekstaziExcludesFromPrev);
        copyDependencyFromCurRoundList.addAll(ekstaziExcludesFromCurRound);  // classNameWithRound
        copyFromPrev(copyDependencyFromPrevList, copyDependencyFromCurRoundList);
    }

    private static boolean wasFailing(String className) {
        className = className.replace(".java","").replace("/",".");
        File testResultsDir = new File(Config.CUR_DIR_V, Names.TEST_RESULTS_DIR_NAME);
        File outcomeFile = new File(testResultsDir, className);
        return outcomeFile.exists();
    }

    /**
     * Sets parallel parameter to null if the parameter is different from null
     * and prints a warning.
     *
     * This method is written in Shanghai'14.
     */
    private static void updateParallel(Object mojo) throws Exception {
        try {
            String currentParallel = getStringField(PARALLEL_FIELD, mojo);
            if (currentParallel != null) {
                warn(mojo, "Ekstazi does not support parallel parameter.  This parameter will be set to null for this run.");
                setField(PARALLEL_FIELD, mojo, null);
            }
        } catch (NoSuchMethodException ex) {
            // "parallel" was introduced in Surefire 2.2, so methods
            // may not exist, but we do not fail because default is
            // sequential execution.
        }
    }

    /**
     * Returns true if one test class is executed in one VM.
     *
     * @param mojo
     *            Mojo
     * @return True if one test class is executed in one VM, false otherwise
     * @throws Exception
     *             If value of the fields cannot be retrieved
     */
    private static boolean isOneVMPerClass(Object mojo) throws Exception {
        // We already know that we fork process (checked in
        // precondition). Threfore if we see reuseForks=false, we know
        // that one class will be run in one VM.
        try {
            return !invokeAndGetBoolean(IS_REUSE_FORKS, mojo);
        } catch (NoSuchMethodException ex) {
            // Nothing: earlier versions (before 2.13) of surefire did
            // not have reuseForks.
            return false;
        }
    }

    public static boolean isJupiterInPom() throws IOException {
        try {
            String pomPath = new java.io.File(".").getCanonicalPath().concat("/pom.xml");
            BufferedReader br = new BufferedReader(new FileReader(pomPath));
            String line;
            while((line = br.readLine()) != null) {
                if (line.contains("<groupId>org.junit.jupiter</groupId>")) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     *
     * @param prevDepList  Dependency data from previous self round
     * @param curRoundDepList Dependency data in the current round from the others (Class is named with XXXTest.java#.ekstazi-{CONFIG}-Round{i})
     * @throws IOException
     */
    public static void copyFromPrev(List<String> prevDepList, List<String> curRoundDepList) throws IOException {
        Config.prepareRound();
        File prevDependencyDir = new File(Config.getCurDirName());
        File rootDir = new File(Config.getCurRoot());
        //No non-affected class.
        boolean noNonAffectedClass = true;
        boolean prev = false;
        boolean cur = false;
        for(String className : prevDepList) {
            if (className.contains(".java")) {
                noNonAffectedClass = false;
                prev = true;
                break;
            }
        }
        for (String className : curRoundDepList) {
            if (className.contains(".java")) {
                noNonAffectedClass = false;
                cur = true;
                break;
            }
        }

        if(noNonAffectedClass) {
            File nextDependencyDir = new File(Config.getNextDirName());
            nextDependencyDir.mkdir();
            FileUtil.deleteDirectory(prevDependencyDir);
            return;
        }

        File nextDependencyDir = new File(Config.getNextDirName());
        if (nextDependencyDir.mkdir()) {
            if (prev && prevDependencyDir.exists()) {
                // Previous failed tests are not rerun, so we need to copy the failure report into the new folder
                if (!Config.FORCE_FAILING_V) {
                    File prevTestResultsDir = new File(prevDependencyDir, Names.TEST_RESULTS_DIR_NAME);
                    File nextTestResultDir = new File(nextDependencyDir, Names.TEST_RESULTS_DIR_NAME);
                    if (prevTestResultsDir.exists() && prevTestResultsDir.isDirectory()) {
                        if (nextTestResultDir.mkdir()) {
                            for(File f : prevTestResultsDir.listFiles()) {
                                Path source = Paths.get(f.getAbsolutePath());
                                Path target = Paths.get(nextTestResultDir.getAbsolutePath(), f.getName());
                                Files.move(source, target);
                            }
                        } else {
                            //Log.d2f("[ERROR] CopyFromPrevious: Failed to create test result folder!");
                            throw new IOException("Failed to create test result folder!");
                        }
                    }
                }
                for (String className : prevDepList) {
                    if (!className.contains(".java") || className.contains("**")) {
                        continue;
                    }
                    String fileName = className.trim().replace(".java", ".clz").replace("/", ".");
                    Path source = Paths.get(prevDependencyDir.getAbsolutePath(), fileName);
                    Path target = Paths.get(nextDependencyDir.getAbsolutePath(), fileName);
                    Files.move(source, target);
                }
                FileUtil.deleteDirectory(prevDependencyDir);
            }
            if (cur) {
                for (String classNameWithRound : curRoundDepList) {
                    if (!classNameWithRound.contains(AffectedChecker.ROUND_SEPARATOR)) {
                        continue;
                    }
                    if (!classNameWithRound.contains(".java") || classNameWithRound.contains("**")) {
                        continue;
                    }
                    String strList[] = classNameWithRound.trim().split(AffectedChecker.ROUND_SEPARATOR);
                    String fileName = strList[0].replace(".java", ".clz").replace("/", ".");
                    String depDirName = strList[1];
                    File sourceDepDir = new File(rootDir, depDirName);
                    if (sourceDepDir.exists() && sourceDepDir.isDirectory()) {
                        Path source = Paths.get(sourceDepDir.getAbsolutePath(), fileName);
                        Path target = Paths.get(nextDependencyDir.getAbsolutePath(), fileName);
                        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
                FileUtil.deleteDirectory(prevDependencyDir);
            }
        } else {
            //Log.d2f("[ERROR] CopyFromPrevious: Can't create new round dependency folder!");
            throw new IOException("Can't create new round dependency folder!");
        }
    }
}
