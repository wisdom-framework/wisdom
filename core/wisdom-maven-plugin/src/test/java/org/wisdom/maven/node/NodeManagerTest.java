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
package org.wisdom.maven.node;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.wisdom.maven.mojos.AbstractWisdomMojo;
import org.wisdom.maven.mojos.CoffeeScriptCompilerMojo;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Check the node manager and node management (especially installation)
 * Most of these tests are 'long' test and need to be launched explicitly.
 */
public class NodeManagerTest {

    File nodeDirectory;
    private Log log;
    private NodeManager manager;


    @Before
    public void setUp() {
        nodeDirectory = new File("target/test/node");
        nodeDirectory.mkdirs();
        log = new SystemStreamLog();
        AbstractWisdomMojo mojo = new AbstractWisdomMojo() {
            @Override
            public void execute() throws MojoExecutionException, MojoFailureException {
                // Do nothing.
            }
        };
        mojo.basedir = new File("target/test");
        manager = new NodeManager(log, nodeDirectory, mojo);
    }

    @Test
    @Category(LongRun.class)
    public void testInstallation() throws IOException {
        FileUtils.deleteQuietly(nodeDirectory);
        nodeDirectory.mkdirs();

        manager.installIfNotInstalled();

        assertThat(manager.getNodeExecutable()).isFile();
        assertThat(manager.getNodeModulesDirectory().getAbsolutePath()).startsWith(nodeDirectory.getAbsolutePath());
    }

    @Test
    @Category(LongRun.class)
    public void testInstallationOfCoffeeScript() throws IOException, ParseException {
        manager.installIfNotInstalled();
        AbstractWisdomMojo mojo = mock(AbstractWisdomMojo.class);
        when(mojo.getLog()).thenReturn(log);
        when(mojo.getNodeManager()).thenReturn(manager);

        NPM npm = NPM.npm(mojo, "coffee-script", CoffeeScriptCompilerMojo.COFFEESCRIPT_VERSION);
        assertThat(npm).isNotNull();

        assertThat(npm.findExecutable("coffee")).isFile();
    }

    @Test
    @Category(LongRun.class)
    public void testReinstallation() throws IOException, ParseException {
        manager.installIfNotInstalled();
        AbstractWisdomMojo mojo = mock(AbstractWisdomMojo.class);
        when(mojo.getLog()).thenReturn(log);
        when(mojo.getNodeManager()).thenReturn(manager);

        NPM npm = NPM.npm(mojo, "coffee-script", CoffeeScriptCompilerMojo.COFFEESCRIPT_VERSION);
        assertThat(npm).isNotNull();
        assertThat(npm.findExecutable("coffee")).isFile();

        NPM npm2 = NPM.npm(mojo, "coffee-script", CoffeeScriptCompilerMojo.COFFEESCRIPT_VERSION);
        assertThat(npm).isEqualTo(npm2);
        assertThat(npm.hashCode()).isEqualTo(npm2.hashCode());
    }

    @Test
    @Category(LongRun.class)
    public void testExecution() throws IOException, ParseException, MojoExecutionException {
        manager.installIfNotInstalled();
        AbstractWisdomMojo mojo = mock(AbstractWisdomMojo.class);
        when(mojo.getLog()).thenReturn(log);
        when(mojo.getNodeManager()).thenReturn(manager);

        NPM npm = NPM.npm(mojo, "coffee-script", CoffeeScriptCompilerMojo.COFFEESCRIPT_VERSION);

        File input = new File("target/test-classes/coffee/test.coffee");
        File output = new File("target/test/coffee");
        output.mkdirs();
        int exit = npm.execute("coffee", "--compile", "--map", "--output", output.getAbsolutePath(),
                input.getAbsolutePath());
        assertThat(exit).isEqualTo(0);
        assertThat(new File(output, "test.js")).isFile();
        assertThat(new File(output, "test.js.map")).isFile();
    }

    @Test
    @Category(LongRun.class)
    public void testSkipPostInstall() throws IOException, ParseException, MojoExecutionException {
        manager.installIfNotInstalled();
        AbstractWisdomMojo mojo = mock(AbstractWisdomMojo.class);
        when(mojo.getLog()).thenReturn(log);
        when(mojo.getNodeManager()).thenReturn(manager);

        NPM npm = NPM.npm(mojo, "optipng-bin", "1.0.0", "--ignore-scripts");
        assertThat(npm).isNotNull();
    }

}
