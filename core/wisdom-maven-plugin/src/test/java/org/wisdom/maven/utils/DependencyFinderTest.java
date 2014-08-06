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
import com.google.common.collect.ImmutableSet;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.wisdom.maven.mojos.AbstractWisdomMojo;

import java.io.File;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DependencyFinderTest {

    @Test
    public void testGetArtifactFileFromPluginDependenciesWithEmptySet() throws Exception {
        MyMojo mojo = new MyMojo();
        mojo.pluginDependencies = Collections.emptyList();

        assertThat(DependencyFinder.getArtifactFileFromPluginDependencies(mojo, "any", "jar"))
                .isNull();
    }

    @Test
    public void testGetArtifactFileFromPluginDependencies() throws Exception {
        MyMojo mojo = new MyMojo();

        Artifact artifact1 = mock(Artifact.class);
        when(artifact1.getArtifactId()).thenReturn("artifact1");
        when(artifact1.getType()).thenReturn("jar");

        File kitten = new File("kitten");
        File puppies = new File("puppies");

        Artifact artifact2 = mock(Artifact.class);
        when(artifact2.getArtifactId()).thenReturn("artifact2");
        when(artifact2.getType()).thenReturn("kitten");
        when(artifact2.getFile()).thenReturn(kitten);

        Artifact artifact2a = mock(Artifact.class);
        when(artifact2a.getArtifactId()).thenReturn("artifact2");
        when(artifact2a.getType()).thenReturn("puppies");
        when(artifact2a.getFile()).thenReturn(puppies);

        Artifact artifact3 = mock(Artifact.class);
        when(artifact3.getArtifactId()).thenReturn("artifact3");
        when(artifact3.getType()).thenReturn("putty");


        mojo.pluginDependencies = ImmutableList.of(
                artifact1,
                artifact2,
                artifact2a,
                artifact3
        );

        assertThat(DependencyFinder.getArtifactFileFromPluginDependencies(mojo, "artifact2",
                "kitten")).isNotNull().isEqualTo(kitten);
        assertThat(DependencyFinder.getArtifactFileFromPluginDependencies(mojo, "artifact2",
                "puppies")).isNotNull().isEqualTo(puppies);

        assertThat(DependencyFinder.getArtifactFileFromPluginDependencies(mojo, "artifact4",
                "puppies")).isNull();
        assertThat(DependencyFinder.getArtifactFileFromPluginDependencies(mojo, "artifact2",
                "bird")).isNull();


    }

    @Test
    public void testGetArtifactFileFromProjectDependenciesWithEmptySet() throws Exception {
        MyMojo mojo = new MyMojo();
        mojo.project = mock(MavenProject.class);
        when(mojo.project.getArtifacts()).thenReturn(Collections.<Artifact>emptySet());

        assertThat(DependencyFinder.getArtifactFileFromProjectDependencies(mojo, "any", "jar"))
                .isNull();
    }

    @Test
    public void testGetArtrifactFileFromProjectDependencies() throws Exception {
        MyMojo mojo = new MyMojo();
        mojo.project = mock(MavenProject.class);

        Artifact artifact1 = mock(Artifact.class);
        when(artifact1.getArtifactId()).thenReturn("artifact1");
        when(artifact1.getType()).thenReturn("jar");

        File kitten = new File("kitten");
        File puppies = new File("puppies");

        Artifact artifact2 = mock(Artifact.class);
        when(artifact2.getArtifactId()).thenReturn("artifact2");
        when(artifact2.getType()).thenReturn("kitten");
        when(artifact2.getFile()).thenReturn(kitten);

        Artifact artifact2a = mock(Artifact.class);
        when(artifact2a.getArtifactId()).thenReturn("artifact2");
        when(artifact2a.getType()).thenReturn("puppies");
        when(artifact2a.getFile()).thenReturn(puppies);

        Artifact artifact3 = mock(Artifact.class);
        when(artifact3.getArtifactId()).thenReturn("artifact3");
        when(artifact3.getType()).thenReturn("putty");
        when(mojo.project.getArtifacts()).thenReturn(ImmutableSet.of(
                artifact1,
                artifact2,
                artifact2a,
                artifact3
        ));

        assertThat(DependencyFinder.getArtifactFileFromProjectDependencies(mojo, "artifact2",
                "kitten")).isNotNull().isEqualTo(kitten);
        assertThat(DependencyFinder.getArtifactFileFromProjectDependencies(mojo, "artifact2",
                "puppies")).isNotNull().isEqualTo(puppies);

        assertThat(DependencyFinder.getArtifactFileFromProjectDependencies(mojo, "artifact4",
                "puppies")).isNull();
        assertThat(DependencyFinder.getArtifactFileFromProjectDependencies(mojo, "artifact2",
                "bird")).isNull();
    }

    @Test
    public void testGetArtifactFile() throws Exception {
        MyMojo mojo = new MyMojo();

        File kitten = new File("kitten");
        File puppies = new File("puppies");

        mojo.project = mock(MavenProject.class);
        Artifact artifact1 = mock(Artifact.class);
        when(artifact1.getArtifactId()).thenReturn("artifact1");
        when(artifact1.getType()).thenReturn("kitten");
        when(artifact1.getFile()).thenReturn(kitten);


        Artifact artifact2 = mock(Artifact.class);
        when(artifact2.getArtifactId()).thenReturn("artifact2");
        when(artifact2.getType()).thenReturn("puppies");
        when(artifact2.getFile()).thenReturn(puppies);

        when(mojo.project.getArtifacts()).thenReturn(ImmutableSet.of(
                artifact1

        ));

        mojo.pluginDependencies = ImmutableList.of(
                artifact2);

        assertThat(DependencyFinder.getArtifactFile(mojo, "artifact1",
                "kitten")).isNotNull().isEqualTo(kitten);
        assertThat(DependencyFinder.getArtifactFile(mojo, "artifact2",
                "puppies")).isNotNull().isEqualTo(puppies);

        assertThat(DependencyFinder.getArtifactFile(mojo, "artifact3",
                "puppies")).isNull();


    }

    @Test
    public void testGetArtifactFileWithDiffType() throws Exception {
        MyMojo mojo = new MyMojo();

        File kitten = new File("kitten");
        File puppies = new File("puppies");

        mojo.project = mock(MavenProject.class);
        Artifact artifact1 = mock(Artifact.class);
        when(artifact1.getArtifactId()).thenReturn("artifact1");
        when(artifact1.getType()).thenReturn("kitten");
        when(artifact1.getFile()).thenReturn(kitten);


        Artifact artifact2 = mock(Artifact.class);
        when(artifact2.getArtifactId()).thenReturn("artifact1");
        when(artifact2.getType()).thenReturn("puppies");
        when(artifact2.getFile()).thenReturn(puppies);

        when(mojo.project.getArtifacts()).thenReturn(ImmutableSet.of(
                artifact1

        ));

        mojo.pluginDependencies = ImmutableList.of(
                artifact2);

        assertThat(DependencyFinder.getArtifactFile(mojo, "artifact1",
                "kitten")).isNotNull().isEqualTo(kitten);
        assertThat(DependencyFinder.getArtifactFile(mojo, "artifact1",
                "puppies")).isNotNull().isEqualTo(puppies);

        assertThat(DependencyFinder.getArtifactFile(mojo, "artifact1",
                "jar")).isNull();


    }

    private class MyMojo extends AbstractWisdomMojo {

        @Override
        public void execute() throws MojoExecutionException, MojoFailureException {
            // Do nothing.
        }
    }
}