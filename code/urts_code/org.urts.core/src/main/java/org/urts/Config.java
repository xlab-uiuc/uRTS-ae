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

package org.urts;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;

import org.urts.data.DependencyAnalyzer;
import org.urts.data.PrefixTxtStorer;
import org.urts.data.Storer;
import org.urts.data.TxtStorer;
import org.urts.hash.FileCachingHasher;
import org.urts.hash.Hasher;
import org.urts.log.Log;

/**
 * Basic Tool configuration.
 */
public final class Config {

    /** Option separator */
    public static final String OPTION_SEPARATOR = ",";

    /** Array separator */
    private static final String ARRAY_SEPARATOR = ":";

    /** Config-Dependency seperator */
    private static final String DEP_SEPARATOR = "@";


    /** Ensures that we initialize this class only once */
    private static boolean sIsInitialized;


    // GENERAL

    public static int curRound;
    public static int nextRound;

    public static void prepareRound() {
        curRound = getCurRound();
        nextRound = getNextRound();
    }

    /**
     *
     * @return the parent Dir of all .urts-{}-Round{} folder.
     */
    public static String getCurRoot() {
        String rootDir = System.getProperty("user.dir");
        return rootDir;
    }

    /**
     * Used in dependency compare (get the current dependency)
     * @return the current dependency data folder name
     */
    public static String getCurDirName() {
        //int round = getCurRound();
        String rootDir = rootDirDefault();
        //return rootDir + "-" + CONFIG_FILE_NAME_V + "-Round" + round;
        return rootDir + "-" + PRODUCTION_NAME_V + "-Round" + curRound;
    }

    /**
     * Used in dependency update (update dependency to the new folder)
     * @return the updated dependency data folder name
     */
    public static String getNextDirName() {
        //int round = getCurRound() + 1;
        String rootDir = rootDirDefault();
        String configName = Config.PRODUCTION_NAME_V;
        int maxRound = getMaxRound();
//        if (maxRound > round)
//            return rootDir + "-" + configName + "-Round" + maxRound;
//        return rootDir + "-" + configName + "-Round" + round;
        return rootDir + "-" + configName + "-Round" + nextRound;
    }

    private static int getNextRound() {
        int round = getCurRound() + 1;
        int maxRound = getMaxRound();
        if (maxRound > round)
            round = maxRound;
        return round;
    }

    private static int getCurRound() {
        Config.preLoadConfigAware();
        String rootDir = rootDirDefault();
        String configName = PRODUCTION_NAME_V;
        File dir = new File (rootDir.substring(0, rootDir.length() - 5));
        File files [] = dir.listFiles();
        for(File f : files) {
            if (f.isDirectory() && f.getName().contains(configName)) {
                String filename = f.getName();
                String[] filenamelist = filename.split("-");
                return Integer.parseInt(filenamelist[filenamelist.length-1].substring(5));
            }
        }
        return 0;
    }

    private static int getMaxRound() {
        String rootDir = rootDirDefault();
        File dir = new File (rootDir.substring(0, rootDir.length() - 5));
        File files [] = dir.listFiles();
        int max = curRound;
        for(File f : files) {
            Config.preLoadConfigAware();
            if(PRODUCTION_NAME_V == null) {
                PRODUCTION_NAME_V = "NonSetConfigFile";
            }
            if (f.isDirectory() && f.getName().contains("Round") && !f.getName().contains(PRODUCTION_NAME_V)) {
                String filename = f.getName();
                String[] filenamelist = filename.split("-");
                int round = Integer.parseInt(filenamelist[filenamelist.length-1].substring(5));
                if (round > max) {
                    max = round;
                }
            }
        }
        return max;
    }

    //TODO: Also Check SAVE() Function
    @Opt(desc = "Name of the directory that keeps current coverage data.")
    public static String CUR_DIR_V = getCurDirName();
    protected static final String CUR_DIR_N = "root.dir";

    @Opt(desc = "Name of the directory that keeps current coverage data.")
    public static String NEXT_DIR_V = getNextDirName();

    private static String rootDirDefault() {
        return System.getProperty("user.dir") + System.getProperty("file.separator") + Names.EKSTAZI_ROOT_DIR_NAME;
    }

    public static String curWorkingDir() {
        return System.getProperty("user.dir") + System.getProperty("file.separator");
    }

    /**
     * Returns a File that describes .urts-{configName}-Round{i} directory. Note that the directory
     * is not created with this invocation.
     *
     * @param parentDir
     *            Parent directory for .urts directory
     * @return File that describes .urts-{configName}-Round{i} directory
     */
    public static File createCurDir(File parentDir) {
        return new File(parentDir,
                Names.EKSTAZI_ROOT_DIR_NAME + "-" + PRODUCTION_NAME_V + "-Round" + getCurRound());
    }

    public static File createNextDir(File parentDir) {
        int round = getCurRound() + 1;
        int maxRound = getMaxRound();
        if (maxRound > round)
            round = maxRound;
        return new File(parentDir,
                Names.EKSTAZI_ROOT_DIR_NAME + "-" + PRODUCTION_NAME_V + "-Round" + round);
    }

    /**
     * Returns absolute path to .urts director (URI.toString). Note
     * that the directory is not created with this invocation.
     *
     * @param parentDir
     *            Parent directory for .urts directory
     * @return An absolute path (URI.toString) that describes .urts directory
     */
    public static String getRootDirURI(File parentDir) {
        //String pathAsString = parentDir.getAbsolutePath() + System.getProperty("file.separator") + Names.EKSTAZI_ROOT_DIR_NAME;
        //return new File(pathAsString).toURI().toString();
        File rootDir = createCurDir(parentDir);
        // rootDir is the full name of the dependency folder, such as "XXX/.urts-default-Round2"
        return rootDir.toURI().toString();
    }

    public static List<File> getSameRoundDirName() {
        Config.preLoadConfigAware();
        List<File> dirs = new ArrayList<>();
        String rootDir = rootDirDefault();
        File dir = new File (rootDir.substring(0, rootDir.length() - 5));
        File files [] = dir.listFiles();
        for(File f : files) {
            if (f.isDirectory() && f.getName().contains("Round" + nextRound) && !f.getName().contains(PRODUCTION_NAME_V)) {
                dirs.add(f);
            }
        }
        if (dirs.size() == 0) {
            return Collections.<File>emptyList();
        }
        return dirs;
    }

    // AGENT

    public static enum AgentMode {
        NONE,
        SINGLE,
        SINGLEFORK,
        MULTI,
        JUNIT,
        JUNIT5INSERTION,
        JUNIT5EXTENSION,
        JUNIT5FORK,
        JUNITFORK,
        JUNIT4RETESTALL,
        JUNIT5RETESTALL,
        SCALATEST;

        public static AgentMode fromString(String text) {
            if (text != null) {
                for (AgentMode b : AgentMode.values()) {
                    if (text.equalsIgnoreCase(b.name())) {
                        //Log.d2f("[INFO] AgenMode Return " + b.name());
                        return b;
                    }
                }
            }
            return NONE;
        }
    }

    @Opt(desc = "Mode")
    public static AgentMode MODE_V = AgentMode.NONE;
    protected static final String MODE_N = "mode";

    @Opt(desc = "Single mode output name")
    public static String SINGLE_NAME_V = "DEFAULT";
    protected static final String SINGLE_NAME_N = "single.name";

    // DEPENDENCIES

    @Opt(desc = "Dependency formatter.")
    private static String DEPENDENCIES_FORMAT_V = Storer.Mode.TXT.toString();
    protected static final String DEPENDENCIES_FORMAT_N = "dependencies.format";

    @Opt(desc = "Collect dependencies on JUnit, Maven, and Hamcrest.")
    public static boolean DEPENDENCIES_INCLUDE_WELLKNOWN_V = false;
    protected static final String DEPENDENCIES_INCLUDE_WELLKNOWN_N = "dependencies.include.wellknown";

    @Opt(desc = "Indicated that dependencies should be appended to the existing dependencies.")
    public static boolean DEPENDENCIES_APPEND_V = false;
    protected static final String DEPENDENCIES_APPEND_N = "dependencies.append";

    @Opt(desc = "Parts of paths to dependencies that should not be collected.")
    public static Pattern DEPENDENCIES_IGNORED_PATHS_V = null;
    protected static final String DEPENDENCIES_IGNORED_PATHS_N = "dependencies.ignored.paths";

    @Opt(desc = "Pattern that describes classes to instrument.")
    public static Pattern DEPENDENCIES_CLASSES_INSTRUMENT_V = null;
    protected static final String DEPENDENCIES_CLASSES_INSTRUMENT_N = "dependencies.classes.instrument";

    @Opt(desc = "Enable/disable collecting file dependencies.")
    public static boolean DEPENDENCIES_NIO_V = false;
    public static final String DEPENDENCIES_NIO_N = "dependencies.nio";

    @Opt(desc = "Pattern that describe files to exclude.")
    public static Pattern DEPENDENCIES_NIO_EXCLUDES_V = null;
    protected static final String DEPENDENCIES_NIO_EXCLUDES_N = "dependencies.nio.excludes";

    @Opt(desc = "Pattern that describes files to include.")
    public static Pattern DEPENDENCIES_NIO_INCLUDES_V = null;
    protected static final String DEPENDENCIES_NIO_INCLUDES_N = "dependencies.nio.includes";

    // OPTIMIZATIONS

    @Opt(desc = "Algorithm to use to hash class data (only if semantic hashing is on), e.g., CRC32, MD5, etc.")
    private static Hasher.Algorithm HASH_ALGORITHM_V = Hasher.Algorithm.CRC32;
    protected static final String HASH_ALGORITHM_N = "hash.algorithm";

    @Opt(desc = "If the flag is set, debug info in a classfile is not included in hash value.")
    private static boolean HASH_WITHOUT_DEBUGINFO_V = true;
    protected static final String HASH_WITHOUT_DEBUGINFO_N = "hash.without.debuginfo";

    @Opt(desc = "Sizes of several caches.")
    public static int CACHE_SIZES_V = 1000;
    protected static final String CACHE_SIZES_N = "cache.sizes";

    @Opt(desc = "Enable/disable caching of recently seen classes when collecting dependencies.")
    public static boolean CACHE_SEEN_CLASSES_V = true;
    protected static final String CACHE_SEEN_CLASSES_N = "cache.seen.classes";

    @Opt(desc = "X: Enable/disable saving instrumented code. (We use files named by hash value of the original content.)")
    public static boolean X_SAVE_INSTRUMENTED_CODE_V = false;
    protected static final String X_SAVE_INSTRUMENTED_CODE_N = "x.save.instrumented.code";

    @Opt(desc = "X: Enable/disable storing hasher cache to file.")
    public static boolean X_SAVE_HASHER_CACHE_V = false;
    protected static final String X_SAVE_HASHER_CACHE_N = "x.save.hasher.cache";

    // DEBUG

    public static enum DebugMode {
        NONE,
        SCREEN,
        FILE,
        EVERYWHERE;

        public static DebugMode fromString(String text) {
            if (text != null) {
                for (DebugMode b : DebugMode.values()) {
                    if (text.equalsIgnoreCase(b.name())) {
                        return b;
                    }
                }
            }
            return NONE;
        }
    }

    @Opt(desc = "Debug mode")
    public static DebugMode DEBUG_MODE_V = DebugMode.NONE;
    protected static String DEBUG_MODE_N = "debug.mode";

    @Opt(desc = "Enable/disable debug mode.")
    public static boolean DEBUG_V = false;
    protected static final String DEBUG_N = "debug";

    public static boolean X_LOG_RUNS_V = false;
    protected static final String X_LOG_RUNS_N = "x.log.runs";

    /** Name of the file to keep info about run */
    public static final String RUN_INFO_V = createFileNameInCoverageDir(Names.RUN_INFO_FILE_NAME);

    // CONFIGURATION-AWARE

    @Opt(desc = "Test Generated Configuration <key, value> file path")
    public static String CONFIG_FILE_DIR_PATH_V = System.getProperty("user.dir");
    protected static final String CONFIG_FILE_DIR_PATH_N = "config.file.dir.path";

    @Opt(desc = "Configuration file name")
    public static String PRODUCTION_NAME_V = "NonSetProductionName";
    protected static final String PRODUCTION_NAME_N = "config.production.name";

//    @Opt(desc = "Default Configuration File Path")
//    public static String DEFAULT_CONFIG_FILE_PATH_V = "NonSetDefaultConfigFile";
//    protected static final String DEFAULT_CONFIG_FILE_PATH_N = "config.default.path";

    @Opt(desc = "ctest mapping file path")
    public static String CTEST_MAPPING_FILE_PATH_V = "NonSetCtestMappingFilePath";
    protected static final String CTEST_MAPPING_FILE_PATH_N = "config.mapping.path";

    @Opt(desc = "Inject Configuration File Path")
    public static String CONFIG_INJECT_FILE_PATH_V = "NonSetConfigInjectionFilePath";
    protected static final String CONFIG_INJECT_FILE_PATH_N = "config.inject.path";

    @Opt(desc = "Production Configuration File Path")
    public static String CONFIG_PROD_FILE_PATH_V = "NonSetConfigInjectionFilePath";
    protected static final String CONFIG_PROD_FILE_PATH_N = "config.prod.path";

    @Opt(desc = "Configurations that will not be involved in comparison")
    public static Set<String> CONFIG_EXCLUDES_V = null;
    protected static final String CONFIG_EXCLUDES_N = "config.excludes";

    @Opt(desc = "Configurations that will not be involved in comparison (as a list of strings concat with '@' and seprataed by ':')")
    public static Map<String, Set<String>> CONFIG_DEPENDENCY_V = null;
    protected static final String CONFIG_DEPENDENCY_N = "config.dependency";

    @Opt(desc = "Getter test class name that will get configuration parameter value")
    public static String CONFIG_GETTER_TEST_NAME_V = "TestGetConfigValueForConfigAware";

    // EXPERIMENT
    @Opt(desc = "Enable/disable analysis time log.")
    public static boolean LOG_ANALYSIS_V = false;
    protected static final String LOG_ANALYSIS_N = "log.analysis";

    @Opt(desc = "Enable/disable test number log.")
    public static boolean LOG_NUMBER_V = false;
    protected static final String LOG_NUMBER_N = "log.number";

    // INCLUDE/EXCLUDE

    @Opt(desc = "Force execution of all tests.")
    public static boolean FORCE_ALL_V;
    protected static final String FORCE_ALL_N = "force.all";

    @Opt(desc = "Force execution of previously failing tests.")
    public static boolean FORCE_FAILING_V;
    protected static final String FORCE_FAILING_N = "force.failing";

    @Opt(desc = "Tests that are never run with Tool (as a list of strings separated by ':').")
    private static String[] SELECTION_EXCLUDES_V = null;
    protected static final String SELECTION_EXCLUDES_N = "selection.excludes";

    @Opt(desc = "Tests that are always run with Tool (as a list of strings separated by ':').")
    private static String[] SELECTION_INCLUDES_V = null;
    protected static final String SELECTION_INCLUDES_N = "selection.includes";

    // OTHER

    @Opt(desc = "X: Enable/disable code instrumentation.")
    public static boolean X_INSTRUMENT_CODE_V = true;
    protected static final String X_INSTRUMENT_CODE_N = "x.instrument.code";

    @Opt(desc = "X: Enable/disable dependency storing.")
    public static boolean X_DEPENDENCIES_SAVE_V = true;
    protected static final String X_DEPENDENCIES_SAVE_N = "x.dependencies.save";

    @Opt(desc = "X: Enable/disable Tool.")
    public static boolean X_ENABLED_V = true;
    protected static final String X_ENABLED_N = "x.enabled";

    @Opt(desc = "X: Enable/disable ignoring the execution of all tests.")
    public static boolean X_IGNORE_ALL_TESTS_V = false;
    protected static final String X_IGNORE_ALL_TESTS_N = "x.ignore.all.tests";

    // INITIALIZE

    public static void loadConfig() {
        loadConfig(null, false);
    }

    /**
     * Load configuration from properties.
     */
    public static void loadConfig(String options, boolean force) {
        if (sIsInitialized && !force) return;
        sIsInitialized = true;
        Properties commandProperties = unpackOptions(options);
        String userHome = getUserHome();
        File userHomeDir = new File(userHome, Names.EKSTAZI_CONFIG_FILE);
        Properties homeProperties = getProperties(userHomeDir);
        File userDir = new File(System.getProperty("user.dir"), Names.EKSTAZI_CONFIG_FILE);
        Properties userProperties = getProperties(userDir);
        loadProperties(homeProperties);
        loadProperties(userProperties);
        loadProperties(commandProperties);
        // Init Log before any print of config/debug.
        Log.init(DEBUG_MODE_V == DebugMode.SCREEN || DEBUG_MODE_V == DebugMode.EVERYWHERE,
                DEBUG_MODE_V == DebugMode.FILE || DEBUG_MODE_V == DebugMode.EVERYWHERE,
                createFileNameInCoverageDir("debug.log"));

        // Print configuration.
        printVerbose(userHomeDir, userDir);
    }


    public static void preLoadConfigAware() {
        File userDir = new File(System.getProperty("user.dir"), Names.EKSTAZI_CONFIG_FILE);
        Properties userProperties = getProperties(userDir);
        preLoadProperties(userProperties);
    }

    protected static void preLoadProperties(Properties props) {
        CUR_DIR_V = getURIString(props, CUR_DIR_N, CUR_DIR_V);
        PRODUCTION_NAME_V = getString(props, PRODUCTION_NAME_N, PRODUCTION_NAME_V);
        CONFIG_FILE_DIR_PATH_V = getString(props, CONFIG_FILE_DIR_PATH_N, CONFIG_FILE_DIR_PATH_V);
        // DEFAULT_CONFIG_FILE_PATH_V = getString(props, DEFAULT_CONFIG_FILE_PATH_N, DEFAULT_CONFIG_FILE_PATH_V);
        CTEST_MAPPING_FILE_PATH_V = getString(props, CTEST_MAPPING_FILE_PATH_N, CTEST_MAPPING_FILE_PATH_V);
        CONFIG_INJECT_FILE_PATH_V = getString(props, CONFIG_INJECT_FILE_PATH_N, CONFIG_INJECT_FILE_PATH_V).replaceAll("-pid", "-" + getPID());
        CONFIG_PROD_FILE_PATH_V = getString(props, CONFIG_PROD_FILE_PATH_N, CONFIG_PROD_FILE_PATH_V);
        CONFIG_EXCLUDES_V = getSet(props, CONFIG_EXCLUDES_N, CONFIG_EXCLUDES_V);
        CONFIG_DEPENDENCY_V = getMapList(props, CONFIG_DEPENDENCY_N, CONFIG_DEPENDENCY_V);
        LOG_ANALYSIS_V = getBoolean(props, LOG_ANALYSIS_N, LOG_ANALYSIS_V);
        LOG_NUMBER_V = getBoolean(props, LOG_NUMBER_N, LOG_NUMBER_V);
    }


    /**
     * Returns path to user home directory. This method is needed for
     * experiments when we assume that /home/name/ (or similar) is home. Note
     * that projects can override user.home in pom.xml or similar file (e.g.
     * Apache Continuum).
     */
    private static String getUserHome() {
        String userHomeProperty = System.getProperty("user.home");
        String userHomeEnv = System.getenv("HOME");
        if (userHomeEnv == null) return userHomeProperty;
        int propertyCount = userHomeProperty.split("/").length;
        int envCount = userHomeEnv.split("/").length;
        return propertyCount <= envCount ? userHomeProperty : userHomeEnv;
    }

    // INTERNAL

    /**
     * Returns process id. Note that Java does not guarantee any format for id,
     * so this is just a common heuristic.
     *
     * @return Current process id
     */
    private static String getPID() {
        String vmName = ManagementFactory.getRuntimeMXBean().getName();
        return vmName.substring(0, vmName.indexOf("@"));
    }

    private static String createFileNameInCoverageDir(String name) {
        return CUR_DIR_V + System.getProperty("file.separator") + name;
    }

    private static Properties getProperties(File file) {
        Properties properties = new Properties();
        if (file.exists()) {
            try {
                properties.load(new FileInputStream(file));
            } catch (IOException e) {
                Log.e("Could not load configuration file", e);
            }
        }
        return properties;
    }

    private static void printVerbose(File userHome, File userDir) {
        if (DEBUG_MODE_V == DebugMode.SCREEN || DEBUG_MODE_V == DebugMode.FILE || DEBUG_MODE_V == DebugMode.EVERYWHERE) {
            Field[] options = getAllNonHiddenOptions(Config.class);

            printVerboseLine();
            if (userHome != null && userHome.exists()) {
                printVerboseLine("Loaded configuration file from", userHome.getAbsolutePath());
            }
            if (userDir != null && userDir.exists()) {
                printVerboseLine("Loaded configuration file from", userDir.getAbsolutePath());
            }
            for (Field option : options) {
                String optionName = (String) getValue(getNameField(Config.class, option));
                Object value = getValue(option);
                if (value == null) {
                    printVerboseLine(optionName, "null");
                } else if (value.getClass().isArray()) {
                    printVerboseLine(optionName, Arrays.toString((String[]) value));
                } else {
                    printVerboseLine(optionName, value);
                }
            }
        }
    }

    private static void printVerboseLine() {
        printVerboseLine("", "");
    }

    private static void printVerboseLine(Object key, Object value) {
        Log.c(key, value);
    }

    protected static void loadProperties(Properties props) {
        CUR_DIR_V = getURIString(props, CUR_DIR_N, CUR_DIR_V);
        MODE_V = AgentMode.fromString(getString(props, MODE_N, MODE_V.toString()));
        SINGLE_NAME_V = getString(props, SINGLE_NAME_N, SINGLE_NAME_V);
        DEPENDENCIES_FORMAT_V = getString(props, DEPENDENCIES_FORMAT_N, DEPENDENCIES_FORMAT_V);
        HASH_ALGORITHM_V = Hasher.Algorithm.fromString(getString(props, HASH_ALGORITHM_N, HASH_ALGORITHM_V.toString()));
        DEPENDENCIES_INCLUDE_WELLKNOWN_V = getBoolean(props, DEPENDENCIES_INCLUDE_WELLKNOWN_N, DEPENDENCIES_INCLUDE_WELLKNOWN_V);
        X_ENABLED_V = getBoolean(props, X_ENABLED_N, X_ENABLED_V);
        X_LOG_RUNS_V = getBoolean(props, X_LOG_RUNS_N, X_LOG_RUNS_V);
        X_INSTRUMENT_CODE_V = getBoolean(props, X_INSTRUMENT_CODE_N, X_INSTRUMENT_CODE_V);
        X_DEPENDENCIES_SAVE_V = getBoolean(props, X_DEPENDENCIES_SAVE_N, X_DEPENDENCIES_SAVE_V);
        DEBUG_V = getBoolean(props, DEBUG_N, DEBUG_V);
        DEBUG_MODE_V = DebugMode.fromString(getString(props, DEBUG_MODE_N, DEBUG_MODE_V.toString()));
        CACHE_SIZES_V = getInteger(props, CACHE_SIZES_N, CACHE_SIZES_V);
        DEPENDENCIES_IGNORED_PATHS_V = getPattern(props, DEPENDENCIES_IGNORED_PATHS_N, DEPENDENCIES_IGNORED_PATHS_V);
        DEPENDENCIES_CLASSES_INSTRUMENT_V = getPattern(props, DEPENDENCIES_CLASSES_INSTRUMENT_N, DEPENDENCIES_CLASSES_INSTRUMENT_V);
        SELECTION_EXCLUDES_V = getArray(props, SELECTION_EXCLUDES_N, SELECTION_EXCLUDES_V);
        SELECTION_INCLUDES_V = getArray(props, SELECTION_INCLUDES_N, SELECTION_INCLUDES_V);
        FORCE_ALL_V = getBoolean(props, FORCE_ALL_N, FORCE_ALL_V);
        FORCE_FAILING_V = getBoolean(props, FORCE_FAILING_N, FORCE_FAILING_V);
        HASH_WITHOUT_DEBUGINFO_V = getBoolean(props, HASH_WITHOUT_DEBUGINFO_N, HASH_WITHOUT_DEBUGINFO_V);
        CACHE_SEEN_CLASSES_V = getBoolean(props, CACHE_SEEN_CLASSES_N, CACHE_SEEN_CLASSES_V);
        X_IGNORE_ALL_TESTS_V = getBoolean(props, X_IGNORE_ALL_TESTS_N, X_IGNORE_ALL_TESTS_V);
        DEPENDENCIES_APPEND_V = getBoolean(props, DEPENDENCIES_APPEND_N, DEPENDENCIES_APPEND_V);
        X_SAVE_INSTRUMENTED_CODE_V = getBoolean(props, X_SAVE_INSTRUMENTED_CODE_N, X_SAVE_INSTRUMENTED_CODE_V);
        X_SAVE_HASHER_CACHE_V = getBoolean(props, X_SAVE_HASHER_CACHE_N, X_SAVE_HASHER_CACHE_V);
        DEPENDENCIES_NIO_V = getBoolean(props, DEPENDENCIES_NIO_N, DEPENDENCIES_NIO_V);
        DEPENDENCIES_NIO_INCLUDES_V = getPattern(props, DEPENDENCIES_NIO_INCLUDES_N, DEPENDENCIES_NIO_INCLUDES_V);
        DEPENDENCIES_NIO_EXCLUDES_V = getPattern(props, DEPENDENCIES_NIO_EXCLUDES_N, DEPENDENCIES_NIO_EXCLUDES_V);
        PRODUCTION_NAME_V = getString(props, PRODUCTION_NAME_N, PRODUCTION_NAME_V);
        CONFIG_FILE_DIR_PATH_V = getString(props, CONFIG_FILE_DIR_PATH_N, CONFIG_FILE_DIR_PATH_V);
        // DEFAULT_CONFIG_FILE_PATH_V = getString(props, DEFAULT_CONFIG_FILE_PATH_N, DEFAULT_CONFIG_FILE_PATH_V);
        CTEST_MAPPING_FILE_PATH_V = getString(props, CTEST_MAPPING_FILE_PATH_N, CTEST_MAPPING_FILE_PATH_V);
        CONFIG_INJECT_FILE_PATH_V = getString(props, CONFIG_INJECT_FILE_PATH_N, CONFIG_INJECT_FILE_PATH_V).replaceAll("-pid", "-" + getPID());
        CONFIG_PROD_FILE_PATH_V = getString(props, CONFIG_PROD_FILE_PATH_N, CONFIG_PROD_FILE_PATH_V);
        CONFIG_EXCLUDES_V = getSet(props, CONFIG_EXCLUDES_N, CONFIG_EXCLUDES_V);
        CONFIG_DEPENDENCY_V = getMapList(props, CONFIG_DEPENDENCY_N, CONFIG_DEPENDENCY_V);
        LOG_ANALYSIS_V = getBoolean(props, LOG_ANALYSIS_N, LOG_ANALYSIS_V);
        LOG_NUMBER_V = getBoolean(props, LOG_NUMBER_N, LOG_NUMBER_V);
    }

    /**
     * Checks if properties have correct names. A property has a correct name if
     * it is one of the configuration options.
     *
     * @param props
     *            properties to check.
     * @return true if properties have correct names, false otherwise.
     */
    protected static boolean checkNamesOfProperties(Properties props) {
        Set<String> names = getAllOptionNames(Config.class);
        for (Object key : props.keySet()) {
            if (!(key instanceof String) || !names.contains(key)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the number of non experimental options for the given class.
     *
     * @param clz
     *            class that defines options.
     *
     * @return number of non experimental options defined in the given class.
     */
    protected static int getNumOfNonExperimentalOptions(Class<?> clz) {
        Field[] options = getAllOptions(clz);
        int count = 0;
        for (Field option : options) {
            count += option.getName().startsWith("X_") ? 0 : 1;
        }
        return count;
    }

    /**
     * This method is used to load path to .ekstazi when the path is
     * given as URI string.  We need to use URI to make sure that we
     * support paths (given to javaagent) even if they contain spaces.
     */
    private static String getURIString(Properties props, String key, String def) {
        try {
            URI uri = new URI(props.getProperty(key, def));
            return new File(uri).getAbsolutePath();
        } catch (Exception ex) {
            return getString(props, key, def);
        }
    }

    private static String getString(Properties props, String key, String def) {
        return props.getProperty(key, def);
    }

    private static boolean getBoolean(Properties props, String key, boolean def) {
        return Boolean.parseBoolean(getString(props, key, Boolean.toString(def)));
    }

    private static int getInteger(Properties props, String key, Integer def) {
        return Integer.parseInt(getString(props, key, Integer.toString(def)));
    }

    protected static Pattern getPattern(Properties props, String key, Pattern def) {
        String val = props.getProperty(key, null);
        if (val == null) {
            return def;
        }
        return Pattern.compile(val);
    }

    private static Set<String> getSet(Properties props, String key, Set<String> def) {
        String val = props.getProperty(key, null);
        if (val == null) return def;
        return new HashSet<String>(Arrays.asList(val.split(ARRAY_SEPARATOR)));
    }

    private static Map<String, Set<String>> getMapList(Properties props, String key, Map<String, Set<String>> def) {
        String val = props.getProperty(key, null);
        if (val == null) return def;
        Map<String, Set<String>> returnMap = new HashMap<>();
        String [] items = val.split(ARRAY_SEPARATOR);
        for (String item : items) {
            String mapKey = item.split(DEP_SEPARATOR)[0];
            Set<String> mapValue = new HashSet<>();
            String [] values = item.split(DEP_SEPARATOR)[1].split(OPTION_SEPARATOR);
            for (String value : values) {
                mapValue.add(value.trim());
            }
            returnMap.put(mapKey, mapValue);
        }
        return returnMap;
    }

    private static String[] getArray(Properties props, String key, String[] def) {
        String val = props.getProperty(key, null);
        if (val == null) return def;
        return val.split(ARRAY_SEPARATOR);
    }

    protected static Set<String> getAllOptionNames(Class<?> clz) {
        Set<String> names = new HashSet<String>();
        Field[] options = getAllOptions(clz);
        for (Field option : options) {
            names.add(option.getName().replace("_V", "").toLowerCase().replaceAll("_", "\\."));
        }
        return names;
    }

    protected static Field[] getAllOptions(Class<?> clz) {
        Field[] declaredFields = clz.getDeclaredFields();
        List<Field> options = new ArrayList<Field>();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Opt.class)) {
                options.add(field);
            }
        }
        return options.toArray(new Field[options.size()]);
    }

    private static Field[] getAllNonHiddenOptions(Class<?> clz) {
        Field[] options = getAllOptions(clz);
        List<Field> nonHiddenOptions = new ArrayList<Field>();
        for (Field option : options) {
            Opt annotation = option.getAnnotation(Opt.class);
            if (!annotation.hidden()) {
                nonHiddenOptions.add(option);
            }
        }
        return nonHiddenOptions.toArray(new Field[nonHiddenOptions.size()]);
    }

    private static Field getNameField(Class<?> clz, Field field) {
        String fieldName = field.getName();
        String valueName = fieldName.substring(0, fieldName.lastIndexOf("_V")) + "_N";
        try {
            return clz.getDeclaredField(valueName);
        } catch (SecurityException e) {
            // e.printStackTrace();
        } catch (NoSuchFieldException e) {
            // e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // e.printStackTrace();
        }
        return null;
    }

    private static Object getValue(Field field) {
        try {
            return field.get(null);
        } catch (IllegalArgumentException e) {
            // e.printStackTrace();
        } catch (IllegalAccessException e) {
            // e.printStackTrace();
        }
        return null;
    }

    protected static String packOptions(Class<?> clz) {
        StringBuilder sb = new StringBuilder();
        Field[] options = getAllOptions(clz);
        for (Field opt : options) {
            String name = (String) getValue(getNameField(clz, opt));
            Object value = getValue(opt);
            if (value != null) {
                if (sb.length() > 0) {
                    sb.append(OPTION_SEPARATOR);
                }
                sb.append(name);
                sb.append("=");
                sb.append(value);
            }
        }
        return sb.toString();
    }

    protected static Properties unpackOptions(String packed) {
        Properties properties = new Properties();
        if (packed != null && !packed.equals("")) {
            String[] options = packed.split(OPTION_SEPARATOR);
            for (String opt : options) {
                String[] keyValue = opt.split("=");
                if (keyValue.length != 2) throw new RuntimeException("Incorrect argument: " + opt);
                properties.put(keyValue[0], keyValue[1]);
                //Log.d2f("Key = " + keyValue[0] + " value = " + keyValue[1]);
            }
        }
        return properties;
    }

    // FACTORIES

    public static Storer createStorer() {
        Storer.Mode mode = Storer.Mode.fromString(DEPENDENCIES_FORMAT_V);
        if (mode == Storer.Mode.TXT) {
            return new TxtStorer();
        } else if (mode == Storer.Mode.PREFIX_TXT) {
            return new PrefixTxtStorer();
        } else {
            Log.e("Storer must have default value.");
            throw new RuntimeException();
        }
    }

    public static Hasher createHasher() {
        return X_SAVE_HASHER_CACHE_V ? new FileCachingHasher(Config.HASH_ALGORITHM_V, Config.CACHE_SIZES_V,
                Config.HASH_WITHOUT_DEBUGINFO_V, new File(Config.CUR_DIR_V, "hasher-cache.txt")) : new Hasher(
                Config.HASH_ALGORITHM_V, Config.CACHE_SIZES_V, Config.HASH_WITHOUT_DEBUGINFO_V);
    }

    public static DependencyAnalyzer createDepenencyAnalyzer() {
        return new DependencyAnalyzer(CACHE_SIZES_V, createHasher(), createStorer(),
                SELECTION_EXCLUDES_V, SELECTION_INCLUDES_V);
    }

    // MAIN

    /**
     * Prints configuration options.
     *
     * @param args
     *            Command line arguments (no argument is expected).
     */
    public static void main(String[] args) {
        DEBUG_MODE_V = DebugMode.SCREEN;
        Log.initScreen();
        printVerbose(null, null);
    }
}
