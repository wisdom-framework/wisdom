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

import org.apache.commons.exec.ProcessDestroyer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.wisdom.maven.Watcher;
import org.wisdom.maven.pipeline.Pipeline;
import org.wisdom.maven.pipeline.Pipelines;
import org.wisdom.maven.pipeline.Watchers;
import org.wisdom.maven.utils.WisdomExecutor;

import java.io.File;

/**
 * The mojo launching the Wisdom server instance.
 * Do not use, this mojo is invoked automatically from {@code mvn wisdom:run}.
 */
@Mojo(name = "internal-run", threadSafe = false,
        // We need to use the TEST scope to let Surefire access its dependencies.
        requiresDependencyResolution = ResolutionScope.TEST,
        requiresProject = true
)
public class InternalRunMojo extends AbstractWisdomMojo implements Contextualizable {

    private Pipeline pipeline;

    /**
     * If set to {@literal true}, it does not collect transitive dependencies. This means that bundles that are
     * transitive dependencies of the current project won't be copied.
     */
    @Parameter(defaultValue = "false")
    private boolean excludeTransitive;

    /**
     * If set to {@literal false} (default), it enables the analysis and the collection of transitive webjars.
     */
    @Parameter(defaultValue = "false")
    private boolean excludeTransitiveWebJars;

    /**
     * Sets the debug port on which the remote debugger can be plugged.
     * If set to 0 the debug is disabled (default).
     */
    @Parameter(defaultValue = "${debug}")
    public int debug;

    /**
     * JVM arguments appended to the Java Command.
     * Arguments are given like: {@code mvn clean wisdom:run -DjvmArgs="-ea -Dfoo=bar"}
     */
    @Parameter(defaultValue = "${jvmArgs}")
    public String jvmArgs;

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
     * Enables / disables the pom file monitoring.
     */
    @Parameter(defaultValue = "${pom.watching}")
    private boolean pomFileMonitoring = true;  //NOSONAR Cannot be a local variable, it's injected.


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

    /**
     * Configures the behavior of non-OSGi dependencies.
     */
    @Parameter
    public Libraries libraries;

    //TODO Why do we have this field ?
    private PlexusContainer container;

    private ProcessController destroyer = new ProcessController();

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
     * @throws org.apache.maven.plugin.MojoExecutionException for init failures.
     */
    @Override
    public void execute() throws MojoExecutionException {
        try {
            init();
        } catch (ContextException e) {
            throw new MojoExecutionException("Cannot extract the watchers from the context", e);
        }

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
            new WisdomExecutor().execute(this, shell || interactive, debug, jvmArgs, destroyer);
        }
        pipeline.shutdown();

    }

    /**
     * Init method, expands the Wisdom Runtime zip and copies compile dependencies to the
     * application directory. If the {@link #wisdomDirectory} parameter is set then the expansion
     * of the zip is skipped.
     *
     * @throws org.apache.maven.plugin.MojoExecutionException if copy of dependencies fails.
     */
    public void init() throws MojoExecutionException, ContextException {
        if (pomFileMonitoring) {
            Watchers.add(session, new PomWatcher());
        }
        pipeline = Pipelines.watchers(session, basedir, this, pomFileMonitoring).watch();
    }

    /**
     * The watcher responsible of stopping everything when the pom.xml file is changed.
     */
    private class PomWatcher implements Watcher {
        /**
         * Checks that the given file is actually the 'pom.xml' file of the watched project.
         *
         * @param file is the file.
         * @return {@literal true} if the watcher is interested in being notified on an event
         * attached to the given file, {@literal false} otherwise.
         */
        @Override
        public boolean accept(File file) {
            return file.equals(new File(project.getBasedir(), "pom.xml"));
        }

        /**
         * Does nothing.
         *
         * @param file is the file.
         * @return {@code true}
         */
        @Override
        public boolean fileCreated(File file) {
            // Ignored event.
            return true;
        }

        /**
         * The 'pom.xml' file was updated.
         * <p>
         * It shutdowns the pipeline, kill the underlying Chameleon instance and exit the JVM with a special exit
         * code (20) instructing the parent process to relaunch Maven.
         *
         * @param file is the file.
         * @return {@literal false}
         */
        @Override
        public boolean fileUpdated(File file) {
            getLog().warn("A change in the 'pom.xml' has been detected, in order to take such a change into account, " +
                    "the Maven process is going to restart automatically. Don't forget to replug your debugger if you" +
                    " are debugging the application.");
            pipeline.shutdown();
            destroyer.stop();
            return false;
        }

        /**
         * Does nothing.
         *
         * @param file the file
         * @return {@literal true}
         */
        @Override
        public boolean fileDeleted(File file) {
            // Ignored event.
            return true;
        }
    }

    @Override
    public void contextualize(Context context) throws ContextException {
        container = (PlexusContainer) context.get(PlexusConstants.PLEXUS_KEY);
    }

    private class ProcessController implements ProcessDestroyer {

        Process process;

        @Override
        public boolean add(Process process) {
            if (this.process == null) {
                this.process = process;
                return true;
            }
            return false;
        }

        @Override
        public boolean remove(Process process) {
            if (this.process == process) {
                this.process = null;
                return true;
            }
            return false;
        }

        @Override
        public int size() {
            if (process != null) {
                return 1;
            }
            return 0;
        }

        public int stop() {
            if (process == null) {
                return 0;
            }

            try {
                process.destroy();
                process.waitFor();
            } catch (InterruptedException e) { //NOSONAR
                // Ignore it.
            }
            return process.exitValue();
        }
    }
}
