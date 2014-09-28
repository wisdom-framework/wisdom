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

import aQute.bnd.osgi.Constants;
import com.google.common.collect.ImmutableList;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.junit.Test;

import java.io.File;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class DependencyEmbedderTest {

    TestReporter reporter = new TestReporter();

    @Test
    public void testSimpleEmbedding() {
        Properties instructions = new Properties();
        instructions.setProperty(DependencyEmbedder.EMBED_DEPENDENCY, "*:acme-sample");
        DependencyEmbedder dependencyEmbedder = new DependencyEmbedder(instructions, reporter);

        final Artifact acme = create("acme", "acme-sample", "1");
        ProjectDependencies dependencies = new ProjectDependencies(
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme),
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme)
        );

        Properties properties = dependencyEmbedder.generate(instructions, dependencies);

        assertThat(properties.getProperty(Constants.INCLUDE_RESOURCE))
                .contains("@" + acme.getFile().getAbsolutePath());

        assertThat(properties.getProperty(Constants.BUNDLE_CLASSPATH)).isNull();
    }

    @Test
    public void testSimpleEmbeddingSpecifyingInline() {
        Properties instructions = new Properties();
        instructions.setProperty(DependencyEmbedder.EMBED_DEPENDENCY, "*:acme-sample;inline=true");
        DependencyEmbedder dependencyEmbedder = new DependencyEmbedder(instructions, reporter);

        final Artifact acme = create("acme", "acme-sample", "1");
        ProjectDependencies dependencies = new ProjectDependencies(
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme),
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme)
        );
        Properties properties = dependencyEmbedder.generate(instructions, dependencies);

        assertThat(properties.getProperty(Constants.INCLUDE_RESOURCE))
                .contains("@" + acme.getFile().getAbsolutePath());

        assertThat(properties.getProperty(Constants.BUNDLE_CLASSPATH)).isNull();
    }

    @Test
    public void testEmbeddingWithoutMatch() {
        Properties instructions = new Properties();
        instructions.setProperty(DependencyEmbedder.EMBED_DEPENDENCY, "*:acme-sample-missing");
        DependencyEmbedder dependencyEmbedder = new DependencyEmbedder(instructions, reporter);

        final Artifact acme = create("acme", "acme-sample", "1");
        ProjectDependencies dependencies = new ProjectDependencies(
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme),
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme)
        );
        Properties properties = dependencyEmbedder.generate(instructions, dependencies);

        assertThat(properties.getProperty(Constants.INCLUDE_RESOURCE)).isNull();
        assertThat(properties.getProperty(Constants.BUNDLE_CLASSPATH)).isNull();
    }

    @Test
    public void testSimpleEmbeddingNoInline() {
        Properties instructions = new Properties();
        instructions.setProperty(DependencyEmbedder.EMBED_DEPENDENCY, "*:acme-sample;inline=false");
        DependencyEmbedder dependencyEmbedder = new DependencyEmbedder(instructions, reporter);

        final Artifact acme = create("acme", "acme-sample", "1");
        ProjectDependencies dependencies = new ProjectDependencies(
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme),
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme)
        );
        Properties properties = dependencyEmbedder.generate(instructions, dependencies);

        assertThat(properties.getProperty(Constants.INCLUDE_RESOURCE))
                .contains(acme.getFile().getAbsolutePath())
                .doesNotContain("@");

        assertThat(properties.getProperty(Constants.BUNDLE_CLASSPATH))
                .contains(".,").contains(acme.getFile().getName());
    }

    @Test
    public void testSimpleEmbeddingInlinePath() {
        Properties instructions = new Properties();
        instructions.setProperty(DependencyEmbedder.EMBED_DEPENDENCY, "*:acme-sample;inline=images/**");
        DependencyEmbedder dependencyEmbedder = new DependencyEmbedder(instructions, reporter);

        final Artifact acme = create("acme", "acme-sample", "1");
        ProjectDependencies dependencies = new ProjectDependencies(
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme),
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme)
        );
        Properties properties = dependencyEmbedder.generate(instructions, dependencies);

        assertThat(properties.getProperty(Constants.INCLUDE_RESOURCE))
                .contains("@" + acme.getFile().getAbsolutePath() + "!/images/**");

        assertThat(properties.getProperty(Constants.BUNDLE_CLASSPATH)).isNull();
    }

    @Test
    public void testMultiEmbedding() {
        Properties instructions = new Properties();
        instructions.setProperty(DependencyEmbedder.EMBED_DEPENDENCY, "acme:*");
        DependencyEmbedder dependencyEmbedder = new DependencyEmbedder(instructions, reporter);

        final Artifact acme = create("acme", "acme-sample", "1");
        final Artifact acme2 = create("acme", "acme-sample-2", "1");
        ProjectDependencies dependencies = new ProjectDependencies(
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme, acme2),
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme, acme2)
        );
        Properties properties = dependencyEmbedder.generate(instructions, dependencies);

        assertThat(properties.getProperty(Constants.INCLUDE_RESOURCE))
                .contains("@" + acme.getFile().getAbsolutePath())
                .contains("@" + acme2.getFile().getAbsolutePath());

        assertThat(properties.getProperty(Constants.BUNDLE_CLASSPATH)).isNull();
    }

    @Test
    public void testMultiEmbeddingNoInline() {
        Properties instructions = new Properties();
        instructions.setProperty(DependencyEmbedder.EMBED_DEPENDENCY, "acme:*;inline=false");
        DependencyEmbedder dependencyEmbedder = new DependencyEmbedder(instructions, reporter);

        final Artifact acme = create("acme", "acme-sample", "1");
        final Artifact acme2 = create("acme", "acme-sample-2", "1");
        ProjectDependencies dependencies = new ProjectDependencies(
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme, acme2),
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme, acme2)
        );
        Properties properties = dependencyEmbedder.generate(instructions, dependencies);

        assertThat(properties.getProperty(Constants.INCLUDE_RESOURCE))
                .contains(acme.getFile().getAbsolutePath())
                .contains(acme2.getFile().getAbsolutePath())
                .doesNotContain("@");

        assertThat(properties.getProperty(Constants.BUNDLE_CLASSPATH))
                .contains(".,")
                .contains(acme.getFile().getName())
                .contains(acme2.getFile().getName());
    }


    private Artifact create(String g, String a, String v) {
        DefaultArtifact artifact = new MavenArtifact();
        artifact.setGroupId(g);
        artifact.setArtifactId(a);
        artifact.setVersion(v);
        artifact.setFile(new File("target/junk/" + a + "-" + v + ".jar"));
        return artifact;
    }

    @Test
    public void tesMultiEmbeddingInlinePath() {
        Properties instructions = new Properties();
        instructions.setProperty(DependencyEmbedder.EMBED_DEPENDENCY, "acme:*;inline=images/**");
        DependencyEmbedder dependencyEmbedder = new DependencyEmbedder(instructions, reporter);

        final Artifact acme = create("acme", "acme-sample", "1");
        final Artifact acme2 = create("acme", "acme-sample-2", "1");
        ProjectDependencies dependencies = new ProjectDependencies(
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme, acme2),
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme, acme2)
        );
        Properties properties = dependencyEmbedder.generate(instructions, dependencies);

        assertThat(properties.getProperty(Constants.INCLUDE_RESOURCE))
                .contains("@" + acme.getFile().getAbsolutePath() + "!/images/**")
                .contains("@" + acme2.getFile().getAbsolutePath() + "!/images/**");

        assertThat(properties.getProperty(Constants.BUNDLE_CLASSPATH)).isNull();
    }

    @Test
    public void testScopeFiltering() {
        Properties instructions = new Properties();
        instructions.setProperty(DependencyEmbedder.EMBED_DEPENDENCY, "acme:*;scope=provided");
        DependencyEmbedder dependencyEmbedder = new DependencyEmbedder(instructions, reporter);

        final Artifact acme = create("acme", "acme-sample", "1");
        acme.setScope("provided");
        final Artifact acme2 = create("acme", "acme-sample-2", "1");
        acme2.setScope("compile");
        ProjectDependencies dependencies = new ProjectDependencies(
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme, acme2),
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme, acme2)
        );
        Properties properties = dependencyEmbedder.generate(instructions, dependencies);

        assertThat(properties.getProperty(Constants.INCLUDE_RESOURCE))
                .contains("@" + acme.getFile().getAbsolutePath())
                .doesNotContain("@" + acme2.getFile().getAbsolutePath());

        assertThat(properties.getProperty(Constants.BUNDLE_CLASSPATH)).isNull();
    }


    @Test
    public void testGroupIdAndVersionFiltering() {
        Properties instructions = new Properties();
        instructions.setProperty(DependencyEmbedder.EMBED_DEPENDENCY, "acme:*:*:1");
        DependencyEmbedder dependencyEmbedder = new DependencyEmbedder(instructions, reporter);

        Artifact acme = create("acme", "acme-sample", "1");
        acme.setScope("provided");
        Artifact acme2 = create("acme", "acme-sample-2", "1");
        ProjectDependencies dependencies = new ProjectDependencies(
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme, acme2),
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme, acme2)
        );
        Properties properties = dependencyEmbedder.generate(instructions, dependencies);

        assertThat(properties.getProperty(Constants.INCLUDE_RESOURCE))
                .contains("@" + acme.getFile().getAbsolutePath())
                .contains("@" + acme2.getFile().getAbsolutePath());

        assertThat(properties.getProperty(Constants.BUNDLE_CLASSPATH)).isNull();

        instructions.setProperty(DependencyEmbedder.EMBED_DEPENDENCY, "acme:*:*:2");
        dependencyEmbedder = new DependencyEmbedder(instructions, reporter);

        acme = create("acme", "acme-sample", "2");
        acme.setScope("provided");
        acme2 = create("acme", "acme-sample-2", "1");
        dependencies = new ProjectDependencies(
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme, acme2),
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme, acme2)
        );
        properties = dependencyEmbedder.generate(instructions, dependencies);

        assertThat(properties.getProperty(Constants.INCLUDE_RESOURCE))
                .contains("@" + acme.getFile().getAbsolutePath())
                .doesNotContain("@" + acme2.getFile().getAbsolutePath());

        assertThat(properties.getProperty(Constants.BUNDLE_CLASSPATH)).isNull();
    }

    @Test
    public void testTransitiveUsingEmbedTransitive() {
        Properties instructions = new Properties();
        instructions.setProperty(DependencyEmbedder.EMBED_DEPENDENCY, "acme:acme-sample,deps:*");
        instructions.setProperty(DependencyEmbedder.EMBED_TRANSITIVE, "True");
        DependencyEmbedder dependencyEmbedder = new DependencyEmbedder(instructions, reporter);

        final Artifact deps = create("deps", "acme-deps", "1");
        final Artifact acme = create("acme", "acme-sample", "1");
        acme.setDependencyTrail(ImmutableList.of(deps.getId(), acme.getId()));
        final Artifact acme2 = create("acme", "acme-sample-2", "1");
        ProjectDependencies dependencies = new ProjectDependencies(
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme, acme2),
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme, acme2, deps)
        );
        Properties properties = dependencyEmbedder.generate(instructions, dependencies);

        assertThat(properties.getProperty(Constants.INCLUDE_RESOURCE))
                .contains("@" + acme.getFile().getAbsolutePath())
                .contains("@" + deps.getFile().getAbsolutePath())
                .doesNotContain("@" + acme2.getFile().getAbsolutePath());

        assertThat(properties.getProperty(Constants.BUNDLE_CLASSPATH)).isNull();
    }

    @Test
    public void testTransitiveWithoutEmbedTransitive() {
        Properties instructions = new Properties();
        instructions.setProperty(DependencyEmbedder.EMBED_DEPENDENCY, "acme:acme-sample,deps:*");
        instructions.setProperty(DependencyEmbedder.EMBED_TRANSITIVE, "false");
        DependencyEmbedder dependencyEmbedder = new DependencyEmbedder(instructions, reporter);

        final Artifact deps = create("deps", "acme-deps", "1");
        final Artifact acme = create("acme", "acme-sample", "1");
        acme.setDependencyTrail(ImmutableList.of(deps.getId(), acme.getId()));
        final Artifact acme2 = create("acme", "acme-sample-2", "1");
        ProjectDependencies dependencies = new ProjectDependencies(
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme, acme2),
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme, acme2, deps)
        );
        Properties properties = dependencyEmbedder.generate(instructions, dependencies);

        assertThat(properties.getProperty(Constants.INCLUDE_RESOURCE))
                .contains("@" + acme.getFile().getAbsolutePath())
                .doesNotContain("@" + deps.getFile().getAbsolutePath())
                .doesNotContain("@" + acme2.getFile().getAbsolutePath());

        assertThat(properties.getProperty(Constants.BUNDLE_CLASSPATH)).isNull();
    }

    @Test
    public void testTransitiveUsingTransitiveAttribute() {
        Properties instructions = new Properties();
        instructions.setProperty(DependencyEmbedder.EMBED_DEPENDENCY, "acme:acme-sample,deps:*;transitive=true");
        DependencyEmbedder dependencyEmbedder = new DependencyEmbedder(instructions, reporter);

        final Artifact deps = create("deps", "acme-deps", "1");
        final Artifact acme = create("acme", "acme-sample", "1");
        acme.setDependencyTrail(ImmutableList.of(deps.getId(), acme.getId()));
        final Artifact acme2 = create("acme", "acme-sample-2", "1");
        ProjectDependencies dependencies = new ProjectDependencies(
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme, acme2),
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme, acme2, deps)
        );
        Properties properties = dependencyEmbedder.generate(instructions, dependencies);

        assertThat(properties.getProperty(Constants.INCLUDE_RESOURCE))
                .contains("@" + acme.getFile().getAbsolutePath())
                .contains("@" + deps.getFile().getAbsolutePath())
                .doesNotContain("@" + acme2.getFile().getAbsolutePath());

        assertThat(properties.getProperty(Constants.BUNDLE_CLASSPATH)).isNull();
    }


    @Test
    public void testTransitiveUsingEmbedTransitiveAndNoInline() {
        Properties instructions = new Properties();
        instructions.setProperty(DependencyEmbedder.EMBED_DEPENDENCY, "acme:acme-sample;inline=false,"
                + "deps:*;inline=false");
        instructions.setProperty(DependencyEmbedder.EMBED_TRANSITIVE, "True");
        DependencyEmbedder dependencyEmbedder = new DependencyEmbedder(instructions, reporter);

        final Artifact deps = create("deps", "acme-deps", "1");
        final Artifact acme = create("acme", "acme-sample", "1");
        acme.setDependencyTrail(ImmutableList.of(deps.getId(), acme.getId()));
        final Artifact acme2 = create("acme", "acme-sample-2", "1");
        ProjectDependencies dependencies = new ProjectDependencies(
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme, acme2),
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme, acme2, deps)
        );
        Properties properties = dependencyEmbedder.generate(instructions, dependencies);

        assertThat(properties.getProperty(Constants.INCLUDE_RESOURCE))
                .contains(acme.getFile().getAbsolutePath())
                .contains(deps.getFile().getAbsolutePath())
                .doesNotContain("@")
                .doesNotContain(acme2.getFile().getName());


        assertThat(properties.getProperty(Constants.BUNDLE_CLASSPATH)).contains(acme.getFile().getName(),
                deps.getFile().getName(), "., ");
    }

    @Test
    public void testExclusionOfTransitiveDependencies() {
        Properties instructions = new Properties();
        instructions.setProperty(DependencyEmbedder.EMBED_DEPENDENCY, "acme:acme-sample," +
                "deps:*;exclude=*:acme-deps-excluded");
        instructions.setProperty(DependencyEmbedder.EMBED_TRANSITIVE, "True");
        DependencyEmbedder dependencyEmbedder = new DependencyEmbedder(instructions, reporter);

        final Artifact deps = create("deps", "acme-deps", "1");
        final Artifact excluded = create("deps", "acme-deps-excluded", "1");
        final Artifact acme = create("acme", "acme-sample", "1");
        acme.setDependencyTrail(ImmutableList.of(deps.getId(), acme.getId()));
        final Artifact acme2 = create("acme", "acme-sample-2", "1");
        ProjectDependencies dependencies = new ProjectDependencies(
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme, acme2),
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme, acme2, deps, excluded)
        );
        Properties properties = dependencyEmbedder.generate(instructions, dependencies);

        assertThat(properties.getProperty(Constants.INCLUDE_RESOURCE))
                .contains("@" + acme.getFile().getAbsolutePath())
                .contains("@" + deps.getFile().getAbsolutePath())
                .doesNotContain("@" + acme2.getFile().getAbsolutePath())
                .doesNotContain("@" + excluded.getFile().getAbsolutePath());

        assertThat(properties.getProperty(Constants.BUNDLE_CLASSPATH)).isNull();
    }

    @Test
    public void testExclusion() {
        Properties instructions = new Properties();
        instructions.setProperty(DependencyEmbedder.EMBED_DEPENDENCY,
                "*;exclude=:acme-sample-2|:wisdom-api");
        DependencyEmbedder dependencyEmbedder = new DependencyEmbedder(instructions, reporter);

        final Artifact deps = create("deps", "acme-deps", "1");
        final Artifact acme = create("acme", "acme-sample", "1");
        acme.setDependencyTrail(ImmutableList.of(deps.getId(), acme.getId()));
        final Artifact acme2 = create("acme", "acme-sample-2", "1");
        ProjectDependencies dependencies = new ProjectDependencies(
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme, acme2),
                ImmutableList.of(create("org.wisdom-framework", "wisdom-api", "1"),
                        acme, acme2, deps)
        );
        Properties properties = dependencyEmbedder.generate(instructions, dependencies);

        assertThat(properties.getProperty(Constants.INCLUDE_RESOURCE))
                .contains("@" + acme.getFile().getAbsolutePath())
                .doesNotContain("deps")
                .doesNotContain("wisdom-api")
                .doesNotContain("@" + acme2.getFile().getAbsolutePath());

        assertThat(properties.getProperty(Constants.BUNDLE_CLASSPATH)).isNull();
    }


    private class TestReporter implements Reporter {

        @Override
        public void error(String msg) {
            System.err.println("ERROR : " + msg);
        }

        @Override
        public void warn(String msg) {
            System.err.println("WARNING : " + msg);
        }
    }

}