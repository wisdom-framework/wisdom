package org.ow2.chameleon.wisdom.maven.mojos;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.ow2.chameleon.wisdom.maven.Constants;
import org.ow2.chameleon.wisdom.maven.WatchingException;
import org.ow2.chameleon.wisdom.maven.utils.WatcherUtils;

import java.io.File;
import java.io.IOException;

/**
 * A watcher appended to the watch pipeline copying the generated bundle to the 'wisdom/application' folder.
 * Even if its usage during the Maven lifecycle is not be important, it prepares the Wisdom execution.
 */
@Mojo(name = "deploy-application", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.PACKAGE)
public class BundleDeployerMojo extends AbstractWisdomWatcherMojo implements Constants {


    @Override
    public void execute() throws MojoExecutionException {
        File file = new File(buildDirectory, project.getArtifactId() + "-" + project.getVersion() + ".jar");
        File destination = new File(getWisdomRootDirectory(), "application/" + file.getName());

        if (!file.isFile()) {
            throw new MojoExecutionException("Cannot find the artifact file " + file.getName());
        }

        try {
            FileUtils.copyFile(file, destination);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot copy the artifact file to the Wisdom application directory", e);
        }
    }

    @Override
    public boolean accept(File file) {
        return WatcherUtils.isInDirectory(file, WatcherUtils.getJavaDestination(basedir));
    }

    @Override
    public boolean fileCreated(File file) throws WatchingException {
        try {
            execute();
            return true;
        } catch (MojoExecutionException e) {
            throw new WatchingException(e.getMessage(), file, e);
        }
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
