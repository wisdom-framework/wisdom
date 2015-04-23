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
package org.wisdom.maven.mojos;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.shared.model.fileset.FileSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WebJarPackagerTest {

    File fake = new File("src/test/resources/fake-project");
    File target = new File("target/junk");
    File classes = new File(target, "classes");

    @Before
    @After
    public void cleanup() {
        FileUtils.deleteQuietly(target);
    }

    private void copy() throws IOException {
        FileUtils.copyDirectory(new File(fake, "src/main/resources/assets"), new File(classes, "assets"));
    }

    @Test
    public void testWithNoConfiguration() throws MojoExecutionException {
        WebJarPackager packager = new WebJarPackager();
        assertThat(packager.enabled()).isFalse();
        packager.execute();
    }

    @Test
    public void testDefaultConfiguration() throws MojoExecutionException {
        WebJarPackager packager = new WebJarPackager();
        packager.project = mock(MavenProject.class);
        packager.projectHelper = mock(MavenProjectHelper.class);
        when(packager.project.getArtifactId()).thenReturn("test");
        when(packager.project.getVersion()).thenReturn("1.0");
        when(packager.project.getBasedir()).thenReturn(new File(""));
        packager.buildDirectory = new File("target");
        packager.packageWebJar = true;
        packager.deployWebJarToWisdom = true;
        packager.execute();
        // No file, so no creation
        assertThat(new File(packager.buildDirectory, "test-1.0-webjar.jar")).doesNotExist();
    }

    @Test
    public void testDefaultPackaging() throws MojoExecutionException, IOException {
        WebJarPackager packager = new WebJarPackager();
        packager.project = mock(MavenProject.class);
        packager.projectHelper = mock(MavenProjectHelper.class);
        when(packager.project.getArtifactId()).thenReturn("test");
        when(packager.project.getVersion()).thenReturn("1.1");
        when(packager.project.getBasedir()).thenReturn(fake);
        packager.buildDirectory = new File("target/junk");
        copy();
        packager.packageWebJar = true;
        packager.deployWebJarToWisdom = true;
        packager.execute();
        final File wj = new File(packager.buildDirectory, "test-1.1-webjar.jar");
        assertThat(wj).isFile();
        JarFile jar = new JarFile(wj);
        assertThat(jar.getEntry(WebJarPackager.ROOT + "test/1.1/less/style.less")).isNotNull();
        assertThat(jar.getEntry(WebJarPackager.ROOT + "test/1.1/missing")).isNull();
        assertThat(jar.getEntry(WebJarPackager.ROOT + "test/1.1/coffee/script.coffee")).isNotNull();
        Attributes attributes = jar.getManifest().getMainAttributes();
        assertThat(attributes.getValue("Webjar-Name")).isEqualTo("test");
        assertThat(attributes.getValue("Webjar-Version")).isEqualTo("1.1");
    }

    @Test
    public void testNameVersionAndClassifierCustomization() throws MojoExecutionException, IOException {
        WebJarPackager packager = new WebJarPackager();
        packager.project = mock(MavenProject.class);
        packager.projectHelper = mock(MavenProjectHelper.class);
        when(packager.project.getArtifactId()).thenReturn("test");
        when(packager.project.getVersion()).thenReturn("1.0");
        when(packager.project.getBasedir()).thenReturn(fake);
        packager.buildDirectory = new File("target/junk");
        copy();
        packager.webjar = new WebJar();
        packager.webjar.setName("library");
        packager.webjar.setVersion("2.0");
        packager.webjar.setClassifier("wb");
        packager.execute();
        final File wj = new File(packager.buildDirectory, "library-2.0-wb.jar");
        assertThat(wj).isFile();
        JarFile jar = new JarFile(wj);
        assertThat(jar.getEntry(WebJarPackager.ROOT + "library/2.0/missing")).isNull();
        assertThat(jar.getEntry(WebJarPackager.ROOT + "library/2.0/coffee/script.coffee")).isNotNull();
        assertThat(jar.getEntry(WebJarPackager.ROOT + "library/2.0/less/style.less")).isNotNull();
        Attributes attributes = jar.getManifest().getMainAttributes();
        assertThat(attributes.getValue("Webjar-Name")).isEqualTo("library");
        assertThat(attributes.getValue("Webjar-Version")).isEqualTo("2.0");
    }

    @Test
    public void testIncludesCustomization() throws MojoExecutionException, IOException {
        WebJarPackager packager = new WebJarPackager();
        packager.project = mock(MavenProject.class);
        packager.projectHelper = mock(MavenProjectHelper.class);
        when(packager.project.getArtifactId()).thenReturn("test");
        when(packager.project.getVersion()).thenReturn("1.0");
        when(packager.project.getBasedir()).thenReturn(fake);
        packager.buildDirectory = new File("target/junk");
        copy();
        packager.webjar = new WebJar();
        FileSet set = new FileSet();
        set.setDirectory(new File(classes, "assets").getAbsolutePath());
        set.setIncludes(ImmutableList.of("**/coffee/*"));
        packager.webjar.setFileset(set);

        packager.execute();
        final File wj = new File(packager.buildDirectory, "test-1.0-webjar.jar");
        assertThat(wj).isFile();
        JarFile jar = new JarFile(wj);
        assertThat(jar.getEntry(WebJarPackager.ROOT + "test/1.0/missing")).isNull();
        assertThat(jar.getEntry(WebJarPackager.ROOT + "test/1.0/coffee/script.coffee")).isNotNull();
        assertThat(jar.getEntry(WebJarPackager.ROOT + "test/1.0/less/style.less")).isNull();
    }

    @Test
    public void testExcludesCustomization() throws MojoExecutionException, IOException {
        WebJarPackager packager = new WebJarPackager();
        packager.project = mock(MavenProject.class);
        packager.projectHelper = mock(MavenProjectHelper.class);
        when(packager.project.getArtifactId()).thenReturn("test");
        when(packager.project.getVersion()).thenReturn("1.0");
        when(packager.project.getBasedir()).thenReturn(fake);
        packager.buildDirectory = new File("target/junk");
        copy();
        packager.webjar = new WebJar();
        FileSet set = new FileSet();
        set.setDirectory(new File(classes, "assets").getAbsolutePath());
        set.setExcludes(ImmutableList.of("**/less/*"));
        packager.webjar.setFileset(set);

        packager.execute();
        final File wj = new File(packager.buildDirectory, "test-1.0-webjar.jar");
        assertThat(wj).isFile();
        JarFile jar = new JarFile(wj);
        assertThat(jar.getEntry(WebJarPackager.ROOT + "test/1.0/missing")).isNull();
        assertThat(jar.getEntry(WebJarPackager.ROOT + "test/1.0/coffee/script.coffee")).isNotNull();
        assertThat(jar.getEntry(WebJarPackager.ROOT + "test/1.0/less/style.less")).isNull();
    }

}