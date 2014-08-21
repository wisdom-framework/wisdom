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
package org.wisdom.mojo.npm;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Before;
import org.junit.Test;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.mojos.AbstractWisdomMojo;
import org.wisdom.maven.node.NodeManager;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class NpmRunnerMojoTest {

    File nodeDirectory;
    private NodeManager manager;

    private File baseDir = new File("target/test/project");
    private File target = new File(baseDir, "target");

    @Before
    public void setUp() throws IOException {
        nodeDirectory = new File("target/test/node");
        nodeDirectory.mkdirs();
        Log log = new SystemStreamLog();
        AbstractWisdomMojo mojo = new AbstractWisdomMojo() {
            @Override
            public void execute() throws MojoExecutionException, MojoFailureException {
                // Do nothing.
            }
        };
        mojo.basedir = this.baseDir;
        manager = new NodeManager(log, nodeDirectory, mojo);

        File assets = new File(baseDir, "src/main/resources/assets");
        assets.mkdirs();

        FileUtils.copyDirectory(new File("src/test/resources"), assets);
    }

    @Test
    public void testInstallationAndExecutionOfJade() throws IOException, MojoExecutionException {
        FileUtils.deleteQuietly(target);
        nodeDirectory.mkdirs();

        NpmRunnerMojo runner = new NpmRunnerMojo();
        runner.basedir = new File("target/project");
        runner.setNodeManager(manager);
        runner.name = "jade";
        runner.version = "1.5.0";
        runner.binary = "jade";
        runner.arguments = new String[] {"src/main/resources/assets", "--out", "target/classes/assets"};

        runner.execute();

        assertThat(new File(target, "classes/assets/template.html")).isFile();
    }

    @Test
    public void testWatch() throws IOException, MojoExecutionException, WatchingException {
        FileUtils.deleteQuietly(target);
        nodeDirectory.mkdirs();

        NpmRunnerMojo runner = new NpmRunnerMojo();
        runner.basedir = new File("target/project");
        runner.setNodeManager(manager);
        runner.name = "jade";
        runner.version = "1.5.0";
        runner.binary = "jade";
        runner.watchFilter = "*.jade";
        runner.arguments = new String[] {"src/main/resources/assets", "--out", "target/classes/assets"};

        assertThat(new File(target, "classes/assets/template.html")).doesNotExist();

        File template = new File(runner.basedir, "src/main/resources/assets/template.jade");
        assertThat(runner.accept(template)).isTrue();
        assertThat(runner.fileCreated(template)).isTrue();
        assertThat(new File(target, "classes/assets/template.html")).isFile();
    }

    @Test
    public void testInstallationOfJadeWithoutVersion() throws IOException, MojoExecutionException {
        FileUtils.deleteQuietly(nodeDirectory);
        nodeDirectory.mkdirs();

        NpmRunnerMojo runner = new NpmRunnerMojo();
        runner.setNodeManager(manager);
        runner.name = "jade";
        runner.version = null;
        runner.binary = "jade";
        runner.arguments = new String[] {"--help"};

        runner.execute();
    }



}