package org.ow2.chameleon.wisdom.maven.processors;

import org.ow2.chameleon.wisdom.maven.mojos.AbstractWisdomMojo;
import org.ow2.chameleon.wisdom.maven.utils.BundlePackagerExecutor;

import java.io.File;

/**
 * The processor packaging the bundle.
 */
public class BundlePackagerProcessor implements Processor {
    private AbstractWisdomMojo mojo;
    private final BundlePackagerExecutor packager = new BundlePackagerExecutor();

    @Override
    public void configure(AbstractWisdomMojo mojo) {
        this.mojo = mojo;
    }

    @Override
    public void processAll() throws ProcessorException {
        packager.execute(mojo, new File(mojo.buildDirectory, mojo.project.getArtifactId() + "-" + mojo.project
                .getVersion() + ".jar"));
    }

    @Override
    public void tearDown() {
        // Do nothing
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
