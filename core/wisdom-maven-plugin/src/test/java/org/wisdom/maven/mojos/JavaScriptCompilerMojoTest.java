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
import com.google.javascript.jscomp.CompilationLevel;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static java.util.Collections.singletonList;
import static org.apache.commons.io.FileUtils.lineIterator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JavaScriptCompilerMojoTest {

    @Test
    public void testAccept() throws Exception {
        JavaScriptCompilerMojo mojo = new JavaScriptCompilerMojo();
        mojo.basedir = new File("junk");
        File file = new File(mojo.basedir, "src/main/resources/assets/foo.js");
        assertThat(mojo.accept(file)).isTrue();
        file = new File(mojo.basedir, "src/main/resources/assets/foo/foo.js");
        assertThat(mojo.accept(file)).isTrue();
        file = new File(mojo.basedir, "src/main/resources/assets/foo.css");
        assertThat(mojo.accept(file)).isFalse();
        // Not a valid resources
        file = new File(mojo.basedir, "src/main/foo.js");
        assertThat(mojo.accept(file)).isFalse();

        file = new File(mojo.basedir, "src/main/assets/foo.js");
        assertThat(mojo.accept(file)).isTrue();
        file = new File(mojo.basedir, "src/main/assets/foo/foo.js");
        assertThat(mojo.accept(file)).isTrue();
        file = new File(mojo.basedir, "src/main/assets/foo.css");
        assertThat(mojo.accept(file)).isFalse();
        // Not a valid resources
        file = new File(mojo.basedir, "src/main/foo.js");
        assertThat(mojo.accept(file)).isFalse();

    }

    @Test
    public void testIsNotMinified() throws Exception {
        JavaScriptCompilerMojo mojo = new JavaScriptCompilerMojo();
        mojo.googleClosureMinifierSuffix = "-minified";

        File file = new File("foo.js");
        assertThat(mojo.isNotMinified(file)).isTrue();
        file = new File("foo-min.js");

        assertThat(mojo.isNotMinified(file)).isFalse();
        file = new File("foo.min.js");
        assertThat(mojo.isNotMinified(file)).isFalse();

        file = new File("foo-minified.js");
        assertThat(mojo.isNotMinified(file)).isFalse();
    }

    @Test
    public void testGetMinifiedFile() {
        JavaScriptCompilerMojo mojo = new JavaScriptCompilerMojo();
        mojo.googleClosureMinifierSuffix = "-minified";
        mojo.basedir = new File("target/junk/root");
        mojo.buildDirectory = new File(mojo.basedir, "target");
        File file = new File(mojo.basedir,
                "src/main/resources/assets/foo.js");
        assertThat(mojo.getMinifiedFile(file).getAbsolutePath()).isEqualTo(
                new File(mojo.buildDirectory, "classes/assets/foo-minified.js").getAbsolutePath());

        file = new File(mojo.basedir,
                "src/main/assets/foo.js");
        assertThat(mojo.getMinifiedFile(file).getAbsolutePath()).isEqualTo(new File(mojo.buildDirectory,
                "wisdom/assets/foo-minified.js").getAbsolutePath());
    }

    @Test
    public void testGetDefaultOutputFile() {
        JavaScriptCompilerMojo mojo = new JavaScriptCompilerMojo();
        mojo.googleClosureMinifierSuffix = "-min";
        mojo.basedir = new File("target/junk/root");
        mojo.buildDirectory = new File(mojo.basedir, "target");
        JavaScript javascript = new JavaScript();
        mojo.javascript = javascript;
        MavenProject project = mock(MavenProject.class);
        when(project.getArtifactId()).thenReturn("my-artifact");
        mojo.project = project;

        Aggregation aggregation = new Aggregation();
        aggregation.setMinification(true);
        javascript.setAggregations(ImmutableList.of(aggregation));

        assertThat(mojo.getDefaultOutputFile(aggregation).getAbsolutePath()).isEqualTo(new File(mojo.buildDirectory,
                "classes/assets/my-artifact-min.js").getAbsolutePath());
    }

    @Test
    public void testAggregatedSourceMapIsCreated() throws IOException, MojoExecutionException, MojoFailureException {
        JavaScriptCompilerMojo mojo = new JavaScriptCompilerMojo();
        mojo.googleClosureMinifierSuffix = "-min";
        mojo.basedir = new File("target/junk/root");
        mojo.buildDirectory = new File(mojo.basedir, "target");
        MavenProject project = mock(MavenProject.class);
        when(project.getArtifactId()).thenReturn("my-artifact");
        mojo.project = project;
        mojo.googleClosureCompilationLevel = CompilationLevel.ADVANCED_OPTIMIZATIONS;
        mojo.googleClosureMap = true;

        Aggregation aggregation = new Aggregation();
        aggregation.setMinification(true);

        JavaScript javascript = new JavaScript();
        javascript.setAggregations(singletonList(new Aggregation()));
        mojo.javascript = javascript;

        mojo.execute();

        File map = new File(mojo.getDefaultOutputFile(aggregation).getParentFile(), "my-artifact-min.js.map");

        assertThat(mojo.getDefaultOutputFile(aggregation)).hasContent("\n//# sourceMappingURL=my-artifact-min.js.map");
        assertThat(map).exists();
        assertThat(lineIterator(map)).contains("\"file\":\"my-artifact-min.js\",");
    }

    /*
    @Test
    public void testSourceMapIsCreatedForInternal() throws IOException, MojoExecutionException, MojoFailureException {
        JavaScriptCompilerMojo mojo = new JavaScriptCompilerMojo();
        mojo.googleClosureMinifierSuffix = "-min";
        mojo.basedir = new File("target/test-classes/fake-project");
        mojo.buildDirectory = new File(mojo.basedir, "target");
        mojo.getInternalAssetOutputDirectory().mkdirs();
        mojo.googleClosureMap = true;

        FileUtils.copyDirectory(mojo.getExternalAssetsDirectory(), mojo.getInternalAssetOutputDirectory());
        mojo.googleClosureCompilationLevel = CompilationLevel.ADVANCED_OPTIMIZATIONS;

        mojo.execute();

        File min = new File(mojo.getInternalAssetOutputDirectory(), "street-min.js");
        File map = new File(mojo.getInternalAssetOutputDirectory(), "street-min.js.map");

        assertThat(lineIterator(min)).endsWith("//# sourceMappingURL=street-min.js.map");
        assertThat(map).exists();
    }
    */
}
