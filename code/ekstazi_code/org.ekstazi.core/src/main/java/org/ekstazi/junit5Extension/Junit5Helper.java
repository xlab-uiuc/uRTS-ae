package org.ekstazi.junit5Extension;

public class Junit5Helper {
    protected static Boolean isTestFailed = false;

    public static Boolean isTestClassTransformNeeded (String className) {
        if (className.contains("Test") &&
                !className.contains("org/apache/tools/ant") &&
                !className.contains("org/apache/maven") &&
                !className.contains("org/junit") &&
                !className.contains("org/opentest4j") &&
                !className.contains("org/ekstazi")) {
            return true;
        }
        return false;
    }
}