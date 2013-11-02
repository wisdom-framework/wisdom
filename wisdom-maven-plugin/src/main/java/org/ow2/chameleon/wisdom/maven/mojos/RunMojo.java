package org.ow2.chameleon.wisdom.maven.mojos;

import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.ow2.chameleon.wisdom.maven.processors.Pipeline;
import org.ow2.chameleon.wisdom.maven.processors.Pipelines;
import org.ow2.chameleon.wisdom.maven.processors.ProcessorException;
import org.ow2.chameleon.wisdom.maven.utils.DependencyCopy;
import org.ow2.chameleon.wisdom.maven.utils.WisdomExecutor;
import org.ow2.chameleon.wisdom.maven.utils.WisdomRuntimeExpander;

import java.io.IOException;

/**
 * Run Mojo
 */
@Mojo(name = "run", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresDirectInvocation = true,
        requiresProject = true)
public class RunMojo extends AbstractWisdomMojo {

    private Pipeline pipeline;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            init();
        } catch (ProcessorException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        new WisdomExecutor().execute(this);

        if (pipeline != null) {
            pipeline.tearDown();
        }

    }

    public void init() throws MojoExecutionException, ProcessorException {
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
        pipeline = Pipelines.watcher()
                .initialize(this)
                .watch();
    }


}
