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

import aQute.bnd.osgi.Analyzer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.model.*;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.ProjectArtifact;
import org.junit.Test;
import org.wisdom.maven.Constants;

import java.io.File;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the Maven Utils.
 */
public class MavenUtilsTest {
    @Test
    public void testDumpEmptyDependencies() throws Exception {
        Model model = new Model();
        model.setPomFile(new File("target/test-classes/maven/test/minimal.xml"));
        MavenProject project = new MavenProject(model);
        project.setFile(new File("target/test-classes/maven/test/minimal.xml"));

        MavenUtils.dumpDependencies(project);

        File deps = new File(project.getBasedir(), Constants.DEPENDENCIES_FILE);
        assertThat(deps).isFile();
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode array = mapper.readValue(deps, ArrayNode.class);
        assertThat(array.size()).isEqualTo(0);
    }

    @Test
    public void testDumpDependencies() throws Exception {
        Model model = new Model();
        model.setPomFile(new File("target/test-classes/maven/test/minimal.xml"));
        MavenProject project = new MavenProject(model);
        project.setFile(new File("target/test-classes/maven/test/minimal.xml"));

        ArtifactHandler handler = mock(ArtifactHandler.class);
        final DefaultArtifact artifactA = new DefaultArtifact("org.acme", "module-A", "1.0.0", "compile", "jar", null, handler);
        artifactA.setFile(new File("/.m2/repository/org/acme/module-A/1.0.0/module-A-1.0.0.jar"));

        final DefaultArtifact artifactB = new DefaultArtifact("org.acme", "module-B", "1.0.0", "test", "jar", null, handler);
        artifactB.setFile(new File("/.m2/repository/org/acme/module-B/1.0.0/module-B-1.0.0.jar"));

        final DefaultArtifact artifactAZip = new DefaultArtifact("org.acme", "module-A", "1.0.0", "compile", "zip",
                null, handler);
        artifactAZip.setFile(new File("/.m2/repository/org/acme/module-A/1.0.0/module-A-1.0.0.zip"));

        final DefaultArtifact artifactC = new DefaultArtifact("org.acme", "module-C", "1.0.0", "provided", "jar", null, handler);
        artifactC.setFile(new File("/.m2/repository/org/acme/module-C/1.0.0/module-C-1.0.0.jar"));

        final DefaultArtifact artifactD = new DefaultArtifact("org.acme", "module-D", "1.0.0", "compile", "jar", "classifier", handler);
        artifactD.setFile(new File("/.m2/repository/org/acme/module-D/1.0.0/module-D-1.0.0-classifier.jar"));

        project.setArtifacts(ImmutableSet.<Artifact>of(
                // A jar
                artifactA,
                // A test jar
                artifactB,
                // A zip
                artifactAZip,
                // A provided jar
                artifactC,
                // A jar with classifier
                artifactD
        ));

        MavenUtils.dumpDependencies(project);

        File deps = new File(project.getBasedir(), Constants.DEPENDENCIES_FILE);
        assertThat(deps).isFile();
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode array = mapper.readValue(deps, ArrayNode.class);
        assertThat(array.size()).isEqualTo(5);
        JsonNode a = array.get(0);
        assertThat(a.get("groupId").asText()).isEqualTo("org.acme");
        assertThat(a.get("artifactId").asText()).isEqualTo("module-A");
        assertThat(a.get("version").asText()).isEqualTo("1.0.0");
        assertThat(a.get("scope").asText()).isEqualTo("compile");
        assertThat(a.get("classifier")).isNull();
        assertThat(a.get("file").asText()).isEqualTo(artifactA.getFile().getAbsolutePath());

        JsonNode b = array.get(1);
        assertThat(b.get("scope").asText()).isEqualTo("test");

        JsonNode c = array.get(3);
        assertThat(c.get("scope").asText()).isEqualTo("provided");

        JsonNode azip = array.get(2);
        assertThat(azip.get("groupId").asText()).isEqualTo("org.acme");
        assertThat(azip.get("artifactId").asText()).isEqualTo("module-A");
        assertThat(azip.get("version").asText()).isEqualTo("1.0.0");
        assertThat(azip.get("scope").asText()).isEqualTo("compile");
        assertThat(azip.get("classifier")).isNull();
        assertThat(azip.get("file").asText()).isEqualTo(artifactAZip.getFile().getAbsolutePath());

        JsonNode d = array.get(4);
        assertThat(d.get("classifier").asText()).isEqualTo("classifier");


    }

    @Test
    public void testGetDefaultPropertiesOnMinimalPom() throws Exception {
        Model model = new Model();
        model.setPomFile(new File("target/test-classes/maven/test/minimal.xml"));
        MavenProject project = new MavenProject(model);
        project.setFile(new File("target/test-classes/maven/test/minimal.xml"));
        project.setArtifactId("acme");
        project.setGroupId("corp.acme");
        project.setVersion("1.0.0-SNAPSHOT");
        final ProjectArtifact artifact = new ProjectArtifact(project);
        project.setArtifact(artifact);
        Build build = new Build();
        build.setDirectory(new File(project.getBasedir(), "target").getAbsolutePath());
        build.setOutputDirectory(new File(project.getBasedir(), "target/classes").getAbsolutePath());
        project.setBuild(build);

        Properties properties = MavenUtils.getDefaultProperties(project);
        assertThat(properties.getProperty("maven-symbolicname")).isEqualTo(DefaultMaven2OsgiConverter
                .getBundleSymbolicName(artifact));
        assertThat(properties.getProperty(org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME)).isEqualTo(DefaultMaven2OsgiConverter
                .getBundleSymbolicName(artifact));

        assertThat(properties.getProperty(org.osgi.framework.Constants.BUNDLE_VERSION)).isEqualTo(DefaultMaven2OsgiConverter
                .getVersion(project.getVersion()));

        assertThat(properties.getProperty(org.osgi.framework.Constants.BUNDLE_DESCRIPTION)).isNull();
        assertThat(properties.getProperty(Analyzer.BUNDLE_LICENSE)).isNull();
        assertThat(properties.getProperty(Analyzer.BUNDLE_VENDOR)).isNull();
        assertThat(properties.getProperty(Analyzer.BUNDLE_DOCURL)).isNull();

        assertThat(properties.getProperty(Analyzer.BUNDLE_LICENSE)).isNull();

        assertThat(properties.getProperty(Analyzer.BUNDLE_NAME)).isEqualTo(project.getArtifactId());
    }

    @Test
    public void testGetDefaultPropertiesOnProjectWithLicenses() throws Exception {
        Model model = new Model();
        model.setPomFile(new File("target/test-classes/maven/test/minimal.xml"));
        MavenProject project = new MavenProject(model);
        project.setFile(new File("target/test-classes/maven/test/minimal.xml"));
        project.setArtifactId("acme");
        project.setGroupId("corp.acme");
        project.setVersion("1.0.0-SNAPSHOT");
        final ProjectArtifact artifact = new ProjectArtifact(project);
        project.setArtifact(artifact);
        Build build = new Build();
        build.setDirectory(new File(project.getBasedir(), "target").getAbsolutePath());
        build.setOutputDirectory(new File(project.getBasedir(), "target/classes").getAbsolutePath());
        project.setBuild(build);

        License license = new License();
        license.setDistribution("repo");
        license.setName("Apache Software License 2.0");
        license.setUrl("http://www.apache.org/licenses/");
        project.setLicenses(ImmutableList.of(license));

        Organization organization = new Organization();
        organization.setName("Acme Corp.");
        organization.setUrl("http://acme.org");
        project.setOrganization(organization);

        project.setDescription("description");

        Properties properties = MavenUtils.getDefaultProperties(project);
        assertThat(properties.getProperty(Analyzer.BUNDLE_LICENSE)).contains(license.getUrl());
        assertThat(properties.getProperty(Analyzer.BUNDLE_VENDOR)).isEqualTo("Acme Corp.");
        assertThat(properties.getProperty(Analyzer.BUNDLE_DOCURL)).isEqualTo(organization.getUrl());
        assertThat(properties.getProperty(Analyzer.BUNDLE_DESCRIPTION)).isEqualTo("description");

        License license2 = new License();
        license2.setDistribution("repo");
        license2.setName("Apache Software License 2.0");
        license2.setUrl("http://www.apache.org/LICENSE.txt");

        project.setLicenses(ImmutableList.of(license, license2));

        properties = MavenUtils.getDefaultProperties(project);
        assertThat(properties.getProperty(Analyzer.BUNDLE_LICENSE)).contains(license.getUrl()).contains(license2.getUrl());
    }

    @Test
    public void testGetDefaultPropertiesOnProjectWithProperties() throws Exception {
        Model model = new Model();
        model.setPomFile(new File("target/test-classes/maven/test/minimal.xml"));
        MavenProject project = new MavenProject(model);
        project.setFile(new File("target/test-classes/maven/test/minimal.xml"));
        project.setArtifactId("acme");
        project.setGroupId("corp.acme");
        project.setVersion("1.0.0-SNAPSHOT");
        final ProjectArtifact artifact = new ProjectArtifact(project);
        project.setArtifact(artifact);
        Build build = new Build();
        build.setDirectory(new File(project.getBasedir(), "target").getAbsolutePath());
        build.setOutputDirectory(new File(project.getBasedir(), "target/classes").getAbsolutePath());
        project.setBuild(build);

        Properties props = new Properties();
        props.put("p", "v");
        model.setProperties(props);

        Properties properties = MavenUtils.getDefaultProperties(project);

        assertThat(properties.getProperty("p")).isEqualTo("v");
    }

    @Test
    public void toArrayOfPaths() {
        String result = MavenUtils.getArray(ImmutableList.of("/foo"));
        assertThat(result).isEqualTo("/foo");

        result = MavenUtils.getArray(ImmutableList.of("/foo", "bar/baz"));
        assertThat(result).isEqualTo("/foo,bar/baz");
    }

    @Test
    public void toStringOfResources() {
        // Just directory.
        Resource resource = new Resource();
        resource.setDirectory("/foo");

        String string = MavenUtils.toString(ImmutableList.of(resource));
        String[] resources = string.split(",");
        assertThat(resources).hasSize(1);

        Pattern pattern = Pattern.compile("(.*);(.*);(.*);");
        Matcher matcher = pattern.matcher(resources[0]);
        assertThat(matcher.matches());
        assertThat(matcher.groupCount()).isEqualTo(3);
        assertThat(matcher.group(1)).isEqualTo("/foo");
        assertThat(matcher.group(2)).isEmpty();
        assertThat(matcher.group(3)).isEmpty();

        Resource resource2 = new Resource();
        resource2.setDirectory("/foo2");
        resource2.setTargetPath("/bar2");
        resource2.setFiltering("true");

        string = MavenUtils.toString(ImmutableList.of(resource, resource2));
        resources = string.split(",");
        assertThat(resources).hasSize(2);

        matcher = pattern.matcher(resources[0]);
        assertThat(matcher.matches());
        assertThat(matcher.groupCount()).isEqualTo(3);
        assertThat(matcher.group(1)).isEqualTo("/foo");
        assertThat(matcher.group(2)).isEmpty();
        assertThat(matcher.group(3)).isEmpty();

        matcher = pattern.matcher(resources[1]);
        assertThat(matcher.matches());
        assertThat(matcher.groupCount()).isEqualTo(3);
        assertThat(matcher.group(1)).isEqualTo("/foo2");
        assertThat(matcher.group(2)).isEqualTo("/bar2");
        assertThat(matcher.group(3)).isEqualTo("true");
    }

}
