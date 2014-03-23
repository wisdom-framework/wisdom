package org.wisdom.maven.mojos;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.pipeline.Pipeline;
import org.wisdom.maven.pipeline.Pipelines;
import org.wisdom.maven.utils.DependencyCopy;
import org.wisdom.maven.utils.WisdomExecutor;
import org.wisdom.maven.utils.WisdomRuntimeExpander;

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

    @Parameter(defaultValue = "false")
    private boolean excludeTransitive;

    @Parameter(defaultValue = "true")
    private boolean excludeTransitiveWebJars;

    /**
     * The dependency graph builder to use.
     */
    @Component(hint = "default")
    private DependencyGraphBuilder dependencyGraphBuilder;

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
            DependencyCopy.copyBundles(this, dependencyGraphBuilder, !excludeTransitive);
            DependencyCopy.extractWebJars(this, dependencyGraphBuilder, !excludeTransitiveWebJars);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot copy dependencies", e);
        }

        pipeline = Pipelines.watchers(session, basedir, this).watch();
    }


}
