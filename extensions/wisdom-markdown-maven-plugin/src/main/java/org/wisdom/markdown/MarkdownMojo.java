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
package org.wisdom.markdown;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.parboiled.Parboiled;
import org.pegdown.Extensions;
import org.pegdown.Parser;
import org.pegdown.PegDownProcessor;
import org.wisdom.maven.Constants;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.mojos.AbstractWisdomWatcherMojo;
import org.wisdom.maven.utils.WatcherUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Compiles Markdown files to HTML documents.
 */
@Mojo(name = "compile-markdown", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.COMPILE)
public class MarkdownMojo extends AbstractWisdomWatcherMojo implements Constants {

    @Parameter(property = "extensions")
    protected List<String> extensions = new ArrayList<>();

    File internalSources;
    File destinationForInternals;
    File externalSources;
    File destinationForExternals;

    protected PegDownProcessor instance;

    public void execute()
            throws MojoExecutionException {
        if (extensions == null || extensions.isEmpty()) {
            extensions = ImmutableList.of("md", "markdown");
        }

        this.internalSources = new File(basedir, MAIN_RESOURCES_DIR + "/assets");
        this.destinationForInternals = new File(buildDirectory, "classes/assets");

        this.externalSources = new File(basedir, ASSETS_SRC_DIR);
        this.destinationForExternals = new File(getWisdomRootDirectory(), ASSETS_DIR);

        if (instance == null) {
            instance = new PegDownProcessor(Extensions.ALL);
        }

        try {
            if (internalSources.isDirectory()) {
                for (File f : FileUtils.listFiles(internalSources, extensions.toArray(new String[extensions.size()
                        ]), true)) {
                    process(f);
                }
            }
            if (externalSources.isDirectory()) {
                for (File f : FileUtils.listFiles(externalSources, extensions.toArray(new String[extensions.size()
                        ]), true)) {
                    process(f);
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Error while processing a Markdown file", e);
        }

    }

    public void process(File input) throws IOException {
        File filtered;

        // Check whether the source was already copied to the destination directories (by the resource copy).
        if (FilenameUtils.directoryContains(internalSources.getCanonicalPath(), input.getCanonicalPath())) {
            filtered = findFileInDirectory(input, destinationForInternals);
        } else {
            filtered = findFileInDirectory(input, destinationForExternals);
        }

        if (filtered == null) {
            // It was not copied.
            getLog().warn("Cannot find the filtered version of " + input.getAbsolutePath() + ", " +
                    "using source file.");
            filtered = input;
        }

        // Actual processing.
        String result = instance.markdownToHtml(FileUtils.readFileToString(filtered));
        FileUtils.write(getOutputFile(input, "html"), result);
    }


    /**
     * Searches for a file with the same name as the given file in the given directory.
     *
     * @param file      the file
     * @param directory the directory
     * @return the found file or {@code null} if not found
     */
    private File findFileInDirectory(File file, File directory) {
        if (!directory.isDirectory()) {
            return null;
        }

        Collection<File> files = FileUtils.listFiles(directory, new NameFileFilter(file.getName()),
                TrueFileFilter.INSTANCE);

        if (files.isEmpty()) {
            return null;
        } else if (files.size() > 1) {
            getLog().warn("Finding several (filtered) candidates for file " + file.getName()
                    + " in " + directory.getAbsolutePath() + " : " + files);
        }

        return files.iterator().next();
    }

    @Override
    public boolean accept(File file) {
        return WatcherUtils.hasExtension(file, extensions);
    }

    @Override
    public boolean fileCreated(File file) throws WatchingException {
        try {
            process(file);
        } catch (IOException e) {
            throw new WatchingException(e.getMessage(), file, e);
        }
        return true;
    }

    @Override
    public boolean fileUpdated(File file) throws WatchingException {
        return fileCreated(file);
    }

    @Override
    public boolean fileDeleted(File file) throws WatchingException {
        File output = getOutputFile(file, "html");
        if (output != null && output.isFile()) {
            FileUtils.deleteQuietly(output);
        }
        return true;
    }

    private File getOutputFile(File input, String extension) {
        File source;
        File destination;
        if (input.getAbsolutePath().startsWith(internalSources.getAbsolutePath())) {
            source = internalSources;
            destination = destinationForInternals;
        } else if (input.getAbsolutePath().startsWith(externalSources.getAbsolutePath())) {
            source = externalSources;
            destination = destinationForExternals;
        } else {
            throw new IllegalArgumentException("Cannot determine the output file for " + input.getAbsolutePath() + "," +
                    " the file is not in a resource directory");
        }

        if (!extension.startsWith(".")) {
            extension = "." + extension;
        }
        String fileName = input.getName().substring(0, input.getName().lastIndexOf(".")) + extension;
        String path = input.getParentFile().getAbsolutePath().substring(source.getAbsolutePath().length());
        return new File(destination, path + File.separator + fileName);
    }
}
