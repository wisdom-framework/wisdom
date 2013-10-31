package org.ow2.chameleon.wisdom.maven.mojos;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.ow2.chameleon.wisdom.maven.Constants;
import org.ow2.chameleon.wisdom.maven.utils.ApplicationSecretGenerator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        ensureNotExisting();
        createDirectories();
        try {
            createApplicationConfiguration();
            createPomFile();
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot create files", e);
        }
    }


    private void createPomFile() throws IOException {
        File pom = new File(root, "pom.xml");
        InputStream is = CreateMojo.class.getClassLoader().getResourceAsStream("pom/quickstart-pom.xml");
        String content = IOUtils.toString(is);
        IOUtils.closeQuietly(is);
        content = content.replace("@@group_id@@", groupId)
            .replace("@@artifact_id@@", artifactId)
            .replace("@@version@@", version)
            .replace("@@package_name@@", getPackageName());

        FileUtils.writeStringToFile(pom, content);
    }

    private String getPackageName() {
        if (packageName == null  || packageName.isEmpty()) {
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

        new File(resources, "assets").mkdirs();
        new File(resources, "templates").mkdirs();

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
