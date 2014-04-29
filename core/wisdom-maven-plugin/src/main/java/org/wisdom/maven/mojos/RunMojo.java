/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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
 * Mojo running a 'watched' instance of Wisdom. It deploys the applications and monitor for changes. On each change,
 * the Maven 'watch' pipeline is triggered to re-deploy the bundle, or update configurations and files.
 */
@Mojo(name = "run", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true
)
@Execute(phase = LifecyclePhase.PACKAGE)
public class RunMojo extends AbstractWisdomMojo {

    private Pipeline pipeline;

    /**
     * If set to {@literal true}, it does not collect transitive dependencies. This means that bundles that are
     * transitive dependencies of the current project won't be copied.
     */
    @Parameter(defaultValue = "false")
    private boolean excludeTransitive;

    /**
     * If set to {@literal false}, it enables the analysis and the collection of transitive webjars.
     */
    @Parameter(defaultValue = "true")
    private boolean excludeTransitiveWebJars;

    /**
     * Sets the debug port on which the remote debugger can be plugged.
     * If set to 0 the debug is disabled (default).
     */
    @Parameter(defaultValue = "${debug}")
    public int debug;

    /**
     * Enables the interactive mode of the launched server (shell prompt).
     * Be ware that exiting the framework must be done using the 'exit' command instead of 'CTRL+C'.
     */
    @Parameter(defaultValue = "${interactive}")
    private boolean interactive;

    /**
     * Enables the interactive mode of the launched server (shell prompt). This option is equivalent to {@literal
     * interactive}. Be ware that exiting the framework must be done using the 'exit' command instead of 'CTRL+C'.
     */
    @Parameter(defaultValue = "${shell}")
    private boolean shell;

    /**
     * A parameter indicating that the current project is using the 'base runtime' instead of the 'full runtime'. This
     * option should only be used by components developed by Wisdom and being part of the 'full runtime'.
     */
    @Parameter
    public boolean useBaseRuntime;

    /**
     * The dependency graph builder to use.
     */
    @Component(hint = "default")
    private DependencyGraphBuilder dependencyGraphBuilder;

    /**
     * A parameter indicating whether or not we should remove from the bundle transitive copy some well-known
     * error-prone bundles.
     */
    @Parameter(defaultValue = "true")
    public boolean useDefaultExclusions;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            init();
        } catch (WatchingException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        new WisdomExecutor().execute(this, shell || interactive, debug);

        pipeline.shutdown();
    }

    public void init() throws MojoExecutionException, WatchingException {
        // Expand if needed.
        if (WisdomRuntimeExpander.expand(this, getWisdomRootDirectory(), useBaseRuntime)) {
            getLog().info("Wisdom Runtime installed in " + getWisdomRootDirectory().getAbsolutePath());
        }

        // Copy compile dependencies that are bundles to the application directory.
        try {
            DependencyCopy.copyBundles(this, dependencyGraphBuilder, !excludeTransitive, false, !useDefaultExclusions);
            DependencyCopy.extractWebJars(this, dependencyGraphBuilder, !excludeTransitiveWebJars);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot copy dependencies", e);
        }

        pipeline = Pipelines.watchers(session, basedir, this).watch();
    }


}
