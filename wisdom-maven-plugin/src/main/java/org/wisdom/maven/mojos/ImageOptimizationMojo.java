package org.wisdom.maven.mojos;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
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
import org.wisdom.maven.node.LoggedOutputStream;
import org.wisdom.maven.utils.ExecUtils;
import org.wisdom.maven.utils.WatcherUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Optimizes images using OptiPNG and JpegTrans.
 * This mojo is using two libraries:
 * <ul>
 * <li>optipng for png files</li>
 * <li>jpegtrans for jpeg files</li>
 * </ul>
 * <p/>
 * The plugin looks from png and jpeg files from the destination folder directly,
 * but listens for changes in the source folders.
 */
@Mojo(name = "optimize-images", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.COMPILE)
public class ImageOptimizationMojo extends AbstractWisdomWatcherMojo implements Constants {

    public static final List<String> OPTIPNG_EXTENSIONS = Arrays.asList("png", "bmp", "gif", "pnm", "tiff");
    public static final List<String> JPEG_EXTENSIONS = Arrays.asList("jpeg", "jpg");

    private File internalSources;
    private File destinationForInternals;
    private File externalSources;
    private File destinationForExternals;

    private File optipng;
    private File jpegtran;

    private String[] extensions;
    public static final String OPTIPNG_DOWNLOAD_BASE_LOCATION =
            "https://raw.github.com/yeoman/node-optipng-bin/master/vendor/";

    public static final String JPEGTRAN_DOWNLOAD_BASE_LOCATION =
            "https://raw.github.com/yeoman/node-jpegtran-bin/master/vendor/";

    @Override
    public void execute() throws MojoExecutionException {
        this.internalSources = new File(basedir, MAIN_RESOURCES_DIR);
        this.destinationForInternals = new File(buildDirectory, "classes");

        this.externalSources = new File(basedir, ASSETS_SRC_DIR);
        this.destinationForExternals = new File(getWisdomRootDirectory(), ASSETS_DIR);

        optipng = installOptiPNGIfNeeded();
        jpegtran = installJPEGTranIfNeeded();

        List<String> list = new ArrayList<>(OPTIPNG_EXTENSIONS);
        list.addAll(JPEG_EXTENSIONS);
        this.extensions = list.toArray(new String[list.size()]);

        optimizeAllImagesFromDirectory(new File(buildDirectory, "classes"));
        optimizeAllImagesFromDirectory(new File(getWisdomRootDirectory(), ASSETS_DIR));
    }

    private File installOptiPNGIfNeeded() {
        // Check we don't have a version already installed
        File directory = new File(System.getProperty("user.home"), ".wisdom/utils/");
        File optipng = new File(directory, "optipng");
        if (ExecUtils.isWindows()) {
            optipng = new File(directory, "optipng.exe");
        }
        if (optipng.isFile()) {
            getLog().info("OptiPNG found : " + optipng.getAbsolutePath());
            return optipng;
        }

        boolean r = directory.mkdirs();
        getLog().debug("attempt to create " + directory.getAbsolutePath() + " : " + r);

        // Install it.
        // Yeoman has stored binaries on github.
        String url = "";
        if (ExecUtils.isWindows()) {
            url = OPTIPNG_DOWNLOAD_BASE_LOCATION + "win/optipng.exe";
        } else if (ExecUtils.isLinux()) {
            if (ExecUtils.is64bit()) {
                url = OPTIPNG_DOWNLOAD_BASE_LOCATION + "linux/x64/optipng";
            } else {
                url = OPTIPNG_DOWNLOAD_BASE_LOCATION + "linux/x86/optipng";
            }
        } else if (ExecUtils.isMac()) {
            url = OPTIPNG_DOWNLOAD_BASE_LOCATION + "osx/optipng";
        }

        getLog().info("Downloading optipng from " + url);
        try {
            FileUtils.copyURLToFile(new URL(url), optipng);
            r = optipng.setExecutable(true);
            getLog().debug("attempt to give the execution flag to " + optipng.getName() + " : " + r);
            getLog().info("optipng downloaded to " + optipng.getAbsolutePath());
            return optipng;
        } catch (IOException e) {
            getLog().error("Cannot download optipng from " + url, e);
            return null;
        }
    }

    private File installJPEGTranIfNeeded() {
        // Check we don't have a version already installed
        File directory = new File(System.getProperty("user.home"), ".wisdom/utils/");
        File jpegtran = new File(directory, "jpegtran");
        if (ExecUtils.isWindows()) {
            jpegtran = new File(directory, "jpegtran.exe");
        }
        if (jpegtran.isFile()) {
            getLog().info("JPEGTran found : " + jpegtran.getAbsolutePath());
            return jpegtran;
        }

        boolean r = directory.mkdirs();
        getLog().debug("attempt to create " + directory.getAbsolutePath() + " : " + r);

        // Install it.
        // Yeoman has stored binaries on github.
        Map<String, String> urls = new LinkedHashMap<>();
        if (ExecUtils.isWindows()) {
            if (ExecUtils.is64bit()) {
                urls.put("jpegtran.exe", JPEGTRAN_DOWNLOAD_BASE_LOCATION + "win/x64/jpegtran.exe");
                urls.put("libjpeg-62.dll", JPEGTRAN_DOWNLOAD_BASE_LOCATION + "win/x64/libjpeg-62.dll");
            } else {
                urls.put("jpegtran.exe", JPEGTRAN_DOWNLOAD_BASE_LOCATION + "win/jpegtran.exe");
                urls.put("libjpeg-62.dll", JPEGTRAN_DOWNLOAD_BASE_LOCATION + "win/libjpeg-62.dll");
            }
        } else if (ExecUtils.isLinux()) {
            if (ExecUtils.is64bit()) {
                urls.put("jpegtran", JPEGTRAN_DOWNLOAD_BASE_LOCATION + "linux/x64/jpegtran");
            } else {
                urls.put("jpegtran", JPEGTRAN_DOWNLOAD_BASE_LOCATION + "linux/x86/jpegtran");
            }
        } else if (ExecUtils.isMac()) {
            urls.put("jpegtran", JPEGTRAN_DOWNLOAD_BASE_LOCATION + "osx/jpegtran");
        }

        getLog().info("Downloading jpegtran from " + urls);
        try {
            for (Map.Entry<String, String> entry : urls.entrySet()) {
                FileUtils.copyURLToFile(new URL(entry.getValue()), new File(directory, entry.getValue()));
            }
            r = jpegtran.setExecutable(true);
            getLog().debug("attempt to give the execution flag to " + jpegtran.getName() + " : " + r);
            getLog().info("jpegtran downloaded to " + jpegtran.getAbsolutePath());
            return jpegtran;
        } catch (IOException e) {
            getLog().error("Cannot download jpegtran from " + urls, e);
            return null;
        }
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
        if (file == null || !file.isFile()) {
            return;
        }

        getLog().info("Optimizing " + file.getAbsolutePath());
        execute(optipng, file.getAbsolutePath());
    }

    private void optimizeJpeg(File file) throws MojoExecutionException {
        if (file == null || !file.isFile()) {
            return;
        }

        getLog().info("Optimizing " + file.getAbsolutePath());
        execute(jpegtran, "-optimize", "-progressive", "-outfile",
                file.getAbsolutePath(), file.getAbsolutePath());
    }

    private void execute(File executable, String... args) throws MojoExecutionException {
        CommandLine line = new CommandLine(executable);
        line.addArguments(args, false);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValue(0);

        PumpStreamHandler streamHandler = new PumpStreamHandler(
                new LoggedOutputStream(getLog(), false),
                new LoggedOutputStream(getLog(), true));

        executor.setStreamHandler(streamHandler);

        getLog().info("Executing " + line.toString());

        try {
            executor.execute(line);
        } catch (IOException e) {
            getLog().error("Error while executing " + executable.getName(), e);
            throw new MojoExecutionException("Error while executing " + executable.getName(), e);
        }
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
