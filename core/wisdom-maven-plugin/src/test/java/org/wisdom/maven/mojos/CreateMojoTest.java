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
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.wisdom.maven.mojos.CreateMojoTest.Child.child;
import static org.wisdom.maven.mojos.CreateMojoTest.Include.include;

/**
 * Checks the project creation.
 */
public class CreateMojoTest {

    public static final File WORK = new File("target/workbench/create");

    @Before
    public void setUp() {
        FileUtils.deleteQuietly(WORK);
    }

    @Test
    public void testDefaultExecute() throws Exception {
        CreateMojo mojo = new CreateMojo();
        mojo.artifactId = "create-test";
        mojo.groupId = "test-group";
        mojo.version = "1.0-SNAPSHOT";
        final String packageName = "org.acme";
        mojo.packageName = packageName;
        mojo.skel = "default";
        mojo.basedir = WORK;
        if (!mojo.basedir.exists()) {
            mojo.basedir.mkdirs();
        }

        mojo.execute();

        File root = new File(mojo.basedir, mojo.artifactId);
        assertThat(root).isDirectory();

        // Pom
        assertThat(new File(root, "pom.xml"))
                .exists()
                .has(include("<artifactId>" + mojo.artifactId + "</artifactId>"))
                .has(include("<groupId>" + mojo.groupId + "</groupId>"))
                .has(include("<version>" + mojo.version + "</version>"))
                .has(include("wisdom-api"))
                .has(include("wisdom-test"))
                .has(include("wisdom-api"))
                .has(include("wisdom-maven-plugin"));

        // Errors templates
        assertThat(new File(root, "src/main/templates/error"))
                .isDirectory()
                .has(child("404.thl.html"))
                .has(child("500.thl.html"));

        // Main template
        assertThat(new File(root, "src/main/resources/templates"))
                .isDirectory()
                .has(child("welcome.thl.html"));

        // Main asset
        assertThat(new File(root, "src/main/resources/assets"))
                .isDirectory()
                .has(child("main.less"));

        // Configuration
        assertThat(new File(root, "src/main/configuration/application.conf"))
                .isFile()
                .has(include("application {"))
                .has(include("secret = "));

        // Package and file
        assertThat(new File(root, "src/main/java/" + packageName.replace(".", "/"))).isDirectory()
                .has(child("WelcomeController.java"));
    }

    @Test
    public void testBlankExecute() throws Exception {
        CreateMojo mojo = new CreateMojo();
        mojo.artifactId = "create-test";
        mojo.groupId = "test-group";
        mojo.version = "1.0-SNAPSHOT";
        mojo.skel = "blank";
        final String packageName = "org.acme";
        mojo.packageName = packageName;
        mojo.basedir = WORK;
        if (!mojo.basedir.exists()) {
            mojo.basedir.mkdirs();
        }

        mojo.execute();

        File root = new File(mojo.basedir, mojo.artifactId);
        assertThat(root).isDirectory();

        // Pom
        assertThat(new File(root, "pom.xml"))
                .exists()
                .has(include("<artifactId>" + mojo.artifactId + "</artifactId>"))
                .has(include("<groupId>" + mojo.groupId + "</groupId>"))
                .has(include("<version>" + mojo.version + "</version>"))
                .has(include("wisdom-api"))
                .has(include("wisdom-test"))
                .has(include("wisdom-api"))
                .has(include("wisdom-maven-plugin"));

        // Errors templates
        assertThat(new File(root, "src/main/templates/error"))
                .isDirectory()
                .has(child("404.thl.html"))
                .has(child("500.thl.html"));

        // Main template
        assertThat(new File(root, "src/main/resources/templates"))
                .isDirectory()
                .doesNotHave(child("welcome.thl.html"));

        // Main asset
        assertThat(new File(root, "src/main/resources/assets"))
                .isDirectory()
                .doesNotHave(child("main.less"));

        // Configuration
        assertThat(new File(root, "src/main/configuration/application.conf"))
                .isFile()
                .has(include("secret ="));

        // Package and file
        assertThat(new File(root, "src/main/java/" + packageName.replace(".", "/"))).isDirectory()
                .doesNotHave(child("WelcomeController.java"));
    }


    public static class Child extends Condition<File> {

        public static Condition<File> child(String name) {
            return new Child(name);
        }

        private final String name;

        private Child(String name) {
            this.name = name;
        }

        @Override
        public boolean matches(File file) {
            return new File(file, name).isFile();
        }
    }

    public static class Include extends Condition<File> {

        public static Condition<File> include(String text) {
            return new Include(text);
        }

        private final String text;

        private Include(String name) {
            this.text = name;
        }

        @Override
        public boolean matches(File file) {
            try {
                return FileUtils.readFileToString(file).contains(text);
            } catch (IOException e) {
                return false;
            }
        }
    }

}
