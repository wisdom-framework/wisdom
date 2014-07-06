/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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
import org.apache.maven.plugins.annotations.Parameter;
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

    public static final List<String> OPTIPNG_EXTENSIONS = Arrays.asList("png");
    public static final List<String> JPEG_EXTENSIONS = Arrays.asList("jpeg", "jpg");


    private File installLocation;
    private File optipng;
    private File jpegtran;

    @Parameter(defaultValue = "false")
    private boolean failOnBrokenAsset;

    private String[] extensions;

    /**
     * The location where the OPTIPNG executable is downloaded.
     */
    public static final String OPTIPNG_DOWNLOAD_BASE_LOCATION =
            "https://raw.github.com/yeoman/node-optipng-bin/master/vendor/";

    /**
     * The location where the JPEGTRAN executable is downloaded.
     */
    public static final String JPEGTRAN_DOWNLOAD_BASE_LOCATION =
            "https://raw.github.com/yeoman/node-jpegtran-bin/master/vendor/";

    /**
     * Skips the image optimization.
     */
    @Parameter(defaultValue = "${skipImageOptimization}", required = false)
    public boolean skipImageOptimization;

    /**
     * Constructor used by Maven.
     * It sets the installation directory to ~/.wisdom/utils.
     */
    public ImageOptimizationMojo() {
        this(new File(System.getProperty("user.home"), ".wisdom/utils/"));
    }

    /**
     * Constructor used for test.
     *
     * @param root the installation directory in which optipng and jpegtran are downloaded.
     */
    public ImageOptimizationMojo(File root) {
        installLocation = root;
    }


    @Override
    public void execute() throws MojoExecutionException {

        if (skipImageOptimization) {
            getLog().info("Image optimization skipped");
            // Don't forget to remove the mojo from the watch pipeline.
            removeFromWatching();
            return;
        }

        optipng = installOptiPNGIfNeeded();
        jpegtran = installJPEGTranIfNeeded();

        List<String> list = new ArrayList<>(OPTIPNG_EXTENSIONS);
        list.addAll(JPEG_EXTENSIONS);
        this.extensions = list.toArray(new String[list.size()]);

        optimizeAllImagesFromDirectory(new File(buildDirectory, "classes"));
        optimizeAllImagesFromDirectory(new File(getWisdomRootDirectory(), ASSETS_DIR));
    }

    private File installOptiPNGIfNeeded() throws MojoExecutionException {
        // Check we don't have a version already installed

        File optipng = new File(installLocation, "optipng");
        if (ExecUtils.isWindows()) {
            optipng = new File(installLocation, "optipng.exe");
        }
        if (optipng.isFile()) {
            getLog().info("OptiPNG found : " + optipng.getAbsolutePath());
            return optipng;
        }

        boolean r = installLocation.mkdirs();
        getLog().debug("attempt to create " + installLocation.getAbsolutePath() + " : " + r);

        // Install it.
        // Yeoman has stored binaries on github.
        String url = null;
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

        if (url == null) {
            throw new MojoExecutionException("Cannot determine the download location of the optipng executable");
        }

        getLog().info("Downloading optipng from " + url);
        try {
            FileUtils.copyURLToFile(new URL(url), optipng);
            r = optipng.setExecutable(true);
            getLog().debug("attempt to give the execution flag to " + optipng.getName() + " : " + r);
            getLog().info("optipng downloaded to " + optipng.getAbsolutePath());
            if (!optipng.isFile()) {
                getLog().error("The installation of optipng has failed");
                return null;
            }
            return optipng;
        } catch (IOException e) {
            getLog().error("Cannot download optipng from " + url, e);
            return null;
        }
    }

    private File installJPEGTranIfNeeded() {
        // Check we don't have a version already installed
        File jpegtran = new File(installLocation, "jpegtran");
        if (ExecUtils.isWindows()) {
            jpegtran = new File(installLocation, "jpegtran.exe");
        }
        if (jpegtran.isFile()) {
            getLog().info("JPEGTran found : " + jpegtran.getAbsolutePath());
            return jpegtran;
        }

        boolean r = installLocation.mkdirs();
        getLog().debug("attempt to create " + installLocation.getAbsolutePath() + " : " + r);

        // Install it.
        // Yeoman has stored binaries on github.
        Map<String, String> urls = new LinkedHashMap<>();
        if (ExecUtils.isWindows()) {
            if (ExecUtils.is64bit()) {
                urls.put("jpegtran.exe", JPEGTRAN_DOWNLOAD_BASE_LOCATION + "win/x64/jpegtran.exe");
                urls.put("libjpeg-62.dll", JPEGTRAN_DOWNLOAD_BASE_LOCATION + "win/x64/libjpeg-62.dll");
            } else {
                urls.put("jpegtran.exe", JPEGTRAN_DOWNLOAD_BASE_LOCATION + "win/x86/jpegtran.exe");
                urls.put("libjpeg-62.dll", JPEGTRAN_DOWNLOAD_BASE_LOCATION + "win/x86/libjpeg-62.dll");
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
                FileUtils.copyURLToFile(new URL(entry.getValue()), new File(installLocation, entry.getKey()));
            }
            r = jpegtran.setExecutable(true);
            getLog().debug("attempt to give the execution flag to " + jpegtran.getName() + " : " + r);
            getLog().info("jpegtran downloaded to " + jpegtran.getAbsolutePath());
            if (!jpegtran.isFile()) {
                getLog().error("The installation of jpegtran" +
                        " has failed");
                return null;
            }
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
                getLog().info("Optimizing " + file.getAbsolutePath());
                if (WatcherUtils.hasExtension(file, OPTIPNG_EXTENSIONS)) {
                    optimizePng(file);
                } else {
                    optimizeJpeg(file);
                }
            }
        }
    }

    private void optimizePng(File file) throws MojoExecutionException {
        if (file == null || !file.isFile() || optipng == null) {
            return;
        }

        getLog().info("Optimizing " + file.getAbsolutePath());
        execute(optipng, file.getAbsolutePath());
    }

    private void optimizeJpeg(File file) throws MojoExecutionException {
        if (file == null || !file.isFile() || jpegtran == null) {
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
            if (failOnBrokenAsset) {
                throw new MojoExecutionException("Error while executing " + executable.getName(), e);
            }
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
