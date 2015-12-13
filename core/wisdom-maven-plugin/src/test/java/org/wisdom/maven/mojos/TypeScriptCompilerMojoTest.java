/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.assertj.core.api.Fail.fail;


/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */

/**
 * Checks the TypeScript Mojo.
 */
public class TypeScriptCompilerMojoTest {

    public static final String FAKE_PROJECT = "target/test-classes/fake-project";
    public static final String FAKE_PROJECT_TARGET = "target/test-classes/fake-project/target";
    File nodeDirectory;
    private TypeScriptCompilerMojo mojo;


    @Before
    public void setUp() throws IOException {
        nodeDirectory = new File("target/test/node");
        nodeDirectory.mkdirs();
        Log log = new SystemStreamLog();
        mojo = new TypeScriptCompilerMojo();
        mojo.basedir = new File(FAKE_PROJECT);
        mojo.buildDirectory = new File(FAKE_PROJECT_TARGET);
        mojo.buildDirectory.mkdirs();

        mojo.typescript = new TypeScript();

        NodeManager manager = new NodeManager(log, nodeDirectory, mojo);
        manager.installIfNotInstalled();
    }

    @Test
    public void testProcessingOfTypeScriptFiles() throws MojoFailureException, MojoExecutionException, IOException {
        cleanup();
        mojo.execute();

        File js = new File(FAKE_PROJECT_TARGET, "classes/assets/ts/Animal.js");
        File decl = new File(FAKE_PROJECT_TARGET, "classes/assets/ts/Animal.d.ts");
        File map = new File(FAKE_PROJECT_TARGET, "classes/assets/ts/Animal.js.map");
        assertThat(js).isFile();
        assertThat(decl).isFile();
        assertThat(map).isFile();


        js = new File(FAKE_PROJECT_TARGET, "wisdom/assets/ts/raytracer.js");
        decl = new File(FAKE_PROJECT_TARGET, "wisdom/assets/ts/raytracer.d.ts");
        map = new File(FAKE_PROJECT_TARGET, "wisdom/assets/ts/raytracer.js.map");
        assertThat(js).isFile();
        assertThat(decl).isFile();
        assertThat(map).isFile();
    }

    @Test
    public void testProcessingOfTypeScriptWithModuleSetToCommonJS() throws MojoFailureException, MojoExecutionException, IOException {
        cleanup();

        // Copy math app
        File dir = new File(FAKE_PROJECT, "src/main/resources/assets/math");
        FileUtils.copyDirectory(new File("src/test/resources/typescript/math"), dir);

        mojo.typescript.setModule("commonjs");
        mojo.execute();

        File js = new File(FAKE_PROJECT_TARGET, "classes/assets/ts/Animal.js");
        File decl = new File(FAKE_PROJECT_TARGET, "classes/assets/ts/Animal.d.ts");
        File map = new File(FAKE_PROJECT_TARGET, "classes/assets/ts/Animal.js.map");
        assertThat(js).isFile();
        assertThat(decl).isFile();
        assertThat(map).isFile();


        js = new File(FAKE_PROJECT_TARGET, "wisdom/assets/ts/raytracer.js");
        decl = new File(FAKE_PROJECT_TARGET, "wisdom/assets/ts/raytracer.d.ts");
        map = new File(FAKE_PROJECT_TARGET, "wisdom/assets/ts/raytracer.js.map");
        assertThat(js).isFile();
        assertThat(decl).isFile();
        assertThat(map).isFile();

        // Check the math app
        js = new File(FAKE_PROJECT_TARGET, "classes/assets/math/demos.js");
        decl = new File(FAKE_PROJECT_TARGET, "classes/assets/math/interfaces.js");
        File impl = new File(FAKE_PROJECT_TARGET, "classes/assets/math/impl/math_demo.js");
        assertThat(js).isFile();
        assertThat(decl).doesNotExist(); // ts.d are not compiled.
        assertThat(impl).isFile();

        FileUtils.deleteQuietly(dir);
    }

    @Test
    public void testProcessingOfTypeScriptFilesWithDifferentConfiguration() throws MojoFailureException,
            MojoExecutionException, IOException {
        mojo.typescript
                .setRemoveComments(false)
                .setGenerateDeclaration(false)
                .setModule("amd")
                .setGenerateMap(false)
                .setNoImplicitAny(false);
        cleanup();
        mojo.execute();

        File js = new File(FAKE_PROJECT_TARGET, "classes/assets/ts/Animal.js");
        File decl = new File(FAKE_PROJECT_TARGET, "classes/assets/ts/Animal.d.ts");
        File map = new File(FAKE_PROJECT_TARGET, "classes/assets/ts/Animal.js.map");
        assertThat(js).isFile();
        assertThat(decl).doesNotExist();
        assertThat(map).doesNotExist();


        js = new File(FAKE_PROJECT_TARGET, "wisdom/assets/ts/raytracer.js");
        decl = new File(FAKE_PROJECT_TARGET, "wisdom/assets/ts/raytracer.d.ts");
        map = new File(FAKE_PROJECT_TARGET, "wisdom/assets/ts/raytracer.js.map");
        assertThat(js).isFile();
        assertThat(decl).doesNotExist();
        assertThat(map).doesNotExist();
    }

    @Test
    public void testTargetArgument() throws MojoFailureException,
            MojoExecutionException, IOException {
        mojo.typescript
                .setRemoveComments(false)
                .setGenerateDeclaration(false)
                .setGenerateMap(false)
                .setNoImplicitAny(false)
                .setTarget("ES6");
        cleanup();
        mojo.execute();

        File js = new File(FAKE_PROJECT_TARGET, "classes/assets/ts/Animal.js");
        File decl = new File(FAKE_PROJECT_TARGET, "classes/assets/ts/Animal.d.ts");
        File map = new File(FAKE_PROJECT_TARGET, "classes/assets/ts/Animal.js.map");
        assertThat(js).isFile();
        assertThat(decl).doesNotExist();
        assertThat(map).doesNotExist();


        js = new File(FAKE_PROJECT_TARGET, "wisdom/assets/ts/raytracer.js");
        decl = new File(FAKE_PROJECT_TARGET, "wisdom/assets/ts/raytracer.d.ts");
        map = new File(FAKE_PROJECT_TARGET, "wisdom/assets/ts/raytracer.js.map");
        assertThat(js).isFile();
        assertThat(decl).doesNotExist();
        assertThat(map).doesNotExist();
    }

    @Test
    public void testWatching() throws MojoFailureException, MojoExecutionException, IOException, WatchingException, InterruptedException {
        cleanup();

        // Copy animal to animal (do not modify var as it is used by other tests).
        final File originalAnimal = new File(FAKE_PROJECT, "src/main/resources/assets/ts/Animal.ts");
        final File newAnimal = new File(FAKE_PROJECT, "src/main/resources/assets/ts/Animal2.ts");
        final File originalRT = new File(FAKE_PROJECT, "src/main/assets/assets/ts/raytracer.ts");
        String originalAnimalContent = FileUtils.readFileToString(originalAnimal);
        String newContent = originalAnimalContent.replace("Animal", "Animal2")
                .replace("Snake", "Snake2")
                .replace("Horse", "Horse2")
                .replace("tom", "tom2")
                .replace("sam", "sam2");
        FileUtils.write(newAnimal, newContent);

        mojo.execute();

        final File anim = new File(FAKE_PROJECT_TARGET, "classes/assets/ts/Animal2.js");
        assertThat(anim).isFile();
        String content = FileUtils.readFileToString(anim);
        assertThat(content).contains("var sam2 = new Snake2(\"Sammy the Python\");");

        final File rt = new File(FAKE_PROJECT_TARGET, "wisdom/assets/ts/raytracer.js");
        assertThat(rt).isFile();
        content = FileUtils.readFileToString(rt);
        assertThat(content).contains("new Plane(new Vector(0.0, 1.0, 0.0), 0.0, Surfaces.checkerboard),");

        // Delete new animal
        newAnimal.delete();
        mojo.fileDeleted(newAnimal);

        assertThat(anim.isFile()).isFalse();
        // Check that the .d.ts and the .js.map are deleted too
        assertThat(new File(FAKE_PROJECT_TARGET, "classes/ts/Animal2.d.ts")).doesNotExist();
        assertThat(new File(FAKE_PROJECT_TARGET, "classes/ts/Animal2.js.map")).doesNotExist();

        // Recreate the file with another name (same content)
        File newFile = new File(FAKE_PROJECT, "src/main/resources/Animal3.ts");
        newContent = originalAnimalContent.replace("Animal", "Animal3")
                .replace("Snake", "Snake3")
                .replace("Horse", "Horse3")
                .replace("tom", "tom3")
                .replace("sam", "sam3");
        FileUtils.write(newFile, newContent);
        mojo.fileCreated(newFile);
        File var3 = new File(FAKE_PROJECT_TARGET, "classes/Animal3.js");
        assertThat(var3).isFile();
        content = FileUtils.readFileToString(var3);
        assertThat(content).contains("var sam3 = new Snake3(\"Sammy the Python\");");

        // Update link
        long originalLastModified = rt.lastModified();
        FileUtils.touch(originalRT);
        mojo.fileUpdated(originalRT);
        // The file should have been updated
        assertThat(rt.lastModified()).isGreaterThanOrEqualTo(originalLastModified);
    }

    @Test
    public void testProcessingOfInvalidTypescriptsFile() throws MojoFailureException, MojoExecutionException, IOException {
        cleanup();
        mojo.execute();
        try {
            mojo.processDirectory(new File("src/test/resources/typescript/invalid"), new File("target/junk"));
            fail("Compilation issue expected");
        } catch (WatchingException e) {
            assertThat(e.getLine()).isEqualTo(3);
            assertThat(e.getCharacter()).isEqualTo(16);
            assertThat(e.getMessage()).contains("Cannot find name 'MathDemo'");
            assertThat(e.getFile().getAbsolutePath()).endsWith("invalid.ts");
        }
    }

    private void cleanup() {
        FileUtils.deleteQuietly(mojo.buildDirectory);
    }
}