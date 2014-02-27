package org.wisdom.maven.mojos;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.wisdom.maven.utils.WisdomExecutor;

import java.io.File;

/**
 * Stop Mojo
 */
@Mojo(name = "stop", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true
)
public class StopMojo extends AbstractWisdomMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        new WisdomExecutor().stop(this);
        File pid = new File(getWisdomRootDirectory(), "RUNNING_PID");
        if (WisdomExecutor.waitForFileDeletion(pid)) {
            getLog().info("Wisdom server stopped.");
        } else {
            throw new MojoExecutionException("The " + pid.getName() + " file still exists after having stopped the " +
                    "Wisdom instance - check log");
        }
    }

}
