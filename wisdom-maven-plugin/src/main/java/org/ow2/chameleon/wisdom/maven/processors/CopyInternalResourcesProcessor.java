package org.ow2.chameleon.wisdom.maven.processors;

import org.ow2.chameleon.wisdom.maven.mojos.AbstractWisdomMojo;
import org.ow2.chameleon.wisdom.maven.utils.ResourceCopy;

import java.io.File;
import java.io.IOException;

/**
 * A processor responsible for copying the resources from `src/main/resources` to `target/classes`.
 * This processor does not interrupt the pipeline.
 */
public class CopyInternalResourcesProcessor implements Processor {
    private AbstractWisdomMojo mojo;

    private File source;
    private File destination;

    @Override
    public void configure(AbstractWisdomMojo mojo) {
        this.mojo = mojo;
        source = new File(mojo.basedir, RESOURCES_DIR);
        destination = new File(mojo.buildDirectory, "classes");
    }

    /**
     * Copies all templates.
     * @throws org.ow2.chameleon.wisdom.maven.processors.ProcessorException
     */
    @Override
    public void processAll() throws ProcessorException {
        try {
            ResourceCopy.copyInternalResources(mojo);
        } catch (IOException e) {
            throw new ProcessorException("Error during resource copy", e);
        }
    }

    @Override
    public void tearDown() {
        // Nothing to do.
    }

    @Override
    public boolean accept(File file) {
        return file.getAbsolutePath().contains(RESOURCES_DIR);
    }

    @Override
    public boolean fileCreated(File file) throws ProcessorException {
        ResourceCopy.copyFileToDir(file, source, destination);
        mojo.getLog().info(file.getName() + " copied to the template directory");
        return true;
    }

    @Override
    public boolean fileUpdated(File file) throws ProcessorException {
        ResourceCopy.copyFileToDir(file, source, destination);
        mojo.getLog().info(file.getName() + " updated in the template directory");
        return true;
    }

    @Override
    public boolean fileDeleted(File file) throws ProcessorException {
        File copied = ResourceCopy.computeRelativeFile(file, source, destination);
        if (copied.exists()) {
            copied.delete();
        }
        mojo.getLog().info(copied.getName() + " deleted");
        return true;
    }
}
