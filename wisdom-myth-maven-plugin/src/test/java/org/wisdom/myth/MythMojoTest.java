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
package org.wisdom.myth;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Before;
import org.junit.Test;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.node.NodeManager;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the Myth Mojo
 */
public class MythMojoTest {

    public static final String FAKE_PROJECT = "target/test-classes/fake-project";
    public static final String FAKE_PROJECT_TARGET = "target/test-classes/fake-project/target";
    File nodeDirectory;
    private MythMojo mojo;


    @Before
    public void setUp() throws IOException {
        nodeDirectory = new File("target/test/node");
        nodeDirectory.mkdirs();
        Log log = new SystemStreamLog();
        NodeManager manager = new NodeManager(log, nodeDirectory);
        manager.installIfNotInstalled();
        mojo = new MythMojo();
        mojo.basedir = new File(FAKE_PROJECT);
        mojo.buildDirectory = new File(FAKE_PROJECT_TARGET);
        mojo.buildDirectory.mkdirs();
    }

    @Test
    public void testProcessingOfCssFiles() throws MojoFailureException, MojoExecutionException, IOException {
        cleanup();
        mojo.execute();

        final File var = new File(FAKE_PROJECT_TARGET, "classes/var.css");
        assertThat(var).isFile();
        String content = FileUtils.readFileToString(var);
        assertThat(content).contains("color: #847AD1;");
        assertThat(content).contains("padding: 10px;");

        final File link = new File(FAKE_PROJECT_TARGET, "wisdom/assets/myth/link.css");
        assertThat(link).isFile();
        content = FileUtils.readFileToString(link);
        assertThat(content).contains("color: rgb(157, 149, 218);");
        assertThat(content).contains(" -webkit-transition: color .2s;");
    }

    @Test
    public void testProcessingOfRecentCssFiles() throws MojoFailureException, MojoExecutionException, IOException {
        cleanup();
        // First execution.
        mojo.execute();

        final File original = new File(FAKE_PROJECT, "src/main/resources/var.css");
        String content = FileUtils.readFileToString(original);
        final File var = new File(FAKE_PROJECT_TARGET, "classes/var.css");
        // Apply a small change in the file
        content = content.replace("var-purple: #847AD1;", "var-purple: #847AD2;");
        // Rewrite the file in the destination folder.
        FileUtils.write(var, content);

        // Second execution, the var file was updated.
        mojo.execute();

        // Check that the processing was made on the right file
        content = FileUtils.readFileToString(var);
        assertThat(content).contains("color: #847AD2;");
    }

    @Test
    public void testWatching() throws MojoFailureException, MojoExecutionException, IOException, WatchingException, InterruptedException {
        cleanup();

        // Copy var to var2 (do not modify var as it is used by other tests).
        final File originalVar = new File(FAKE_PROJECT, "src/main/resources/var.css");
        final File newVar = new File(FAKE_PROJECT, "src/main/resources/var2.css");
        final File originalLink = new File(FAKE_PROJECT, "src/main/assets/myth/link.css");
        String originalVarContent = FileUtils.readFileToString(originalVar);
        FileUtils.copyFile(originalVar, newVar);

        mojo.execute();

        final File var = new File(FAKE_PROJECT_TARGET, "classes/var2.css");
        assertThat(var).isFile();
        String content = FileUtils.readFileToString(var);
        assertThat(content).contains("color: #847AD1;");
        assertThat(content).contains("padding: 10px;");

        final File link = new File(FAKE_PROJECT_TARGET, "wisdom/assets/myth/link.css");
        assertThat(link).isFile();
        content = FileUtils.readFileToString(link);
        assertThat(content).contains("color: rgb(157, 149, 218);");
        assertThat(content).contains(" -webkit-transition: color .2s;");

        // Delete var
        newVar.delete();
        mojo.fileDeleted(newVar);

        assertThat(var.isFile()).isFalse();

        // Recreate the file with another name (same content)
        File newFile = new File(FAKE_PROJECT, "src/main/resources/var3.css");
        FileUtils.write(newFile, originalVarContent);
        mojo.fileCreated(newFile);
        File var3 = new File(FAKE_PROJECT_TARGET, "classes/var3.css");
        assertThat(var3).isFile();
        content = FileUtils.readFileToString(var3);
        assertThat(content).contains("color: #847AD1;");
        assertThat(content).contains("padding: 10px;");

        // Update link
        long originalLastModified = link.lastModified();
        FileUtils.touch(originalLink);
        mojo.fileUpdated(originalLink);
        // The file should have been updated
        assertThat(link.lastModified()).isGreaterThanOrEqualTo(originalLastModified);
    }

    private void cleanup() {
        FileUtils.deleteQuietly(mojo.buildDirectory);
    }
}
