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

/**
 * Minifies CSS files using <a href="https://github.com/GoalSmashers/clean-css">clean-css</a>.
 * It takes all CSS files form the internal and external assets directories (so
 * src/main/resources/assets, and src/main/assets) and minifies them.
 */

@Mojo(name = "minify-css", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class CSSMinifierMojo extends AbstractWisdomWatcherMojo {

    public static final String CLEANCSS_NPM_NAME = "clean-css";
    public static final String CLEANCSS_NPM_VERSION = "2.2.8";

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
        cleancss = NPM.npm(this, CLEANCSS_NPM_NAME, CLEANCSS_NPM_VERSION);

        for (File file : getResources(ImmutableList.of("css"))) {
            try {
                minify(file);
            } catch (WatchingException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
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
                        || (WatcherUtils.isInDirectory(file, WatcherUtils.getResources(basedir)))
                )
                        && WatcherUtils.hasExtension(file, "css")
                        && !isMinified(file)
                        && !JavaScriptCompilerMojo.isInLibs(file);
    }

    /**
     * Checks to see if the file is already minified.
     *
     * @param file the current file we are looking at.
     * @return a boolean.
     */
    public boolean isMinified(File file) {
        return file.getName().endsWith("min.css")
                || file.getName().endsWith(cssMinifierSuffix + ".css");
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
        minify(file);
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
        if (!isMinified(file)) {
            File minified = getMinifiedFile(file);
            if (minified.isFile()) {
                FileUtils.deleteQuietly(minified);
            }
        }
        return true;
    }

    /**
     * Minifies the CSS file using Clean CSS.
     *
     * @param file that we wish to minify.
     * @throws WatchingException if errors occur during minification.
     */
    private void minify(File file) throws WatchingException {
        getLog().info("Minifying CSS files from " + file.getName() + " using Clean CSS");

        File filtered = getFilteredVersion(file);
        if (filtered == null) {
            filtered = file;
        }


        File output = getMinifiedFile(file);
        if (output.exists()) {
            FileUtils.deleteQuietly(output);
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
    private File getMinifiedFile(File input) {
        File output = getOutputFile(input);
        return new File(output.getParentFile().getAbsoluteFile(),
                output.getName().replace(".css", cssMinifierSuffix + ".css"));
    }
}

