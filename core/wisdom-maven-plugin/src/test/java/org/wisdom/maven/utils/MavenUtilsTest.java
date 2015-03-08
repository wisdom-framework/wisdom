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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.apache.maven.model.*;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.ProjectArtifact;
import org.junit.Test;
import org.wisdom.maven.Constants;
import org.wisdom.maven.osgi.Classpath;
import org.wisdom.maven.osgi.ProjectDependencies;

import java.io.File;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

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

        Classpath.store(project);

        File deps = new File(project.getBasedir(), Constants.DEPENDENCIES_FILE);
        assertThat(deps).isFile();
        ObjectMapper mapper = new ObjectMapper();
        ProjectDependencies dependencies = mapper.readValue(deps, ProjectDependencies.class);
        System.out.println(dependencies.getDirectDependencies());

        assertThat(Classpath.load(project.getBasedir()).getDirectDependencies()).isEmpty();
        assertThat(Classpath.load(project.getBasedir()).getTransitiveDependencies()).isEmpty();
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
        assertThat(matcher.group(3)).isEqualTo("true");

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
        assertThat(matcher.group(3)).isEqualTo("true");

        matcher = pattern.matcher(resources[1]);
        assertThat(matcher.matches());
        assertThat(matcher.groupCount()).isEqualTo(3);
        assertThat(matcher.group(1)).isEqualTo("/foo2");
        assertThat(matcher.group(2)).isEqualTo("/bar2");
        assertThat(matcher.group(3)).isEqualTo("true");
    }

}
