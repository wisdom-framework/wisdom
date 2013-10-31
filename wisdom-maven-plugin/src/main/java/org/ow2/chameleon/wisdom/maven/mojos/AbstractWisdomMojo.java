package org.ow2.chameleon.wisdom.maven.mojos;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.ow2.chameleon.wisdom.maven.Constants;

import java.io.File;
import java.util.List;

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
     * The Maven BuildPluginManager component.
     */
    @Component
    public BuildPluginManager pluginManager;

    public File getWisdomRootDirectory() {
        File wisdom = new File(buildDirectory, Constants.WISDOM_DIRECTORY_NAME);
        wisdom.mkdirs();
        return wisdom;
    }

    @Parameter(defaultValue = "${java.home}", required = true, readonly = true)
    public File javaHome;

}
