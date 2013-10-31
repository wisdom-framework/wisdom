package org.ow2.chameleon.wisdom.maven.utils;

import org.apache.commons.io.FileUtils;
import org.ow2.chameleon.wisdom.maven.Constants;
import org.ow2.chameleon.wisdom.maven.mojos.AbstractWisdomMojo;
import org.ow2.chameleon.wisdom.maven.processors.ProcessorException;

import java.io.File;
import java.io.IOException;

/**
 * Resource copy utilities.
 */
public class ResourceCopy {

    /**
     * Copies the file <tt>file</tt> to the directory <tt>dir</tt>, keeping the structure relative to <tt>rel</tt>
     * @throws ProcessorException
     */
    public static void copyFileToDir(File file, File rel, File dir) throws ProcessorException {
        try {
            File out = computeRelativeFile(file, rel, dir);
            if (out.getParentFile() != null) {
                out.getParentFile().mkdirs();
                FileUtils.copyFileToDirectory(file, out.getParentFile());
            } else {
                throw new ProcessorException("Cannot copy file - parent directory not accessible for "
                        + file.getAbsolutePath());
            }
        } catch (IOException e) {
            throw new ProcessorException("Cannot copy file " + file.getName(), e);
        }
    }

    /**
     * Gets a File object representing a File in the directory <tt>dir</tt> which has the same path as the file
     * <tt>file</tt> from the directory <tt>rel</tt>.
     * @param file
     * @param rel
     * @param dir
     * @return
     */
    public static File computeRelativeFile(File file, File rel, File dir) {
        String path = file.getAbsolutePath();
        String relativePath = path.substring(rel.getAbsolutePath().length());
        return new File(dir, relativePath);
    }

    public static void copyConfiguration(AbstractWisdomMojo mojo) throws IOException, ProcessorException {
        File in = new File(mojo.basedir, Constants.CONFIGURATION_SRC_DIR);
        File out = new File(mojo.getWisdomRootDirectory(), Constants.CONFIGURATION_DIR);
        if (! in.isDirectory()) {
            throw new ProcessorException("The configuration directory (" + in.getAbsolutePath() + ") must exist");
        }
        FileUtils.copyDirectory(in, out);
    }

    public static void copyExternalAssets(AbstractWisdomMojo mojo) throws IOException {
        File in = new File(mojo.basedir, Constants.ASSETS_SRC_DIR);
        if (! in.exists()) {
            return;
        }
        File out = new File(mojo.getWisdomRootDirectory(), Constants.ASSETS_DIR);
        FileUtils.copyDirectory(in, out);
    }

    public static void copyTemplates(AbstractWisdomMojo mojo) throws IOException {
        File in = new File(mojo.basedir, Constants.TEMPLATES_SRC_DIR);
        if (! in.exists()) {
            return;
        }
        File out = new File(mojo.getWisdomRootDirectory(), Constants.TEMPLATES_DIR);
        FileUtils.copyDirectory(in, out);
    }

    /**
     * Copy `src/main/resources` to `target/classes`
     * @param mojo the mojo
     */
    public static void copyInternalResources(AbstractWisdomMojo mojo) throws IOException {
        File in = new File(mojo.basedir, Constants.MAIN_RESOURCES_DIR);
        if (! in.exists()) {
            return;
        }
        File out = new File(mojo.buildDirectory, "classes");
        FileUtils.copyDirectory(in, out);
    }
}
