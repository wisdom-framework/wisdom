package org.wisdom.configuration;

import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

/**
 * Implementation of the configuration service reading application/conf and an external (optional) property.
 */
@Component
@Provides
@Instantiate
public class ApplicationConfiguration implements org.wisdom.api.configuration.ApplicationConfiguration {

    private final String ERROR_KEY_NOT_FOUND = "Key %s does not exist. Please include it in your application.conf. " +
            "Otherwise this application will not work";
    private final Logger logger = LoggerFactory.getLogger(ApplicationConfiguration.class);
    private final PropertiesConfiguration configuration;
    private final Mode mode;
    private final File baseDirectory;

    public ApplicationConfiguration() {
        String location = System.getProperty("application.configuration");
        if (location == null) {
            location = "conf/application.conf";
        }

        configuration = loadConfigurationInUtf8(location);

        if (configuration == null) {
            throw new RuntimeException("Cannot load the application configuration (" + location + ") - Wisdom cannot " +
                    "work properly with such configuration");
        }

        File conf = new File(location);
        // The base directory is the parent of the parent
        baseDirectory = conf.getParentFile().getParentFile();

        // Determine the mode.
        String mode = System.getProperty("application.mode");
        if (mode == null) {
            mode = get("application.mode");
        }
        if (mode == null) {
            this.mode = Mode.DEV;
        } else {
            this.mode = Mode.valueOf(mode);
        }

        logger.info("Wisdom running in " + this.mode.toString() + " mode");
    }

    /**
     * This is important: We load stuff as UTF-8.
     * <p/>
     * We are using in the default Apache Commons loading mechanism.
     * <p/>
     * With two little tweaks: 1. We don't accept any delimimter by default 2.
     * We are reading in UTF-8
     * <p/>
     * More about that:
     * http://commons.apache.org/configuration/userguide/howto_filebased
     * .html#Loading
     * <p/>
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
    public PropertiesConfiguration loadConfigurationInUtf8(String fileOrUrlOrClasspathUrl) {

        PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
        propertiesConfiguration.setEncoding("utf-8");
        propertiesConfiguration.setDelimiterParsingDisabled(true);
        propertiesConfiguration.setFileName(fileOrUrlOrClasspathUrl);
        propertiesConfiguration.getLayout().setSingleLine(APPLICATION_SECRET, true);

        try {
            propertiesConfiguration.load(fileOrUrlOrClasspathUrl);
        } catch (ConfigurationException e) {
            logger.info("Could not load file " + fileOrUrlOrClasspathUrl
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
     * Get a String property or null if it is not there...
     *
     * @param key the key
     * @return the property of null if not there
     */
    @Override
    public String get(String key) {
        String v = System.getProperty(key);
        if (v == null) {
            return configuration.getString(key);
        } else {
            return v;
        }
    }

    /**
     * Get a String property or a default value when property cannot be found in
     * any configuration file.
     *
     * @param key          the key used in the configuration file.
     * @param defaultValue Default value returned, when value cannot be found in
     *                     configuration.
     * @return the value of the key or the default value.
     */
    @Override
    public String getWithDefault(String key, String defaultValue) {
        String v = System.getProperty(key);
        if (v == null) {
            return configuration.getString(key, defaultValue);
        }
        return v;
    }

    /**
     * Get a property as Integer of null if not there / or property no integer
     *
     * @param key the key
     * @return the property or null if not there or property no integer
     */
    @Override
    public Integer getInteger(String key) {
        Integer v = Integer.getInteger(key);
        if (v == null) {
            return configuration.getInt(key);
        } else {
            return v;
        }
    }

    /**
     * Get a Integer property or a default value when property cannot be found
     * in any configuration file.
     *
     * @param key          the key used in the configuration file.
     * @param defaultValue Default value returned, when value cannot be found in
     *                     configuration.
     * @return the value of the key or the default value.
     */
    @Override
    public Integer getIntegerWithDefault(String key, Integer defaultValue) {
        Integer v = Integer.getInteger(key);
        if (v == null) {
            return configuration.getInt(key, defaultValue);
        }
        return v;
    }

    /**
     * @param key the key
     * @return the property or null if not there or property no boolean
     */
    @Override
    public Boolean getBoolean(String key) {
        if (System.getProperty(key) != null) {
            return Boolean.getBoolean(key);
        } else {
            return configuration.getBoolean(key);
        }
    }

    /**
     * Get a Boolean property or a default value when property cannot be found
     * in any configuration file.
     *
     * @param key          the key used in the configuration file.
     * @param defaultValue Default value returned, when value cannot be found in
     *                     configuration.
     * @return the value of the key or the default value.
     */
    @Override
    public Boolean getBooleanWithDefault(String key, Boolean defaultValue) {
        if (System.getProperty(key) != null) {
            return Boolean.getBoolean(key);
        } else {
            return configuration.getBoolean(key, defaultValue);
        }
    }

    /**
     * The "die" method forces this key to be set. Otherwise a runtime exception
     * will be thrown.
     *
     * @param key the key
     * @return the boolean or a RuntimeException will be thrown.
     */
    @Override
    public Boolean getBooleanOrDie(String key) {
        Boolean value = getBoolean(key);

        if (value == null) {
            logger.error(String.format(ERROR_KEY_NOT_FOUND, key));
            throw new RuntimeException(String.format(ERROR_KEY_NOT_FOUND, key));
        } else {
            return value;
        }
    }

    /**
     * The "die" method forces this key to be set. Otherwise a runtime exception
     * will be thrown.
     *
     * @param key the key
     * @return the Integer or a RuntimeException will be thrown.
     */
    @Override
    public Integer getIntegerOrDie(String key) {
        Integer value = getInteger(key);

        if (value == null) {
            logger.error(String.format(ERROR_KEY_NOT_FOUND, key));
            throw new RuntimeException(String.format(ERROR_KEY_NOT_FOUND, key));
        } else {
            return value;
        }
    }

    /**
     * The "die" method forces this key to be set. Otherwise a runtime exception
     * will be thrown.
     *
     * @param key the key
     * @return the String or a RuntimeException will be thrown.
     */
    @Override
    public String getOrDie(String key) {
        String value = get(key);

        if (value == null) {
            logger.error(String.format(ERROR_KEY_NOT_FOUND, key));
            throw new RuntimeException(String.format(ERROR_KEY_NOT_FOUND, key));
        } else {
            return value;
        }
    }

    /**
     * eg. key=myval1,myval2
     * <p/>
     * Delimiter is a comma "," as outlined in the example above.
     *
     * @return an array containing the values of that key or null if not found.
     */
    @Override
    public String[] getStringArray(String key) {
        return configuration.getStringArray(key);
    }

    /**
     * Whether we are in dev mode
     *
     * @return True if we are in dev mode
     */
    @Override
    public boolean isDev() {
        return mode == Mode.DEV;
    }

    /**
     * Whether we are in test mode
     *
     * @return True if we are in test mode
     */
    @Override
    public boolean isTest() {
        return mode == Mode.TEST;
    }

    /**
     * Whether we are in prod mode
     *
     * @return True if we are in prod mode
     */
    @Override
    public boolean isProd() {
        return mode == Mode.PROD;
    }

    /**
     * @return All properties that are currently loaded from internal and
     *         external files
     */
    @Override
    public Properties getAllCurrentProperties() {
        return ConfigurationConverter.getProperties(configuration);
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
}
