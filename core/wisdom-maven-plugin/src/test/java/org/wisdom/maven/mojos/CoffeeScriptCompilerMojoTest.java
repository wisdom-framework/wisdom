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
import static org.assertj.core.api.Assertions.fail;

/**
 * Check the behavior of the CoffeeScript mojo.
 */
public class CoffeeScriptCompilerMojoTest {

    public static final String FAKE_PROJECT = "target/test-classes/fake-project";
    public static final String FAKE_PROJECT_TARGET = "target/test-classes/fake-project/target";
    File nodeDirectory;
    private CoffeeScriptCompilerMojo mojo;


    @Before
    public void setUp() throws IOException {
        nodeDirectory = new File("target/test/node");
        nodeDirectory.mkdirs();
        Log log = new SystemStreamLog();
        mojo = new CoffeeScriptCompilerMojo();
        NodeManager manager = new NodeManager(log, nodeDirectory, mojo);
        manager.installIfNotInstalled();
        mojo.basedir = new File(FAKE_PROJECT);
        mojo.buildDirectory = new File(FAKE_PROJECT_TARGET);
        mojo.buildDirectory.mkdirs();
        mojo.coffeeScriptVersion = CoffeeScriptCompilerMojo.COFFEESCRIPT_VERSION;
        cleanup();
    }

    @Test
    public void testProcessingOfCoffeeFiles() throws MojoFailureException, MojoExecutionException, IOException {
        cleanup();
        mojo.execute();

        File script = new File(FAKE_PROJECT_TARGET, "classes/assets/coffee/script.js");
        assertThat(script).isFile();
        assertThat(FileUtils.readFileToString(script))
                .contains("square = function(x) {")
                .contains("return \"Filling the \" + container + \" with \" + liquid + \"...\";");

        script = new File(FAKE_PROJECT_TARGET, "wisdom/assets/script.js");
        assertThat(script).isFile();
        assertThat(FileUtils.readFileToString(script))
                .contains("square = function(x) {")
                .contains("return \"Filling the \" + container + \" with \" + liquid + \"...\";");

        File map = new File(FAKE_PROJECT_TARGET, "wisdom/assets/script.js.map");
        assertThat(map).isFile();
    }

    @Test
    public void testWatching() throws MojoFailureException, MojoExecutionException, IOException, WatchingException, InterruptedException {
        cleanup();

        // Copy script to script2 (do not modify script as it is used by other tests).
        final File originalInternalScript = new File(FAKE_PROJECT, "src/main/resources/assets/coffee/script.coffee");
        final File newInternalScript = new File(FAKE_PROJECT, "src/main/resources/assets/coffee/script2.coffee");
        final File originalExternalScript = new File(FAKE_PROJECT, "src/main/assets/script.coffee");

        String originalScriptContent = FileUtils.readFileToString(originalInternalScript);
        FileUtils.copyFile(originalInternalScript, newInternalScript);

        mojo.execute();

        File script = new File(FAKE_PROJECT_TARGET, "classes/assets/coffee/script2.js");
        File map = new File(FAKE_PROJECT_TARGET, "classes/assets/coffee/script2.js.map");
        assertThat(script).isFile();
        assertThat(map).isFile();
        assertThat(FileUtils.readFileToString(script))
                .contains("square = function(x) {")
                .contains("return \"Filling the \" + container + \" with \" + liquid + \"...\";");

        File ext = new File(FAKE_PROJECT_TARGET, "wisdom/assets/script.js");
        assertThat(ext).isFile();
        assertThat(FileUtils.readFileToString(ext))
                .contains("square = function(x) {")
                .contains("return \"Filling the \" + container + \" with \" + liquid + \"...\";");

        // Delete script 2
        newInternalScript.delete();
        mojo.fileDeleted(newInternalScript);

        assertThat(new File(FAKE_PROJECT_TARGET, "classes/assets/coffee/script2.js")).doesNotExist();
        assertThat(new File(FAKE_PROJECT_TARGET, "classes/assets/coffee/script2.js.map")).doesNotExist();

        // Recreate the file with another name (same content)
        File newFile = new File(FAKE_PROJECT, "src/main/resources/assets/script3.coffee");
        FileUtils.write(newFile, originalScriptContent);
        mojo.fileCreated(newFile);
        File script3 = new File(FAKE_PROJECT_TARGET, "classes/assets/script3.js");
        assertThat(FileUtils.readFileToString(script3))
                .contains("square = function(x) {")
                .contains("return \"Filling the \" + container + \" with \" + liquid + \"...\";");

        // Update link
        long originalLastModified = ext.lastModified();
        FileUtils.touch(originalExternalScript);
        mojo.fileUpdated(originalExternalScript);
        // The file should have been updated
        assertThat(ext.lastModified()).isGreaterThanOrEqualTo(originalLastModified);
    }

    @Test
    public void testErrorDetection() throws IOException, MojoExecutionException {
        cleanup();
        // Execute also initialize the mojo
        mojo.execute();

        // Copy the broken file
        File broken = new File("src/test/resources/coffee/invalid.coffee");
        final File copy = new File(mojo.basedir, "src/main/resources/assets/invalid.coffee");
        FileUtils.copyFile(broken, copy);
        try {
            mojo.fileCreated(copy);
            fail("Watching Exception expected when compiling a broken CoffeeScript file");
        } catch (WatchingException e) {
            assertThat(e.getLine()).isEqualTo(3);
            assertThat(e.getFile().getAbsolutePath()).isEqualTo(copy.getAbsolutePath());
            assertThat(e.getCharacter()).isEqualTo(25);
            assertThat(e.getMessage()).contains("unexpected end of input");
        } finally {
            FileUtils.deleteQuietly(copy);
        }
    }

    private void cleanup() {
        FileUtils.deleteQuietly(mojo.buildDirectory);
    }


}
