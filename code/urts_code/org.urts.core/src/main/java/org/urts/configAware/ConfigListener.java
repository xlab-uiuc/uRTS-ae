package org.urts.configAware;


import org.urts.Config;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class instruments Listener methods to monitor configuration loading.
 */
public class ConfigListener {
    /** Exercised configuration <name, value> pairs */
    private static final Map<String, String> sGetConfigMap = new HashMap<String, String>();

    /** configuration pairs that from set() API */
    private static final Set<String> sSetConfigSet = new HashSet<String>();

    /** configuration pairs that from set() API */
    private static final Map<String, Set<String>> sConfigDependencyMap = Config.CONFIG_DEPENDENCY_V;

    /** Lock for this listener */
    private static final ReentrantLock sLock = new ReentrantLock();

    // Helper methods

    /**
     * Clean collected Configuration <name, value> pairs
     * */
    public static void clean() {
        try {
            sLock.lock();
            clean0();
        } finally {
            sLock.unlock();
        }
    }

    private static void clean0() {
        sGetConfigMap.clear();
        sSetConfigSet.clear();
    }

    /**
     * Add the exercised configuration into collected info.
     * @param configName Exercised configuration name;
     * @param configValue Exercised configuration value;
     */
    public static void addGetConfig(String configName, String configValue) {
        try {
            sLock.lock();
            addGetConfig0(configName, configValue);
        } finally {
            sLock.unlock();
        }
    }

    private static void addGetConfig0(String configName, String configValue) {
        if (sGetConfigMap.containsKey(configName)) {
            sGetConfigMap.replace(configName, configValue);
        } else {
            sGetConfigMap.put(configName, configValue);
        }
    }

    /**
     * Add the set configuration into collected info.
     * @param configName set configuration name;
     */
    public static void addSetConfig(String configName) {
        try {
            sLock.lock();
            addSetConfig0(configName);
        } finally {
            sLock.unlock();
        }
    }

    private static void addSetConfig0(String configName) {
        sSetConfigSet.add(configName);
    }

    /**
     * In the dependency file, Configuration <name, value> are sorted by key
     * to make comparing config-diff easier.
     * @return Configuration Map sorted by key
     */
    public static Map<String, String> sortConfigMap() {
        try {
            sLock.lock();
            Map<String, String> sortedMap = new HashMap<String, String>();
            SortedSet<String> keys = new TreeSet<>(sGetConfigMap.keySet());
            for (String key : keys) {
                sortedMap.put(key, sGetConfigMap.get(key));
            }
            return sortedMap;
        } finally {
            sLock.unlock();
        }
    }

    public static Map<String, String> getConfigMap() {
        Set<String> configSet = new HashSet<>(sSetConfigSet);
        Map<String, String> configGet = new HashMap<>(sGetConfigMap);
        // Insert parameter P into Set Map if P's value is depend on parameter Q and Q is hardcoded by SET()
        if (sConfigDependencyMap != null && !configSet.isEmpty() && !sConfigDependencyMap.isEmpty()) {
            for (Map.Entry<String, Set<String>> depEntry : sConfigDependencyMap.entrySet()) {
                Set<String> depConfig = depEntry.getValue();
                for (String dep : depConfig) {
                    if (configSet.contains(dep)) {
                        String depKey = depEntry.getKey();
                        configSet.add(depKey);
                        //Log.d2f("[INFO] " + dep + " is reset, its dependent parameter " + depKey + " also put into reset");
                        break;
                    }
                }
            }
        }

        Map<String, String> iteraotr = new HashMap<>(configGet);
        // Remove those configuration pairs that hardcoded by SET() API in the unit tests.
        if (!configSet.isEmpty() && !iteraotr.isEmpty()) {
            for (String resetConfig : configSet) {
                if (iteraotr.containsKey(resetConfig)) {
                    configGet.remove(resetConfig);
                }
            }
        }
        return configGet;
    }

    /**
     * Remove blank space, \r, \n, \t in a given string
     */
    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    // Touch method

    /**
     * Software's Configuration APIs invoke this method to collect
     * exercised configuration.
     * @param name Configuration name that used by get/set config-API
     * @param value Configuration value that used by get/set config-API
     */
    public static void recordGetConfig(String name, String value) {
        if (name != null && value != null && !name.equals("") && !value.equals("")) {
            name = replaceBlank(name);
            value = replaceBlank(value);
            addGetConfig(name, value);
        }
    }

    public static void recordSetConfig(String name, String value) {
        if (name != null && value != null && !name.equals("") && !value.equals("")) {
            name = replaceBlank(name);
            addSetConfig(name);
        }
    }

}
