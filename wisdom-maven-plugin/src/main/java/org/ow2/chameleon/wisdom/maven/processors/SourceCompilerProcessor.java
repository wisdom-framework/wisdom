package org.ow2.chameleon.wisdom.maven.processors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.ow2.chameleon.wisdom.maven.mojos.AbstractWisdomMojo;
import org.ow2.chameleon.wisdom.maven.utils.CompilerExecutor;

import java.io.File;
import java.util.Collection;

/**
 * A processor compiling java sources.
 */
public class SourceCompilerProcessor implements Processor {
    public static final String JAVA_EXTENSION = ".java";
    private AbstractWisdomMojo mojo;
    private File classes;

    private CompilerExecutor compiler = new CompilerExecutor();

    @Override
    public void configure(AbstractWisdomMojo mojo) {
        this.mojo = mojo;
        classes = new File(mojo.buildDirectory, "classes");
    }

    @Override
    public void processAll() throws ProcessorException {
        try {
            compiler.execute(mojo);
        } catch (MojoExecutionException e) {
            mojo.getLog().error("Compilation error", e);
        }
    }

    @Override
    public void tearDown() {
        // Nothing to do.
    }

    @Override
    public boolean accept(File file) {
        return file.getAbsolutePath().contains(MAIN_SRC_DIR)  && file.getName().endsWith(JAVA_EXTENSION);
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
    public boolean fileDeleted(final File file) throws ProcessorException {
        // Delete the associated class file.
        // We delete more than required... but the inner class case is very tricky.
        Collection<File> files = FileUtils.listFiles(classes, new IOFileFilter() {
            @Override
            public boolean accept(File test) {
                String classname = FilenameUtils.getBaseName(test.getName());
                String filename = FilenameUtils.getBaseName(file.getName());
                return classname.equals(filename)  || classname.startsWith(filename + "$");
            }

            @Override
            public boolean accept(File dir, String name) {
                return accept(new File(dir, name));
            }
        }, TrueFileFilter.INSTANCE);

        for (File clazz : files) {
            clazz.delete();
        }

        processAll();
        return true;
    }
}
