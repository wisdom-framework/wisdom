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
import org.osgi.framework.BundleException;
import org.ow2.chameleon.core.Chameleon;
import org.ow2.chameleon.core.ChameleonConfiguration;
import org.wisdom.maven.utils.ChameleonInstanceHolder;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Executes Karma test from {@literal src/test/javascript}. It requires a Karma configuration file as:
 * <pre>{@code
 * module.exports = function(config) {
 *  config.set({
 *      basePath: '${basedir}',
 *      frameworks: ['jasmine'],
 *      files: [
 *          '${project.build.outputDirectory}/assets/square.js',
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
 * Used plugins must be also configured in the plugin configuration to be installed automatically. The configuration
 * can contain Maven variables replaced by filtering.
 */
@Mojo(name = "test-javascript-it", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.TEST,
        requiresProject = true,
        defaultPhase = LifecyclePhase.INTEGRATION_TEST)
public class KarmaIntegrationTestMojo extends KarmaUnitTestMojo {

    /**
     * Path to your karma configuration file, relative to the working directory (default is
     * "src/test/javascript/karma.conf-it.js")
     */
    @Parameter(defaultValue = "src/test/javascript/karma.conf-it.js", property = "karmaITConfPath")
    private File karmaITConfPath;

    @Override
    File getConfiguration() {
        return karmaITConfPath;
    }

    public void execute() throws MojoFailureException, MojoExecutionException {
        removeFromWatching();
        try {
            startChameleon();

            super.execute();
        } catch (IOException | BundleException e) {
            // Others exception are related to the Chameleon startup / handling
            getLog().error("Cannot start the Chameleon instance", e);
            throw new MojoExecutionException("Cannot start the Chameleon instance", e);
        } finally {
            // This property is set when launching the Chameleon instance to find the configuration. Clear it.
            System.clearProperty("application.configuration");
            stopChameleon();
        }
    }

    @Override
    Properties getAdditionalPropertiesForConfigurationFiltering() {
        // Extract the metadata of the server, so the karma configuration filtering can rely on them.
        Properties additional = new Properties();
        try {
            // The filtering component consuming this object, only support Strings.
            additional.put("httpPort", String.valueOf(ChameleonInstanceHolder.getHttpPort()));
            additional.put("httpsPort", String.valueOf(ChameleonInstanceHolder.getHttpsPort()));
            additional.put("hostname", ChameleonInstanceHolder.getHostName());
        } catch (Exception e) {
            throw new IllegalStateException("Cannot retrieve the metadata from the running Chameleon");
        }
        return additional;
    }

    private void startChameleon() throws IOException, BundleException {
        if (ChameleonInstanceHolder.get() != null) {
            getLog().info("Reusing running Chameleon");
        } else {
            ChameleonConfiguration configuration = new ChameleonConfiguration(getWisdomRootDirectory());
            // Use a different cache for testing.
            configuration.put("org.osgi.framework.storage",
                    getWisdomRootDirectory().getAbsolutePath() + "/chameleon-test-cache");

            // Set the httpPort to 0 to use the random port feature.
            // Except if already set explicitly
            String port = System.getProperty("http.port");
            if (port == null) {
                System.setProperty("http.port", "0");
            }

            System.setProperty("application.configuration",
                    new File(getWisdomRootDirectory(), "/conf/application.conf").getAbsolutePath());

            Chameleon chameleon = new Chameleon(configuration);
            ChameleonInstanceHolder.fixLoggingSystem(getWisdomRootDirectory());
            chameleon.start().waitForStability();
            ChameleonInstanceHolder.set(chameleon);
        }
    }

    private void stopChameleon() {
        try {
            Chameleon reference = ChameleonInstanceHolder.get();
            if (reference != null) {
                reference.stop();
                ChameleonInstanceHolder.set(null);
            }
        } catch (Exception e) {
            getLog().error("Cannot stop the Chameleon instance gracefully", e);
        }
    }

}
