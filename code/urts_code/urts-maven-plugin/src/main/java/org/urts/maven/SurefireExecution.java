package org.urts.maven;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;

public class SurefireExecution {
    /** Names for Maven Surefire Configuration Elements */
    private final String MAVEN_SUREFIRE_INCLUDES = "includes";
    private final String MAVEN_SUREFIRE_INCLUDE = "include";
    private final String MAVEN_SUREFIRE_EXCLUDES = "excludes";
    private final String MAVEN_SUREFIRE_TEST_GOAL = "test";
    private final String MAVEN_FORKCOUNT = "forkCount";

    /** Maven Project objects */
    private Plugin surefire;
    private MavenProject mavenProject;
    private MavenSession mavenSession;
    private BuildPluginManager pluginManager;

    /** The Getter Test Class Name To Run With Surefire */
    private String testClassName;

    /** Maven log */
    private Log log;

    public SurefireExecution(Plugin surefire, MavenProject mavenProject, MavenSession mavenSession,
                             BuildPluginManager pluginManager, String testClassName, Log log) {
        this.surefire = surefire;
        this.mavenProject = mavenProject;
        this.mavenSession = mavenSession;
        this.pluginManager = pluginManager;
        this.testClassName = testClassName;
        this.log = log;
    }

    /**
     * Invoke Maven Surefire to run @testClassName
     * @throws MojoExecutionException
     */
    public void run() throws MojoExecutionException {
        try {
            Xpp3Dom configuration = modifySurefireConfiguration((Xpp3Dom) this.surefire.getConfiguration());
            MojoExecutor.executeMojo(this.surefire, MojoExecutor.goal(MAVEN_SUREFIRE_TEST_GOAL), configuration,
                    MojoExecutor.executionEnvironment(this.mavenProject, this.mavenSession, this.pluginManager));
        } catch (MojoExecutionException e) {
            throw new MojoExecutionException("Unable to execute maven surefire" + e);
        }
    }

    /**
     * Modify the Maven Surefire Configuration to only include
     * getter test class @testClassName
     * @param conf
     * @return Modified Maven Surefire Configuration Object
     * @throws MojoExecutionException
     */
    private Xpp3Dom modifySurefireConfiguration(Xpp3Dom conf) throws MojoExecutionException {
        if(conf == null) {
            conf = new Xpp3Dom("configuration");
        }
        Xpp3Dom retConf = new Xpp3Dom(conf);
        if (!removeChild(retConf, MAVEN_SUREFIRE_INCLUDES) || !removeChild(retConf, MAVEN_SUREFIRE_EXCLUDES)) {
            throw new MojoExecutionException("Unable to remove includes and excludes from Maven Surefire Configuration");
        }
        addGetterIncludes(retConf);
        modifyMavenForkCountToZero(retConf);
        return retConf;
    }

    // Internal

    /** For some projects that will fail surefire:test when fork count
     * is larger than 0. (e.g., ZooKeeper)
     * @param node
     */
    private void modifyMavenForkCountToZero(Xpp3Dom node) {
        int nodeIndex = getChildIndex(node, MAVEN_FORKCOUNT);
        if (nodeIndex >= 0) {
            node.removeChild(nodeIndex);
        }
        Xpp3Dom modifiedForkCount = makeNode(MAVEN_FORKCOUNT, "0");
        node.addChild(modifiedForkCount);
    }

    private boolean removeChild(Xpp3Dom node, String name) {
        int index = getChildIndex(node, name);
        if (index < 0) {
            return true;
        }
        if (index >= 0) {
            this.log.debug("remove " + name + " now");
            node.removeChild(index);
            return true;
        }
        return false;
    }

    private int getChildIndex(Xpp3Dom node, String childName) {
        for(int i = 0; i < node.getChildCount(); i++) {
            if (node.getChild(i).getName().equalsIgnoreCase(childName)) {
                this.log.debug(childName + " at index: " + i);
                return i;
            }
        }
        return -1;
    }

    private void addGetterIncludes(Xpp3Dom node) {
        Xpp3Dom includes = makeNode(MAVEN_SUREFIRE_INCLUDES);
        includes.addChild(makeNode(MAVEN_SUREFIRE_INCLUDE, "**/" + testClassName));
        this.log.debug("adding includes " + includes.toString());
        node.addChild(includes);
    }

    private Xpp3Dom makeNode(String name) {
        Xpp3Dom node = new Xpp3Dom(name);
        return node;
    }

    private Xpp3Dom makeNode(String name, String value) {
        Xpp3Dom node = new Xpp3Dom(name);
        node.setValue(value);
        return node;
    }
}
