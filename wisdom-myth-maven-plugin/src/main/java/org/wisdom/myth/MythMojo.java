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
package org.wisdom.myth;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.wisdom.maven.Constants;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.mojos.AbstractWisdomWatcherMojo;
import org.wisdom.maven.node.NPM;
import org.wisdom.maven.utils.WatcherUtils;

import java.io.File;
import java.util.Collection;

import static org.wisdom.maven.node.NPM.npm;

/**
 * A Mojo extending Wisdom to support <a href="http://www.myth.io/">Myth CSS</a>.
 * It watches CSS files from 'src/main/resources/assets' and 'src/main/assets' and process them using Myth. If the
 * CSS file is already present in the destination directories (and more recent than the original file),
 * it processes these one, letting this plugin work seamlessly with the Wisdom CSS features.
 *
 * Less files are not processed.
 */
@Mojo(name = "compile-myth", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.COMPILE)
public class MythMojo extends AbstractWisdomWatcherMojo implements Constants {

    private static final String MYTH_NPM_NAME = "myth";
    private static final String MYTH_NPM_VERSION = "0.2.0";

    private File internalSources;
    private File destinationForInternals;
    private File externalSources;
    private File destinationForExternals;

    private NPM myth;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        this.internalSources = new File(basedir, MAIN_RESOURCES_DIR);
        this.destinationForInternals = new File(buildDirectory, "classes");

        this.externalSources = new File(basedir, ASSETS_SRC_DIR);
        this.destinationForExternals = new File(getWisdomRootDirectory(), ASSETS_DIR);

        myth = npm(this, MYTH_NPM_NAME, MYTH_NPM_VERSION);

        try {
            if (internalSources.isDirectory()) {
                getLog().info("Compiling CSS files with Myth from " + internalSources.getAbsolutePath());
                Collection<File> files = FileUtils.listFiles(internalSources, new String[]{"css"}, true);
                for (File file : files) {
                    if (file.isFile()) {
                        process(file);
                    }
                }
            }

            if (externalSources.isDirectory()) {
                getLog().info("Compiling CSS files with Myth from " + externalSources.getAbsolutePath());
                Collection<File> files = FileUtils.listFiles(externalSources, new String[]{"css"}, true);
                for (File file : files) {
                    if (file.isFile()) {
                        process(file);
                    }
                }
            }
        } catch (WatchingException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    @Override
    public boolean accept(File file) {
        return
                (WatcherUtils.isInDirectory(file, WatcherUtils.getInternalAssetsSource(basedir))
                        || (WatcherUtils.isInDirectory(file, WatcherUtils.getExternalAssetsSource(basedir)))
                )
                        && WatcherUtils.hasExtension(file, "css");
    }

    @Override
    public boolean fileCreated(File file) throws WatchingException {
        process(file);
        return true;
    }

    private void process(File input) throws WatchingException {
        // We are going to process a CSS file using Myth.
        // First, determine which file we must process, indeed, the file may already have been copies to the
        // destination directory
        File destination = getOutputCSSFile(input);

        // Create the destination folder.
        if (! destination.getParentFile().isDirectory()) {
            destination.getParentFile().mkdirs();
        }

        // If the destination file is more recent (or equally recent) than the input file, process that one
        if (destination.isFile()  && destination.lastModified() >= input.lastModified()) {
            getLog().info("Processing " + destination.getAbsolutePath() + " instead of " + input.getAbsolutePath() +
                    " - the file was already processed");
            input = destination;
        }

        // Now execute Myth
        try {
            int exit = myth.execute("myth", input.getAbsolutePath(), destination.getAbsolutePath());
            getLog().debug("Myth execution exiting with " + exit + " status");
        } catch (MojoExecutionException e) {
            throw new WatchingException("An error occurred during Myth processing of " + input.getAbsolutePath(), e);
        }

    }

    @Override
    public boolean fileUpdated(File file) throws WatchingException {
        process(file);
        return true;
    }

    @Override
    public boolean fileDeleted(File file) {
        File theFile = getOutputCSSFile(file);
        FileUtils.deleteQuietly(theFile);
        return true;
    }


    private File getOutputCSSFile(File input) {
        File source = null;
        File destination = null;
        if (input.getAbsolutePath().startsWith(internalSources.getAbsolutePath())) {
            source = internalSources;
            destination = destinationForInternals;
        } else {
            source = externalSources;
            destination = destinationForExternals;
        }

        String path = input.getParentFile().getAbsolutePath().substring(source.getAbsolutePath().length());
        return new File(destination, path + "/" + input.getName());
    }
}
