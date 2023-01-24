package org.urts.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * This Mojo is used only to define Ekstazi lifecycle.  As Ekstazi has
 * to be run before test phase, there is a need for a Mojo that is
 * executed in test phase to active execution of tests.
 */
@Mojo(name = "retestall", defaultPhase = LifecyclePhase.TEST)
@Execute(goal = "retestall", phase = LifecyclePhase.TEST, lifecycle = "retestall")
public class RetestAllCycleMojo extends ReTestAllMojo {

    public void execute() throws MojoExecutionException {
        // Nothing.
    }
}
