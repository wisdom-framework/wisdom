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

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.wisdom.maven.Constants;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.node.NPM;
import org.wisdom.maven.utils.WatcherUtils;

import java.io.File;
import java.util.List;

import static org.wisdom.maven.node.NPM.npm;

/**
 * Optimizes images using OptiPNG and JpegTrans.
 * This mojo is using two libraries:
 * <ul>
 * <li>optipng for png files</li>
 * <li>jpegtrans for jpeg files</li>
 * </ul>
 * <p>
 * The plugin looks from png and jpeg files from the destination folder directly,
 * but listens for changes in the source folders.
 */
@Mojo(name = "optimize-images", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.COMPILE)
public class ImageOptimizationMojo extends AbstractWisdomWatcherMojo implements Constants {

    /**
     * The ImageMin (CLI) NPM name.
     */
    public static final String NPM_NAME = "imagemin-cli";

    /**
     * The command to be launched.
     */
    public static final String COMMAND = "imagemin";

    public static final List<String> EXTENSIONS = ImmutableList.of("png", "jpeg", "jpg", "svg", "gif");

    @Parameter(defaultValue = "false")
    boolean failOnBrokenAsset;

    @Parameter
    ImageMinification imageMinification;


    /**
     * Skips the image optimization.
     */
    @Parameter(defaultValue = "${skipImageOptimization}", required = false)
    public boolean skipImageOptimization;

    private NPM npm;

    @Override
    public void execute() throws MojoExecutionException {
        if (imageMinification == null) {
            imageMinification = new ImageMinification();
        }

        if (skipImageOptimization  || ! imageMinification.isEnabled()) {
            getLog().info("Image minification skipped");
            // Don't forget to remove the mojo from the watch pipeline.
            removeFromWatching();
            return;
        }

        npm = npm(this, NPM_NAME, imageMinification.getVersion());

        optimizeAllImagesFromDirectory(new File(buildDirectory, "classes"));
        optimizeAllImagesFromDirectory(new File(getWisdomRootDirectory(), ASSETS_DIR));
    }


    private void optimizeAllImagesFromDirectory(File directory) throws MojoExecutionException {
        if (! directory.isDirectory()) {
            return;
        }

        try {
            npm.registerOutputStream(true);
            npm.execute(COMMAND, imageMinification.getArguments(directory));
        } catch (MojoExecutionException e) {
            String message = "";
            if (npm.getLastErrorStream() != null  && ! npm.getLastErrorStream().isEmpty()) {
                message = " : " + npm.getLastErrorStream();
            }
            if (failOnBrokenAsset) {
                getLog().error("An error has been caught while optimizing images from " + directory.getAbsolutePath() + message);
                throw e;
            } else {
                getLog().warn("An error has been caught while optimizing images from " + directory.getAbsolutePath() + message);
                getLog().warn("Check log for details");
            }
        }
    }

    @Override
    public boolean accept(File file) {
        return
                (WatcherUtils.isInDirectory(file, WatcherUtils.getInternalAssetsSource(basedir))
                        || (WatcherUtils.isInDirectory(file, WatcherUtils.getExternalAssetsSource(basedir)))
                )
                        && WatcherUtils.hasExtension(file, EXTENSIONS);
    }

    @Override
    public boolean fileCreated(File file) throws WatchingException {
        try {
            optimizeAllImagesFromDirectory(new File(buildDirectory, "classes"));
            optimizeAllImagesFromDirectory(new File(getWisdomRootDirectory(), ASSETS_DIR));
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
