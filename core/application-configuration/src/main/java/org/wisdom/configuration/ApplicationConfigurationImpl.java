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
package org.wisdom.configuration;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.core.services.AbstractDeployer;
import org.ow2.chameleon.core.services.Deployer;
import org.ow2.chameleon.core.services.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.content.ParameterFactories;

import java.io.File;

/**
 * Implementation of the configuration service reading application/conf and an external (optional) property.
 */
@Component
@Provides
@Instantiate
public class ApplicationConfigurationImpl extends ConfigurationImpl implements org.wisdom.api.configuration
        .ApplicationConfiguration {

    public static final String APPLICATION_CONFIGURATION = "application.configuration";
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfigurationImpl.class);
    private final Mode mode;
    private final File baseDirectory;
    private static final String APPMODE = "application.mode";
    private ServiceRegistration<Deployer> registration;

    /**
     * This service controller let unregisters the service when the configuration is reloaded.
     */
    @ServiceController(value = false)
    boolean controller;

    Watcher watcher;

    /**
     * The configuration file.
     */
    private final File configFile;

    /**
     * Creates the application configuration object.
     *
     * @param converters the ParameterConvert service
     * @param context    the Bundle Context
     * @param watcher    the Watcher service
     */
    public ApplicationConfigurationImpl(@Requires ParameterFactories converters,
                                        @Context BundleContext context,
                                        @Requires(optional = true) Watcher watcher) {
        super(converters);
        String location = reloadConfiguration();

        configFile = new File(location);
        // The base directory is the parent of the parent
        // getParentFile must be call on an absolute file, if not `null` is returned.
        baseDirectory = configFile.getParentFile().getAbsoluteFile().getParentFile();

        // Determine the mode.
        String localMode = System.getProperty(APPMODE);
        if (localMode == null) {
            localMode = get(APPMODE);
        }
        if (localMode == null) {
            this.mode = Mode.DEV;
        } else {
            this.mode = Mode.valueOf(localMode);
        }

        if (context != null && (isDev() || getBooleanWithDefault("application.configuration.watch", false))) {
            this.watcher = watcher;
            LOGGER.info("Enabling the watching of the configuration file");
            watcher.add(configFile.getParentFile(), true);
            registration = context.registerService(Deployer.class, new ConfigurationDeployer(), null);
        }

        LOGGER.info("Configuration file : {}", configFile.getAbsoluteFile());
        LOGGER.info("Base directory : {}", baseDirectory.getAbsoluteFile());
        LOGGER.info("Wisdom running in " + this.mode.toString());
    }

    @Validate
    public void start() {
        // Publish the service.
        controller = true;
    }

    /**
     * Reloads the configuration file.
     *
     * @return the location of the file.
     */
    private String reloadConfiguration() {
        String location = System.getProperty(APPLICATION_CONFIGURATION);
        if (location == null) {
            location = "conf/application.conf";
        }

        PropertiesConfiguration configuration = loadConfigurationInUtf8(location);

        if (configuration == null) {
            throw new IllegalStateException("Cannot load the application configuration (" + location + ") - Wisdom cannot " +
                    "work properly with such configuration");
        }

        setConfiguration(configuration);
        return location;
    }

    @Invalidate
    public void stop() {
        if (registration != null) {
            registration.unregister();
            registration = null;
            try {
                watcher.removeAndStopIfNeeded(configFile.getParentFile());
            } catch (RuntimeException e) { //NOSONAR
                // An exception can be thrown when the platform is shutting down.
                // ignore it.
            }
        }

    }

    /**
     * This is important: We load stuff as UTF-8.
     * <p>
     * We are using in the default Apache Commons loading mechanism.
     * <p>
     * With two little tweaks: 1. We don't accept any delimimter by default 2.
     * We are reading in UTF-8
     * <p>
     * More about that:
     * http://commons.apache.org/configuration/userguide/howto_filebased
     * .html#Loading
     * <p>
     * From the docs: - If the combination from base path and file name is a
     * full URL that points to an existing file, this URL will be used to load
     * the file. - If the combination from base path and file name is an
     * absolute file name and this file exists, it will be loaded. - If the
     * combination from base path and file name is a relative file path that
     * points to an existing file, this file will be loaded. - If a file with
     * the specified name exists in the user's home directory, this file will be
     * loaded. - Otherwise the file name is interpreted as a resource name, and
     * it is checked whether the data file can be loaded from the classpath.
     *
     * @param fileOrUrlOrClasspathUrl Location of the file. Can be on file system, or on the
     *                                classpath. Will both work.
     * @return A PropertiesConfiguration or null if there were problems getting it.
     */
    public final PropertiesConfiguration loadConfigurationInUtf8(String fileOrUrlOrClasspathUrl) {

        PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
        propertiesConfiguration.setEncoding("utf-8");
        propertiesConfiguration.setDelimiterParsingDisabled(true);
        propertiesConfiguration.setFileName(fileOrUrlOrClasspathUrl);
        propertiesConfiguration.getLayout().setSingleLine(APPLICATION_SECRET, true);

        try {
            propertiesConfiguration.load(fileOrUrlOrClasspathUrl);
        } catch (ConfigurationException e) {
            LOGGER.info("Could not load file " + fileOrUrlOrClasspathUrl
                    + " (not a bad thing necessarily, but you should have a look)", e);
            return null;
        }

        return propertiesConfiguration;
    }

    @Override
    public File getBaseDir() {
        return baseDirectory;
    }


    /**
     * Get a property as Integer or null if not there / or if the property is not an integer.
     *
     * @param key the key
     * @return the property or {@literal null} if not there or property no integer
     */
    @Override
    public Integer getInteger(String key) {
        Integer r = super.getInteger(key);
        if (r == null) {
            LOGGER.error(ERROR_NOSUCHKEY + key + "\"");
            return null;
        }
        return r;
    }

    /**
     * @param key the key
     * @return the property or null if not there or if the property is not a boolean.
     */
    @Override
    public Boolean getBoolean(String key) {
        Boolean r = super.getBoolean(key);
        if (r == null) {
            LOGGER.error(ERROR_NOSUCHKEY + key + "\"");
            return null;
        }
        return r;
    }

    /**
     * @param key the key
     * @return the property or null if not there or if the property is not a long.
     */
    @Override
    public Long getLong(String key) {
        Long r = super.getLong(key);
        if (r == null) {
            LOGGER.error(ERROR_NOSUCHKEY + key + "\"");
            return null;
        }
        return r;
    }

    /**
     * Whether we are in dev mode.
     *
     * @return True if we are in dev mode
     */
    @Override
    public boolean isDev() {
        return mode == Mode.DEV;
    }

    /**
     * Whether we are in test mode.
     *
     * @return True if we are in test mode
     */
    @Override
    public boolean isTest() {
        return mode == Mode.TEST;
    }

    /**
     * Whether we are in prod mode.
     *
     * @return True if we are in prod mode
     */
    @Override
    public boolean isProd() {
        return mode == Mode.PROD;
    }

    /**
     * Get a File property or a default value when property cannot be found in
     * any configuration file.
     * The file object is constructed using <code>new File(basedir, value)</code>.
     *
     * @param key  the key
     * @param file the default file
     * @return the file object
     */
    @Override
    public File getFileWithDefault(String key, String file) {
        String value = get(key);
        if (value == null) {
            return new File(baseDirectory, file);
        } else {
            return new File(baseDirectory, value);
        }
    }

    /**
     * Get a File property or a default value when property cannot be found in
     * any configuration file.
     * The file object is constructed using <code>new File(basedir, value)</code>.
     *
     * @param key  the key
     * @param file the default file
     * @return the file object
     */
    @Override
    public File getFileWithDefault(String key, File file) {
        String value = get(key);
        if (value == null) {
            return file;
        } else {
            return new File(baseDirectory, value);
        }
    }

    public ParameterFactories getConverters() {
        return converters;
    }

    private class ConfigurationDeployer extends AbstractDeployer {


        /**
         * Checks that the file is the configuration file.
         *
         * @param file the file
         * @return {@literal true} if the given file is the configuration file.
         */
        @Override
        public boolean accept(File file) {
            return file.getAbsoluteFile().equals(configFile.getAbsoluteFile());
        }

        /**
         * The configuration file is updated.
         *
         * @param file the configuration file
         */
        @Override
        public void onFileChange(File file) {
            controller = false;
            reloadConfiguration();
            controller = true;
        }
    }
}
