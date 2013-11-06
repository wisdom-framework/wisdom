package org.ow2.chameleon.wisdom.maven.processors;

import org.ow2.chameleon.wisdom.maven.mojos.AbstractWisdomMojo;
import org.ow2.chameleon.wisdom.maven.node.NPM;

import java.io.File;

/**
 * Processor compiling coffee script files.
 */
public class CoffeeScriptProcessor implements Processor {
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
        install.install("coffee-script", "1.6.3"); //TODO constants.
    }

    @Override
    public void processAll() throws ProcessorException {
        if (internalSources.isDirectory()) {
            mojo.getLog().info("Compiling coffeescript files from " + internalSources.getAbsolutePath());
            new NPM.Execution(mojo).npm("coffee-script").command("coffee").args("--compile", "--map", "--output",
                    destinationForInternals.getAbsolutePath(), internalSources.getAbsolutePath()).execute();
        }

        if (externalSources.isDirectory()) {
            mojo.getLog().info("Compiling coffeescript files from " + externalSources.getAbsolutePath());
            new NPM.Execution(mojo).npm("coffee-script").command("coffee").args("--compile", "--map", "--output",
                    destinationForExternals.getAbsolutePath(), externalSources.getAbsolutePath()).execute();
        }
    }

    @Override
    public void tearDown() {
        // Nothing.
    }

    @Override
    public boolean accept(File file) {
        return file.getName().endsWith(".coffee");
    }

    private File getOutputJSFile(File input) {
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

        String jsFileName = input.getName().substring(0, input.getName().length() - ".coffee".length()) + ".js";
        String path = input.getParentFile().getAbsolutePath().substring(source.getAbsolutePath().length());
        return new File(destination, path + "/" + jsFileName);
    }

    private void compile(File file) throws ProcessorException {
        File out = getOutputJSFile(file);
        if (file == null) {
            return;
        }
        mojo.getLog().info("Compiling " + file.getAbsolutePath() + " to " + out.getAbsolutePath());

        new NPM.Execution(mojo).npm("coffee-script").command("coffee").args("--compile", "--map", "--output",
                out.getParentFile().getAbsolutePath(), file.getAbsolutePath()).execute();
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
        File theFile = getOutputJSFile(file);
        if (theFile.exists()) {
            theFile.delete();
        }
        return true;
    }

}
