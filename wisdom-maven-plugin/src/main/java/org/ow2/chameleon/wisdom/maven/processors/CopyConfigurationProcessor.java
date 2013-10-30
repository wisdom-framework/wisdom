package org.ow2.chameleon.wisdom.maven.processors;

import org.ow2.chameleon.wisdom.maven.mojos.AbstractWisdomMojo;
import org.ow2.chameleon.wisdom.maven.utils.ResourceCopy;

import java.io.File;
import java.io.IOException;

/**
 * A processor responsible for copying the templates from `src/main/configuration` to `wisdom/conf`.
 */
public class CopyConfigurationProcessor implements Processor {
    private AbstractWisdomMojo mojo;

    private File source;
    private File destination;

    @Override
    public void configure(AbstractWisdomMojo mojo) {
        this.mojo = mojo;
        source = new File(mojo.basedir, CONFIGURATION_SRC_DIR);
        destination = new File(mojo.getWisdomRootDirectory(), CONFIGURATION_DIR);
    }

    /**
     * Copies all configurations.
     * @throws ProcessorException
     */
    @Override
    public void processAll() throws ProcessorException {
        try {
            ResourceCopy.copyConfiguration(mojo);
        } catch (IOException e) {
            throw new ProcessorException("Error during configuration copy", e);
        }
    }

    @Override
    public void tearDown() {
        // Nothing to do.
    }

    @Override
    public boolean accept(File file) {
        return file.getAbsolutePath().contains(CONFIGURATION_SRC_DIR);
    }

    @Override
    public boolean fileCreated(File file) throws ProcessorException {
        ResourceCopy.copyFileToDir(file, source, destination);
        mojo.getLog().info(file.getName() + " copied to the conf directory");
        return false;
    }

    @Override
    public boolean fileUpdated(File file) throws ProcessorException {
        ResourceCopy.copyFileToDir(file, source, destination);
        mojo.getLog().info(file.getName() + " updated in the conf directory");
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
