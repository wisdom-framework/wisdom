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
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.traversal.DependencyNodeVisitor;
import org.wisdom.maven.mojos.AbstractWisdomMojo;
import org.wisdom.maven.mojos.Libraries;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.ow2.chameleon.core.utils.BundleHelper.isBundle;

/**
 * Copy all compile dependencies from the project (excluding transitive) to the expected directories.
 */
public class DependencyCopy {

    public static final String SCOPE_PROVIDED = "provided";
    public static final String SCOPE_COMPILE = "compile";
    public static final String SCOPE_TEST = "test";

    /**
     * Copies dependencies, that are bundles, to the application directory.
     * If the bundle is already in core or runtime, the bundle is not copied.
     *
     * @param mojo                     the mojo
     * @param graph                    the dependency graph builder
     * @param transitive               whether or not we include the transitive dependencies.
     * @param deployTestDependencies   whether of not we need to deploy bundles declared a
     *                                 dependencies in the 'test' scope
     * @param disableDefaultExclusions whether or not to removed from well known artifacts from the copy.
     * @throws IOException when a bundle cannot be copied
     */
    public static void copyBundles(AbstractWisdomMojo mojo, DependencyGraphBuilder graph, boolean transitive,
                                   boolean deployTestDependencies, boolean disableDefaultExclusions, Libraries libraries)
            throws IOException {
        File applicationDirectory = new File(mojo.getWisdomRootDirectory(), "application");
        File runtimeDirectory = new File(mojo.getWisdomRootDirectory(), "runtime");
        File coreDirectory = new File(mojo.getWisdomRootDirectory(), "core");

        Set<Artifact> artifacts = getArtifactsToConsider(mojo, graph, transitive, null);

        for (Artifact artifact : artifacts) {
            // Is it an excluded dependency
            if (!disableDefaultExclusions && BundleExclusions.isExcluded(artifact)) {
                mojo.getLog().info("Dependency " + artifact + " not copied - the artifact is on the exclusion list");
                continue;
            }

            // We still have to do this test, as when using the direct dependencies we may include test and provided
            // dependencies.
            if (SCOPE_COMPILE.equalsIgnoreCase(artifact.getScope()) || deployTestDependencies && SCOPE_TEST
                    .equalsIgnoreCase(artifact.getScope())) {
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

                if (libraries != null && libraries.hasLibraries() && libraries.isExcludeFromApplication()) {
                    if (!libraries.getReverseFilter().include(artifact)) {
                        mojo.getLog().info("Dependency " + file.getName() + " not copied - excluded from the " +
                                "libraries settings");
                        continue;
                    }
                }

                // Check that it's a bundle.
                if (isBundle(file)) {
                    File destination = new File(applicationDirectory,
                            DefaultMaven2OsgiConverter.getBundleFileName(artifact));
                    mojo.getLog().info("Dependency " + file.getName() + " is a bundle, " +
                            "artifact copied to " + destination.getAbsolutePath());
                    FileUtils.copyFile(file, destination, true);
                }  else {
                    mojo.getLog().debug("Dependency " + file.getName() + " is not a bundle");
                }
            }
        }
    }

    /**
     * Copy direct (non-transitive) dependencies that are <strong>not bundles</strong> to the {@literal libs} directory
     * of the Wisdom server. As using such kind of dependencies does not really embrace the modular way promoted by
     * Wisdom, the copy is "explicit" meaning that the dependencies must be declared in the project that is going to
     * be run.
     * <p>
     * Only direct dependencies from the scope {@code compile} are copied.
     *
     * @param mojo  the mojo
     * @param graph the dependency graph builder
     * @return the list of artifact copied to the 'libs' directory. Empty is none.
     * @throws IOException when a file cannot be copied
     */
    public static Set<Artifact> copyLibs(AbstractWisdomMojo mojo, DependencyGraphBuilder graph, Libraries libraries)
            throws IOException {

        if (libraries == null || !libraries.hasLibraries()) {
            return Collections.emptySet();
        }

        File libsDirectory = new File(mojo.getWisdomRootDirectory(), "libs");
        ArtifactFilter filter = libraries.getFilter();

        Set<Artifact> artifacts = getArtifactsToConsider(mojo, graph, true, null);

        for (final Iterator<Artifact> it = artifacts.iterator(); it.hasNext(); ) {
            final Artifact artifact = it.next();

            if (!filter.include(artifact)) {
                it.remove();
                if (mojo.getLog().isDebugEnabled()) {
                    mojo.getLog().debug(artifact.getId() + " was removed by filters.");
                }
                continue;
            }

            if (BundleExclusions.isExcluded(artifact)) {
                it.remove();
                mojo.getLog().info("Dependency " + artifact + " not copied - the artifact is on the exclusion list");
                continue;
            }

            // We still have to do this test, as when using the direct dependencies we may include test and provided
            // dependencies.
            if (SCOPE_COMPILE.equalsIgnoreCase(artifact.getScope())) {
                File file = artifact.getFile();
                if (file != null && file.isFile()) {
                    mojo.getLog().warn("Copying " + file.getName() + " to the libs directory");
                    FileUtils.copyFileToDirectory(file, libsDirectory);
                } else {
                    mojo.getLog().warn("Cannot copy the file associated with " + artifact.getArtifactId() + " - the " +
                            "file is missing");
                }
            } else {
                it.remove();
            }
        }

        return artifacts;
    }

    /**
     * Gets the list of artifact to consider during the analysis.
     *
     * @param mojo       the mojo
     * @param graph      the dependency graph builder
     * @param transitive do we have to include transitive dependencies
     * @return the set of artifacts
     */
    public static Set<Artifact> getArtifactsToConsider(AbstractWisdomMojo mojo, DependencyGraphBuilder graph,
                                                       boolean transitive, ArtifactFilter filter) {
        // No transitive.
        Set<Artifact> artifacts;
        if (!transitive) {
            // Direct dependencies that the current project has (no transitives)
            artifacts = mojo.project.getDependencyArtifacts();
        } else {
            // All dependencies that the current project has, including transitive ones. Contents are lazily
            // populated, so depending on what phases have run dependencies in some scopes won't be
            // included.
            artifacts = getTransitiveDependencies(mojo, graph, filter);
        }
        return artifacts;
    }

    /**
     * Collects the transitive dependencies of the current projects.
     *
     * @param mojo  the mojo
     * @param graph the dependency graph builder
     * @return the set of resolved transitive dependencies.
     */
    private static Set<Artifact> getTransitiveDependencies(AbstractWisdomMojo mojo, DependencyGraphBuilder graph,
                                                           ArtifactFilter filter) {
        Set<Artifact> artifacts;
        artifacts = new LinkedHashSet<>();
        try {
            Set<Artifact> transitives = new LinkedHashSet<>();
            DependencyNode node = graph.buildDependencyGraph(mojo.project, filter);
            node.accept(new ArtifactVisitor(mojo, transitives));
            mojo.getLog().debug(transitives.size() + " transitive dependencies have been collected : " +
                    transitives);

            // Unfortunately, the retrieved artifacts are not resolved, we need to find their 'surrogates' in the
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
            mojo.getLog().debug("Here is the thrown exception having disabled the transitive dependency collection", e);
            artifacts = mojo.project.getDependencyArtifacts();
        }
        return artifacts;
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

            if (SCOPE_COMPILE.equals(artifact.getScope())) {
                mojo.getLog().debug("Adding " + artifact.toString() + " to the transitive list");
                artifacts.add(artifact);
            }

            if (SCOPE_PROVIDED.equals(artifact.getScope())) {
                mojo.getLog().debug("Adding " + artifact.toString() + " to the transitive list");
                artifacts.add(artifact);
                return false;
            }

            // The scope of the artifact we retrieved in context-aware. For instance,
            // if we have a dependency in the test scope, all its dependencies will be considered as test dependencies.
            // So we can visit the children, as the pruning is made in the if statement above. (this is related to
            // #263).
            return true;
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
