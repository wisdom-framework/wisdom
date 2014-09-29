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
package org.wisdom.maven.osgi;

import aQute.bnd.osgi.Jar;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.project.MavenProject;
import org.wisdom.maven.Constants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility function to manipulate the 'classpath' used to build bundles. Maven dependencies are dumped into a file by
 * the 'initialize' mojo and reloaded when required.
 */
public class Classpath {

    static ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper
                .registerModule(
                        new SimpleModule()
                                .addDeserializer(VersionRange.class, new JsonDeserializer<VersionRange>() {
                                    @Override
                                    public VersionRange deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
                                        return null;
                                    }
                                }).addDeserializer(ArtifactHandler.class, new JsonDeserializer<ArtifactHandler>() {
                            @Override
                            public ArtifactHandler deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
                                return mapper.readValue(jp, DefaultArtifactHandler.class);
                            }
                        }))
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Stores the dependencies from the given project into the 'dependencies.json' file.
     *
     * @param project the project
     * @throws IOException if the file cannot be created.
     */
    public static void store(MavenProject project) throws IOException {
        final File output = new File(project.getBasedir(), Constants.DEPENDENCIES_FILE);
        output.getParentFile().mkdirs();
        ProjectDependencies dependencies = new ProjectDependencies(project);
        mapper.writer()
                .withDefaultPrettyPrinter()
                .writeValue(
                        output,
                        dependencies
                );
    }

    /**
     * Reloads the dependencies stored in the 'dependencies.json' file.
     *
     * @param basedir the base directory
     * @return the list of artifacts.
     * @throws IOException if the file cannot be read.
     */
    public static ProjectDependencies load(File basedir) throws IOException {
        return mapper.reader(ProjectDependencies.class)
                .readValue(new File(basedir, Constants.DEPENDENCIES_FILE));
    }

    static Jar[] computeClassPath(File basedir) throws IOException {
        List<Jar> list = new ArrayList<>();
        File classes = new File(basedir, "target/classes");

        if (classes.isDirectory()) {
            list.add(new Jar("", classes));
        }

        Set<Artifact> artifacts = load(basedir).getTransitiveDependencies();

        for (Artifact artifact : artifacts) {
            if (!"test".equalsIgnoreCase(artifact.getScope())
                    && artifact.getArtifactHandler().isAddedToClasspath()) {
                File file = artifact.getFile();
                if (file.getName().endsWith(".jar") && file.isFile()) {
                    list.add(new Jar(artifact.getArtifactId(), file));
                }
            }
        }

        Jar[] cp = new Jar[list.size()];
        list.toArray(cp);

        return cp;
    }

    static Set<String> computeClassPathElement(File basedir) throws IOException {
        Set<String> list = new LinkedHashSet<>();
        File classes = new File(basedir, "target/classes");

        if (classes.isDirectory()) {
            list.add(classes.getAbsolutePath());
        }

        Set<Artifact> artifacts = load(basedir).getTransitiveDependencies();

        for (Artifact artifact : artifacts) {
            if (!"test".equalsIgnoreCase(artifact.getScope())
                    && artifact.getArtifactHandler().isAddedToClasspath()) {
                File file = artifact.getFile();
                if (file.getName().endsWith(".jar") && file.isFile()) {
                    list.add(file.getAbsolutePath());
                }
            }
        }

        return list;
    }
}
