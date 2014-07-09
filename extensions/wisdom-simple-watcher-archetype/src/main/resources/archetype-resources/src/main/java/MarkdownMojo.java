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
package ${package};

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
 * This is our generated watcher. It compiles Markdown files to HTML documents. It locates all Markdown files from the
 * internal assets and external assets directories (i.e. src/main/resources/assets and src/main/assets) and processed
 * them using PegDown.
 * <p>
 * Watchers are enhanced Mojos (Maven Plugin), that comply with the Mojo rules (i.e. having an {@code execute}
 * methods). By default, our Watcher-Mojo defines the "compile-markdown" goal, executed in the "COMPILE" phase. Check
 * the Mojo Developer guide to select your right set of date (
 * http://maven.apache.org/guides/plugin/guide-java-plugin-development.html).
 * <p>
 * Unlike regular Mojo, extending {@code AbstractMojo}, watchers must extend the {@code AbstractWisdomWatcherMojo}
 * class to be considered as a {@code Watcher}. By default, watchers are automatically registered to the Watcher
 * Pipeline and so are participating in the Wisdom Watch Mode.
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
     * <p>
     * As stated above, Watchers are regular Mojos and so can be configured as a plain Mojo (using {@code @Parameter}
     * or {@code @Component}. Notice that the injected values are also accessible in the watch mode.
     */
    @Parameter(property = "extensions")
    protected List<String> extensions = new ArrayList<>();

    /**
     * The Pegdown instance (can be reused).
     */
    protected PegDownProcessor instance;

    /**
     * Compiles all markdown files located in the internal and external asset directories.
     * <p>
     * This is the main Mojo entry point. The {@code execute} method is invoked by the regular Maven execution.
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
            // The getResources method locates all the assets files from "src/main/resources/assets" (internal
            // assets) and "src/main/assets" (exernal assets) having on of the given extensions.
            for (File f : getResources(extensions)) {
                process(f);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Error while processing a Markdown file", e);
        }

    }

    /**
     * Processes the given markdown file. When the 'filtered' version of the file exists, it uses it.
     * <p>
     * It's generally a good practice to define such a method used by the {@code execute} method and watcher methods.
     *
     * @param input the input file
     * @throws IOException if the file cannot be processed.
     */
    public void process(File input) throws IOException {
        // The file may have been filtered (copied to the output directory and placeholders have been filled with
        // actual values. In this case, use the filtered version.
        File filtered = getFilteredVersion(input);
        if (filtered == null) {
            // It was not copied.
            getLog().warn("Cannot find the filtered version of " + input.getAbsolutePath() + ", " +
                    "using source file.");
            filtered = input;
        }

        // Actual markdown processing.
        String result = instance.markdownToHtml(FileUtils.readFileToString(filtered));

        // Write the output file. We can compute the output file using the 'getOutputFile' method taking as parameter
        // the input file and the output extension.
        FileUtils.write(getOutputFile(input, OUTPUT_EXTENSION), result);
    }

    /**
     * The markdown mojo only accepts Markdown files, i.e. files using the {@code .md, .markdown} extensions,
     * or onle of the custom extensions set.
     * <p>
     * This is the first watcher method. It fileters files handled by the watcher. Implementations must not check
     * for the file existence, as this method is also used on deleted files. In most cases,
     * it checks for extensions and / or for locations. The WatcherUtils classes provides two handy methods for this:
     * {@code isInDirectory} checks whether a file is in a given directory (including sub-directories),
     * and {@code hasExtension} checking for the file extension.
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
     * <p>
     * This is also a watcher method called when an accepted file is created. The returned value determines whether
     * the pipeline must continue after the execution of the current watcher. If {@code false} is returned,
     * the subsequent watchers are not invoked (the pipeline traversal is interupted).
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
            // Throwing WatchingException is pretty important. You can configure the files having provoked the error,
            // as well as the error's line number and position. More detailed the exception is,
            // better the error message. Watching Exceptions are analysed and displayed to the developer in a web
            // page.
            throw new WatchingException(e.getMessage(), file, e);
        }
        return true;
    }

    /**
     * An accepted file was updated - re-processes it.
     * <p>
     * This is also a watcher method called when an accepted file is updated. The returned value determines whether
     * the pipeline must continue after the execution of the current watcher. If {@code false} is returned,
     * the subsequent watchers are not invoked (the pipeline traversal is interupted).
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
     * <p>
     * This is also a watcher method called when an accepted file is deleted. The returned value determines whether
     * the pipeline must continue after the execution of the current watcher. If {@code false} is returned,
     * the subsequent watchers are not invoked (the pipeline traversal is interupted).
     * <p>
     * This methods is often used to clear the output generated for the given input file.
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
