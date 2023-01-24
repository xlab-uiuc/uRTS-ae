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

package org.urts.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.urts.Config;
import org.urts.configAware.ConfigListener;
import org.urts.configAware.ConfigLoader;
import org.urts.configAware.ConfigMapping;
import org.urts.hash.Hasher;
import org.urts.log.Log;
import org.urts.monitor.CoverageMonitor;
import org.urts.research.Research;
import org.urts.util.LRUMap;

/**
 * Analyzes regression data to check if resource has been modified.
 */
public final class DependencyAnalyzer {

    /** "Method" name when saving coverage per test class */
    public static final String CLASS_EXT = "clz";

    /** "Method" name when storing coverage for arbitrary run */
    public static final String COV_EXT = "cov";

    /** Cache: file->modified (note that we limit the size); cannot switch to Set */
    private final Map<String, Boolean> mUrlExternalForm2Modified;

    /** Cache of test->run mappings; useful if there are Parameterized tests (note that we limit the size) */
    private final Map<String, Boolean> mFullTestName2Rerun;

    /** IO Storer */
    private final Storer mStorer;

    /** Hasher */
    private final Hasher mHasher;

    /** A test (full name) that starts with any element in array is always run */
    private final String[] mExcludes;

    /** A test (full name) that starts with any element in array is run with Tool */
    private final String[] mIncludes;

    /** current dir */
    private final String mCurDir;

    /** next dir */
    private final String mNextDir;


    /** dependencies.append */
    private final boolean mDependenciesAppend;

    /** Flag to indicate configuration's value is got from default-reset */
    private final String configDefaultFlag = "@DEFAULTVALUE4CONFIGAWARE@";

    /** Flag to separator configuration's value that got from several APIs */
    private final String configValueSeparator = "@CONFIGAWARESEPARATOR@";

    /**
     * Constructor.
     */
    public DependencyAnalyzer(int cacheSizes, Hasher hasher, Storer storer, String[] excludes, String[] includes) {
        this.mStorer = storer;
        this.mHasher = hasher;
        this.mExcludes = excludes;
        this.mIncludes = includes;

        Config.prepareRound();
        this.mCurDir = Config.getCurDirName();
        this.mNextDir = Config.getNextDirName();
        //this.mRootDir = Config.ROOT_DIR_V;
        this.mDependenciesAppend = Config.DEPENDENCIES_APPEND_V;

        this.mUrlExternalForm2Modified = new LRUMap<String, Boolean>(cacheSizes);
        this.mFullTestName2Rerun = new LRUMap<String, Boolean>(cacheSizes);
    }

    public synchronized void beginCoverage(String name) {
        beginCoverage(name, COV_EXT, false);
    }

    public synchronized void endCoverage(String name) {
        endCoverage(name, COV_EXT);
    }

    public synchronized boolean isAffected(String name) {
        String fullMethodName = name + "." + COV_EXT;
        Set<RegData> regData = mStorer.loadRegData(mCurDir, name, COV_EXT);
        Map<String, String> configMap = mStorer.loadConfigData(mCurDir, name, COV_EXT);
        boolean isAffected = isAffected(regData, mCurDir, name) || isAffected(configMap, mCurDir, name);
        recordTestAffectedOutcome(fullMethodName, isAffected);
        return isAffected;
    }

    /**
     * This method should be invoked to indicate that coverage measurement
     * should start. In JUnit it is expected that this method will be invoked to
     * measure coverage for a test class.
     *
     * @param className
     *            Tag used to identify this coverage measure.
     */
    public synchronized void beginClassCoverage(String className) {
        if (isOnMustRunList(className)) {
            return;
        }
        // Note that we do not record affected outcome is already recorded
        // when checked if class is affected.
        beginCoverage(className, CLASS_EXT, false);
    }

    /**
     * This method should be invoked to indicated that coverage measurement
     * should end. In JUnit it is expected that this method will be invoked to
     * measure coverage for a test class.
     *
     * @param className
     *            Tag used to identify this coverage measure.
     */
    public synchronized void endClassCoverage(String className) {
        if (isOnMustRunList(className)) {
            return;
        }
        endCoverage(className, CLASS_EXT);
    }

    /**
     * Checks if class is affected since the last run.
     *
     * @param className
     *            Name of the class.
     * @return True if class if affected, false otherwise.
     *
     * TODO: this method and starting coverage do some duplicate work
     */
    public synchronized boolean isClassAffected(String className) {
        if (isOnMustRunList(className)) {
            return true;
        }
        boolean isAffected = true;
        String fullMethodName = className + "." + CLASS_EXT;
        Set<RegData> regData = mStorer.loadRegData(mCurDir, className, CLASS_EXT);
        Map<String, String> configMap = mStorer.loadConfigData(mCurDir, className, CLASS_EXT);
        isAffected = isAffected(regData, mCurDir, className) || isAffected(configMap, mCurDir, className);
        recordTestAffectedOutcome(fullMethodName, isAffected);
        return isAffected;
    }

    // INTERNAL

    /**
     * We identify cases that are currently (almost) impossible to
     * support purely from Java.
     *
     * org.apache.log4j.net.SocketServerTestCase (from Log4j project)
     * test is run/specified in build.xml.  The test runs a Java
     * process and test in parallel (two VMs).  In addition Java
     * process takes as input the number of tests that would be
     * executed.  Of course this would fail in the first place because
     * of the number of tests.  However, that is not the only problem;
     * Java process is a server that waits for connection, so if no
     * test is executed this process would just be alive forever.
     * See http://svn.apache.org/repos/asf/logging/log4j/trunk
     * (revision 1344108) and file tests/build.xml for details.
     */
    @Research
    private boolean isOnMustRunList(String className) {
        return className.equals("org.apache.log4j.net.SocketServerTestCase");
    }

    private boolean beginCoverage(String className, String methodName, boolean isRecordAffectedOutcome) {
        // Fully qualified method name.
        String fullMethodName = className + "." + methodName;

        // Clean previously collected coverage.
        CoverageMonitor.clean();

        // Clean previously configuration collection.
        ConfigListener.clean();

        // Check if test is included (note that we do not record info).
        if (!isIncluded(fullMethodName)) {
            return true;
        }

        // Check if test should be always run.
        if (isExcluded(fullMethodName)) {
            if (isRecordAffectedOutcome) {
                recordTestAffectedOutcome(fullMethodName, true);
            }
            return true;
        }

        // Check if test has been seen; this can happen when a test is in
        // Parameterized class or if the same test is invoked twice (which is
        // present in some projects: as part of a test suite and separate).
        // We force the execution as the execution may differ and we union
        // the coverage (load the old one and new one will be appended).
        if (mFullTestName2Rerun.containsKey(fullMethodName)) {
            Set<RegData> regData = mStorer.loadRegData(mCurDir, className, methodName);
            // Shuai: Load previous round's coverage to the current Coverage Monitor
            // So that old and new coverage will append to this round.
            CoverageMonitor.addURLs(extractExternalForms(regData));
            return mFullTestName2Rerun.get(fullMethodName);
        }

        Set<RegData> regData = mStorer.loadRegData(mCurDir, className, methodName);
        Map<String, String> configMap = mStorer.loadConfigData(mCurDir, className, methodName);
        boolean isAffected = isAffected(regData, mCurDir, className) || isAffected(configMap, mCurDir, className);
        if (isRecordAffectedOutcome) {
            recordTestAffectedOutcome(fullMethodName, isAffected);
        }

        // If in append mode add urls; we used this append mode when we noticed that
        // some runs give non-deterministic coverage, so we wanted to run the same
        // test several times and do union of coverage.
        if (mDependenciesAppend) {
            CoverageMonitor.addURLs(extractExternalForms(regData));
            // Force run
            isAffected = true;
        }

        // Collect tests that have been affected.
        mFullTestName2Rerun.put(fullMethodName, isAffected);

        return isAffected;
    }

    private boolean isIncluded(String fullMethodName) {
        boolean isIncluded = false;
        if (mIncludes != null) {
            for (String s : mIncludes) {
                if (fullMethodName.startsWith(s)) {
                    isIncluded = true;
                    break;
                }
            }
        } else {
            isIncluded = true;
        }
        return isIncluded;
    }

    /**
     * Checks if user requested that the given name is always run. True if this
     * name has to be always run, false otherwise.
     */
    private boolean isExcluded(String fullMethodName) {
        if (mExcludes != null) {
            for (String s : mExcludes) {
                if (fullMethodName.startsWith(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String[] extractExternalForms(Set<RegData> regData) {
        String[] externalForms = new String[regData.size()];
        int i = 0;
        for (RegData el : regData) {
            externalForms[i] = el.getURLExternalForm();
            i++;
        }
        return externalForms;
    }

    private void endCoverage(String className, String methodName) {
        Map<String, String> hashes = mHasher.hashExternalForms(CoverageMonitor.getURLs());
        Set<RegData> regData = new TreeSet<RegData>(new RegData.RegComparator());
        for (Entry<String, String> entry : hashes.entrySet()) {
            regData.add(new RegData(entry.getKey(), entry.getValue()));
        }
        //Config-aware mapping information
        Map<String, String> configMap = ConfigListener.getConfigMap();
        if (new File(mCurDir).exists()) {
            mStorer.save(mCurDir, className, methodName, regData, configMap);
        } else {
            mStorer.save(mNextDir, className, methodName, regData, configMap);
        }
        CoverageMonitor.clean();
    }

    /**
     * Returns true if test is affected. Test is affected if hash of any
     * resource does not match old hash.
     */
    private boolean isAffected(Set<RegData> regData, String dirName, String className) {
        return regData == null || regData.size() == 0 || hasHashChanged(regData, dirName, className);
    }

    protected boolean isAffected(Map<String, String> configMap, String dirName, String className) {
        return configMap != null && !configMap.isEmpty() && hasConfigChanged(configMap, dirName, className);
    }

    /**
     * Check if the configuration has changed.
     *
     */
    private boolean hasConfigChanged(Map<String, String> configMap, String dirName, String className) {
        Map<String, String> ChangedConfig = ConfigLoader.getChangedConfigMap();
        if (ChangedConfig == null) {
            Log.configDiffLog(Config.curWorkingDir(), "", "", "", "Failed to get user configuration", className);
            //Log.d2f("[ERROR] hasConfigChanged(): Failed to get user configuration");
            return true;
        }

        // For evaluation
        Boolean diff = false;
        // For every configuration pairs, compare with user's setting.
//        for(Map.Entry<String, String> entry : configMap.entrySet()) {
//            String key = entry.getKey();
//            String value = entry.getValue();
//
//            // (1) If user doesn't set, return true;
//            if (!ChangedConfig.containsKey(key)) {
//                Log.configDiffLog(key, value, "", "User didn't set this config", className);
//                diff = true;
//            }
//
//            // (2) If user's setting is different, return true;
//            if (!ChangedConfig.get(key).equals(value)) {
//                Log.configDiffLog(key, value, ChangedConfig.get(key), "Value different!", className);
//                Log.d2f("Diff!! Key = " + key + " value = " + value + " / " + ChangedConfig.get(key));
//                diff = true;
//            }
//        }

        String testNames [] = className.split("\\.");
        String testName = testNames[testNames.length - 1];
        Set<String> CtestMappingConfigs = new HashSet<>();
        if (ConfigMapping.getOnlyTestNameMapping().containsKey(testName)) {
            //Log.d2f("[INFO] " + testName + " Mapping load successfully for comparison");
            CtestMappingConfigs = ConfigMapping.getOnlyTestNameMapping().get(testName);
        }
        for (Map.Entry<String, String> entry : ChangedConfig.entrySet()) {
            String key = entry.getKey();
            String [] userValues = entry.getValue().split(configValueSeparator);
            // (1) If this config is not used by test, continue
            if (!configMap.containsKey(key)) {
                continue;
            }

            // (2) If this config is in ctest mapping (can't be tested under current test), continue
            if (CtestMappingConfigs.contains(key)) {
                //Log.d2f("[INFO] " + key + " is skipped for " + testName + " comparison due to ctest mapping");
                continue;
            }

            String depValue = configMap.get(key);

            // (3) If user's setting is different, return true
            Boolean atLeastOneValueSame = false;
            for (String userValue : userValues) {
                if (!depValue.equals(userValue)) {
                    if (depValue.contains(configDefaultFlag)) {
                        if (userValue.equals("null")) {
                            atLeastOneValueSame = true;
                        } else if (userValue.equals(depValue.replace(configDefaultFlag, ""))) {
                            atLeastOneValueSame = true;
                        }
                    }
                } else {
                    atLeastOneValueSame = true;
                }
            }
            if (!atLeastOneValueSame) {
                Log.configDiffLog(Config.curWorkingDir(), key, configMap.get(key), entry.getValue(), "Value different in AbstractCheck! Compared with " + dirName, className);
                diff = true;
            }
        }

        if (diff) {
            return true;
        }
        // (4) else return false;
        return false;
    }


    /**
     * Hashes files and compares with the old hashes. If any hash is different,
     * returns true; false otherwise.
     */
    private boolean hasHashChanged(Set<RegData> regData, String dirName, String className) {
        for (RegData el : regData) {
            if (hasHashChanged(mHasher, el)) {
                Log.codeDiffLog(Config.curWorkingDir(), el.getURLExternalForm(), dirName, className, " Code diff in DepAnalyzer");
                Log.d("CHANGED", el.getURLExternalForm());
                return true;
            }
        }
        return false;
    }

    /**
     * Hashes file and compares with the old hash. If hashes are different,
     * return true; false otherwise
     */
    private boolean hasHashChanged(Hasher hasher, RegData regDatum) {
        String urlExternalForm = regDatum.getURLExternalForm();
        Boolean modified = mUrlExternalForm2Modified.get(urlExternalForm);
        if (modified != null) {
            return modified;
        }
        // Check hash
        String newHash = hasher.hashURL(urlExternalForm);
        modified = !newHash.equals(regDatum.getHash());
        mUrlExternalForm2Modified.put(urlExternalForm, modified);
        return modified;
    }

    @Research
    private void recordTestAffectedOutcome(String fullMethodName, boolean isAffected) {
        if (!Config.X_LOG_RUNS_V) {
            return;
        }
        try {
            File f = new File(Config.RUN_INFO_V);
            f.getParentFile().mkdirs();
            PrintWriter pw = new PrintWriter(new FileOutputStream(f, true));
            pw.println(mCurDir + "." + fullMethodName + " " + (isAffected ? "RUN" : "SKIP"));
            pw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
