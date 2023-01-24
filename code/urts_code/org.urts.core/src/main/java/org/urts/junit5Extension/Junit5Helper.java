package org.urts.junit5Extension;

import org.urts.Config;

public class Junit5Helper {
    protected static Boolean isTestFailed = false;

    public static Boolean isTestClassTransformNeeded (String className) {
        if (className.contains("Test") &&
                !className.contains(Config.CONFIG_GETTER_TEST_NAME_V) &&
                !className.contains("org/apache/tools/ant") &&
                !className.contains("org/apache/maven") &&
                !className.contains("org/junit") &&
                !className.contains("org/opentest4j") &&
                !className.contains("org/urts")) {
            return true;
        }
        return false;
    }
}
