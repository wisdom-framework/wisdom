package org.ow2.chameleon.wisdom.maven.mojos;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.ow2.chameleon.wisdom.maven.Constants;
import org.ow2.chameleon.wisdom.maven.WatchingException;
import org.ow2.chameleon.wisdom.maven.utils.ResourceCopy;

import java.io.File;
import java.io.IOException;

/**
 * A mojo responsible for copying the assets from `src/main/assets` to `wisdom/assets`.
 */
@Mojo(name = "copy-assets", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class CopyExternalAssetMojo extends AbstractWisdomWatcherMojo implements Constants {

    private File source;
    private File destination;

    @Component
    private MavenResourcesFiltering filtering;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        source = new File(basedir, ASSETS_SRC_DIR);
        destination = new File(getWisdomRootDirectory(), ASSETS_DIR);

        try {
            ResourceCopy.copyExternalAssets(this, filtering);
        } catch (IOException e) {
            throw new MojoExecutionException("Error during asset copy", e);
        }
    }

    @Override
    public boolean accept(File file) {
        return file.getAbsolutePath().contains(ASSETS_DIR);
    }

    @Override
    public boolean fileCreated(File file) throws WatchingException {
        try {
            ResourceCopy.copyFileToDir(file, source, destination, this, filtering);
        } catch (IOException e) {
            throw new WatchingException(e.getMessage(), file, e);
        }
        getLog().info(file.getName() + " copied to the asset directory");
        return true;
    }

    @Override
    public boolean fileUpdated(File file) throws WatchingException {
        try {
            ResourceCopy.copyFileToDir(file, source, destination, this, filtering);
        } catch (IOException e) {
            throw new WatchingException(e.getMessage(), file, e);
        }
        getLog().info(file.getName() + " updated in the asset directory");
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
