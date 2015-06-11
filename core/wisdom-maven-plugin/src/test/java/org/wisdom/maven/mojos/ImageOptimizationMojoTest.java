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

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wisdom.maven.utils.ExecUtils;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the image optimization mojo.
 */
public class ImageOptimizationMojoTest {

    File installation = new File("target/workbench/image/tools");
    File basedir = new File("target/workbench/image/project");

    File c = new File("src/test/resources/img/C.png");
    File m = new File("src/test/resources/img/Mercedes.jpg");

    File target = new File(basedir, "target");

    final File classes = new File(basedir, "target/classes");
    final File assets = new File(basedir, "target/wisdom/assets");

    File internalNested = new File(classes, "img");
    File externalNested = new File(assets, "img");

    @Before
    public void prepare() throws IOException {
        if (installation.isDirectory()) {
            FileUtils.deleteQuietly(installation);
        }
        basedir.mkdirs();

        internalNested.mkdirs();
        externalNested.mkdirs();
        target.mkdirs();

        FileUtils.copyFileToDirectory(c, classes);
        FileUtils.copyFileToDirectory(c, assets);
        FileUtils.copyFileToDirectory(m, classes);
        FileUtils.copyFileToDirectory(m, assets);
        FileUtils.copyFileToDirectory(m, internalNested);
        FileUtils.copyFileToDirectory(m, externalNested);
        FileUtils.copyFileToDirectory(c, internalNested);
        FileUtils.copyFileToDirectory(c, externalNested);
    }

    @Test
    public void testInstallationAndOptimization() throws MojoExecutionException {
        if (systemValue == null) {
            System.setProperty("skipSystemPathLookup", "true");
        }

        ImageOptimizationMojo mojo = new ImageOptimizationMojo(installation);
        mojo.basedir = basedir;
        mojo.buildDirectory = target;
        mojo.optipngDownloadBaseLocation = ImageOptimizationMojo.OPTIPNG_DOWNLOAD_BASE_LOCATION;
        mojo.jpegtranDownloadBaseLocation = ImageOptimizationMojo.JPEGTRAN_DOWNLOAD_BASE_LOCATION;
        mojo.execute();

        String optipng = "optipng";
        String jpegtran = "jpegtran";

        if (ExecUtils.isWindows()) {
            optipng += ".exe";
            jpegtran += ".exe";
        }

        if (systemValue == null) {
            assertThat(new File(installation, optipng)).isFile();
            assertThat(new File(installation, jpegtran)).isFile();
        }

        assertThat(new File(classes, m.getName()).length()).isLessThan(m.length());
        assertThat(new File(classes, c.getName()).length()).isLessThan(c.length());
        assertThat(new File(assets, m.getName()).length()).isLessThan(m.length());
        assertThat(new File(assets, c.getName()).length()).isLessThan(c.length());
        assertThat(new File(internalNested, m.getName()).length()).isLessThan(m.length());
        assertThat(new File(internalNested, c.getName()).length()).isLessThan(c.length());
        assertThat(new File(externalNested, m.getName()).length()).isLessThan(m.length());
        assertThat(new File(externalNested, c.getName()).length()).isLessThan(c.length());
    }

    private String systemValue;

    @Before
    public void setUp() {
        systemValue = System.getProperty("skipSystemPathLookup");
    }

    @After
    public void tearDown() {
        if (systemValue == null) {
            System.clearProperty("skipSystemPathLookup");
        }
    }
}
