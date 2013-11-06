package org.ow2.chameleon.wisdom.maven.mojos;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.ow2.chameleon.wisdom.maven.utils.DependencyCopy;
import org.ow2.chameleon.wisdom.maven.utils.WisdomRuntimeExpander;

import java.io.IOException;

/**
 * Mojo preparing the Wisdom runtime.
 */
@Mojo(name = "initialize", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.INITIALIZE)
public class InitializeMojo extends AbstractWisdomMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // Expand if needed.
        if (WisdomRuntimeExpander.expand(this, getWisdomRootDirectory())) {
            getLog().info("Wisdom Runtime installed in " + getWisdomRootDirectory().getAbsolutePath());
        }

        // Copy compile dependencies that are bundles to the application directory.
        try {
            DependencyCopy.copy(this);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot copy dependencies", e);
        }

        // Install node.
        try {
            node.installIfNotInstalled();
        } catch (IOException e) {
            getLog().error("Cannot install node and npm - asset processing won't work", e);
        }
    }
}
