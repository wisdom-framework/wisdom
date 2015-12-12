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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.wisdom.maven.Constants;
import org.wisdom.maven.node.NodeManager;

import java.io.File;
import java.util.List;

/**
 * Shared logic, fields and methods between all Wisdom Mojos.
 */
public abstract class AbstractWisdomMojo extends AbstractMojo {

    /**
     * The maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    public MavenProject project;

    /**
     * The project base directory.
     */
    @Parameter(defaultValue = "${basedir}", required = true, readonly = true)
    public File basedir;
    /**
     * The target directory of the project.
     */
    @Parameter(defaultValue = "${project.build.directory}", required = true, readonly = true)
    public File buildDirectory;

    /**
     * The current build session instance.
     */
    @Parameter(defaultValue = "${session}", readonly = true )
    public MavenSession session;

    /**
     * Maven ProjectHelper.
     */
    @Component
    public MavenProjectHelper projectHelper;

    /**
     * The plugin dependencies.
     */
    @Parameter(defaultValue = "${plugin.artifacts}")
    public List<Artifact> pluginDependencies;

    /**
     * The plugin.
     */
    @Parameter(defaultValue = "${plugin}")
    public PluginDescriptor plugin;

    /**
     * The Maven BuildPluginManager component.
     */
    @Component
    public BuildPluginManager pluginManager;

    /**
     * The repository system.
     */
    @Component
    public RepositorySystem repoSystem;

    /**
     * The session to access the repository system.
     */
    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    public RepositorySystemSession repoSession;

    /**
     * The list of remote repositories.
     */
    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
    public List<RemoteRepository> remoteRepos;

    /**
     * Indicates the location of the Wisdom directory to use. Generated artifacts (Bundles,
     * templates...) are copied to this directory.
     */
    @Parameter(defaultValue = "${wisdomDirectory}")
    public File wisdomDirectory;

    @Component(role = SettingsDecrypter.class)
    public SettingsDecrypter decrypter;

    @Parameter(defaultValue = "${nodeVersion}")
    private String nodeVersion;

    @Parameter(defaultValue = "${npmVersion}")
    private String npmVersion;

    /**
     * Gets the root directory of the Wisdom server. Generally it's 'target/wisdom' except if the
     * {@link #wisdomDirectory} parameter is configured. In this case,
     * it returns the location specified by this parameter.
     *
     * @return the Wisdom's root.
     */
    public File getWisdomRootDirectory() {
        File wisdom;
        if (wisdomDirectory == null) {
            wisdom = new File(buildDirectory, Constants.WISDOM_DIRECTORY_NAME);
        } else {
            this.getLog().debug("Using Wisdom Directory : " + wisdomDirectory.getAbsolutePath());
            wisdom = wisdomDirectory;
        }

        if (wisdom.mkdirs()) {
            this.getLog().debug(wisdom.getAbsolutePath() + " directory created.");
        }
        return wisdom;
    }

    /**
     * The JAVA_HOME value.
     */
    @Parameter(defaultValue = "${java.home}", required = true, readonly = true)
    public File javaHome;


    /**
     * The url to download the node distribution.
     * Default value is 'http://nodejs.org/dist/'.
     */
    @Parameter(defaultValue = "${nodeDistributionRootUrl}")
    protected String nodeDistributionRootUrl;

    /**
     * Gets the root url where the node distribution is downloaded from. Default value is 'http://nodejs.org/dist/' except if the
     * {@link #nodeDistributionUrl} parameter is configured. In this case,
     * it returns the url specified by this parameter.
     *
     * @return the root url used to download the node distribution.
     */
    public String getNodeDistributionRootUrl() {
        String ret = nodeDistributionRootUrl;
        if (nodeDistributionRootUrl == null) {
            ret = Constants.NODE_DIST_ROOT_URL;
        }

        return ret;
    }

    /**
     * The root url of the npm registry.
     * Default value is 'https://registry.npmjs.org/'.
     */
    @Parameter(defaultValue = "${npmRegistryRootUrl}")
    protected String npmRegistryRootUrl;

    /**
     * Gets the root url of the npm registry. Default value is 'https://registry.npmjs.org/' except if the
     * {@link #npmRegistryRootUrl} parameter is configured. In this case,
     * it returns the url specified by this parameter.
     *
     * @return the root url of the npm registry.
     */
    public String getNpmRegistryRootUrl() {
        if (npmRegistryRootUrl == null) {
            return Constants.NPM_REGISTRY_ROOT_URL;
        } else {
            return npmRegistryRootUrl;
        }
    }


    /**
     * The Node manager.
     */
    private NodeManager node = new NodeManager(this);

    /**
     * @return the node manager.
     */
    public NodeManager getNodeManager() {
        return node;
    }

    /**
     * Sets the node manager. For testing purpose only.
     */
    public void setNodeManager(NodeManager manager) {
        this.node = manager;
    }

    /**
     * @return the directory in which internal assets are stored.
     */
    public File getInternalAssetsDirectory() {
        return new File(basedir, Constants.MAIN_RESOURCES_DIR + "/assets");
    }

    /**
     * @return the directory in which internal assets are copied.
     */
    public File getInternalAssetOutputDirectory() {
        return new File(buildDirectory, "classes/assets");
    }

    /**
     * @return the directory in which external assets are stored.
     */
    public File getExternalAssetsDirectory() {
        return new File(basedir, Constants.ASSETS_SRC_DIR);
    }

    /**
     * @return the directory in which external assets are copied.
     */
    public File getExternalAssetsOutputDirectory() {
        return new File(getWisdomRootDirectory(), Constants.ASSETS_DIR);
    }

    public String getNodeVersion() {
        if (nodeVersion == null) {
            return Constants.NODE_VERSION;
        } else {
            if (nodeVersion.startsWith("v")) {
                return nodeVersion;
            } else {
                return "v" + nodeVersion;
            }
        }
    }

    public String getNPMVersion() {
        if (npmVersion == null) {
            return Constants.NPM_VERSION;
        } else {
            return nodeVersion;
        }
    }
}
