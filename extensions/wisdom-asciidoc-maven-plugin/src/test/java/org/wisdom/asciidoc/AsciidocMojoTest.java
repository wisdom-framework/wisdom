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
package org.wisdom.asciidoc;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the behavior of the Asciidoc Mojo
 */
public class AsciidocMojoTest {

    File basedir = new File("target/workbench/project");

    @Before
    public void setUp() {
        FileUtils.deleteQuietly(basedir);
    }

    @Test
    public void testInitializationWithNoFiles() throws MojoExecutionException {
        AsciidocMojo mojo = new AsciidocMojo();
        mojo.basedir = basedir;
        mojo.buildDirectory = new File(mojo.basedir, "target");
        mojo.doctype = "article";
        mojo.backend = "html5";
        mojo.execute();

        assertThat(mojo.instance).isNotNull();
        assertThat(mojo.getAsciidoctorInstance()).isNotNull();
        assertThat(mojo.getInternalAssetsDirectory()).isEqualTo(new File(mojo.basedir, "src/main/resources/assets"));
        assertThat(mojo.getExternalAssetsDirectory()).isEqualTo(new File(mojo.basedir, "src/main/assets"));
        assertThat(mojo.getInternalAssetOutputDirectory()).isEqualTo(new File(mojo.buildDirectory, "classes/assets"));
        assertThat(mojo.getExternalAssetsOutputDirectory()).isEqualTo(new File(mojo.buildDirectory, "wisdom/assets"));
    }

    @Test
    public void testInitializationWithUnfilteredInternalAndExternalFilesUsingRegularExtensions() throws
            MojoExecutionException, IOException {
        AsciidocMojo mojo = new AsciidocMojo();
        mojo.basedir = basedir;
        mojo.buildDirectory = new File(mojo.basedir, "target");
        mojo.doctype = "article";
        mojo.backend = "html5";
        FileUtils.copyFile(new File("src/test/resources/hello.ad"), new File(basedir,
                "src/main/resources/assets/doc/hello.ad"));
        FileUtils.copyFile(new File("src/test/resources/hello.ad"), new File(basedir,
                "src/main/assets/doc/hello.asciidoc"));
        mojo.execute();

        final File internal = new File(mojo.getInternalAssetOutputDirectory(), "doc/hello.html");
        final File external = new File(mojo.getExternalAssetsOutputDirectory(), "doc/hello.html");
        assertThat(internal).isFile();
        assertThat(external).isFile();

        assertThat(FileUtils.readFileToString(internal)).contains("<h1>Hello, " +
                "Wisdom!</h1>").contains("href=\"http://asciidoc.org\"");
        assertThat(FileUtils.readFileToString(external)).contains("<h1>Hello, " +
                "Wisdom!</h1>").contains("href=\"http://asciidoc.org\"");
    }

    @Test
    public void testInitializationWithFilteredInternalAndExternalFilesUsingRegularExtensions() throws
            MojoExecutionException, IOException {
        AsciidocMojo mojo = new AsciidocMojo();
        mojo.basedir = basedir;
        mojo.buildDirectory = new File(mojo.basedir, "target");
        mojo.doctype = "article";
        mojo.backend = "html5";
        FileUtils.copyFile(new File("src/test/resources/hello.ad"), new File(basedir,
                "src/main/resources/assets/doc/hello.ad"));
        // Filtered version:
        FileUtils.copyFile(new File("src/test/resources/hello.ad"), new File(basedir,
                "target/classes/assets/doc/hello.ad"));
        FileUtils.copyFile(new File("src/test/resources/hello.ad"), new File(basedir,
                "src/main/assets/doc/hello.asciidoc"));
        // Filtered version:
        FileUtils.copyFile(new File("src/test/resources/hello.ad"), new File(basedir,
                "target/wisdom/assets/doc/hello.asciidoc"));

        mojo.execute();

        final File internal = new File(mojo.getInternalAssetOutputDirectory(), "doc/hello.html");
        final File external = new File(mojo.getExternalAssetsOutputDirectory(), "doc/hello.html");
        assertThat(internal).isFile();
        assertThat(external).isFile();

        assertThat(FileUtils.readFileToString(internal)).contains("<h1>Hello, " +
                "Wisdom!</h1>").contains("href=\"http://asciidoc.org\"");
        assertThat(FileUtils.readFileToString(external)).contains("<h1>Hello, " +
                "Wisdom!</h1>").contains("href=\"http://asciidoc.org\"");
    }

    @Test
    public void testAccept() throws MojoExecutionException {
        AsciidocMojo mojo = new AsciidocMojo();
        mojo.basedir = basedir;
        mojo.buildDirectory = new File(mojo.basedir, "target");
        mojo.doctype = "article";
        mojo.backend = "html5";
        // Regular extensions.
        mojo.execute();

        assertThat(mojo.accept(new File("hello.ad"))).isTrue();
        assertThat(mojo.accept(new File("hello.adoc"))).isTrue();
        assertThat(mojo.accept(new File("hello.asciidoc"))).isTrue();
        assertThat(mojo.accept(new File("hello.html"))).isFalse();

        mojo.extensions = ImmutableList.of("ascii");
        // Regular extensions.
        mojo.execute();
        assertThat(mojo.accept(new File("hello.ad"))).isFalse();
        assertThat(mojo.accept(new File("hello.ascii"))).isTrue();

    }
}
