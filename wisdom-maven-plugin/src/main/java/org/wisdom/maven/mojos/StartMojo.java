package org.wisdom.maven.mojos;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.wisdom.maven.utils.WisdomExecutor;

import java.io.File;
import java.io.IOException;

/**
 * Start Mojo
 */
@Mojo(name = "start", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true
)
@Execute(phase = LifecyclePhase.PACKAGE)
public class StartMojo extends AbstractWisdomMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        new WisdomExecutor().executeInBackground(this);
        File pid = new File(getWisdomRootDirectory(), "RUNNING_PID");
        if (WisdomExecutor.waitForFile(pid)) {
            try {
                getLog().info("Wisdom launched in background in process '" + FileUtils.readFileToString(pid) + "'.");
            } catch (IOException e) {
                getLog().warn("Cannot read the 'RUNNING_PID' file", e);
            }
        } else {
            getLog().error("The " + pid.getName() + " file was not created despite the 'successful' launch of Wisdom");
        }
    }


}
