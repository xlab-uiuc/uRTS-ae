package org.apache.hadoop.hbase;

import org.apache.hadoop.conf.Configuration;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.util.*;
import org.junit.ClassRule;
import org.junit.experimental.categories.Category;
import org.apache.hadoop.hbase.testclassification.MiscTests;
import org.apache.hadoop.hbase.testclassification.SmallTests;

@Category({MiscTests.class, SmallTests.class})
public class TestGetConfigValueForConfigAware {

  @ClassRule
  public static final HBaseClassTestRule CLASS_RULE =
      HBaseClassTestRule.forClass(TestGetConfigValueForConfigAware.class);


  @BeforeClass
  public static void before() {
    String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
    File src = new File("curConfigFile.xml");
    File dest = new File("target/test-classes/hbase-ctest-" + pid + ".xml");
    try {
        Files.copy(src.toPath(), dest.toPath());
    } catch (Exception e) {
        e.printStackTrace();
    }
    
  }


  @AfterClass
  public static void after() {
    String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
    File src = new File("target/test-classes/hbase-ctest-" + pid + ".xml");
    if (src.exists()) {
        src.delete();
    }
  }


  @Test
  public void testAllConfig(){
    Map<String, String> config = new HashMap<String, String>();
    Set<String> configSet = new HashSet<String>();
    Configuration conf = HBaseConfiguration.create();
    String filePath [] = {"../hbase-common/src/main/resources/hbase-default.xml"};  // put configuration file path here
    String tagName = "property";
    String tagConfigName = "name";
    String tagConfigValue = "value";
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
                String configDefValue = "";
                for (int j = 0; j < nl2.getLength(); j++) {
                    Node n = nl2.item(j);
                    if (n.getNodeName().equals(tagConfigName)) {
                        configName = n.getTextContent();
                    }
                    if (n.getNodeName().equals(tagConfigValue)) {
                        configDefValue = n.getTextContent();
                    }
                }
                if (!Objects.equals(configName, "")) {
                String configValue = conf.get(configName);
                bufferWriter.write(configName.trim() + "=CONFIGAWARE=");
                if (configName.equals("hbase.tmp.dir") || configName.equals("hbase.rootdir")) {
                    bufferWriter.write(configValue + "@CONFIGAWARESEPARATOR@" + configDefValue + "@CONFIGAWARE@\n");
                } else if (configName.equals("dfs.client.read.shortcircuit")) {
                    bufferWriter.write(configValue + "@CONFIGAWARESEPARATOR@" + "false" + "@CONFIGAWARE@\n");
                } else {
                    bufferWriter.write(configValue + "@CONFIGAWARE@\n");
                }
                }
            }
        }
        bufferWriter.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
}
