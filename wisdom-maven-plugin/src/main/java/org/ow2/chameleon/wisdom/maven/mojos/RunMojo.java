package org.ow2.chameleon.wisdom.maven.mojos;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.ow2.chameleon.wisdom.maven.WatchingException;
import org.ow2.chameleon.wisdom.maven.pipeline.Pipeline;
import org.ow2.chameleon.wisdom.maven.pipeline.Pipelines;
import org.ow2.chameleon.wisdom.maven.utils.DependencyCopy;
import org.ow2.chameleon.wisdom.maven.utils.WisdomExecutor;
import org.ow2.chameleon.wisdom.maven.utils.WisdomRuntimeExpander;

import java.io.IOException;

/**
 * Run Mojo
 */
@Mojo(name = "run", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true
        )
@Execute(phase = LifecyclePhase.PACKAGE)
public class RunMojo extends AbstractWisdomMojo {

    private Pipeline pipeline;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            init();
        } catch (WatchingException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        new WisdomExecutor().execute(this);

        pipeline.shutdown();
    }

    public void init() throws MojoExecutionException, WatchingException {
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

        pipeline = Pipelines.watchers(session, this).watch();
    }


}
