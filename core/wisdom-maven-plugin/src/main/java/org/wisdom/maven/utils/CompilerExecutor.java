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

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.mojos.AbstractWisdomMojo;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        Xpp3Dom configuration = PluginExtractor.getBuildPluginMainConfiguration(mojo, MAVEN_COMPILER_PLUGIN);

        if (version == null) {
            version = DEFAULT_VERSION;
        }

        if (configuration == null) {
            configuration = configuration(
                    element(name("compileSourceRoots"), "${project.compileSourceRoots}"),
                    element(name("classpathElements"), "${project.compileClasspathElements}"),
                    element(name("outputDirectory"), "${project.build.outputDirectory}"),
                    element(name("projectArtifact"), "${project.artifact}"),
                    element(name("generatedSourcesDirectory"),
                            "${project.build.directory}/generated-sources/annotations"),
                    element("target", "1.7"),
                    element("source", "1.7")
            );
        } else {
            mojo.getLog().debug("Loading maven-compiler-plugin configuration:");
            mojo.getLog().debug(configuration.toString());
        }

        // Compile sources
        executeMojo(
                plugin(
                        groupId(GROUP_ID),
                        artifactId(MAVEN_COMPILER_PLUGIN),
                        version(version)
                ),
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
     * We can't access the {@link org.apache.maven.plugin.compiler.CompilationFailureException} directly,
     * because the mojo is loaded in another classloader. So, we have to use this method to retrieve the 'compilation
     * failures'.
     *
     * @param mojo      the mojo
     * @param exception the exception that must be a {@link org.apache.maven.plugin.compiler.CompilationFailureException}
     * @return the long message, {@literal null} if it can't be extracted from the exception
     */
    public static String getLongMessage(AbstractWisdomMojo mojo, Object exception) {
        try {
            return (String) exception.getClass().getMethod("getLongMessage").invoke(exception);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            mojo.getLog().error("Cannot extract the long message from the Compilation Failure Exception " + exception, e);
        }
        return null;
    }

    public static Pattern JAVA_COMPILATION_ERROR = Pattern.compile("(.*):\\[(.*),(.*)\\](.*)");

    public static WatchingException build(AbstractWisdomMojo mojo, Throwable exception) {
        String message = getLongMessage(mojo, exception);
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
            return new WatchingException("Java Compilation Error: " + reason, file, Integer.valueOf(line),
                    Integer.valueOf(character), null);
        } else {
            return new WatchingException("Java Compilation Error", exception);
        }
    }
}
