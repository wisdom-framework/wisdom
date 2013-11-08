package org.ow2.chameleon.wisdom.maven.mojos;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.ow2.chameleon.wisdom.maven.utils.BundlePackagerExecutor;
import org.ow2.chameleon.wisdom.maven.utils.PlexusLoggerWrapper;

import java.io.File;
import java.io.IOException;

/**
 * Mojo packaging the application.
 */
@Mojo(name = "package", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.PACKAGE)
public class PackageMojo extends AbstractWisdomMojo {

    private final BundlePackagerExecutor packager = new BundlePackagerExecutor();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            createApplicationBundle();
            createApplicationDistribution();
        } catch (Exception e) {
            throw new MojoExecutionException("Cannot package the Wisdom application", e);
        }
    }

    private void createApplicationBundle() throws Exception {
        File finalFile = new File(this.buildDirectory, this.project.getArtifactId() + "-" + this.project
                .getVersion() + ".jar");
        packager.execute(this, finalFile);

        // Declare the bundle as main project artifact.
        Artifact mainArtifact = project.getArtifact();
        mainArtifact.setFile(finalFile);

        // Copy the build file to the application directory.
        FileUtils.copyFileToDirectory(finalFile, new File(getWisdomRootDirectory(), "application"), true);
    }

    private void createApplicationDistribution() throws IOException {
        File distFile = new File(this.buildDirectory, this.project.getArtifactId() + "-" + this.project
                .getVersion() + ".zip");
        ZipArchiver archiver = new ZipArchiver();
        archiver.enableLogging(new PlexusLoggerWrapper(getLog()));
        archiver.addDirectory(getWisdomRootDirectory());
        archiver.setDestFile(distFile);
        archiver.createArchive();

        projectHelper.attachArtifact(project, "zip", distFile);
    }
}
