package org.wisdom.maven.mojos;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.wisdom.maven.Constants;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.node.NPM;
import org.wisdom.maven.utils.WatcherUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.wisdom.maven.node.NPM.npm;

/**
 * Optimizes images using OptiPNG and JpegTrans.
 * This mojo is using two libraries:
 * <ul>
 *     <li>optipng for png files</li>
 *     <li>jpegtrans for jpeg files</li>
 * </ul>
 *
 * The plugin looks from png and jpeg files from the destination folder directly,
 * but listens for changes in the source folders.
 */
@Mojo(name = "optimize-images", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.COMPILE)
public class ImageOptimizationMojo extends AbstractWisdomWatcherMojo implements Constants {

    public static final String OPTIPNG_NPM_NAME = "optipng-bin";
    public static final String OPTIPNG_NPM_VERSION = "0.3.1";
    public static final String OPTIPNG_COMMAND = "optipng";

    public static final String JPEGTRAN_NPM_NAME = "jpegtran-bin";
    public static final String JPEGTRAN_NPM_VERSION = "0.2.3";
    public static final String JPEGTRAN_COMMAND = "jpegtran";

    public static final List<String> OPTIPNG_EXTENSIONS = Arrays.asList("png", "bmp", "gif", "pnm", "tiff");
    public static final List<String> JPEG_EXTENSIONS = Arrays.asList("jpeg", "jpg");

    private File internalSources;
    private File destinationForInternals;
    private File externalSources;
    private File destinationForExternals;

    private NPM optipng;
    private NPM jpegtran;

    private String[] extensions;

    @Override
    public void execute() throws MojoExecutionException {
        this.internalSources = new File(basedir, MAIN_RESOURCES_DIR);
        this.destinationForInternals = new File(buildDirectory, "classes");

        this.externalSources = new File(basedir, ASSETS_SRC_DIR);
        this.destinationForExternals = new File(getWisdomRootDirectory(), ASSETS_DIR);

        optipng = npm(this, OPTIPNG_NPM_NAME, OPTIPNG_NPM_VERSION);
        jpegtran = npm(this, JPEGTRAN_NPM_NAME, JPEGTRAN_NPM_VERSION);

        List<String> list = new ArrayList<>(OPTIPNG_EXTENSIONS);
        list.addAll(JPEG_EXTENSIONS);
        this.extensions = list.toArray(new String[list.size()]);

        optimizeAllImagesFromDirectory(new File(buildDirectory, "classes"));
        optimizeAllImagesFromDirectory(new File(getWisdomRootDirectory(), ASSETS_DIR));
    }

    private void optimizeAllImagesFromDirectory(File directory) throws MojoExecutionException {
        if (directory.isDirectory()) {
            IOFileFilter filter = new AbstractFileFilter() {
                @Override
                public boolean accept(File file) {
                    return WatcherUtils.hasExtension(file, extensions);
                }
            };
            for (File file : FileUtils.listFiles(directory, filter, TrueFileFilter.INSTANCE)) {
                if (WatcherUtils.hasExtension(file, OPTIPNG_EXTENSIONS)) {
                    optimizePng(file);
                } else {
                    optimizeJpeg(file);
                }
            }
        }
    }

    private void optimizePng(File file) throws MojoExecutionException {
        if (file == null || ! file.isFile()) {
            return;
        }

        getLog().info("Optimizing " + file.getAbsolutePath());
        if (getLog().isDebugEnabled()) {
            optipng.execute(OPTIPNG_COMMAND, "-v", file.getAbsolutePath());
        } else {
            optipng.execute(OPTIPNG_COMMAND, file.getAbsolutePath());
        }
    }

    private void optimizeJpeg(File file) throws MojoExecutionException {
        if (file == null || ! file.isFile()) {
            return;
        }

        getLog().info("Optimizing " + file.getAbsolutePath());
        jpegtran.execute(JPEGTRAN_COMMAND, "-optimize", "-progressive", "-outfile",
                file.getAbsolutePath(), file.getAbsolutePath());
    }

    @Override
    public boolean accept(File file) {
        return
                (WatcherUtils.isInDirectory(file, WatcherUtils.getInternalAssetsSource(basedir))
                        || (WatcherUtils.isInDirectory(file, WatcherUtils.getExternalAssetsSource(basedir)))
                )
                        && WatcherUtils.hasExtension(file, extensions);
    }

    private File getOutputFile(File input) {
        File source;
        File destination;
        if (input.getAbsolutePath().startsWith(internalSources.getAbsolutePath())) {
            source = internalSources;
            destination = destinationForInternals;
        } else if (input.getAbsolutePath().startsWith(externalSources.getAbsolutePath())) {
            source = externalSources;
            destination = destinationForExternals;
        } else {
            return null;
        }
        String path = input.getParentFile().getAbsolutePath().substring(source.getAbsolutePath().length());
        return new File(destination, path + "/" + input.getName());
    }

    @Override
    public boolean fileCreated(File file) throws WatchingException {
        try {
            if (WatcherUtils.hasExtension(file, OPTIPNG_EXTENSIONS)) {
                optimizePng(getOutputFile(file));
            } else {
                optimizeJpeg(getOutputFile(file));
            }
        } catch (MojoExecutionException e) {
            getLog().error("Error while optimizing " + file.getAbsolutePath(), e);
        }
        return true;
    }

    @Override
    public boolean fileUpdated(File file) throws WatchingException {
        fileCreated(file);
        return true;
    }

    @Override
    public boolean fileDeleted(File file) {
        // The file should already have been deleted by the resource copy.
        File theFile = getOutputFile(file);
        FileUtils.deleteQuietly(theFile);
        return true;
    }

}
