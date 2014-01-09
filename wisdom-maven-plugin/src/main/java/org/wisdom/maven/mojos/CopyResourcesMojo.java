package org.wisdom.maven.mojos;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.wisdom.maven.Constants;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.utils.ResourceCopy;
import org.wisdom.maven.utils.WatcherUtils;

import java.io.File;
import java.io.IOException;

/**
 * A mojo responsible for copying the resources from `src/main/resources` to `target/classes`.
 */
@Mojo(name = "copy-resources", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class CopyResourcesMojo extends AbstractWisdomWatcherMojo implements Constants {

    @Component
    private MavenResourcesFiltering filtering;

    private File source;
    private File destination;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        source = new File(basedir, MAIN_RESOURCES_DIR);
        destination = new File(buildDirectory, "classes");

        try {
            ResourceCopy.copyInternalResources(this, filtering);
        } catch (IOException e) {
            throw new MojoExecutionException("Error during asset copy", e);
        }
    }

    @Override
    public boolean accept(File file) {
        return WatcherUtils.isInDirectory(file, WatcherUtils.getResources(basedir));
    }

    @Override
    public boolean fileCreated(File file) throws WatchingException {
        try {
            ResourceCopy.copyFileToDir(file, source, destination, this, filtering);
            getLog().info(file.getName() + " copied to the target/classes directory");
        } catch (IOException e) {
            throw new WatchingException(e.getMessage(), file, e);
        }
        return true;
    }

    @Override
    public boolean fileUpdated(File file) throws WatchingException {
        try {
            ResourceCopy.copyFileToDir(file, source, destination, this, filtering);
            getLog().info(file.getName() + " copied to the target/classes directory");
        } catch (IOException e) {
            throw new WatchingException(e.getMessage(), file, e);
        }
        return true;
    }

    @Override
    public boolean fileDeleted(File file) throws WatchingException {
        File copied = ResourceCopy.computeRelativeFile(file, source, destination);
        if (copied.exists()) {
            copied.delete();
        }
        getLog().info(copied.getName() + " deleted");
        return true;
    }

}
