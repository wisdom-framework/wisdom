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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.wisdom.maven.MavenWatcher;
import org.wisdom.maven.pipeline.Watchers;
import org.wisdom.maven.utils.WatcherUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Common part.
 */
public abstract class AbstractWisdomWatcherMojo extends AbstractWisdomMojo implements MavenWatcher {

    /**
     * Sets the Maven Session and registers the current mojo to the watcher list (stored in the session).
     *
     * @param session the maven session
     */
    public void setSession(MavenSession session) {
        this.session = session;
        Watchers.add(session, this);
    }

    /**
     * Removes the current mojo from the watcher list.
     */
    public void removeFromWatching() {
        Watchers.remove(session, this);
    }

    @Override
    public MavenSession session() {
        return session;
    }

    @Override
    public MavenProject project() {
        return project;
    }

    // A set of utility methods

    /**
     * Finds all resources from internal and external assets directories having one of the specified extensions.
     *
     * @param extensions the extensions
     * @return the set of file, potentially empty if no files match.
     */
    public Collection<File> getResources(List<String> extensions) {
        List<File> files = new ArrayList<>();
        files.addAll(WatcherUtils.getAllFilesFromDirectory(getInternalAssetsDirectory(), extensions));
        files.addAll(WatcherUtils.getAllFilesFromDirectory(getExternalAssetsDirectory(), extensions));
        return files;
    }

    /**
     * Searches if the given file has already being copied to its output directory and so may have been 'filtered'.
     *
     * @param input the input file
     * @return the 'filtered' file if exists, {@code null} otherwise
     */
    public File getFilteredVersion(File input) {
        File out = getOutputFile(input);
        if (!out.isFile()) {
            return null;
        }
        return out;
    }

    /**
     * Gets the output file for the given input file where the output file has its extension changed from the
     * original extension to the given one. For example, {@code hello.coffee} becomes {@code hello.js}. If the input
     * file is already in an output directory, a new file object is returned with the new extension. This file stands
     * in the same directory as the input file.
     * <p>
     * This method does not check for the existence of the file, just computes its {@link java.io.File} object.
     *
     * @param input     the input file
     * @param extension the extension of the output file
     * @return the output file
     */
    public File getOutputFile(File input, String extension) {
        File source;
        File destination;
        if (!extension.startsWith(".")) {
            extension = "." + extension;
        }
        String fileName = input.getName().substring(0, input.getName().lastIndexOf(".")) + extension;
        if (input.getAbsolutePath().startsWith(getInternalAssetsDirectory().getAbsolutePath())) {
            source = getInternalAssetsDirectory();
            destination = getInternalAssetOutputDirectory();
        } else if (input.getAbsolutePath().startsWith(getExternalAssetsDirectory().getAbsolutePath())) {
            source = getExternalAssetsDirectory();
            destination = getExternalAssetsOutputDirectory();
        } else if (input.getAbsolutePath().startsWith(getInternalAssetOutputDirectory().getAbsolutePath())
                ||input.getAbsolutePath().startsWith(getExternalAssetsOutputDirectory().getAbsolutePath())) {
            return new File(input.getParentFile(), fileName);
        } else {
            throw new IllegalArgumentException("Cannot determine the output file for " + input.getAbsolutePath() + "," +
                    " the file is not in a resource directory");
        }
        String path = input.getParentFile().getAbsolutePath().substring(source.getAbsolutePath().length());
        return new File(destination, path + File.separator + fileName);
    }

    /**
     * Gets the output files for the given input file. Unlike {@link #getOutputFile(java.io.File, String)},
     * this method does not change the output file's extension. If the file is already in an output directory, the
     * file is returned as it is.
     * <p>
     * This method does not check for the existence of the file, just computes its {@link java.io.File} object.
     *
     * @param input the input file
     * @return the output file
     */
    public File getOutputFile(File input) {
        File source;
        File destination;
        if (input.getAbsolutePath().startsWith(getInternalAssetsDirectory().getAbsolutePath())) {
            source = getInternalAssetsDirectory();
            destination = getInternalAssetOutputDirectory();
        } else if (input.getAbsolutePath().startsWith(getExternalAssetsDirectory().getAbsolutePath())) {
            source = getExternalAssetsDirectory();
            destination = getExternalAssetsOutputDirectory();
        } else if (input.getAbsolutePath().startsWith(getInternalAssetOutputDirectory().getAbsolutePath())) {
            return input;
        } else if (input.getAbsolutePath().startsWith(getExternalAssetsOutputDirectory().getAbsolutePath())) {
            return input;
        } else {
            throw new IllegalArgumentException("Cannot determine the output file for " + input.getAbsolutePath() + "," +
                    " the file is not in a resource directory");
        }
        String path = input.getParentFile().getAbsolutePath().substring(source.getAbsolutePath().length());
        return new File(destination, path + File.separator + input.getName());
    }


}
