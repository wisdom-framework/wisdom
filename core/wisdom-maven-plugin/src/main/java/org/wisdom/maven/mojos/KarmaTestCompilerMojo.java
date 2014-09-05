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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.node.NPM;
import org.wisdom.maven.utils.WatcherUtils;

import java.io.File;
import java.util.List;

/**
 * Executes Karma test from {@literal src/test/javascript}. It requires a Karma configuration file as:
 * <pre>{@code
 * module.exports = function(config) {
 *  config.set({
 *      basePath: '../../..',
 *      frameworks: ['jasmine'],
 *      files: [
 *          'target/classes/assets/square.js',
 *          'src/test/javascript/*.js'
 *      ],
 *      exclude: ['src/test/javascript/karma.conf*.js'],
 *      port: 9876,
 *      logLevel: config.LOG_INFO,
 *      browsers: ['PhantomJS'],
 *      singleRun: true,
 *      plugins: [
 *          'karma-jasmine',
 *          'karma-phantomjs-launcher',
 *          'karma-junit-reporter'
 *      ],
 *      reporters:['progress', 'junit'],
 *      junitReporter: {
 *          outputFile: 'target/surefire-reports/karma-test-results.xml',
 *          suite: ''
 *      }
 *    });
 * };}</pre>
 * <p>
 * It is recommended to use {@literal target/classes/assets/*} as sources instead of {@literal
 * src/main/resources/assets}, so file are already preprocessed.
 * <p>
 * Used plugins must be also configured in the plugin configuration to be installed automatically.
 */
@Mojo(name = "test-javascript", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.TEST,
        requiresProject = true,
        defaultPhase = LifecyclePhase.TEST_COMPILE)
public class KarmaTestCompilerMojo extends AbstractWisdomWatcherMojo {


    /**
     * Path to your karma configuration file, relative to the working directory (default is "src/test/javascript/karma
     * .conf.js")
     */
    @Parameter(defaultValue = "src/test/javascript/karma.conf.js", property = "karmaConfPath")
    private File karmaConfPath;

    /**
     * Whether you should skip running the tests (default is false)
     */
    @Parameter(property = "skipTests", required = false, defaultValue = "false")
    private Boolean skipTests;

    /**
     * Whether you should continue build when some test will fail (default is false)
     */
    @Parameter(property = "testFailureIgnore", required = false, defaultValue = "false")
    private Boolean testFailureIgnore;

    /**
     * The Karma version to use.
     */
    @Parameter(defaultValue = "0.12.23")
    String karmaVersion;

    /**
     * The list of Karma plugin that are going to be used, and so need to be installed. The list is given as follows:
     * <pre>
     *     {@code
     *      <karmaPlugins>
     *          <plugin>karma-jasmine,0.1.5</plugin>
     *          <plugin>karma-phantomjs-launcher,0.1.1</plugin>
     *          <plugin>karma-junit-reporter</plugin>
     *      </karmaPlugins>
     *     }
     * </pre>
     * The two first plugins define the version to be used. The last one uses the latest version.
     */
    @Parameter
    List<String> karmaPlugins;

    /**
     * the Karma NPM.
     */
    private NPM npm;

    /**
     * The Karma executable.
     */
    private File karma;

    /**
     * Initializes and executes Karma tests. This method installs Karma and its dependencies. It also checks that the
     * installation provides the Karma executable.
     *
     * @throws MojoExecutionException if the installation of Karma failed
     * @throws MojoFailureException   if the Karma test failed.
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skipTests) {
            getLog().info("Skipping karma tests.");
            removeFromWatching();
            return;
        }

        if (!karmaConfPath.isFile()) {
            getLog().info("Karma configuration missing (" + karmaConfPath.getAbsolutePath() + "), skipping Karma test");
            return;
        }

        npm = NPM.npm(this, "karma", karmaVersion);
        File karmaDir = new File(getNodeManager().getNodeModulesDirectory(), "karma");
        if (!karmaDir.isDirectory()) {
            throw new MojoExecutionException("Cannot find the path of Karma node module: "
                    + karmaDir.getAbsolutePath());
        }
        File karma = new File(karmaDir, "bin/karma");
        if (!karma.isFile()) {
            throw new MojoExecutionException("Cannot find the path to Karma: " + karma.getAbsolutePath());
        }

        installDependencies();

        launchKarmaTests();
    }

    /**
     * Launches the Karma tests
     *
     * @throws MojoFailureException the test failed. This exception is not throw if {@link #testFailureIgnore} is set
     *                              to {@code true}.
     */
    private void launchKarmaTests() throws MojoFailureException {
        try {
            npm.registerOutputStream(true);
            npm.execute(karma, "start", karmaConfPath.getAbsolutePath(), "--no-auto-watch", "--no-colors");
        } catch (MojoExecutionException e) {
            if (testFailureIgnore) {
                return;
            }
            throw new MojoFailureException("Karma Test Failures", e);
        }
    }

    /**
     * Installs the listed dependencies.
     */
    private void installDependencies() {
        if (karmaPlugins == null || karmaPlugins.isEmpty()) {
            getLog().warn("No karma plugin specified, relying on already installed NPM modules");
            return;
        }

        for (String s : karmaPlugins) {
            String[] segments = s.split(",");
            if (segments.length == 1) {
                NPM.npm(this, s, null);
            } else {
                NPM.npm(this, segments[0], segments[1]);
            }
        }
    }

    /**
     * The <em>watch</em> filter.
     *
     * @param file is the file.
     * @return {@code true} if the file has ".js", ".ts" ".coffee" as extension.
     */
    @Override
    public boolean accept(File file) {
        return WatcherUtils.hasExtension(file, "js", "coffee", "ts");
    }

    /**
     * Method called when a file is created, updated or deleted (as it's the same action every time). It executes the
     * Karma tests.
     *
     * @param file is the file.
     * @return {@code true}
     * @throws WatchingException if the tests failed.
     */
    @Override
    public boolean fileCreated(File file) throws WatchingException {
        try {
            launchKarmaTests();
            return true;
        } catch (MojoFailureException e) {
            throw new WatchingException("Karma Test Failures", computeMessage(), file, null);
        }
    }

    /**
     * Retrieves the output stream of the NPM execution, and prepare it to be used in the HTML error report (pipeline
     * error). Basically, it remove ANSI characters.
     *
     * @return the computed message
     */
    private String computeMessage() {
        return npm.getLastOutputStream()
                .replaceAll("\u001B\\[[;\\d]*[ -/]*[@-~]", "")
                .replace("\n", "<br/>")
                .replace("FAILED", "<strong>FAILED</strong>");

    }

    /**
     * Calls {@link #fileCreated(java.io.File)}.
     * @param file is the file.
     * @return {@code true}
     * @throws WatchingException if the tests failed.
     */
    @Override
    public boolean fileUpdated(File file) throws WatchingException {
        return fileCreated(file);
    }

    /**
     * Calls {@link #fileCreated(java.io.File)}.
     * @param file is the file.
     * @return {@code true}
     * @throws WatchingException if the tests failed.
     */
    @Override
    public boolean fileDeleted(File file) throws WatchingException {
        return fileCreated(file);
    }
}
