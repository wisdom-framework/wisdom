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
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the image optimization mojo.
 */
public class ImageOptimizationMojoTest {

    File installation = new File("target/workbench/image/tools");
    File basedir = new File("target/workbench/image/project");

    File png = new File("src/test/resources/img/C.png");
    File jpg = new File("src/test/resources/img/Mercedes.jpg");
    File gif = new File("src/test/resources/img/Circle_radians.gif");

    File invalid = new File("src/test/resources/img/not-an-image.jpg");



    File target = new File(basedir, "target");

    final File classes = new File(basedir, "target/classes");
    final File assets = new File(basedir, "target/wisdom/assets");

    File internalNested = new File(classes, "img");
    File externalNested = new File(assets, "img");

    File invalidOutput = new File(classes, "not-an-image.jpg");

    @Before
    public void prepare() throws IOException {
        if (installation.isDirectory()) {
            FileUtils.deleteQuietly(installation);
        }
        basedir.mkdirs();

        internalNested.mkdirs();
        externalNested.mkdirs();
        target.mkdirs();

        FileUtils.copyFileToDirectory(png, classes);
        FileUtils.copyFileToDirectory(png, assets);
        FileUtils.copyFileToDirectory(gif, classes);
        FileUtils.copyFileToDirectory(gif, assets);
        FileUtils.copyFileToDirectory(jpg, classes);
        FileUtils.copyFileToDirectory(jpg, assets);
        FileUtils.copyFileToDirectory(jpg, internalNested);
        FileUtils.copyFileToDirectory(jpg, externalNested);
        FileUtils.copyFileToDirectory(png, internalNested);
        FileUtils.copyFileToDirectory(png, externalNested);
        FileUtils.copyFileToDirectory(gif, internalNested);
        FileUtils.copyFileToDirectory(gif, externalNested);

        FileUtils.deleteQuietly(invalidOutput);
    }

    @Test
    public void testInstallationAndOptimization() throws MojoExecutionException {
        ImageOptimizationMojo mojo = new ImageOptimizationMojo();
        mojo.basedir = basedir;
        mojo.buildDirectory = target;
        mojo.execute();

        assertThat(new File(classes, jpg.getName()).length()).isLessThan(jpg.length());
        assertThat(new File(classes, png.getName()).length()).isLessThan(png.length());
        assertThat(new File(assets, jpg.getName()).length()).isLessThan(jpg.length());
        assertThat(new File(assets, png.getName()).length()).isLessThan(png.length());
        assertThat(new File(assets, gif.getName()).length()).isLessThan(gif.length());
        assertThat(new File(internalNested, jpg.getName()).length()).isLessThan(jpg.length());
        assertThat(new File(internalNested, png.getName()).length()).isLessThan(png.length());
        assertThat(new File(internalNested, gif.getName()).length()).isLessThan(gif.length());
        assertThat(new File(externalNested, jpg.getName()).length()).isLessThan(jpg.length());
        assertThat(new File(externalNested, png.getName()).length()).isLessThan(png.length());
        assertThat(new File(externalNested, gif.getName()).length()).isLessThan(gif.length());
    }

    @Test(expected = MojoExecutionException.class)
    public void testInstallationAndOptimizationWithInvalidImage() throws MojoExecutionException, IOException {
        ImageOptimizationMojo mojo = new ImageOptimizationMojo();
        mojo.basedir = basedir;
        mojo.buildDirectory = target;
        mojo.failOnBrokenAsset = true;
        FileUtils.copyFile(invalid, invalidOutput);
        mojo.execute();
    }

    @Test
    public void testInstallationAndOptimizationWithInvalidImageButErrorIgnored() throws MojoExecutionException, IOException {
        ImageOptimizationMojo mojo = new ImageOptimizationMojo();
        mojo.basedir = basedir;
        mojo.buildDirectory = target;
        mojo.failOnBrokenAsset = false;
        FileUtils.copyFile(invalid, invalidOutput);
        mojo.execute();
    }


    @Test
    public void testInstallationAndOptimizationWithParameters() throws MojoExecutionException, IOException {
        ImageOptimizationMojo mojo = new ImageOptimizationMojo();
        mojo.basedir = basedir;
        mojo.buildDirectory = target;
        mojo.failOnBrokenAsset = true;
        mojo.imageMinification = new ImageMinification();
        mojo.imageMinification.setInterlaced(true);
        mojo.imageMinification.setOptimizationLevel(5);
        mojo.imageMinification.setProgressive(true);
        mojo.execute();
    }
}
