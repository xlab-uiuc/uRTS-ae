package alluxio;

import alluxio.conf.AlluxioProperties;
import alluxio.conf.InstancedConfiguration;
import alluxio.conf.PropertyKey;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;

import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class TestGetConfigValueForConfigAware {

    @BeforeClass
    public static void before() {
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        File src = new File("../curConfigFile.properties");
        File dest = new File("../alluxio-ctest-" + pid + ".properties");
        try {
            Files.copy(src.toPath(), dest.toPath());
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }


    @AfterClass
    public static void after() {
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        File src = new File("../alluxio-ctest-" + pid + ".properties");
        if (src.exists()) {
            src.delete();
        }
    }


    @Test
    public void testAllConfig() {
        AlluxioProperties mProperties = new AlluxioProperties();
        InstancedConfiguration ic = new InstancedConfiguration(mProperties);
        Map<String, String> configMap = new HashMap<String, String>();
        Field allFiled [] = PropertyKey.class.getDeclaredFields();
        for (Field f : allFiled) {
            if (Modifier.isFinal(f.getModifiers()) && Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers()) && f.getType().equals(PropertyKey.class)) {
                String name = "";
                String value1 = "null";
                String value2 = "null";
                try {
                    PropertyKey key = (PropertyKey) f.get(PropertyKey.class);
                    name = key.getName();
                    value1 = mProperties.get((PropertyKey) f.get(PropertyKey.class));
                    value2 = ic.get((PropertyKey) f.get(PropertyKey.class));
                } catch (Exception e){
                    value2 = "null";
                }
                if (!name.equals("")) {
                    if (Objects.equals(value1, value2)) {
                        configMap.put(name, value1);
                    } else {
                        configMap.put(name, value1 + "@CONFIGAWARESEPARATOR@" + value2);
                    }
                }
            }
        }
        try {
            File f = new File("../.ConfigValue");
            if (f.exists()) {
                File oldFile = new File("../.OldConfigValue");
                if (oldFile.exists()) {
                    oldFile.delete();
                }
                f.renameTo(oldFile);
                f.delete();
            }
            FileWriter myWriter = new FileWriter("../.ConfigValue", true);
            BufferedWriter bufferWriter = new BufferedWriter(myWriter);
            for (Map.Entry<String, String> entry : configMap.entrySet()) {
                bufferWriter.write(entry.getKey() + "=CONFIGAWARE=" + entry.getValue() + "@CONFIGAWARE@\n");
            }
            bufferWriter.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
