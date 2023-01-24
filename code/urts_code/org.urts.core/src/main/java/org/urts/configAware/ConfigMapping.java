package org.urts.configAware;

import org.urts.Config;
import org.urts.log.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class ConfigMapping {
    /** Ctest grouped mapping, key = Test_Full_Name, Value = Set<configName>*/
    private static final Map<String, Set<String>> sGroupedMapping = new HashMap<String, Set<String>>();

    /** Ctest grouped mapping, key = TestName, Value = Set<configName>*/
    private static final Map<String, Set<String>> sOnlyTestNameMapping = new HashMap<>();

    /** Return the grouped mapping */
    public static Map<String, Set<String>> getConfigMapping(){
        if (sGroupedMapping.isEmpty()) {
            generateGroupedMapping();
        }
        return sGroupedMapping;
    }

    /** Return the grouped mapping */
    public static Map<String, Set<String>> getOnlyTestNameMapping(){
        if (sOnlyTestNameMapping.isEmpty()) {
            generateGroupedMapping();
        }
        return sOnlyTestNameMapping;
    }

    /** Generate the grouped mapping */
    private static void generateGroupedMapping(){
        String mappingFilePath = Config.CTEST_MAPPING_FILE_PATH_V;
        File mappingFile = new File(mappingFilePath);
        if (!mappingFile.exists() || mappingFile.isDirectory()) {
            Log.e("Mapping file is not exist");
            //Log.d2f("[ERROR] generateGroupedMapping(): Mapping file is not exist");
            return;
        }
        try {
            FileReader fileReader = new FileReader(mappingFile);
            BufferedReader br = new BufferedReader(fileReader);
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.contains(":")) {
                    continue;
                }
                String [] strArray = line.split(":");
                if (strArray.length <= 1)
                    continue;
                String test =  strArray[0].trim();
                String [] configs =strArray[1].split(",");
                Set<String> configName = new HashSet<>();
                for (String config : configs) {
                    configName.add(config.trim());
                }
                sGroupedMapping.put(test, configName);
                String [] testName = test.split("\\.");
                sOnlyTestNameMapping.put(testName[testName.length - 1], configName);
            }
            fileReader.close();
        } catch (Exception e) {
            Log.e("Failed to generate group ctest mapping " + e.getMessage());
            //Log.d2f("[ERROR] Failed to generate group ctest mapping " + e.getMessage());
            sGroupedMapping.clear();
            e.printStackTrace();
        }
    }

    /**
     *
     * @param testName
     * @return the real inject configuration pairs for running ${testName}
     */
    public static Map<String, String> getInjectConfigPairs(String testName) {
        // Map<String, String> defaultConfigPairs = ConfigLoader.getDefaultConfigMap();
        Map<String, String> prodConfigPairs = ConfigLoader.getProdConfigMap();
        Map<String, String> injectPairs = new HashMap<String, String>();
        Map<String, String> returnPairs = new HashMap<String, String>();

        // find different configuration pairs
        for (Map.Entry<String, String> entry : prodConfigPairs.entrySet()) {
            String configName = entry.getKey();
            String configValue = entry.getValue();
//            if (defaultConfigPairs.containsKey(configName) && !configValue.equals(defaultConfigPairs.get(configName))) {
//                injectPairs.put(configName, configValue);
//                returnPairs.put(configName, configValue);
//            }
            injectPairs.put(configName, configValue);
            returnPairs.put(configName, configValue);
        }

        // filter out those configuration that can't be tested by testName
        Map<String, Set<String>> configMapping = getConfigMapping();
        if (configMapping != null && !configMapping.isEmpty() && configMapping.containsKey(testName)) {
            Set<String> unTestableConfigSet = configMapping.get(testName);
            if (!injectPairs.isEmpty() && !unTestableConfigSet.isEmpty()) {
                for (String config : injectPairs.keySet()) {
                    if (unTestableConfigSet.contains(config)) {
                        //Log.d2f("[INFO] Do not inject config " + config + " for test " + testName);
                        returnPairs.remove(config);
                    }
                }
            }
        }

        for (Map.Entry<String, String> entry : returnPairs.entrySet()) {
            //Log.d2f("[INFO] " + testName + ": InjectLog " + entry.getKey() + " : " + entry.getValue() + " for test = " + testName);
        }

        return returnPairs;
    }


    // simple test
    public static void main(String args[]) {
        Config.CTEST_MAPPING_FILE_PATH_V = "/Users/alenwang/Desktop/test1.json";
        generateGroupedMapping();
        for (Map.Entry<String, Set<String>> entry : sGroupedMapping.entrySet()) {
            System.out.println(entry.getKey());
            int count = 0;
            for (String s : entry.getValue()) {
                System.out.println(s);
                count ++;
            }
            System.out.println(count);
        }
    }
}
