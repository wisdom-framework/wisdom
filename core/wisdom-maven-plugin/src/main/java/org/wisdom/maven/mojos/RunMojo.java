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
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.wisdom.maven.pipeline.Pipeline;
import org.wisdom.maven.pipeline.Pipelines;
import org.wisdom.maven.utils.DependencyCopy;
import org.wisdom.maven.utils.WisdomExecutor;
import org.wisdom.maven.utils.WisdomRuntimeExpander;

import java.io.IOException;
import java.util.List;

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
     * <p>
     * This parameter is deprecated, used the "distribution" parameter instead.
     */
    @Parameter
    @Deprecated
    boolean useBaseRuntime;

    /**
     * A parameter to select the Wisdom distribution to use. Are accepted a profile's name among: {base, equinox,
     * regular} (regular is the default distribution), or artifact coordinated given under the form:
     * GROUP_ID:ARTIFACT_ID:EXTENSION:CLASSIFIER:VERSION. If not set the regular distribution is used.
     */
    @Parameter(defaultValue = "regular")
    String wisdomRuntime;

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

    @Parameter
    public Libraries libraries;

    /**
     * This method is called when the JVM is shutting down and notify all the waiting threads.
     */
    private void unblock() {
        synchronized (this) {
            // This notify can be considered as 'naked', because it does not modify the current object state (the
            // object on which the synchronized is done). However in this very case,
            // it's normal. The wait-notify protocol is only used to block the thread until the JVM stops.
            notifyAll(); //NOSONAR
        }
    }

    /**
     * Execute method, initializes the <em>watch</em> pipeline. If the {@link #wisdomDirectory}
     * parameter is set then the execution of the wisdom server is skipped,
     * and keeps the thread alive until the shutdown of the JVM. Otherwise the wisdom server is
     * started. Before returning, the method cleans up the pipeline.
     *
     * @throws MojoExecutionException for init failures.
     */
    @Override
    public void execute() throws MojoExecutionException {
        if (useBaseRuntime) {
            wisdomRuntime = "base";
        }

        init();

        if (wisdomDirectory != null) {
            getLog().info("Wisdom Directory set to " + wisdomDirectory.getAbsolutePath() + " - " +
                    "skipping the execution of the wisdom server for " + project.getArtifactId());

            // Here things are a bit tricky. As we are not going to start the Wisdom server,
            // the current thread is going to continue. We need to hold it and release it when
            // the JVM stops. For this purpose we register a shutdown hook that is going to
            // release the current thread we put in the waiting queue. This synchronization
            // protocol is quite safe, as only one thread can enter this block.

            // Register a shutdown hook that will unblock the execution when called.
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

                /**
                 * Calls the unblock method.
                 */
                @Override
                public void run() {
                    unblock();
                }
            }));

            /**
             * Entering a blocking block.
             * We are going to wait until the JVM shuts down.
             */
            synchronized (this) {
                try {
                    // Here again, it's an unconditional wait. There are no condition on which we are waiting,
                    // we just hold the current thread until the JVM stops.
                    wait(); //NOSONAR
                } catch (InterruptedException e) {
                    getLog().warn("We were interrupted", e);
                }
            }
        } else {
            new WisdomExecutor().execute(this, shell || interactive, debug);

        }
        pipeline.shutdown();

    }

    /**
     * Init method, expands the Wisdom Runtime zip and copies compile dependencies to the
     * application directory. If the {@link #wisdomDirectory} parameter is set then the expansion
     * of the zip is skipped.
     *
     * @throws MojoExecutionException if copy of dependencies fails.
     */
    public void init() throws MojoExecutionException {
        // Expand if needed.
        if (wisdomDirectory != null) {
            getLog().info("Skipping Wisdom Runtime unzipping because you are using a remote " +
                    "Wisdom server: " + wisdomDirectory.getAbsolutePath());
        } else {
            if (WisdomRuntimeExpander.expand(this, getWisdomRootDirectory(), wisdomRuntime)) {
                getLog().info("Wisdom Runtime installed in " + getWisdomRootDirectory().getAbsolutePath());
            }
        }

        // Copy the dependencies
        try {
            // Bundles.
            DependencyCopy.copyBundles(this, dependencyGraphBuilder, !excludeTransitive, false,
                    !useDefaultExclusions, libraries);
            // Unpack web jars.
            DependencyCopy.extractWebJars(this, dependencyGraphBuilder, !excludeTransitiveWebJars);

            // Non-bundle dependencies.
            DependencyCopy.copyLibs(this, dependencyGraphBuilder, libraries);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot copy dependencies", e);
        }

        pipeline = Pipelines.watchers(session, basedir, this).watch();
    }


}
