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

import com.typesafe.config.*;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.core.services.AbstractDeployer;
import org.ow2.chameleon.core.services.Deployer;
import org.ow2.chameleon.core.services.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.configuration.Configuration;
import org.wisdom.api.content.ParameterFactories;

import java.io.File;
import java.util.*;

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
    private final BundleContext context;
    private ServiceRegistration<Deployer> registration;
    private Map<Configuration, ServiceRegistration<Configuration>> confRegistrations = new HashMap<>();

    /**
     * This service controller let unregisters the service when the configuration is reloaded.
     */
    @ServiceController(value = false)
    boolean controller;

    /**
     * The watcher service.
     * It was injected in the constructor, but leads to issue when not available. This is an iPOJO bug.
     */
    @Requires(optional = true, nullable = true)
    Watcher watcher;

    /**
     * The configuration file.
     */
    private final File configFile;

    /**
     * The configuration.
     */
    private Config appConf;

    /**
     * Creates the application configuration object.
     *
     * @param converters the ParameterFactories service
     * @param context    the Bundle Context
     */
    public ApplicationConfigurationImpl(@Requires ParameterFactories converters,
                                        @Context BundleContext context) {
        super(converters);
        String location = reloadConfiguration();
        this.context = context;

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

        configFile = new File(location);
        if (!configFile.isFile()) {
            LOGGER.error("Cannot load the application configuration (" + location + ") - the file does not exist, " +
                    "Wisdom is using system properties only");
            baseDirectory = new File("");
        } else {
            // The base directory is the parent of the parent
            // getParentFile must be call on an absolute file, if not `null` is returned.
            baseDirectory = configFile.getParentFile().getAbsoluteFile().getParentFile();
            LOGGER.info("Configuration file : {}", configFile.getAbsoluteFile());
            manageWatcher(context);
        }

        LOGGER.info("Base directory : {}", baseDirectory.getAbsoluteFile());
        LOGGER.info("Wisdom running in " + this.mode.toString());
    }

    protected void manageWatcher(BundleContext context) {
        if (context != null && (isDev() || getBooleanWithDefault("application.watch-configuration", false))
                && watcher != null) {
            LOGGER.info("Enabling the watching of the configuration file");
            watcher.add(configFile.getParentFile(), true);
            registration = context.registerService(Deployer.class, new ConfigurationDeployer(), null);
        }
    }

    @Validate
    public void start() {
        // Publish the service.
        controller = true;
        registerFirstLevelConfigurationAsServices();

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

        Config configuration = loadConfiguration(location);

        if (configuration == null) {
            throw new IllegalStateException("Cannot load the application configuration (" + location + ") - Wisdom cannot " +
                    "work properly without such configuration");
        }

        setConfiguration(configuration);

        return location;
    }

    /**
     * Stops the service.
     */
    @Invalidate
    public void stop() {
        unregisterConfigurationsExposedAsServices();
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

    private Config loadConfiguration(String location) {
        ConfigFactory.invalidateCaches();
        if (location != null) {
            File file = new File(location);
            appConf = ConfigFactory.parseFileAnySyntax(file, ConfigParseOptions.defaults().setSyntax
                    (ConfigSyntax.CONF));
            Properties properties = new Properties();
            properties.put(APPLICATION_BASEDIR, file.getParentFile().getAbsoluteFile().getParentFile().getAbsolutePath());
            return
                    ConfigFactory
                            .defaultOverrides()
                            .withFallback(appConf)
                            .withFallback(ConfigFactory.parseProperties(properties))
                            .resolve();
        } else {
            appConf = ConfigFactory.defaultOverrides();
            return
                    ConfigFactory
                            .defaultOverrides()
                            .resolve();
        }
    }

    /**
     * {@inheritDoc}.
     */
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
     * @return the property or {@code null} if not there or if the property is not a boolean.
     */
    @Override
    public Boolean getBoolean(String key) {
        Boolean r = super.getBoolean(key);
        if (r == null) {
            LOGGER.error(ERROR_NOSUCHKEY + key + "\"");
            return null; //NOSONAR returning null to denotes that the property is not present.
        }
        return r;
    }

    /**
     * @param key the key
     * @return the property or {@code null} if not there or if the property is not a long.
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
     * @return {@code true} if we are in dev mode
     */
    @Override
    public boolean isDev() {
        return mode == Mode.DEV;
    }

    /**
     * Whether we are in test mode.
     *
     * @return {@code true} if we are in test mode
     */
    @Override
    public boolean isTest() {
        return mode == Mode.TEST;
    }

    /**
     * Whether we are in prod mode.
     *
     * @return {@code true} if we are in prod mode
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
            unregisterConfigurationsExposedAsServices();
            controller = false;
            reloadConfiguration();
            controller = true;
            registerFirstLevelConfigurationAsServices();
        }
    }

    protected void unregisterConfigurationsExposedAsServices() {
        // Unregister all services if not done yet
        for (ServiceRegistration<Configuration> conf : confRegistrations.values()) {
            conf.unregister();
        }
        confRegistrations.clear();
    }

    private void registerFirstLevelConfigurationAsServices() {
        // Registers configuration
        if (appConf == null) {
            return;
        }
        for (Map.Entry<String, ConfigValue> entry : appConf.root().entrySet()) {
            if (entry.getValue().valueType() == ConfigValueType.OBJECT) {
                // Register it.
                Dictionary<String, String> properties = new Hashtable<>();
                properties.put("configuration.name", entry.getKey());
                properties.put("configuration.path", entry.getKey());

                final ConfigurationImpl cf = new
                        ConfigurationImpl(converters, appConf.getConfig(entry.getKey()));
                ServiceRegistration<Configuration> reg = context.registerService(Configuration.class, cf,
                        properties);
                confRegistrations.put(cf, reg);
            }
        }
    }
}
