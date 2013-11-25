package org.ow2.chameleon.wisdom.maven.mojos;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.ow2.chameleon.wisdom.maven.Constants;
import org.ow2.chameleon.wisdom.maven.WatchingException;
import org.ow2.chameleon.wisdom.maven.mojos.AbstractWisdomMojo;
import org.ow2.chameleon.wisdom.maven.mojos.AbstractWisdomWatcherMojo;
import org.ow2.chameleon.wisdom.maven.utils.BundlePackagerExecutor;
import org.ow2.chameleon.wisdom.maven.utils.PlexusLoggerWrapper;
import org.ow2.chameleon.wisdom.maven.utils.WatcherUtils;

import java.io.File;
import java.io.IOException;

/**
 * The mojo packaging the wisdom application.
 */
@Mojo(name = "package", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.PACKAGE)
public class BundlePackagerMojo extends AbstractWisdomWatcherMojo implements Constants {

    @Override
    public void execute() throws MojoExecutionException {
        try {
            createApplicationBundle();
            createApplicationDistribution();
        } catch (Exception e) {
            throw new MojoExecutionException("Cannot build wisdom application", e);
        }
    }

    private void createApplicationBundle() throws Exception {
        File finalFile = new File(this.buildDirectory, this.project.getArtifactId() + "-" + this.project
                .getVersion() + ".jar");
        BundlePackagerExecutor.execute(this, finalFile);

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

    @Override
    public boolean accept(File file) {
        return WatcherUtils.isInDirectory(file, WatcherUtils.getJavaSource(basedir))
                || WatcherUtils.isInDirectory(file, WatcherUtils.getResources(basedir));
    }

    @Override
    public boolean fileCreated(File file) throws WatchingException {
        try {
            createApplicationBundle();
        } catch (Exception e) {
            throw new WatchingException(e.getMessage(), file, e);
        }
        return true;
    }

    @Override
    public boolean fileUpdated(File file) throws WatchingException {
        return fileCreated(file);
    }

    @Override
    public boolean fileDeleted(File file) throws WatchingException {
        return fileCreated(file);
    }
}
