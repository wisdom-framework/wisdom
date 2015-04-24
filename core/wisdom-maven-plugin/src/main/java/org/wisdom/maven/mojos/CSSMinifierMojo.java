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
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.node.NPM;
import org.wisdom.maven.utils.WatcherUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregates and minifies CSS files using <a href="https://github.com/jakubpawlowicz/clean-css">clean-css</a>.
 * It takes all CSS files form the internal and external assets directories (so
 * src/main/resources/assets, and src/main/assets) and minifies them.
 *
 * To configure aggregation, use the {@code <stylesheets></stylesheets>} element.
 */

@Mojo(name = "minify-css", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class CSSMinifierMojo extends AbstractWisdomWatcherMojo {

    public static final String CLEANCSS_NPM_NAME = "clean-css";
    public static final String CLEANCSS_NPM_VERSION = "3.2.3";

    /**
     * The NPM object.
     */
    protected NPM cleancss;

    /**
     * Flag to determine if we skip minification.
     */
    @Parameter(defaultValue = "${skipCleanCSS}")
    public boolean skipCleanCSS;

    /**
     * Minified file extension parameter, lets the user define their own extensions to use with
     * minification. Must not contain the {@literal .css} extension.
     */
    @Parameter(defaultValue = "-min")
    public String cssMinifierSuffix;

    /**
     * Configure the stylesheets processing. This element let you configure the CSS aggregation.
     */
    @Parameter
    protected Stylesheets stylesheets;

    /**
     * The Clean CSS NPM version.
     */
    @Parameter(defaultValue = CLEANCSS_NPM_VERSION)
    protected String cleanCssVersion;

    /**
     * Checks if the skipCleanCSS flag has been set if so, we stop watching css files. If not we
     * continue by setting our Clean CSS NPM object and calling the minify method for all css
     * files found.
     *
     * @throws MojoExecutionException  when modification fails.
     */
    @Override
    public void execute() throws MojoExecutionException {
        if (skipCleanCSS) {
            getLog().debug("Skipping CSS minification");
            removeFromWatching();
            return;
        }

        cleancss = NPM.npm(this, CLEANCSS_NPM_NAME, cleanCssVersion);
        getLog().info("Clean CSS version: " + cleanCssVersion);

        // Check whether or not we have a custom configuration
        if (stylesheets == null) {
            getLog().info("No 'stylesheets' processing configuration, minifying all '.css' files individually");
            for (File file : getResources(ImmutableList.of("css"))) {
                try {
                    process(file);
                } catch (WatchingException e) {
                    throw new MojoExecutionException(e.getMessage(), e);
                }
            }
        } else {
            process(stylesheets);
        }
    }

    protected void process(Stylesheets stylesheets) throws MojoExecutionException {
        if (stylesheets.getAggregations() == null  || stylesheets.getAggregations().isEmpty()) {
            getLog().warn("No 'aggregation' configured in the 'stylesheets' processing configuration - skip " +
                    "processing");
            return;
        }

        for (Aggregation aggregation : stylesheets.getAggregations()) {
            process(aggregation);
        }
    }

    private void process(Aggregation aggregation) throws MojoExecutionException {
        File output;
        if (aggregation.getOutput() == null) {
            output = getDefaultOutputFile(aggregation);
        } else {
            output = new File(aggregation.getOutput());
            output = fixPath(output);
        }

        if (! output.getParentFile().isDirectory()) {
            getLog().debug("Create directory " + output.getParentFile().getAbsolutePath() + " : "
                    + output.getParentFile().mkdirs());
        }

        List<String> arguments = new ArrayList<>();
        arguments.add("-o");
        arguments.add(output.getAbsolutePath());
        arguments.add("-r");
        arguments.add(getInternalAssetOutputDirectory().getAbsolutePath());

        if (! aggregation.isMinification()) {
            arguments.add("--skip-advanced");
            arguments.add("--skip-aggressive-merging");
            arguments.add("--keep-line-breaks");
        }

        for (String file : aggregation.getFiles()) {
            File theFile = new File(file);
            if (theFile.exists()) {
                arguments.add(theFile.getAbsolutePath());
            } else {
                File f = new File(getInternalAssetOutputDirectory(), file);
                if (! f.exists()  && ! f.getName().endsWith("css")) {
                    // Append the extension
                    f = new File(getInternalAssetOutputDirectory(), file + ".css");
                }

                if (! f.exists()) {
                    throw new MojoExecutionException("Cannot compute aggregated CSS - the '" + f.getAbsolutePath() + "'" +
                            " file does not exist");
                }

                arguments.add(f.getAbsolutePath());
            }
        }

        cleancss.execute("cleancss", arguments.toArray(new String[arguments.size()]));

    }

    private File fixPath(File output) {
        if (output.isAbsolute()) {
            return output;
        } else {
            return new File(getInternalAssetOutputDirectory(), output.getPath());
        }
    }

    protected File getDefaultOutputFile(Aggregation aggregation) {
        String classifier = cssMinifierSuffix;
        if (aggregation.isMinification()) {
            if (stylesheets.getMinifierSuffix() != null) {
                classifier = stylesheets.getMinifierSuffix();
            }
        } else {
            classifier = "";
        }
        return new File(getInternalAssetOutputDirectory(), project.getArtifactId() + classifier + ".css");
    }

    /**
     * Checks that the given file is a valid CSS files.
     *
     * @param file is the file.
     * @return {@literal true} if the watcher is interested in being notified on an event
     * attached to the given file,
     * {@literal false} otherwise.
     */
    @Override
    public boolean accept(File file) {
        return
                (WatcherUtils.isInDirectory(file, WatcherUtils.getExternalAssetsSource(basedir))
                        || (WatcherUtils.isInDirectory(file, WatcherUtils.getInternalAssetsSource(basedir)))
                )
                        && WatcherUtils.hasExtension(file, "css")
                        && isNotMinified(file)
                        && JavaScriptCompilerMojo.isNotInLibs(file);
    }

    /**
     * Checks to see if the file is already minified.
     *
     * @param file the current file we are looking at.
     * @return a boolean.
     */
    public boolean isNotMinified(File file) {
        return !file.getName().endsWith("min.css")
                && !file.getName().endsWith(cssMinifierSuffix + ".css");
    }

    /**
     * Minifies the created files.
     *
     * @param file is the file.
     * @return {@literal false} if the pipeline processing must be interrupted for this event. Most watchers should
     * return {@literal true} to let other watchers be notified.
     * @throws org.wisdom.maven.WatchingException if the watcher failed to process the given file.
     */
    @Override
    public boolean fileCreated(File file) throws WatchingException {
        if (stylesheets != null) {
            try {
                process(stylesheets);
            } catch (MojoExecutionException e) {
                throw new WatchingException("Error while aggregating or minifying CSS resources", file, e);
            }
        } else {
            process(file);
        }
        return true;
    }

    /**
     * Minifies the updated files.
     *
     * @param file is the file.
     * @return {@literal false} if the pipeline processing must be interrupted for this event. Most watchers should
     * returns {@literal true} to let other watchers to be notified.
     * @throws org.wisdom.maven.WatchingException if the watcher failed to process the given file.
     */
    @Override
    public boolean fileUpdated(File file) throws WatchingException {
        return fileCreated(file);
    }

    /**
     * Cleans the output file if any.
     *
     * @param file the file
     * @return {@literal false} if the pipeline processing must be interrupted for this event. Most watchers should
     * return {@literal true} to let other watchers be notified.
     * @throws org.wisdom.maven.WatchingException if the watcher failed to process the given file.
     */
    @Override
    public boolean fileDeleted(File file) throws WatchingException {
        if (isNotMinified(file)) {
            File minified = getMinifiedFile(file);
            FileUtils.deleteQuietly(minified);
        }
        return true;
    }

    /**
     * Minifies the CSS file using Clean CSS.
     *
     * @param file that we wish to minify.
     * @throws WatchingException if errors occur during minification.
     */
    private void process(File file) throws WatchingException {
        getLog().info("Minifying CSS files from " + file.getName() + " using Clean CSS");

        File filtered = getFilteredVersion(file);
        if (filtered == null) {
            filtered = file;
        }

        File output = getMinifiedFile(file);
        if (output.exists()) {
            FileUtils.deleteQuietly(output);
        }

        if (! output.getParentFile().isDirectory()) {
            getLog().debug("Creating output directory for " + output.getAbsolutePath() + " : "
                    + output.getParentFile().mkdirs());
        }

        getLog().info("Minifying " + filtered.getAbsolutePath() + " to " + output.getAbsolutePath());
        try {
            int exit = cleancss.execute("cleancss", "-o", output.getAbsolutePath(),
                    filtered.getAbsolutePath());
            getLog().debug("CSS minification execution exiting with " + exit + " status");
        } catch (MojoExecutionException e) {
            throw new WatchingException("Error during the minification of " + filtered.getName(), e);
        }
    }

    /**
     * Creates out minified output file replacing the current extension with the minified one.
     *
     * @param input the file to minify.
     * @return the output file where the minified code will go.
     */
    protected File getMinifiedFile(File input) {
        File output = getOutputFile(input);
        return new File(output.getParentFile().getAbsoluteFile(),
                output.getName().replace(".css", cssMinifierSuffix + ".css"));
    }
}

