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
import org.wisdom.maven.mojos.AbstractWisdomMojo;

import java.util.ArrayList;
import java.util.List;

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
                    element("source", "1.7"));
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
}
