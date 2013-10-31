package org.ow2.chameleon.wisdom.maven.processors;

import org.apache.commons.io.FileUtils;
import org.ow2.chameleon.wisdom.maven.mojos.AbstractWisdomMojo;

import java.io.File;
import java.io.IOException;

/**
 * Copy the bundle to the applicaiton directory.
 */
public class BundleDeployerProcessor implements Processor {
    private AbstractWisdomMojo mojo;
    private File file;
    private File destination;

    @Override
    public void configure(AbstractWisdomMojo mojo) {
        this.mojo = mojo;
        this.file = new File(mojo.buildDirectory, mojo.project.getArtifactId() + "-" + mojo.project
                .getVersion() + ".jar");
        this.destination = new File(mojo.getWisdomRootDirectory(), "application/" + file.getName());
    }

    @Override
    public void processAll() throws ProcessorException {
        try {
            FileUtils.copyFile(file, destination);
        } catch (IOException e) {
            throw new ProcessorException("Cannot copy file " + this.file.getAbsoluteFile(), e);
        }
    }

    @Override
    public void tearDown() {
        // Do nothing.
    }

    @Override
    public boolean accept(File file) {
        return file.getAbsolutePath().contains(MAIN_SRC_DIR)  || file.getAbsolutePath().contains(MAIN_RESOURCES_DIR);
    }

    @Override
    public boolean fileCreated(File file) throws ProcessorException {
        processAll();
        return true;
    }

    @Override
    public boolean fileUpdated(File file) throws ProcessorException {
        processAll();
        return true;
    }

    @Override
    public boolean fileDeleted(File file) throws ProcessorException {
        processAll();
        return true;
    }
}
