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
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;
import org.wisdom.maven.Constants;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.mojos.AbstractWisdomWatcherMojo;
import org.wisdom.maven.utils.WatcherUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Compiles Markdown files to HTML documents. It locates all Markdown files from the internal assets and external
 * assets directories (i.e. src/main/resources/assets and src/main/assets) and processed them using PegDown.
 */
@Mojo(name = "compile-markdown", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.COMPILE)
public class MarkdownMojo extends AbstractWisdomWatcherMojo implements Constants {

    /**
     * The extension of output files.
     */
    public static final String OUTPUT_EXTENSION = "html";

    /**
     * A sets of extensions handled by the mojo. By default, it supports {@code .md} and {@code .markdown} files.
     */
    @Parameter(property = "extensions")
    protected List<String> extensions = new ArrayList<>();


    protected PegDownProcessor instance;

    /**
     * Compiles all markdown files located in the internal and external asset directories.
     *
     * @throws MojoExecutionException if a markdown file cannot be processed
     */
    public void execute()
            throws MojoExecutionException {
        if (extensions == null || extensions.isEmpty()) {
            extensions = ImmutableList.of("md", "markdown");
        }

        if (instance == null) {
            instance = new PegDownProcessor(Extensions.ALL);
        }

        try {
            for (File f : getResources(extensions)) {
                process(f);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Error while processing a Markdown file", e);
        }

    }

    /**
     * Processes the given markdown file. When the 'filtered' version of the file exists, it uses it.
     *
     * @param input the input file
     * @throws IOException if the file cannot be processed.
     */
    public void process(File input) throws IOException {
        File filtered = getFilteredVersion(input);
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
     * The markdown mojo only accepts Markdown files, i.e. files using the {@code .md, .markdown} extensions,
     * or onle of the custom extensions set.
     *
     * @param file is the file.
     * @return {@code true} if the file is accepted.
     */
    @Override
    public boolean accept(File file) {
        return WatcherUtils.hasExtension(file, extensions);
    }

    /**
     * An accepted file was created - processes it.
     *
     * @param file is the file.
     * @return {@code true}
     * @throws WatchingException if the file cannot be processed correctly
     */
    @Override
    public boolean fileCreated(File file) throws WatchingException {
        try {
            process(file);
        } catch (IOException e) {
            throw new WatchingException(e.getMessage(), file, e);
        }
        return true;
    }

    /**
     * An accepted file was updated - re-processes it.
     *
     * @param file is the file.
     * @return {@code true}
     * @throws WatchingException if the file cannot be processed correctly
     */
    @Override
    public boolean fileUpdated(File file) throws WatchingException {
        return fileCreated(file);
    }

    /**
     * An accepted file was deleted - deletes the output file.
     *
     * @param file the file
     * @return {@code true}
     */
    @Override
    public boolean fileDeleted(File file) {
        File output = getOutputFile(file, OUTPUT_EXTENSION);
        FileUtils.deleteQuietly(output);
        return true;
    }
}
