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
import org.ow2.chameleon.wisdom.maven.utils.WatcherUtils;

import java.io.File;
import java.io.IOException;

/**
 * A mojo responsible for copying the templates from `src/main/templates` to `wisdom/templates`.
 */
@Mojo(name = "copy-templates", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class CopyExternalTemplateMojo extends AbstractWisdomWatcherMojo implements Constants {

    private File source;
    private File destination;

    @Component
    private MavenResourcesFiltering filtering;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        source = new File(basedir, TEMPLATES_SRC_DIR);
        destination = new File(getWisdomRootDirectory(), TEMPLATES_DIR);

        try {
            ResourceCopy.copyTemplates(this, filtering);
        } catch (IOException e) {
            throw new MojoExecutionException("Error during template copy", e);
        }
    }

    @Override
    public boolean accept(File file) {
        return WatcherUtils.isInDirectory(file, WatcherUtils.getExternalTemplateSource(basedir));
    }

    @Override
    public boolean fileCreated(File file) throws WatchingException {
        try {
            ResourceCopy.copyFileToDir(file, source, destination, this, filtering);
        } catch (IOException e) {
            throw new WatchingException(e.getMessage(), file, e);
        }
        getLog().info(file.getName() + " copied to the template directory");
        return false;
    }

    @Override
    public boolean fileUpdated(File file) throws WatchingException {
        try {
            ResourceCopy.copyFileToDir(file, source, destination, this, filtering);
        } catch (IOException e) {
            throw new WatchingException(e.getMessage(), file, e);
        }
        getLog().info(file.getName() + " updated in the template directory");
        return false;
    }

    @Override
    public boolean fileDeleted(File file) throws WatchingException {
        File copied = ResourceCopy.computeRelativeFile(file, source, destination);
        if (copied.exists()) {
            copied.delete();
        }
        getLog().info(copied.getName() + " deleted");
        return false;
    }

}
