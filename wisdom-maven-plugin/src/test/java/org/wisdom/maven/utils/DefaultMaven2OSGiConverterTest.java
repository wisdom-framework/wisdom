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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test the Maven - OSGi conversions
 */
public class DefaultMaven2OSGiConverterTest {

    @Test
    public void testBundleSymbolicNameConversion() {
        //if artifactId is equal to last section of groupId then groupId is returned. eg.
        // org.apache.maven:maven -> org.apache.maven
        Artifact artifact = new DefaultArtifact("org.acme", "acme", "1.0", null, "jar", "", null);
        assertThat(DefaultMaven2OsgiConverter.getBundleSymbolicName(artifact)).isEqualTo("org.acme");
        //if artifactId starts with last section of groupId that portion is removed. eg.
        // org.apache.maven:maven-core -> org.apache.maven.core
        artifact = new DefaultArtifact("org.acme", "acme-api", "1.0", null, "jar", "", null);
        assertThat(DefaultMaven2OsgiConverter.getBundleSymbolicName(artifact)).isEqualTo("org.acme.api");
        // if artifactId starts with groupId then the artifactId is removed. eg.
        // org.apache:org.apache.maven.core -> org.apache.maven.core
        artifact = new DefaultArtifact("org.acme", "org.acme.api", "1.0", null, "jar", "", null);
        assertThat(DefaultMaven2OsgiConverter.getBundleSymbolicName(artifact)).isEqualTo("org.acme.api");
        // Default
        artifact = new DefaultArtifact("org.acme", "wisdom-api", "1.0", null, "jar", "", null);
        assertThat(DefaultMaven2OsgiConverter.getBundleSymbolicName(artifact)).isEqualTo("org.acme.wisdom.api");
    }

    @Test
    public void testVersionConversion() {
        // Regular versions
        assertThat(DefaultMaven2OsgiConverter.getVersion("1.0")).isEqualTo("1.0.0");
        assertThat(DefaultMaven2OsgiConverter.getVersion("1.0.0")).isEqualTo("1.0.0");
        assertThat(DefaultMaven2OsgiConverter.getVersion("1")).isEqualTo("1.0.0");

        // Snapshots
        assertThat(DefaultMaven2OsgiConverter.getVersion("1.0-SNAPSHOT")).isEqualTo("1.0.0.SNAPSHOT");
        assertThat(DefaultMaven2OsgiConverter.getVersion("1.0-1.0-20131224.083751-42"))
                .isEqualTo("1.0.0.1_0-20131224_083751-42");

        // Non-numeric versions
        assertThat(DefaultMaven2OsgiConverter.getVersion("alpha")).isEqualTo("0.0.0.alpha");

        // Version with qualifier
        assertThat(DefaultMaven2OsgiConverter.getVersion("1.0-alpha")).isEqualTo("1.0.0.alpha");
        assertThat(DefaultMaven2OsgiConverter.getVersion("1.0.0-RC1")).isEqualTo("1.0.0.RC1");
        assertThat(DefaultMaven2OsgiConverter.getVersion("1.0-RC-1")).isEqualTo("1.0.0.RC-1");
    }
}
