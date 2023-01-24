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

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.apache.maven.project.MavenProject;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.Plugin;

import org.codehaus.plexus.util.xml.Xpp3Dom;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.urts.Config;
import org.urts.util.FileUtil;

public abstract class AbstractEkstaziMojo extends AbstractMojo {

    /** Marker printed to excludesFile to set boundaries */
    protected static final String EKSTAZI_LINE_MARKER = "# Ekstazi excluded";

    /** Name of surefire plugin */
    protected static final String SUREFIRE_PLUGIN_KEY = "org.apache.maven.plugins:maven-surefire-plugin";

    /** Name of Ekstazi plugin */
    protected static final String EKSTAZI_PLUGIN_KEY = "org.urts:urts-maven-plugin";

    /** 'exclude' property used by Ekstazi to pass list of classes to exclude */
    protected static final String EKSTAZI_EXCLUDES_PROPERTY = "EkstaziExcludes";

    /** Name of property that is used by surefireplugin to set JVM arguments */
    protected static final String ARG_LINE_PARAM_NAME = "argLine";

    /** Name of 'excludesFile' parmeter in surefire */
    protected static final String EXCLUDES_FILE_PARAM_NAME = "excludesFile";

    /** Name of 'parallel' parameter in surefire */
    protected static final String PARALLEL_PARAM_NAME = "parallel";

    /** Name of 'reuseForks' parameter in surefire */
    protected static final String REUSE_FORKS_PARAM_NAME = "reuseForks";

    /** Name of 'forkCount' parameter in surefire */
    protected static final String FORK_COUNT_PARAM_NAME = "forkCount";

    /** Name of 'forkMode' parameter in surefire */
    protected static final String FORK_MODE_PARAM_NAME = "forkMode";

    /** Name of 'excludes' parameter in surefire */
    protected static final String EXCLUDES_PARAM_NAME = "excludes";

    /** Name of 'exclude' parameter in surefire */
    protected static final String EXCLUDE_PARAM_NAME = "exclude";

    /** Name of configuration value getter test class and method */
    protected static final String GETTER_TEST_CLASS_NAME = System.getProperty("getterClass", "TestGetConfigValueForConfigAware");
    protected static final String GETTER_TEST_METHOD_NAME = System.getProperty("getterMethod");

    /** Name of getter test generated configuration value file */
    protected static final String GETTER_GENERATED_CONFIG_VALUE_FILE = System.getProperty("configValueFile", ".ConfigValue");

    @Parameter(property="project")
    protected MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}")
    protected String projectBuildDir;

    @Parameter(defaultValue = "${basedir}")
    protected File basedir;

    @Component
    protected MavenSession mavenSession;
    @Component
    protected BuildPluginManager pluginManager;

    /**
     * Clone of "skipTests" in surefire.  Ekstazi is not executed if
     * this flag is true.  This property should not be set only for
     * Ekstazi configuration.
     */
    @Parameter(property = "skipTests", defaultValue = "false")
    private boolean skipTests;

    /**
     * If set to true, skip using Ekstazi.
     *
     * @since 4.1.0
     */
    @Parameter(property = "ekstazi.skipme", defaultValue = "false")
    private boolean skipme;

    /**
     * Parent of .ekstazi directory.
     *
     * @since 4.5.0
     */
    @Parameter(property = "ekstazi.parentdir", defaultValue = "${basedir}")
    protected File parentdir;

    /**
     * Prefix for evaluation.
     */
    protected String evalStrPrefix = "===============[uRTS Evaluation: ";

    public boolean getSkipTests() {
        return skipTests;
    }

    public boolean getSkipme() {
        return skipme;
    }

    public File getParentdir() {
        return parentdir;
    }

    /**
     * Find plugin based on the plugin key. Returns null if plugin
     * cannot be located.
     */
    protected Plugin lookupPlugin(String key) {
        List<Plugin> plugins = project.getBuildPlugins();

        for (Iterator<Plugin> iterator = plugins.iterator(); iterator.hasNext();) {
            Plugin plugin = iterator.next();
            if(key.equalsIgnoreCase(plugin.getKey())) {
                return plugin;
            }
        }
        return null;
    }

    /**
     * Returns true if restore goal is present, false otherwise.
     */
    protected boolean isRestoreGoalPresent() {
        Plugin ekstaziPlugin = lookupPlugin(EKSTAZI_PLUGIN_KEY);
        if (ekstaziPlugin == null) {
            return false;
        }
        for (Object execution : ekstaziPlugin.getExecutions()) {
            for (Object goal : ((PluginExecution) execution).getGoals()) {
                if (((String) goal).equals("restore")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Locate paramName in the given (surefire) plugin. Returns value
     * of the file.
     */
    protected String extractParamValue(Plugin plugin, String paramName) throws MojoExecutionException {
        Xpp3Dom configuration = (Xpp3Dom) plugin.getConfiguration();
        if (configuration == null) {
            return null;
        }
        Xpp3Dom paramDom = configuration.getChild(paramName);
        return paramDom == null ? null : paramDom.getValue();
    }

    protected String extractParamValue(PluginExecution pluginExe, String paramName) throws MojoExecutionException {
        Xpp3Dom configuration = (Xpp3Dom) pluginExe.getConfiguration();
        if (configuration == null) {
            return null;
        }
        Xpp3Dom paramDom = configuration.getChild(paramName);
        return paramDom == null ? null : paramDom.getValue();
    }

    protected List<String> getWorkingDirs(Plugin plugin) throws MojoExecutionException {
        List<String> workingDirs = new ArrayList<String>();
        // Get all executions.
        List<PluginExecution> executions = plugin.getExecutions();
        for (PluginExecution execution : executions) {
            // Default phase is "test".
            if (execution.getPhase() == null || execution.getPhase().equals("test")) {
                String value = extractParamValue(execution, "workingDirectory");
                value = value == null ? "." : value;
                if (!workingDirs.contains(value)) {
                    workingDirs.add(value);
                }
            }
        }
        return workingDirs;
    }

    /**
     * Removes lines from excludesFile that are added by Ekstazi.
     */
    private void restoreExcludesFile(File excludesFileFile) throws MojoExecutionException {
        if (!excludesFileFile.exists()) {
            return;
        }

        try {
            String[] lines = FileUtil.readLines(excludesFileFile);
            List<String> newLines = new ArrayList<String>();
            for (String line : lines) {
                if (line.equals(EKSTAZI_LINE_MARKER)) break;
                newLines.add(line);
            }
            FileUtil.writeLines(excludesFileFile, newLines);
        } catch (IOException ex) {
            throw new MojoExecutionException("Could not restore 'excludesFile'", ex);
        }
    }

    protected void restoreExcludesFile(Plugin plugin) throws MojoExecutionException {
        String excludesFileName = extractParamValue(plugin, EXCLUDES_FILE_PARAM_NAME);
        File excludesFileFile = new File(excludesFileName);
        restoreExcludesFile(excludesFileFile);
        removeExcludesFileIfEmpty(excludesFileFile);
    }

    private void removeExcludesFileIfEmpty(File excludesFileFile) throws MojoExecutionException {
        if (!excludesFileFile.exists()) {
            return;
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(excludesFileFile));
            if (br.readLine() == null) {
                excludesFileFile.delete();
            }
        } catch (IOException ex) {
            throw new MojoExecutionException("Could not remove 'excludesFile'", ex);
        }
    }

    protected void preCheckConfigAwareFiles() throws MojoExecutionException {
        Config.preLoadConfigAware();
        String configFile = Paths.get(Paths.get(Config.CONFIG_FILE_DIR_PATH_V).toString(),
                GETTER_GENERATED_CONFIG_VALUE_FILE).toString();
        String files [] = {configFile, Config.CTEST_MAPPING_FILE_PATH_V, Config.CONFIG_PROD_FILE_PATH_V};
        for (String file : files) {
            File checkFile = new File(file);
            if (!checkFile.exists()) {
                throw new MojoExecutionException("Config-aware file does not exist + " + file + "; Please check your .urtsrc file ");
            }
        }
    }

    public void runConfigValueGetterTestThroughSurefire() throws MojoExecutionException {
        if (isStringNullOrEmpty(GETTER_TEST_CLASS_NAME)) {
            throw new MojoExecutionException("Could not get configuration value getter class, please " +
                    "set with -DgetterClass.");
        }
        SurefireExecution se = new SurefireExecution(lookupPlugin(SUREFIRE_PLUGIN_KEY),
                project, mavenSession, pluginManager, GETTER_TEST_CLASS_NAME, getLog());
        Config.CONFIG_GETTER_TEST_NAME_V = GETTER_TEST_CLASS_NAME;
        se.run();
    }

    /**
     * Run this method to invoke configuration value getter test
     * This getter is used for creating .ConfigValue file
     * @throws MojoExecutionException
     */
    public void runConfigValueGetterTest() throws MojoExecutionException {
        if (isStringNullOrEmpty(GETTER_TEST_CLASS_NAME) || isStringNullOrEmpty(GETTER_TEST_METHOD_NAME)) {
            throw new MojoExecutionException("Could not get configuration value getter class and method, please " +
                    "set with -DgetterClass and -DgetterMethod");
        }
        runConfigValueGetterTest(GETTER_TEST_CLASS_NAME, GETTER_TEST_METHOD_NAME);
    }

    private void runConfigValueGetterTest(String testClassName, String testMethod) throws MojoExecutionException {
        ClassLoader loader;
        Class<?> testClass;
        setConfigurationFromMavenSurefire();
        try {
            List<String> classpathElements = project.getTestClasspathElements();
            loader = new URLClassLoader(
                    stringsToUrls(classpathElements.toArray(new String[0])),
                    getClass().getClassLoader());
            testClass = java.lang.Class.forName(testClassName, true, loader);
        } catch (DependencyResolutionRequiredException | MalformedURLException e) {
            throw new MojoExecutionException("Could not get project classpath", e);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("Could not get getter class: " + testClassName, e);
        }

        Request testRequest = Request.method(testClass, testMethod);
        Runner testRunner = testRequest.getRunner();
        JUnitCore junit = new JUnitCore();
        getLog().debug("uRTS starts Running Getter Test: " + testClassName + "#" + testMethod);
        Result res = junit.run(testRunner);
        if (!res.wasSuccessful()) {
            throw new MojoExecutionException("Getter Test has failure! " + printFailure(res.getFailures()));
        }
        getLog().debug("uRTS finishes Running Getter Test: " + testClassName + "#" + testMethod);
    }

    private String printFailure(List<Failure> failure) {
        String ret = "";
        for (Failure f : failure) {
            ret += f.getTrace() + " \nException: " + f.getMessage() + " \ntest header: " + f.getTestHeader();
        }
        return ret;
    }

    private static URL[] stringsToUrls(String[] paths) throws MalformedURLException {
        URL[] urls = new URL[paths.length];
        for (int i = 0; i < paths.length; i++) {
            urls[i] = new File(paths[i]).toURI().toURL();
        }
        return urls;
    }

    private boolean isStringNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static void setEnv(String key, String value) {
        try {
            Map<String, String> env = System.getenv();
            Class<?> cl = env.getClass();
            Field field = cl.getDeclaredField("m");
            field.setAccessible(true);
            Map<String, String> writableEnv = (Map<String, String>) field.get(env);
            writableEnv.put(key, value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set environment variable", e);
        }
    }

    public void setConfigurationFromMavenSurefire() {
        List<Plugin> plugins = project.getBuildPlugins();
        for (Plugin p : plugins) {
            if (p.getArtifactId().contains("maven-surefire-plugin")) {
                Xpp3Dom environmentVariables = ((Xpp3Dom)p.getConfiguration()).getChild("environmentVariables");
                Xpp3Dom systemPropertyVariables = ((Xpp3Dom)p.getConfiguration()).getChild("systemPropertyVariables");
                for (int i = 0; i < environmentVariables.getChildCount(); i++) {
                    Xpp3Dom child = environmentVariables.getChild(i);
                    String envName = child.getName();
                    String envValue = child.getValue();
                    getLog().debug("Setting environment variable [" + envName + "] [" + envValue + "]");
                    setEnv(envName, envValue);
                }
                for (int i = 0; i < systemPropertyVariables.getChildCount(); i++) {
                    Xpp3Dom child = systemPropertyVariables.getChild(i);
                    String systemPropertyName = child.getName();
                    String systemPropertyValue = child.getValue();
                    getLog().debug("Setting system property [" + systemPropertyName + "] [" + systemPropertyValue + "]");
                    System.setProperty(systemPropertyName, systemPropertyValue);
                }
                break;
            }
        }
    }

}
