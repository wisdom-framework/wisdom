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
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.node.NodeManager;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CSSMinifierMojoTest {

    public static final String FAKE_PROJECT = "target/test-classes/fake-project";
    public static final String FAKE_PROJECT_TARGET = "target/test-classes/fake-project/target";
    File nodeDirectory;
    private CSSMinifierMojo mojo;
    private LessCompilerMojo less;


    @Before
    public void setUp() throws IOException {
        MavenProject project = new MavenProject();
        project.setArtifactId("test-artifact");

        nodeDirectory = new File("target/test/node");
        nodeDirectory.mkdirs();
        Log log = new SystemStreamLog();
        mojo = new CSSMinifierMojo();
        mojo.project = project;
        NodeManager manager = new NodeManager(log, nodeDirectory, mojo);
        manager.installIfNotInstalled();
        mojo.basedir = new File(FAKE_PROJECT);
        mojo.buildDirectory = new File(FAKE_PROJECT_TARGET);
        mojo.buildDirectory.mkdirs();
        mojo.cleanCssVersion = CSSMinifierMojo.CLEANCSS_NPM_VERSION;
        mojo.cssMinifierSuffix = "-min";

        // Less stuff
        less = new LessCompilerMojo();
        NodeManager manager2 = new NodeManager(log, nodeDirectory, less);
        manager2.installIfNotInstalled();
        less.project = project;
        less.basedir = new File(FAKE_PROJECT);
        less.buildDirectory = new File(FAKE_PROJECT_TARGET);
        less.lessVersion = LessCompilerMojo.LESS_VERSION;

        cleanup();
    }

    private void cleanup() {
        FileUtils.deleteQuietly(mojo.buildDirectory);
    }

    @Test
    public void testAccept() throws Exception {
        mojo.basedir = new File("junk");
        File file = new File(mojo.basedir, "src/main/resources/assets/foo.css");
        assertThat(mojo.accept(file)).isTrue();
        file = new File(mojo.basedir, "src/main/resources/assets/foo/foo.css");
        assertThat(mojo.accept(file)).isTrue();
        file = new File(mojo.basedir, "src/main/resources/assets/foo.js");
        assertThat(mojo.accept(file)).isFalse();
        // Not a valid resources
        file = new File(mojo.basedir, "src/main/foo.css");
        assertThat(mojo.accept(file)).isFalse();

        file = new File(mojo.basedir, "src/main/assets/foo.css");
        assertThat(mojo.accept(file)).isTrue();
        file = new File(mojo.basedir, "src/main/assets/foo/foo.css");
        assertThat(mojo.accept(file)).isTrue();
        file = new File(mojo.basedir, "src/main/assets/foo.js");
        assertThat(mojo.accept(file)).isFalse();
        // Not a valid resources
        file = new File(mojo.basedir, "src/main/foo.css");
        assertThat(mojo.accept(file)).isFalse();
    }

    @Test
    public void testIsNotMinified() throws Exception {
        mojo.cssMinifierSuffix = "-minified";

        File file = new File("foo.css");
        assertThat(mojo.isNotMinified(file)).isTrue();
        file = new File("foo-min.css");

        assertThat(mojo.isNotMinified(file)).isFalse();
        file = new File("foo.min.css");
        assertThat(mojo.isNotMinified(file)).isFalse();

        file = new File("foo-minified.css");
        assertThat(mojo.isNotMinified(file)).isFalse();
    }

    @Test
    public void testGetMinifiedFile() {
        mojo.cssMinifierSuffix = "-minified";
        mojo.basedir = new File("target/junk/root");
        mojo.buildDirectory = new File(mojo.basedir, "target");
        File file = new File(mojo.basedir,
                "src/main/resources/assets/foo.css");
        assertThat(mojo.getMinifiedFile(file).getAbsolutePath()).isEqualTo(new File(mojo.buildDirectory,
                "classes/assets/foo-minified.css").getAbsolutePath());

        file = new File(mojo.basedir,
                "src/main/assets/foo.css");
        assertThat(mojo.getMinifiedFile(file).getAbsolutePath()).isEqualTo(new File(mojo.buildDirectory,
                "wisdom/assets/foo-minified.css").getAbsolutePath());
    }

    @Test
    public void testGetDefaultOutputFile() {
        mojo.cssMinifierSuffix = "-min";
        mojo.basedir = new File("target/junk/root");
        mojo.buildDirectory = new File(mojo.basedir, "target");
        Stylesheets stylesheets = new Stylesheets();
        mojo.stylesheets = stylesheets;
        MavenProject project = mock(MavenProject.class);
        when(project.getArtifactId()).thenReturn("my-artifact");
        mojo.project = project;

        Aggregation aggregation = new Aggregation();
        aggregation.setMinification(true);
        stylesheets.setAggregations(ImmutableList.of(aggregation));

        assertThat(mojo.getDefaultOutputFile(aggregation).getAbsolutePath()).isEqualTo(new File(mojo.buildDirectory,
                "classes/assets/my-artifact-min.css").getAbsolutePath());
    }

    @Test
    public void testProcessingOfFiles() throws MojoFailureException, MojoExecutionException, IOException {
        cleanup();
        less.execute();
        mojo.execute();

        File site = new File(FAKE_PROJECT_TARGET, "classes/assets/css/site-min.css");
        assertThat(site).isFile();
        assertThat(FileUtils.readFileToString(site))
                .contains("h1{color:red}");

        File style = new File(FAKE_PROJECT_TARGET, "classes/assets/less/style-min.css");
        assertThat(style).isFile();
    }

    @Test
    public void testCustomArguments() throws MojoFailureException, MojoExecutionException, IOException {
        cleanup();
        less.execute();
        mojo.cleanCssArguments = "--debug -c ie7 -b";
        mojo.execute();

        File site = new File(FAKE_PROJECT_TARGET, "classes/assets/css/site-min.css");
        assertThat(site).isFile();
        assertThat(FileUtils.readFileToString(site))
                .contains("h1{color:red}");

        File style = new File(FAKE_PROJECT_TARGET, "classes/assets/less/style-min.css");
        assertThat(style).isFile();
        assertThat(FileUtils.readLines(style)).hasSize(2); // We don't remove line break.

    }

    @Test
    public void testDeletionAndReCreation() throws MojoFailureException, MojoExecutionException, IOException,
            WatchingException {
        cleanup();
        less.execute();
        mojo.execute();

        File site = new File(FAKE_PROJECT_TARGET, "classes/assets/css/site-min.css");
        File siteMap = new File(FAKE_PROJECT_TARGET, "classes/assets/css/site-min.css.map");
        assertThat(site).isFile();
        assertThat(siteMap).isFile();
        assertThat(FileUtils.readFileToString(site))
                .contains("h1{color:red}");

        File style = new File(FAKE_PROJECT_TARGET, "classes/assets/less/style-min.css");
        File styleMap = new File(FAKE_PROJECT_TARGET, "classes/assets/less/style-min.css.map");
        assertThat(style).isFile();
        assertThat(styleMap).isFile();

        File source = new File(FAKE_PROJECT, "src/main/resources/assets/css/site.css");
        mojo.fileDeleted(source);
        assertThat(site).doesNotExist();
        assertThat(siteMap).doesNotExist();

        // Recreate it.
        mojo.fileUpdated(source);
        assertThat(site).isFile();
        assertThat(siteMap).isFile();
        assertThat(FileUtils.readFileToString(site))
                .contains("h1{color:red}");
    }

    @Test
    public void testProcessingOfStylesheets() throws MojoFailureException, MojoExecutionException, IOException {
        cleanup();

        // Copy site file to target as the aggregation checks file fron the output directory.
        final File destDir = new File(mojo.buildDirectory, "classes/assets/css");
        destDir.mkdirs();
        FileUtils.copyFileToDirectory(new File(mojo.basedir, "src/main/resources/assets/css/site.css"),
                destDir);

        mojo.stylesheets = new Stylesheets();
        Aggregation aggregation1 = new Aggregation();
        aggregation1.setFiles(ImmutableList.of(
                "less/style.css",
                "css/site.css"
        ));
        aggregation1.setOutput("aggregation1.css");
        Aggregation aggregation2 = new Aggregation();
        aggregation2.setFiles(ImmutableList.of(
                "css/site.css"
        ));
        mojo.stylesheets.setAggregations(ImmutableList.of(aggregation1, aggregation2));

        less.execute();
        mojo.execute();

        File agg = new File(FAKE_PROJECT_TARGET, "classes/assets/test-artifact-min.css");
        assertThat(agg).isFile();
        assertThat(FileUtils.readFileToString(agg))
                .contains("h1{color:red}")
                .doesNotContain(".box");

        File agg1 = new File(FAKE_PROJECT_TARGET, "classes/assets/aggregation1.css");
        assertThat(agg1).isFile();
        assertThat(FileUtils.readFileToString(agg1))
                .contains("h1{color:red}")
                .contains(".box");

    }

    @Test
    public void testProcessingOfStylesheetsWithoutMinification() throws MojoFailureException, MojoExecutionException,
            IOException {
        cleanup();

        // Copy site file to target as the aggregation checks file fron the output directory.
        final File destDir = new File(mojo.buildDirectory, "classes/assets/css");
        destDir.mkdirs();
        FileUtils.copyFileToDirectory(new File(mojo.basedir, "src/main/resources/assets/css/site.css"),
                destDir);

        mojo.stylesheets = new Stylesheets();
        Aggregation aggregation1 = new Aggregation();
        aggregation1.setFiles(ImmutableList.of(
                "less/style.css",
                "css/site.css"
        ));
        aggregation1.setMinification(false);
        Aggregation aggregation2 = new Aggregation();
        aggregation2.setFiles(ImmutableList.of(
                "css/site.css"
        ));
        aggregation2.setMinification(false);
        aggregation2.setOutput("aggregation2.css");
        mojo.stylesheets.setAggregations(ImmutableList.of(aggregation1, aggregation2));

        less.execute();
        mojo.execute();

        File agg = new File(FAKE_PROJECT_TARGET, "classes/assets/test-artifact.css");
        assertThat(agg).isFile();
        assertThat(FileUtils.readFileToString(agg))
                .contains("h1{color:red}")
                .contains(".box");

        File agg2 = new File(FAKE_PROJECT_TARGET, "classes/assets/aggregation2.css");
        assertThat(agg2).isFile();
        assertThat(FileUtils.readFileToString(agg2))
                .contains("h1{color:red}")
                .doesNotContain(".box");

    }

}