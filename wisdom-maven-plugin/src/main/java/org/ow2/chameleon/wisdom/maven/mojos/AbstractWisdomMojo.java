package org.ow2.chameleon.wisdom.maven.mojos;

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
import org.ow2.chameleon.wisdom.maven.Constants;
import org.ow2.chameleon.wisdom.maven.node.NodeManager;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Common part.
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

    @Component
    public RepositorySystem repoSystem;


    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    public RepositorySystemSession repoSession;

    @Parameter(defaultValue = "${project.remotePluginRepositories}", readonly = true)
    public List<RemoteRepository> remoteRepos;


    public File getWisdomRootDirectory() {
        File wisdom = new File(buildDirectory, Constants.WISDOM_DIRECTORY_NAME);
        wisdom.mkdirs();
        return wisdom;
    }

    @Parameter(defaultValue = "${java.home}", required = true, readonly = true)
    public File javaHome;

    public NodeManager node = new NodeManager(this);
}
