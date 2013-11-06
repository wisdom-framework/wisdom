package org.ow2.chameleon.wisdom.maven.processors;

import org.apache.commons.io.FileUtils;
import org.ow2.chameleon.wisdom.maven.mojos.AbstractWisdomMojo;
import org.ow2.chameleon.wisdom.maven.node.NPM;

import java.io.File;
import java.util.Collection;

/**
 * Processor compiling less files.
 */
public class LessProcessor implements Processor {
    private AbstractWisdomMojo mojo;
    private File internalSources;
    private File destinationForInternals;
    private File externalSources;
    private File destinationForExternals;

    @Override
    public void configure(AbstractWisdomMojo mojo) {
        this.mojo = mojo;
        this.internalSources = new File(mojo.basedir, MAIN_RESOURCES_DIR);
        this.destinationForInternals = new File(mojo.buildDirectory, "classes");

        this.externalSources = new File(mojo.basedir, ASSETS_SRC_DIR);
        this.destinationForExternals = new File(mojo.getWisdomRootDirectory(), ASSETS_DIR);

        NPM.Install install = new NPM.Install(mojo);
        install.install("less", "1.5.0"); //TODO constants.
    }

    @Override
    public void processAll() throws ProcessorException {
        if (internalSources.isDirectory()) {
            mojo.getLog().info("Compiling less files from " + internalSources.getAbsolutePath());
            Collection<File> files = FileUtils.listFiles(internalSources, new String[]{"less"}, true);
            for (File file : files) {
                if (file.isFile()) {
                    compile(file);
                }
            }
        }

        if (externalSources.isDirectory()) {
            mojo.getLog().info("Compiling less files from " + externalSources.getAbsolutePath());
            Collection<File> files = FileUtils.listFiles(externalSources, new String[]{"less"}, true);
            for (File file : files) {
                if (file.isFile()) {
                    compile(file);
                }
            }
        }
    }

    @Override
    public void tearDown() {
        // Nothing.
    }

    @Override
    public boolean accept(File file) {
        return file.getName().endsWith(".less");
    }

    private File getOutputCSSFile(File input) {
        File source;
        File destination;
        if (input.getAbsolutePath().startsWith(internalSources.getAbsolutePath())) {
           source = internalSources;
            destination= destinationForInternals;
        } else if (input.getAbsolutePath().startsWith(externalSources.getAbsolutePath())) {
            source = externalSources;
            destination= destinationForExternals;
        } else {
            return null;
        }

        String cssFileName = input.getName().substring(0, input.getName().length() - ".less".length()) + ".css";
        String path = input.getParentFile().getAbsolutePath().substring(source.getAbsolutePath().length());
        return new File(destination, path + "/" + cssFileName);
    }

    public void compile(File file) throws ProcessorException {
        File out = getOutputCSSFile(file);
        mojo.getLog().info("Compiling " + file.getAbsolutePath() + " to " + out.getAbsolutePath());
        new NPM.Execution(mojo).npm("less").command("lessc").args(file.getAbsolutePath(), out.getAbsolutePath()).execute();

        if (! out.isFile()) {
            throw new ProcessorException("Error during the compilation of " + file.getAbsoluteFile() + " check log");
        }
    }


    @Override
    public boolean fileCreated(File file) throws ProcessorException {
        compile(file);
        return true;
    }

    @Override
    public boolean fileUpdated(File file) throws ProcessorException {
        compile(file);
        return true;
    }

    @Override
    public boolean fileDeleted(File file) {
        File theFile = getOutputCSSFile(file);
        if (theFile.exists()) {
            theFile.delete();
        }
        return true;
    }

}
