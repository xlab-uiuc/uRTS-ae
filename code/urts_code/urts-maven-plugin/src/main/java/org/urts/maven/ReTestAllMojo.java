package org.urts.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.urts.Config;
import org.urts.agent.AgentLoader;
import java.io.File;

@Mojo(name = "retestall-init", defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES)
public class ReTestAllMojo extends StaticSelectEkstaziMojo {

    @Override
    public void execute() throws MojoExecutionException {
        preCheckReTestAllFiles();
        System.setProperty("retestall", "true");
        if (AgentLoader.loadEkstaziAgent()) {
            System.setProperty(AbstractMojoInterceptor.ARGLINE_INTERNAL_PROP, prepareEkstaziOptions());
        } else {
            throw new MojoExecutionException("Ekstazi cannot attach to the JVM, please specify Ekstazi 'restore' explicitly.");
        }
    }

    protected void preCheckReTestAllFiles() throws MojoExecutionException {
        Config.preLoadConfigAware();
        String files [] = {Config.CTEST_MAPPING_FILE_PATH_V, Config.CONFIG_PROD_FILE_PATH_V};
        for (String file : files) {
            File checkFile = new File(file);
            if (!checkFile.exists()) {
                throw new MojoExecutionException("ReTestAll configuration file does not exist + " + file + "; Please check your .urtsrc file ");
            }
        }
    }

    private String prepareEkstaziOptions() {
        return "force.all=" + getForceall() +
                ",force.failing=" + getForcefailing() +
                "," + getRootDirOption() +
                (getXargs() == null || getXargs().equals("") ? "" : "," + getXargs());
    }
}
