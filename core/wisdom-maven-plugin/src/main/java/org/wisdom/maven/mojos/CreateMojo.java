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

import com.google.common.base.Strings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.wisdom.maven.Constants;
import org.wisdom.maven.utils.ApplicationSecretGenerator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * This mojo is used to generate the skeleton of Wisdom Applications.
 */
@Mojo(name = "create", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresDirectInvocation = true,
        requiresProject = false)
public class CreateMojo extends AbstractWisdomMojo {

    /**
     * The name of the skeleton. By default it use quickstart. 'blank' can be used to create an empty project
     * (without additional applications and the controller).
     */
    @Parameter(defaultValue = "quickstart")
    public String skel;
    /**
     * The artifact Id of the generated project.
     */
    @Parameter(required = true, defaultValue = "${artifactId}")
    public String artifactId;
    /**
     * The group Id of the generated project.
     */
    @Parameter(required = true, defaultValue = "${groupId}")
    public String groupId;
    /**
     * The version of the generated project.
     */
    @Parameter(required = true, defaultValue = "${version}")
    public String version;

    /**
     * The root package of the generated project.
     */
    @Parameter(required = false, defaultValue = "${package}")
    public String packageName;

    private File sources;
    private File test;
    private File configuration;
    private File root;
    private File packageDirectory;
    private File packageDirectoryForTest;
    private File templates;
    private File assets;

    /**
     * Generates the project structure.
     * If a directory with the 'artifactId\ name already exist, nothing is generated as we don't want to overridde
     * anything.
     *
     * @throws MojoExecutionException
     */
    @Override
    public void execute() throws MojoExecutionException {
        try {
            ensureNotExisting();
            createDirectories();

            if ("blank".equalsIgnoreCase(skel)) {
                createApplicationConfiguration();
                createBlankPomFile();
                createPackageStructure();
                copyDefaultErrorTemplates();
            } else {
                createApplicationConfiguration();
                createPomFile();
                createPackageStructure();
                createDefaultController();
                createTests();
                copyAssets();
                createWelcomeTemplate();
                copyDefaultErrorTemplates();
            }

            printStartGuide();
        } catch (IOException e) {
            throw new MojoExecutionException("Error during project generation", e);
        }
    }

    private void printStartGuide() {
        getLog().info("You application is ready !");
        getLog().info("Wanna try it right away ?");
        getLog().info("\t cd " + artifactId);
        getLog().info("\t mvn wisdom:run");
        getLog().info("That's all !");
    }

    private void createDefaultController() throws IOException {
        File ctrl = new File(packageDirectory, "WelcomeController.java");
        InputStream is = CreateMojo.class.getClassLoader().getResourceAsStream("project/controller/sample/WelcomeController.java");
        String content = IOUtils.toString(is);
        IOUtils.closeQuietly(is);
        content = content.replace("package sample;", "package " + getPackageName() + ";");

        FileUtils.writeStringToFile(ctrl, content);
    }

    private void createTests() throws IOException {
        File testCase = new File(packageDirectoryForTest, "UnitTest.java");
        InputStream is = CreateMojo.class.getClassLoader().getResourceAsStream
                ("project/tests/UnitTest.java");
        String content = IOUtils.toString(is);
        IOUtils.closeQuietly(is);
        content = content.replace("package sample;", "package " + getPackageName() + ";");
        FileUtils.writeStringToFile(testCase, content);

        testCase = new File(packageDirectoryForTest, "InContainerIT.java");
        is = CreateMojo.class.getClassLoader().getResourceAsStream
                ("project/tests/InContainerIT.java");
        content = IOUtils.toString(is);
        IOUtils.closeQuietly(is);
        content = content.replace("package sample;", "package " + getPackageName() + ";");
        FileUtils.writeStringToFile(testCase, content);

        testCase = new File(packageDirectoryForTest, "BlackBoxIT.java");
        is = CreateMojo.class.getClassLoader().getResourceAsStream
                ("project/tests/BlackBoxIT.java");
        content = IOUtils.toString(is);
        IOUtils.closeQuietly(is);
        content = content.replace("package sample;", "package " + getPackageName() + ";");
        FileUtils.writeStringToFile(testCase, content);

        testCase = new File(packageDirectoryForTest, "FluentLeniumIT.java");
        is = CreateMojo.class.getClassLoader().getResourceAsStream
                ("project/tests/FluentLeniumIT.java");
        content = IOUtils.toString(is);
        IOUtils.closeQuietly(is);
        content = content.replace("package sample;", "package " + getPackageName() + ";");
        FileUtils.writeStringToFile(testCase, content);
    }

    private void createWelcomeTemplate() throws IOException {
        File template = new File(templates, "welcome.thl.html");
        InputStream is = CreateMojo.class.getClassLoader().getResourceAsStream("project/templates/welcome.thl.html");
        String content = IOUtils.toString(is);
        IOUtils.closeQuietly(is);
        content = content.replace("@@group_id@@", "${project.groupId}")
                .replace("@@artifact_id@@", "${project.artifactId}")
                .replace("@@version@@", "${project.version}")
                .replace("@@package_name@@", getPackageName());
        FileUtils.writeStringToFile(template, content);
    }

    private void copyAssets() throws IOException {
        File css = new File(assets, "main.less");
        InputStream is = CreateMojo.class.getClassLoader().getResourceAsStream("project/assets/main.less");
        FileUtils.copyInputStreamToFile(is, css);
        IOUtils.closeQuietly(is);

        File favico = new File(assets, "owl-small.png");
        is = CreateMojo.class.getClassLoader().getResourceAsStream("project/assets/owl-small.png");
        FileUtils.copyInputStreamToFile(is, favico);
        IOUtils.closeQuietly(is);
    }

    private void copyDefaultErrorTemplates() throws IOException {
        File templateDirectory = new File(root, Constants.TEMPLATES_SRC_DIR);
        File error = new File(templateDirectory, "error");
        if (error.mkdirs()) {
            getLog().debug(error.getAbsolutePath() + " directory created");
        }

        // Copy 404
        InputStream is = CreateMojo.class.getClassLoader().getResourceAsStream("templates/error/404.thl.html");
        FileUtils.copyInputStreamToFile(is, new File(error, "404.thl.html"));
        IOUtils.closeQuietly(is);

        // Copy 500
        is = CreateMojo.class.getClassLoader().getResourceAsStream("templates/error/500.thl.html");
        FileUtils.copyInputStreamToFile(is, new File(error, "500.thl.html"));
        IOUtils.closeQuietly(is);

        // Copy pipeline
        is = CreateMojo.class.getClassLoader().getResourceAsStream("templates/error/pipeline.thl.html");
        FileUtils.copyInputStreamToFile(is, new File(error, "pipeline.thl.html"));
        IOUtils.closeQuietly(is);
    }

    private void createPackageStructure() {
        String name = getPackageName();
        name = name.replace(".", "/");
        packageDirectory = new File(sources, name);
        packageDirectoryForTest = new File(test, name);
        if (packageDirectory.mkdirs()) {
            getLog().debug(packageDirectory.getAbsolutePath() + " directory created");
        }
        if (packageDirectoryForTest.mkdirs()) {
            getLog().debug(packageDirectoryForTest.getAbsolutePath() + " directory created");
        }
    }


    private void createPomFile() throws IOException {
        File pom = new File(root, "pom.xml");
        InputStream is = CreateMojo.class.getClassLoader().getResourceAsStream("project/pom/quickstart-pom.xml");
        String content = IOUtils.toString(is);
        IOUtils.closeQuietly(is);
        content = content.replace("@@group_id@@", groupId)
                .replace("@@artifact_id@@", artifactId)
                .replace("@@version@@", version)
                .replace("@@package_name@@", getPackageName());

        FileUtils.writeStringToFile(pom, content);
    }

    private void createBlankPomFile() throws IOException {
        File pom = new File(root, "pom.xml");
        InputStream is = CreateMojo.class.getClassLoader().getResourceAsStream("project/pom/blank-pom.xml");
        String content = IOUtils.toString(is);
        IOUtils.closeQuietly(is);
        content = content.replace("@@group_id@@", groupId)
                .replace("@@artifact_id@@", artifactId)
                .replace("@@version@@", version)
                .replace("@@package_name@@", getPackageName());

        FileUtils.writeStringToFile(pom, content);
    }

    private String getPackageName() {
        if (Strings.isNullOrEmpty(packageName)) {
            return "sample";
        } else {
            return packageName;
        }
    }

    private void createApplicationConfiguration() throws IOException {
        File application = new File(configuration, "application.conf");
        InputStream is = CreateMojo.class.getClassLoader().getResourceAsStream("configuration/application.conf");
        String content = IOUtils.toString(is);
        IOUtils.closeQuietly(is);
        content = content.replace("@@application.secret@@", ApplicationSecretGenerator.generate());
        FileUtils.writeStringToFile(application, content);
    }

    private void createDirectories() {
        root = new File(basedir, artifactId);
        if (root.mkdirs()) {
            getLog().debug(root.getAbsolutePath() + " directory created.");
        }
        sources = new File(root, Constants.MAIN_SRC_DIR);
        if (sources.mkdirs()) {
            getLog().debug(sources.getAbsolutePath() + " directory created");
        }
        test = new File(root, Constants.TEST_SRC_DIR);
        if (test.mkdirs()) {
            getLog().debug(test.getAbsolutePath() + " directory created");
        }
        File resources = new File(root, Constants.MAIN_RESOURCES_DIR);
        if (resources.mkdirs()) {
            getLog().debug(resources.getAbsolutePath() + " directory created");
        }

        assets = new File(resources, "assets");
        if (assets.mkdirs()) {
            getLog().debug(assets.getAbsolutePath() + " directory created");
        }

        templates = new File(resources, "templates");
        if (templates.mkdirs()) {
            getLog().debug(templates.getAbsolutePath() + " directory created");
        }

        File file = new File(root, Constants.TEST_SRC_DIR);
        if (file.mkdirs()) {
            getLog().debug(file.getAbsolutePath() + " directory created");
        }
        file = new File(root, Constants.TEST_RESOURCES_DIR);
        if (file.mkdirs()) {
            getLog().debug(file.getAbsolutePath() + " directory created");
        }

        file = new File(root, Constants.ASSETS_SRC_DIR);
        if (file.mkdirs()) {
            getLog().debug(file.getAbsolutePath() + " directory created");
        }
        file = new File(root, Constants.TEMPLATES_SRC_DIR);
        if (file.mkdirs()) {
            getLog().debug(file.getAbsolutePath() + " directory created");
        }
        configuration = new File(root, Constants.CONFIGURATION_SRC_DIR);
        if (configuration.mkdirs()) {
            getLog().debug(configuration.getAbsolutePath() + " directory created");
        }

    }

    private void ensureNotExisting() throws MojoExecutionException {
        File file = new File(basedir, artifactId);
        if (file.exists()) {
            throw new MojoExecutionException("Cannot create the Wisdom application - " + artifactId + " already " +
                    "exist");
        }
    }
}
