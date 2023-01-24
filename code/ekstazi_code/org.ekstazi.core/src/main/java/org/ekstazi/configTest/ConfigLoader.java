package org.ekstazi.configTest;

import org.ekstazi.Config;
import org.ekstazi.log.Log;
import org.ekstazi.util.FileUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigLoader {
    private static final Map<String, String> sProdConfigMap = new HashMap<String, String>();
    private static final String configFileSeparator = ",";
    public static Map<String, String> getProdConfigMap() {
        if (sProdConfigMap.isEmpty() || sProdConfigMap == null) {
            loadProdFromFile();
        }
        return sProdConfigMap;
    }

    /**
     *  Load prod configuration file
     */
    public static void loadProdFromFile() {
        String prodFileName = Config.CONFIG_PROD_FILE_PATH_V;
        load0(prodFileName, sProdConfigMap);
    }

    // Load default Configuration from Configuration file
    private static void load0(String filenameList, Map<String, String> map) {
        InputStream is = null;

        //Handle several configuration files, separated by ","
        String files [] = filenameList.split(configFileSeparator);
        for (String filename : files) {
            filename = filename.trim();
            if (filename == null || filename.isEmpty()) {
                //Log.d2f("[INFO] Load configuration: Continue next file, current filename: " + filename);
                continue;
            }
            if (!hasConfigFile(filename)) {
                //Log.d2f("[ERROR] Can't find user's configuration file: " + filename);
                return;
            }
            try {
                is = new FileInputStream(filename);
                parseConfigurationFile(filename, is, map);
                is.close();
            } catch (IOException e) {
                Log.e("Loading configuration is not successful", e);
                //Log.d2f("[ERROR] Loading configuration is not successful" + e);
                map.clear();
            } finally {
                FileUtil.closeAndIgnoreExceptions(is);
            }
        }
    }

    public static Boolean hasConfigFile(String configFileName) {
        //String configFileName = Config.CONFIG_FILE_PATH_V;
        if (configFileName.isEmpty() || configFileName == null) {
            return false;
        }
        File configFile = new File(configFileName);
        return configFile.exists();
    }

    private static String getFileSuffix(String filename) {
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    private static void parseConfigurationFile(String filename, InputStream is, Map<String, String> map) throws IOException {
        String fileSuffix = getFileSuffix(filename).toLowerCase();
        switch (fileSuffix) {
            case "xml":
                loadFromXML(is, map);
                break;
            case "properties":
            case "cfg":
                loadFromPropertiesAndCFG(is, map);
                break;
            default:
                Log.e("Can't load configuration from ." + fileSuffix + " file");
                throw new IOException();
        }
    }

    private static void loadFromXML(InputStream is, Map<String, String> map) {
        parseXML(is, map, "property", "name", "value");
    }

    private static void loadFromPropertiesAndCFG(InputStream is, Map<String, String> map) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            while(reader.ready()) {
                String line = reader.readLine();
                String configPairs [] = line.split("=");
                if (!line.contains("=")) {
                    continue;
                }else if (configPairs.length == 1) {
                    String configName = configPairs[0];
                    String configValue = "null";
                    if (!Objects.equals(configName, "")) {
                        map.put(replaceBlank(configName), replaceBlank(configValue));
                    }
                } else if (configPairs.length == 2) {
                    String configName = configPairs[0];
                    String configValue = configPairs[1];
                    if (!Objects.equals(configName, "")) {
                        map.put(replaceBlank(configName), replaceBlank(configValue));
                    }
                } else {
                    //Log.d2f("[ERROR] Incorrectly parse configuration from properties file: configPairs length is too long " + Arrays.toString(configPairs));
                }
            }
        } catch (IOException e) {
            //Log.d2f("[ERROR] Loading configuration is not successful: " + e.getStackTrace());
            Log.e("Loading configuration is not successful", e);
            map.clear();
        }
    }

    public static void parseXML(InputStream is, Map<String, String> map, String tagName, String tagConfigName, String tagConfigValue) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);
            NodeList nl = doc.getElementsByTagName(tagName);
            for (int i = 0; i < nl.getLength(); i++) {
                NodeList nl2 = nl.item(i).getChildNodes();
                String configName = "";
                String configValue = "";
                for (int j = 0; j < nl2.getLength(); j++) {
                    Node n = nl2.item(j);
                    if (n.getNodeName().equals(tagConfigName)) configName = n.getTextContent();
                    if (n.getNodeName().equals(tagConfigValue)) configValue = n.getTextContent();
                }

                // Multiple configuration files may have duplicated settings. We choose the last one as the final value (Overwrite)
                // This is the same idea as some real-world software like Hadoop.
                if (!Objects.equals(configName, "")) {
                    map.put(replaceBlank(configName), replaceBlank(configValue));
                }
                //System.out.println(configName + " , " + configValue);
            }
        } catch (Exception e) {
            //Log.d2f("[ERROR] Loading configuration is not successful: " + e.getStackTrace());
            Log.e("Loading configuration is not successful", e);
            map.clear();
        }
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
}
