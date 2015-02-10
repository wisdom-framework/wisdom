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
package org.wisdom.maven.utils;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.*;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.mojos.AbstractWisdomMojo;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;


/**
 * Executes the maven-compiler-plugin.
 */
public class CompilerExecutor {

    /**
     * The maven compiler plugin artifact id.
     */
    public static final String MAVEN_COMPILER_PLUGIN = "maven-compiler-plugin";
    /**
     * The maven compiler plugin default version.
     */
    public static final String DEFAULT_VERSION = "3.1";

    /**
     * The maven compiler plugin group id.
     */
    public static final String GROUP_ID = "org.apache.maven.plugins";

    /**
     * The maven compiler plugin goal to compile `src/main/java` classes.
     */
    public static final String COMPILE_GOAL = "compile";

    /**
     * The maven compiler plugin goal to compile `src/test/java` classes.
     */
    public static final String TEST_COMPILE_GOAL = "testCompile";

    /**
     * The prefix to compilation error message.
     */
    public static final String ERROR_TITLE = "Java Compilation Error: ";

    /**
     * Compiles java classes from 'src/main/java'.
     *
     * @param mojo the mojo
     * @throws MojoExecutionException if the compilation fails.
     */
    public void execute(AbstractWisdomMojo mojo) throws MojoExecutionException {
        String version = PluginExtractor.getBuildPluginVersion(mojo, MAVEN_COMPILER_PLUGIN);
        if (version == null) {
            version = DEFAULT_VERSION;
        }
        final Plugin plugin = plugin(
                GROUP_ID,
                MAVEN_COMPILER_PLUGIN,
                version
        );

        Xpp3Dom configuration = PluginExtractor.getBuildPluginConfiguration(mojo, MAVEN_COMPILER_PLUGIN, COMPILE_GOAL);
        if (configuration == null) {
            Properties properties = mojo.project.getProperties();
            String source = properties.getProperty("maven.compiler.source", "1.7");
            String target = properties.getProperty("maven.compiler.target", "1.7");
            configuration = configuration(
                    element("compileSourceRoots", "${project.compileSourceRoots}"),
                    element("classpathElements", "${project.compileClasspathElements}"),
                    element("outputDirectory", "${project.build.outputDirectory}"),
                    element("projectArtifact", "${project.artifact}"),
                    element("generatedSourcesDirectory",
                            "${project.build.directory}/generated-sources/annotations"),
                    element("target", source),
                    element("source", target)
            );
        } else {
            mojo.getLog().debug("Loading maven-compiler-plugin configuration:");
            PluginExtractor.extractEligibleConfigurationForGoal(mojo, plugin, COMPILE_GOAL, configuration);
            mojo.getLog().debug(configuration.toString());

        }

        // Compile sources
        executeMojo(
                plugin,
                goal(COMPILE_GOAL),
                configuration,
                executionEnvironment(
                        mojo.project,
                        mojo.session,
                        mojo.pluginManager
                )
        );
    }

    /**
     * Compiles java classes from 'src/test/java'.
     *
     * @param mojo the mojo
     * @throws MojoExecutionException if the compilation fails.
     */
    public void executeForTests(AbstractWisdomMojo mojo) throws MojoExecutionException {
        // Generating unique System Property to allow multi-execution
        String version = PluginExtractor.getBuildPluginVersion(mojo, MAVEN_COMPILER_PLUGIN);
        if (version == null) {
            version = DEFAULT_VERSION;
        }
        final Plugin plugin = plugin(
                GROUP_ID,
                MAVEN_COMPILER_PLUGIN,
                version
        );

        Xpp3Dom configuration = PluginExtractor.getBuildPluginConfigurationForGoal(mojo, MAVEN_COMPILER_PLUGIN,
                TEST_COMPILE_GOAL);

        if (configuration == null) {
            Properties properties = mojo.project.getProperties();
            String source = properties.getProperty("maven.compiler.source", "1.7");
            String target = properties.getProperty("maven.compiler.target", "1.7");
            String sourceTest = properties.getProperty("maven.compiler.testSource", "1.7");
            String targetTest = properties.getProperty("maven.compiler.testTarget", "1.7");
            configuration = configuration(
                    element("compileSourceRoots", "${project.testCompileSourceRoots}"),
                    element("classpathElements", "${project.testClasspathElements}"),
                    element("outputDirectory", "${project.build.testOutputDirectory}"),
                    element("generatedTestSourcesDirectory",
                            "${project.build.directory}/generated-test-sources/test-annotations"),
                    element("source", source),
                    element("target", target),
                    element("testSource", sourceTest),
                    element("testTarget", targetTest)
            );
        } else {
            mojo.getLog().debug("Loading maven-compiler-plugin configuration (for goal 'testCompile' or 'global' if " +
                    "not set):");
            PluginExtractor.extractEligibleConfigurationForGoal(mojo, plugin, TEST_COMPILE_GOAL, configuration);
            mojo.getLog().debug(configuration.toString());
        }

        // Compile sources
        executeMojo(
                plugin,
                goal(TEST_COMPILE_GOAL),
                configuration,
                executionEnvironment(
                        mojo.project,
                        mojo.session,
                        mojo.pluginManager
                )
        );
    }

    /**
     * We can't access the {@link org.apache.maven.plugin.compiler.CompilationFailureException} directly,
     * because the mojo is loaded in another classloader. So, we have to use this method to retrieve the 'compilation
     * failures'.
     *
     * @param mojo      the mojo
     * @param exception the exception that must be a {@link org.apache.maven.plugin.compile.CompilationFailureException}
     * @return the long message, {@literal null} if it can't be extracted from the exception
     */
    public static String getLongMessage(AbstractWisdomMojo mojo, Object exception) {
        try {
            return (String) exception.getClass().getMethod("getLongMessage").invoke(exception);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            mojo.getLog().error("Cannot extract the long message from the Compilation Failure Exception "
                    + exception, e);
        }
        return null;
    }

    /**
     * Pattern to parse Java compilation error.
     */
    public static Pattern JAVA_COMPILATION_ERROR = Pattern.compile("(.*):\\[(.*),(.*)\\](.*)");

    /**
     * Creates a {@link org.wisdom.maven.WatchingException} object from the given Java compilation exception.
     *
     * @param mojo      the mojo
     * @param exception the exception, thrown by the execution of the maven-compiler-plugin
     * @return the watching exception
     */
    public static WatchingException build(AbstractWisdomMojo mojo, Throwable exception) {
        String message = getLongMessage(mojo, exception);
        if (message == null) {
            // The extraction has failed.
            return new WatchingException(ERROR_TITLE, exception.getMessage(), null, exception);
        }

        if (message.contains("\n")) {
            message = message.substring(0, message.indexOf("\n")).trim();
        }

        final Matcher matcher = JAVA_COMPILATION_ERROR.matcher(message);
        if (matcher.matches()) {
            String path = matcher.group(1);
            String line = matcher.group(2);
            String character = matcher.group(3);
            String reason = matcher.group(4);
            File file = new File(path);
            return new WatchingException(ERROR_TITLE, reason, file, Integer.valueOf(line),
                    Integer.valueOf(character), null);
        } else {
            return new WatchingException(ERROR_TITLE, exception.getMessage(), null, exception);
        }
    }
}
