package org.ow2.chameleon.wisdom.maven.processors;

import org.ow2.chameleon.wisdom.maven.mojos.AbstractWisdomMojo;
import org.ow2.chameleon.wisdom.maven.utils.ResourceCopy;

import java.io.File;
import java.io.IOException;

/**
 * A processor responsible for copying the templates from `src/main/templates` to `wisdom/templates`.
 */
public class CopyExternalTemplateProcessor implements Processor {
    private AbstractWisdomMojo mojo;

    private File source;
    private File destination;

    @Override
    public void configure(AbstractWisdomMojo mojo) {
        this.mojo = mojo;
        source = new File(mojo.basedir, TEMPLATES_SRC_DIR);
        destination = new File(mojo.getWisdomRootDirectory(), TEMPLATES_DIR);
    }

    /**
     * Copies all templates.
     * @throws ProcessorException
     */
    @Override
    public void processAll() throws ProcessorException {
        try {
            ResourceCopy.copyTemplates(mojo);
        } catch (IOException e) {
            throw new ProcessorException("Error during template copy", e);
        }
    }

    @Override
    public void tearDown() {
        // Nothing to do.
    }

    @Override
    public boolean accept(File file) {
        return file.getAbsolutePath().contains(TEMPLATES_SRC_DIR);
    }

    @Override
    public boolean fileCreated(File file) throws ProcessorException {
        ResourceCopy.copyFileToDir(file, source, destination);
        mojo.getLog().info(file.getName() + " copied to the template directory");
        return false;
    }

    @Override
    public boolean fileUpdated(File file) throws ProcessorException {
        ResourceCopy.copyFileToDir(file, source, destination);
        mojo.getLog().info(file.getName() + " updated in the template directory");
        return false;
    }

    @Override
    public boolean fileDeleted(File file) throws ProcessorException {
        File copied = ResourceCopy.computeRelativeFile(file, source, destination);
        if (copied.exists()) {
            copied.delete();
        }
        mojo.getLog().info(copied.getName() + " deleted");
        return false;
    }
}
