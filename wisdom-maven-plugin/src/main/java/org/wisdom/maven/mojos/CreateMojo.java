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
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.wisdom.maven.Constants;
import org.wisdom.maven.utils.ApplicationSecretGenerator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * A Mojo to create a Wisdom Application.
 */
@Mojo(name = "create", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresDirectInvocation = true,
        requiresProject = false)
public class CreateMojo extends AbstractWisdomMojo {

    @Parameter(defaultValue = "quickstart")
    public String skel;
    @Parameter(required = true, defaultValue = "${artifactId}")
    public String artifactId;
    @Parameter(required = true, defaultValue = "${groupId}")
    public String groupId;
    @Parameter(required = true, defaultValue = "${version}")
    public String version;

    @Parameter(required = false, defaultValue = "${package}")
    public String packageName;

    private File sources;
    private File resources;
    private File configuration;
    private File root;
    private File packageDirectory;
    private File templates;
    private File assets;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            ensureNotExisting();
            createDirectories();
            createApplicationConfiguration();
            createPomFile();
            createPackageStructure();
            createDefaultController();
            createCSS();
            createWelcomeTemplate();
            copyDefaultErrorTemplates();
            printStartGuide();
        } catch (IOException e) {
            throw new MojoExecutionException("Error during project generation", e);
        }
    }

    private void printStartGuide() {
        getLog().info("You application is ready !");
        getLog().info("Wanna try it right away ?");
        getLog().info("\t cd " + artifactId);
        getLog().info("\t mvn clean wisdom:run");
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

    private void createCSS() throws IOException {
        File css = new File(assets, "main.less");
        InputStream is = CreateMojo.class.getClassLoader().getResourceAsStream("project/assets/main.less");
        FileUtils.copyInputStreamToFile(is, css);
        IOUtils.closeQuietly(is);
    }

    private void copyDefaultErrorTemplates() throws IOException {
        File templateDirectory = new File(root, Constants.TEMPLATES_SRC_DIR);
        File error = new File(templateDirectory, "error");
        error.mkdirs();

        // Copy 404
        InputStream is = CreateMojo.class.getClassLoader().getResourceAsStream("templates/error/404.thl.html");
        FileUtils.copyInputStreamToFile(is, new File(error, "404.thl.html"));
        IOUtils.closeQuietly(is);

        // Copy 500
        is = CreateMojo.class.getClassLoader().getResourceAsStream("templates/error/500.thl.html");
        FileUtils.copyInputStreamToFile(is, new File(error, "500.thl.html"));
        IOUtils.closeQuietly(is);
    }

    private void createPackageStructure() {
        String name = getPackageName();
        name = name.replace(".", "/").replace("", "");
        packageDirectory = new File(sources, name);
        packageDirectory.mkdirs();
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

    private String getPackageName() {
        if (packageName == null || packageName.isEmpty()) {
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
        root.mkdirs();
        sources = new File(root, Constants.MAIN_SRC_DIR);
        resources = new File(root, Constants.MAIN_RESOURCES_DIR);
        sources.mkdirs();
        resources.mkdirs();

        assets = new File(resources, "assets");
        assets.mkdirs();

        templates = new File(resources, "templates");
        templates.mkdirs();

        new File(root, Constants.TEST_SRC_DIR).mkdirs();
        new File(root, Constants.TEST_RESOURCES_DIR).mkdirs();

        new File(root, Constants.ASSETS_SRC_DIR).mkdirs();
        new File(root, Constants.TEMPLATES_SRC_DIR).mkdirs();
        configuration = new File(root, Constants.CONFIGURATION_SRC_DIR);
        configuration.mkdirs();
    }

    private void ensureNotExisting() throws MojoExecutionException {
        File file = new File(basedir, artifactId);
        if (file.exists()) {
            throw new MojoExecutionException("Cannot create the Wisdom application - " + artifactId + " already " +
                    "exist");
        }
    }
}
