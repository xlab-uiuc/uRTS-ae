package org.apache.hadoop.conf;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.*;

public class TestGetConfigValueForConfigAware {

    @BeforeClass
    public static void before() {
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        File src = new File("curConfigFile.xml");
        File dest = new File("target/classes/core-ctest-" + pid + ".xml");
        try {
            Files.copy(src.toPath(), dest.toPath());
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }


    @AfterClass
    public static void after() {
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        File src = new File("target/classes/core-ctest-" + pid + ".xml");
        if (src.exists()) {
            src.delete();
        }
    }

  
    @Test
    public void testAllConfig(){
        Map<String, String> config = new HashMap<String, String>();
        Set<String> configSet = new HashSet<String>();
        Configuration conf = new Configuration();
        String filePath [] = {"./src/main/resources/core-default.xml", "./src/test/resources/core-site.xml"};  // put configuration file path here
        String tagName = "property";
        String tagConfigName = "name";
        File f = new File(".ConfigValue");
        if (f.exists()) {
            File oldFile = new File(".OldConfigValue");
            if (oldFile.exists()) {
                oldFile.delete();
            }
            f.renameTo(oldFile);
            f.delete();
        }
        try {
            FileWriter myWriter = new FileWriter(".ConfigValue", true);
            BufferedWriter bufferWriter = new BufferedWriter(myWriter);
            for (String ConfigFile : filePath) {
                File file = new File(ConfigFile);
                Assert.assertTrue(file.exists());
                InputStream is = new FileInputStream(file);
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(is);
                NodeList nl = doc.getElementsByTagName(tagName);
                for (int i = 0; i < nl.getLength(); i++) {
                    NodeList nl2 = nl.item(i).getChildNodes();
                    String configName = "";
                    for (int j = 0; j < nl2.getLength(); j++) {
                        Node n = nl2.item(j);
                        if (n.getNodeName().equals(tagConfigName)) {
                            configName = n.getTextContent();
                        }
                    }
                    if (!Objects.equals(configName, "")) {
                        String configValue = conf.get(configName);
                        bufferWriter.write(configName.trim() + "=CONFIGAWARE=");
                        bufferWriter.write(configValue + "@CONFIGAWARE@\n");
                    }
                }
            }
            bufferWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}