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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.traversal.DependencyNodeVisitor;
import org.slf4j.LoggerFactory;
import org.wisdom.maven.mojos.AbstractWisdomMojo;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.ow2.chameleon.core.utils.BundleHelper.isBundle;

/**
 * Copy all compile dependencies from the project (excluding transitive) to the expected directories.
 */
public class DependencyCopy {

    /**
     * Copies dependencies, that are bundles, to the application directory.
     * If the bundle is already in core or runtime, the bundle is not copied.
     *
     * @param mojo       the mojo
     * @param graph      the dependency graph builder
     * @param transitive whether or not we include the transitive dependencies.
     * @throws IOException when a bundle cannot be copied
     */
    public static void copyBundles(AbstractWisdomMojo mojo, DependencyGraphBuilder graph, boolean transitive)
            throws IOException {
        File applicationDirectory = new File(mojo.getWisdomRootDirectory(), "application");
        File runtimeDirectory = new File(mojo.getWisdomRootDirectory(), "runtime");
        File coreDirectory = new File(mojo.getWisdomRootDirectory(), "core");

        // No transitive.
        Set<Artifact> artifacts = getArtifactsToConsider(mojo, graph, transitive);

        for (Artifact artifact : artifacts) {
            // We still have to do this test, as when using the direct dependencies we may include test and provided
            // dependencies.
            if ("compile".equalsIgnoreCase(artifact.getScope())) {
                File file = artifact.getFile();

                // Check it's a 'jar file'
                if (file == null || !file.getName().endsWith(".jar")) {
                    mojo.getLog().info("Dependency " + artifact + " not copied - it does not look like a jar " +
                            "file");
                    continue;
                }

                // Do we already have this file in core or runtime ?
                File test = new File(coreDirectory, file.getName());
                if (test.exists()) {
                    mojo.getLog().info("Dependency " + file.getName() + " not copied - already existing in `core`");
                    continue;
                }
                test = new File(runtimeDirectory, file.getName());
                if (test.exists()) {
                    mojo.getLog().info("Dependency " + file.getName() + " not copied - already existing in `runtime`");
                    continue;
                }
                // Check that it's a bundle.
                if (!isBundle(file)) {
                    mojo.getLog().info("Dependency " + file.getName() + " not copied to 'application' - it's not a " +
                            "bundle");
                    continue;
                }

                // All check done, let's copy !
                FileUtils.copyFileToDirectory(file, applicationDirectory);
            }
        }
    }

    /**
     * Extracts dependencies, that are webjars, to the 'assets/libs' directory.
     * Only the 'webjar' part of the jar file is unpacked.
     *
     * @param mojo       the mojo
     * @param graph      the dependency graph builder
     * @param transitive whether or not we include the transitive dependencies.
     * @throws IOException when a jar cannot be copied
     */
    public static void extractWebJars(AbstractWisdomMojo mojo, DependencyGraphBuilder graph,
                                      boolean transitive) throws IOException {
        File webjars = new File(mojo.getWisdomRootDirectory(), "assets/libs");
        Set<Artifact> artifacts = getArtifactsToConsider(mojo, graph, transitive);


        for (Artifact artifact : artifacts) {
            if ("compile".equalsIgnoreCase(artifact.getScope())) {
                File file = artifact.getFile();

                // Check it's a 'jar file'
                if (!file.getName().endsWith(".jar")) {
                    mojo.getLog().debug("Dependency " + file.getName() + " not copied - it does not look like a jar " +
                            "file");
                    continue;
                }

                // Check that it's a bundle.
                if (!isWebJar(file)) {
                    mojo.getLog().debug("Dependency " + file.getName() + " not copied to 'libs' - it's not a webjar");
                    continue;
                }

                // All check done, unpack.
                extract(mojo, file, webjars);
            }
        }
    }

    /**
     * Gets the list of artifact to consider during the analysis.
     * @param mojo the mojo
     * @param graph the dependency graph builder
     * @param transitive do we have to include transitive dependencies
     * @return the set of artifacts
     */
    private static Set<Artifact> getArtifactsToConsider(AbstractWisdomMojo mojo, DependencyGraphBuilder graph, boolean transitive) {
        // No transitive.
        Set<Artifact> artifacts;
        if (!transitive) {
            // Direct dependencies that the current project has (no transitive)
            artifacts = mojo.project.getDependencyArtifacts();
        } else {
            // All dependencies that the current project has, including transitive ones. Contents are lazily
            // populated, so depending on what phases have run dependencies in some scopes won't be
            // included.
            artifacts = getTransitiveDependencies(mojo, graph);
        }
        return artifacts;
    }

    /**
     * Collects the transitive dependencies of the current projects.
     * @param mojo the mojo
     * @param graph the dependency graph builder
     * @return the set of resolved transitive dependencies.
     */
    private static Set<Artifact> getTransitiveDependencies(AbstractWisdomMojo mojo, DependencyGraphBuilder graph) {
        Set<Artifact> artifacts;
        artifacts = new LinkedHashSet<>();
        try {
            Set<Artifact> transitives = new LinkedHashSet<>();
            DependencyNode node = graph.buildDependencyGraph(mojo.project, null);
            node.accept(new ArtifactVisitor(mojo, transitives));
            mojo.getLog().debug(transitives.size() + " transitive dependencies have been collected : " +
                    transitives);

            // Unfortunately, the retrieve artifacts are not resolved, we need to find their 'surrogates' in the
            // resolved list.
            Set<Artifact> resolved = mojo.project.getArtifacts();
            for (Artifact a : transitives) {
                Artifact r = getArtifact(a, resolved);
                if (r == null) {
                    mojo.getLog().warn("Cannot find resolved artifact for " + a);
                } else {
                    artifacts.add(r);
                }
            }
        } catch (DependencyGraphBuilderException e) {
            mojo.getLog().error("Cannot traverse the project's dependencies to collect transitive dependencies, " +
                    "ignoring transitive");
            artifacts = mojo.project.getDependencyArtifacts();
        }
        return artifacts;
    }

    /**
     * A regex extracting the library name and version from Zip Entry names.
     */
    public static final Pattern WEBJAR_REGEX = Pattern.compile(".*META-INF/resources/webjars/([^/]+)/([^/]+)/.*");

    public static final String WEBJAR_LOCATION = "META-INF/resources/webjars/";

    private static void extract(final AbstractWisdomMojo mojo, File in, File out) throws IOException {
        ZipFile file = new ZipFile(in);
        try {
            Enumeration<? extends ZipEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().startsWith(WEBJAR_LOCATION) && !entry.isDirectory()) {
                    // Compute destination.
                    File output = new File(out,
                            entry.getName().substring(WEBJAR_LOCATION.length()));
                    InputStream stream = null;
                    try {
                        stream = file.getInputStream(entry);
                        output.getParentFile().mkdirs();
                        FileUtils.copyInputStreamToFile(stream, output);
                    } catch (IOException e) {
                        mojo.getLog().error("Cannot unpack " + entry.getName() + " from " + file.getName(), e);
                        throw e;
                    } finally {
                        IOUtils.closeQuietly(stream);
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(file);
        }
    }

    /**
     * Checks whether the given file is a WebJar or not (http://www.webjars.org/documentation).
     * The check is based on the presence of {@literal META-INF/resources/webjars/} directory in the jar file.
     *
     * @param file the file.
     * @return {@literal true} if it's a bundle, {@literal false} otherwise.
     */
    public static boolean isWebJar(File file) {
        Set<String> found = new LinkedHashSet<>();
        if (file.isFile() && file.getName().endsWith(".jar")) {
            JarFile jar = null;
            try {
                jar = new JarFile(file);

                // Fast return if the base structure is not there
                if (jar.getEntry(WEBJAR_LOCATION) == null) {
                    return false;
                }

                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    Matcher matcher = WEBJAR_REGEX.matcher(entry.getName());
                    if (matcher.matches()) {
                        found.add(matcher.group(1) + "-" + matcher.group(2));
                    }
                }
            } catch (IOException e) {
                LoggerFactory.getLogger(DependencyCopy.class).error("Cannot check if the file {} is a webjar, " +
                        "cannot open it", file.getName(), e);
                return false;
            } finally {
                final JarFile finalJar = jar;
                IOUtils.closeQuietly(new Closeable() {
                    @Override
                    public void close() throws IOException {
                        if (finalJar != null) {
                            finalJar.close();
                        }
                    }
                });
            }

            for (String lib : found) {
                LoggerFactory.getLogger(DependencyCopy.class).info("Web Library found in {} : {}",
                        file.getName(), lib);
            }

            return !found.isEmpty();
        }

        return false;
    }

    private static class ArtifactVisitor implements DependencyNodeVisitor {
        private final AbstractWisdomMojo mojo;
        private final Set<Artifact> artifacts;

        public ArtifactVisitor(AbstractWisdomMojo mojo, Set<Artifact> artifacts) {
            this.mojo = mojo;
            this.artifacts = artifacts;
        }

        @Override
        public boolean visit(DependencyNode dependencyNode) {
            Artifact artifact = dependencyNode.getArtifact();
            if (artifact == null) {
                return false;
            }
            if (artifact.getScope() == null) {
                // no scope means the current artifact (root).
                // we have to return true to traverse the dependencies.
                return true;
            }

            if ("compile".equals(artifact.getScope())) {
                mojo.getLog().debug("Adding " + artifact.toString() + " to the transitive list");
                artifacts.add(artifact);
                return true;
            }

            return false;
        }

        @Override
        public boolean endVisit(DependencyNode dependencyNode) {
            return true;
        }
    }

    private static Artifact getArtifact(Artifact artifact, Set<Artifact> list) {
        for (Artifact candidate : list) {
            if (artifact.getArtifactId().equalsIgnoreCase(candidate.getArtifactId())
                    && artifact.getGroupId().equalsIgnoreCase(candidate.getGroupId())
                    && artifact.getVersion().equalsIgnoreCase(candidate.getVersion())) {
                return candidate;
            }
        }
        return null;
    }
}
