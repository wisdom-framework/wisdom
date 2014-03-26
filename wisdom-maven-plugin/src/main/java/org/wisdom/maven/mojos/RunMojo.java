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
            DependencyCopy.copyBundles(this, dependencyGraphBuilder, !excludeTransitive, false);
            DependencyCopy.extractWebJars(this, dependencyGraphBuilder, !excludeTransitiveWebJars);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot copy dependencies", e);
        }

        pipeline = Pipelines.watchers(session, basedir, this).watch();
    }


}
