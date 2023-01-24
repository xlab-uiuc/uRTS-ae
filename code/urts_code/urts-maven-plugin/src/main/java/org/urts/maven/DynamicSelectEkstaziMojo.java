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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugins.annotations.ResolutionScope;
import org.urts.Config;
import org.urts.agent.AgentLoader;

/**
 * Implements selection process and integrates with Surefire.  This
 * mojo does not require any changes in configuration to any plugin
 * used during the build.  The goal implemented here is an alternative
 * to using "select" and "restore" goals, which require some changes
 * to Surefire configuration.
 */
@Mojo(name = "select",
        requiresDependencyResolution= ResolutionScope.TEST,
        defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES)
public class DynamicSelectEkstaziMojo extends StaticSelectEkstaziMojo {

//
//    @Parameter(property = "ekstazi.configfilename", defaultValue = "core")
//    private String configfilename;
//
//    public String getConfigFileName() {
//        return configfilename;
//    }

    public void execute() throws MojoExecutionException {
        long startTime = System.currentTimeMillis();
        if (getSkipme()) {
            getLog().info("Ekstazi is skipped.");
            return;
        }
        if (getSkipTests()) {
            getLog().info("Tests are skipped.");
            return;
        }

        checkIfEkstaziDirCanBeCreated();
        runConfigValueGetterTestThroughSurefire();
        preCheckConfigAwareFiles();

        if (isRestoreGoalPresent()) {
            super.execute();
        } else {
            executeThis();
        }
        long endTime = System.currentTimeMillis();
        double timeElapsed = (endTime - startTime) / 1000.0;

//        String [] folder = Config.getNextDirName().split("/");
//        String folderName = folder[folder.length - 1];
//        MojoLog.timerLog(folderName, timeElapsed);
        getLog().info(evalStrPrefix + "ANALYSIS_TIME: " + timeElapsed);
    }

    // INTERNAL

    /**
     * Checks if .ekstazi directory can be created. For example, the
     * problems can happen if there is no sufficient permission.
     */
    private void checkIfEkstaziDirCanBeCreated() throws MojoExecutionException {
        File curEkstaziDir = Config.createCurDir(parentdir);
        File nextEkstaziDir = Config.createNextDir(parentdir);
        // If .ekstazi does not exist and cannot be created, let them
        // know.  (We also remove directory if successfully created.)
        // !curEkstaziDir.exists()false !curEkstaziDir.mkdirs()true !curEkstaziDir.delete()true curEkstaziDir.exists()true !nextEkstaziDir.mkdir() true !nextEkstaziDir.delete()true
        if ((!curEkstaziDir.exists() && (!curEkstaziDir.mkdirs() || !curEkstaziDir.delete()))
            || (curEkstaziDir.exists()) && (!nextEkstaziDir.mkdir() || !nextEkstaziDir.delete())) {
            //MojoLog.d2f("!curEkstaziDir.exists()" + !curEkstaziDir.exists() + " !curEkstaziDir.mkdirs()" + !curEkstaziDir.mkdirs() + " !curEkstaziDir.delete()"  +
            //        !curEkstaziDir.delete() + " curEkstaziDir.exists()" + curEkstaziDir.exists() + " !nextEkstaziDir.mkdir() " + !nextEkstaziDir.mkdir() +
            //        " !nextEkstaziDir.delete()" + !nextEkstaziDir.delete());
            throw new MojoExecutionException("Cannot create Ekstazi directory in " + parentdir);
        }
    }

    /**
     * Implements 'select' that does not require changes to any
     * existing plugin in configuration file(s).
     */
    private void executeThis() throws MojoExecutionException {
        // Try to attach agent that will modify Surefire.
        if (AgentLoader.loadEkstaziAgent()) {
            // Prepare initial list of options and set property.
            System.setProperty(AbstractMojoInterceptor.ARGLINE_INTERNAL_PROP, prepareEkstaziOptions());
            // Find non affected classes and set property.
            List<List<String>> nonAffectedClassesList = computeNonAffectedClassesForDynamicSelect();
            List<String> nonAffectedClassesFromPrev = nonAffectedClassesList.get(0);
            List<String> nonAffectedClassesFromCurRound = nonAffectedClassesList.get(1);

            // [DEBUG] generating debug info
            Config.preLoadConfigAware();
            String [] folder = Config.getNextDirName().split("/");
            String folderName = folder[folder.length - 1];
            MojoLog.unAffectedLog(parentdir.getAbsolutePath(), folderName, nonAffectedClassesFromPrev, nonAffectedClassesFromCurRound);

            System.setProperty(AbstractMojoInterceptor.EXCLUDES_INTERNAL_PREV_PROP, Arrays.toString(nonAffectedClassesFromPrev.toArray(new String[0])));
            System.setProperty(AbstractMojoInterceptor.EXCLUDES_INTERNAL_CURROUND_PROP, Arrays.toString(nonAffectedClassesFromCurRound.toArray(new String[0])));
        } else {
            throw new MojoExecutionException("Ekstazi cannot attach to the JVM, please specify Ekstazi 'restore' explicitly.");
        }
    }

    /**
     * Prepares option for Ekstazi (mostly from pom configuration).
     * Note that some other options (e.g., "mode" or path to the
     * agent) are prepared when Surefire starts execution.
     */
    private String prepareEkstaziOptions() {
        return "force.all=" + getForceall() +
            ",force.failing=" + getForcefailing() +
            "," + getRootDirOption() +
            (getXargs() == null || getXargs().equals("") ? "" : "," + getXargs());
    }
}
