package org.wisdom.maven.utils;

import org.apache.maven.plugin.MojoExecutionException;
import org.wisdom.maven.mojos.AbstractWisdomMojo;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;


/**
 * Executes the maven-compiler-plugin.
 */
public class CompilerExecutor {

    public static final String MAVEN_COMPILER_PLUGIN = "maven-compiler-plugin";
    public static final String DEFAULT_VERSION = "3.1";
    public static final String GROUP_ID = "org.apache.maven.plugins";
    public static final String COMPILE_GOAL = "compile";

    public void execute(AbstractWisdomMojo mojo) throws MojoExecutionException {
        String version = PluginExtractor.getBuildPluginVersion(mojo, MAVEN_COMPILER_PLUGIN);

        if (version == null) {
            version = DEFAULT_VERSION;
        }

        // Compile sources
        executeMojo(
                plugin(
                        groupId(GROUP_ID),
                        artifactId(MAVEN_COMPILER_PLUGIN),
                        version(version)
                ),
                goal(COMPILE_GOAL),
                configuration(
                        element(name("compileSourceRoots"), "${project.compileSourceRoots}"),
                        element(name("classpathElements"), "${project.compileClasspathElements}"),
                        element(name("outputDirectory"), "${project.build.outputDirectory}"),
                        element(name("projectArtifact"), "${project.artifact}"),
                        element(name("generatedSourcesDirectory"), "${project.build.directory}/generated-sources/annotations")
                ),
                executionEnvironment(
                        mojo.project,
                        mojo.session,
                        mojo.pluginManager
                )
        );
    }

}
