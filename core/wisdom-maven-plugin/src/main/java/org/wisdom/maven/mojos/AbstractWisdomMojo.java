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
     * The directory to run the compiler from if fork is true.
     */
    @Parameter(defaultValue = "${basedir}", required = true, readonly = true)
    public File basedir;
    /**
     * The target directory of the compiler if fork is true.
     */
    @Parameter(defaultValue = "${project.build.directory}", required = true, readonly = true)
    public File buildDirectory;

    /**
     * The current build session instance.
     */
    @Component
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
     * Gets the root directory of the Wisdom server. Generally it's 'target/wisdom'.
     *
     * @return the Wisdom's root.
     */
    public File getWisdomRootDirectory() {
        File wisdom = new File(buildDirectory, Constants.WISDOM_DIRECTORY_NAME);
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
     * The Node manager.
     */
    private NodeManager node = new NodeManager(this);

    /**
     * @return the node manager.
     */
    public NodeManager getNodeManager() {
        return node;
    }
}
