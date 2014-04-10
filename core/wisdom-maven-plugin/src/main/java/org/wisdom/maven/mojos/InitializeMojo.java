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


import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.wisdom.maven.Constants;
import org.wisdom.maven.utils.BuildConstants;
import org.wisdom.maven.utils.DependencyCopy;
import org.wisdom.maven.utils.MavenUtils;
import org.wisdom.maven.utils.WisdomRuntimeExpander;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Mojo preparing the Wisdom runtime.
 */
@Mojo(name = "initialize", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.INITIALIZE)
public class InitializeMojo extends AbstractWisdomMojo {

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
     * Deploy the test dependencies to run tests. This option should be used with caution as it may add to much
     * bundles to your runtime.
     */
    @Parameter(defaultValue = "false")
    private boolean deployTestDependencies;

    /**
     * The dependency graph builder to use.
     */
    @Component(hint = "default")
    private DependencyGraphBuilder dependencyGraphBuilder;

    /**
     * A parameter indicating that the current project is using the 'base runtime' instead of the 'full runtime'. This
     * option should only be used by components developed by Wisdom and being part of the 'full runtime'.
     */
    @Parameter
    boolean useBaseRuntime;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("Wisdom Maven Plugin version: " + BuildConstants.get("WISDOM_PLUGIN_VERSION"));

        // Expand if needed.
        if (WisdomRuntimeExpander.expand(this, getWisdomRootDirectory(), useBaseRuntime)) {
            getLog().info("Wisdom Runtime installed in " + getWisdomRootDirectory().getAbsolutePath());
        }

        // Copy compile dependencies that are bundles to the application directory.
        try {
            DependencyCopy.copyBundles(this, dependencyGraphBuilder, !excludeTransitive, deployTestDependencies);
            DependencyCopy.extractWebJars(this, dependencyGraphBuilder, !excludeTransitiveWebJars);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot copy dependencies", e);
        }

        // Install node.
        try {
            getNodeManager().installIfNotInstalled();
        } catch (IOException e) {
            getLog().error("Cannot install node and npm - asset processing won't work", e);
        }

        // Prepare OSGi packaging
        try {
            Properties properties = MavenUtils.getDefaultProperties(project);
            write(properties);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot write the OSGi metadata to " + Constants.OSGI_PROPERTIES, e);
        }

        // Store dependencies as JSON
        try {
            MavenUtils.dumpDependencies(project);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot write the dependency metadata to " + Constants.DEPENDENCIES_FILE, e);
        }
    }

    private void write(Properties properties) throws IOException {
        File file = new File(project.getBasedir(), Constants.OSGI_PROPERTIES);
        file.getParentFile().mkdirs();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            properties.store(fos, "");
        } finally {
            IOUtils.closeQuietly(fos);
        }
    }


}
