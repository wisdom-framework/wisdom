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


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.wisdom.maven.Constants;
import org.wisdom.maven.osgi.Classpath;
import org.wisdom.maven.utils.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.wisdom.maven.node.NPM;

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
     * If set to {@literal false} (default), it enables the analysis and the collection of transitive webjars.
     */
    @Parameter(defaultValue = "false")
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
     * Whether or not webjars are unpacked to {@code target/webjars}.
     */
    @Parameter(defaultValue = "false")
    private boolean unpackWebJars;


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

    /**
     * Execute, first expands the wisdom runtime from zip if needed and if the {@link
     * #wisdomDirectory} parameter is not set. Then copies dependencies from the {@literal
     * compile} scope (including transitives if not disabled).
     *
     * @throws MojoExecutionException when the copy of compile dependencies fails,
     *                                or OSGi packaging fails, or storing dependencies in a JSON
     *                                file fails.
     */
    @Override
    public void execute() throws MojoExecutionException {
        getLog().debug("Wisdom Maven Plugin version: " + BuildConstants.get("WISDOM_PLUGIN_VERSION"));

        // Still here for compatibility
        //noinspection deprecation
        if (useBaseRuntime) {
            wisdomRuntime = "base";
        }

        // Expands if needed.
        if (wisdomDirectory == null && WisdomRuntimeExpander.expand(this,
                getWisdomRootDirectory(), wisdomRuntime)) {
            getLog().info("Wisdom Runtime installed in " + getWisdomRootDirectory().getAbsolutePath());
        }

        // Copy the dependencies
        try {
            // Bundles.
            DependencyCopy.copyBundles(this, dependencyGraphBuilder, !excludeTransitive, deployTestDependencies,
                    !useDefaultExclusions, libraries);
            // Unpack or copy web jars.
            WebJars.manageWebJars(this, dependencyGraphBuilder, !excludeTransitiveWebJars, unpackWebJars);

            // Non-bundle dependencies.
            DependencyCopy.copyLibs(this, dependencyGraphBuilder, libraries);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot copy dependencies", e);
        }

        // Install node.
        try {
            getNodeManager().installIfNotInstalled();
        } catch (IOException e) {
            getLog().error("Cannot install node and npm - asset processing won't work", e);
        }
        
        // Configure NPM registry 
        NPM.configureRegistry(getNodeManager(), getLog(), getNpmRegistryRootUrl());
        
        // Prepare OSGi packaging
        try {
            Properties properties = MavenUtils.getDefaultProperties(project);
            write(properties);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot write the OSGi metadata to " + Constants.OSGI_PROPERTIES, e);
        }

        // Store dependencies as JSON
        try {
            Classpath.store(project);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot dump the project's dependencies to "
                    + Constants.DEPENDENCIES_FILE, e);
        }

        // Ensure that the conf file as a secret.
        try {
            ApplicationSecretGenerator.ensureOrGenerateSecret(project, getLog());
        } catch (IOException e) {
            throw new MojoExecutionException("Update the application configuration to set the secret key", e);
        }

        // Cleanup pipeline error is any
        clearPipelineError();
    }

    /**
     * Deletes all error report from the pipeline error directory.
     */
    private void clearPipelineError() {
        File dir = new File(buildDirectory, "pipeline");
        if (dir.isDirectory()) {
            try {
                FileUtils.cleanDirectory(dir);
            } catch (IOException e) {
                getLog().warn("Cannot clean the pipeline directory", e);
            }
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
