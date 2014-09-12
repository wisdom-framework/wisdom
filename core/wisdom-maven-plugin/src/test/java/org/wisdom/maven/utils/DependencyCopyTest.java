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

import com.google.common.collect.ImmutableList;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.internal.DefaultDependencyNode;
import org.junit.Before;
import org.junit.Test;
import org.wisdom.maven.mojos.AbstractWisdomMojo;
import org.wisdom.maven.mojos.Libraries;

import java.io.File;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ow2.chameleon.core.utils.BundleHelper.isBundle;

/**
 * check the dependency copy.
 */
public class DependencyCopyTest {

    @Test
    public void testThatANonExistingFileCannotBeAWebJar() {
        File file = new File("does_not_exist");
        assertThat(WebJars.isWebJar(file)).isFalse();
    }

    @Test
    public void testThatAWebJarIsAWebJar() {
        File file = new File("target/test-classes/webjars/bootstrap-3.1.1.jar");
        assertThat(file).exists();
        assertThat(WebJars.isWebJar(file)).isTrue();
    }

    @Test
    public void testThatABundleIsNotAWebJar() {
        File file = new File("target/test-classes/webjars/org.apache.felix.log-1.0.1.jar");
        assertThat(file).exists();
        assertThat(WebJars.isWebJar(file)).isFalse();
    }

    @Test
    public void testThatATextFileIsNotAWebJar() {
        File file = new File("target/test-classes/webjars/not-a-webjar.txt");
        assertThat(file).exists();
        assertThat(WebJars.isWebJar(file)).isFalse();
    }

    @Test
    public void testThatABundleIsABundle() {
        File file = new File("target/test-classes/webjars/org.apache.felix.log-1.0.1.jar");
        assertThat(file).exists();
        assertThat(isBundle(file)).isTrue();
    }


    @Test
    public void testCopyLibs() throws Exception {
        AbstractWisdomMojo mojo = new AbstractWisdomMojo() {
            @Override
            public void execute() throws MojoExecutionException, MojoFailureException {
                // Do nothing;
            }
        };
        mojo.basedir = new File("target/junk/project1");
        mojo.basedir.mkdirs();
        mojo.project = new MavenProject();
        mojo.project.setArtifacts(resolved);
        mojo.buildDirectory = new File(mojo.basedir, "target");

        // Create the artifacts
        final Artifact asmArtifact = artifact("asm");
        final Artifact parboiledCoreArtifact = artifact("parboiledCore");
        final Artifact parboiledArtifact = artifact("parboiled");
        final Artifact pegdownArtifact = artifact("pegdown");
        final Artifact projectArtifact = artifact("project");

        // Define the trails
        setTrail(projectArtifact, projectArtifact);
        setTrail(pegdownArtifact, projectArtifact, pegdownArtifact);
        setTrail(parboiledArtifact, projectArtifact, pegdownArtifact, parboiledArtifact);
        setTrail(parboiledCoreArtifact, projectArtifact, pegdownArtifact, parboiledArtifact, parboiledCoreArtifact);
        setTrail(asmArtifact, projectArtifact, pegdownArtifact, asmArtifact);

        DefaultDependencyNode root = new DefaultDependencyNode(null, projectArtifact, null, null, null);
        DefaultDependencyNode pegdown = new DefaultDependencyNode(root, pegdownArtifact, null, null, null);
        DefaultDependencyNode parboiled = new DefaultDependencyNode(pegdown, parboiledArtifact, null, null, null);
        DefaultDependencyNode parboiledCore = new DefaultDependencyNode(parboiled, parboiledCoreArtifact, null,
                null, null);
        DefaultDependencyNode asm = new DefaultDependencyNode(parboiled, asmArtifact, null, null, null);

        root.setChildren(ImmutableList.<DependencyNode>of(pegdown));
        pegdown.setChildren(ImmutableList.<DependencyNode>of(parboiled));
        parboiled.setChildren(ImmutableList.<DependencyNode>of(parboiledCore, asm));
        parboiledCore.setChildren(Collections.<DependencyNode>emptyList());
        asm.setChildren(Collections.<DependencyNode>emptyList());

        DependencyGraphBuilder builder = mock(DependencyGraphBuilder.class);
        when(builder.buildDependencyGraph(mojo.project, null)).thenReturn(root);

        // First execution, with transitive enabled.
        Libraries libraries = new Libraries();
        libraries.setResolveTransitive(true);
        libraries.setIncludes(ImmutableList.of(":pegdown"));
        Set<Artifact> copied = DependencyCopy.copyLibs(mojo, builder, libraries);

        // In that case, everything is copied, so size == 4
        assertThat(copied).hasSize(4);

        // First execution, without transitives.
        libraries.setResolveTransitive(false);
        libraries.setIncludes(ImmutableList.of(":pegdown"));
        copied = DependencyCopy.copyLibs(mojo, builder, libraries);

        // In that case, only pegdown is copied, so size == 1
        assertThat(copied).hasSize(1);

        // Re-enabled the transitive and exclude asm
        libraries.setResolveTransitive(true);
        libraries.setIncludes(ImmutableList.of(":pegdown"));
        libraries.setExcludes(ImmutableList.of(":asm"));
        copied = DependencyCopy.copyLibs(mojo, builder, libraries);

        // In that case, asm is not copied, size = 3
        assertThat(copied).hasSize(3);

        // Ensure that excluded dependencies are not copied, for this we modify the graph adding a dependency on org
        // .apache.felix.ipojo.annotations.
        Artifact annotation = artifact("org.apache.felix.ipojo.annotations");
        when(annotation.getGroupId()).thenReturn("org.apache.felix");
        setTrail(annotation, projectArtifact, pegdownArtifact);
        DefaultDependencyNode ann = new DefaultDependencyNode(parboiled, annotation, null, null, null);
        ann.setChildren(Collections.<DependencyNode>emptyList());
        pegdown.setChildren(ImmutableList.<DependencyNode>of(parboiled, ann));

        // Re-enabled the transitive and exclude asm
        libraries.setResolveTransitive(true);
        libraries.setIncludes(ImmutableList.of(":pegdown"));
        libraries.setExcludes(ImmutableList.of(":asm"));
        copied = DependencyCopy.copyLibs(mojo, builder, libraries);

        // In that case, asm is not copied, annotation either, size = 3
        assertThat(copied).hasSize(3);

    }

    private static Set<Artifact> resolved = new HashSet<>();

    public static Artifact artifact(String a) {

        Artifact artifact = mock(Artifact.class);
        when(artifact.getArtifactId()).thenReturn(a);
        when(artifact.getGroupId()).thenReturn("org.acme");
        when(artifact.getVersion()).thenReturn("1.0-SNAPSHOT");
        when(artifact.getScope()).thenReturn("compile");
        when(artifact.getId()).thenReturn("org.acme:" + a + ":jar:1.0-SNAPSHOT");
        when(artifact.toString()).thenReturn("{artifact: " + a + "}");
        when(artifact.getDependencyConflictId()).thenReturn("org.acme:" + a + ":jar");

        resolved.add(artifact);
        return artifact;
    }

    public static Artifact setTrail(Artifact artifact, Artifact... artifacts) {
        List<String> trail = new ArrayList<>();
        if (artifacts != null) {
            for (Artifact parent : artifacts) {
                trail.add(parent.getId());
            }
        }
        trail.add(artifact.getId());

        when(artifact.getDependencyTrail()).thenReturn(trail);
        return artifact;
    }

    @Before
    public void cleanup() {
        resolved.clear();
    }
}
