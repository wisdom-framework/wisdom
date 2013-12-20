
package org.wisdom.api.configuration;

import java.io.File;
import java.util.Properties;

/**
 * Service interface to access application configuration.
 */
public interface ApplicationConfiguration {

    /**
     * The default configuration. Make sure that file exists. Otherwise the
     * application won't start up.
     */
    String CONF_FILE_LOCATION_BY_CONVENTION = "conf/application.conf";

    /**
     * The application secret key.
     */
    String APPLICATION_SECRET = "application.secret";
    
    /**
     * The application initialization vector.
     */
    String APPLICATION_IV = "application.iv";

    /**
     * The HTTP port key.
     */
    String HTTP_PORT = "http.port";

    /**
     * The HTTPS port key.
     */
    String HTTPS_PORT = "https.port";


    /**
     * Gets the base directory of the Wisdom application.
     * @return the base directory
     */
    File getBaseDir();

    /**
     * Get a String property or null if it is not there...
     * 
     * @param key the key
     * @return the property of null if not there
     */
    String get(String key);

    /**
     * Get a String property or a default value when property cannot be found in
     * any configuration file.
     * 
     * @param key
     *            the key used in the configuration file.
     * @param defaultValue
     *            Default value returned, when value cannot be found in
     *            configuration.
     * @return the value of the key or the default value.
     */
    String getWithDefault(String key, String defaultValue);

    /**
     * Get a property as Integer of null if not there / or property no integer
     * 
     * @param key
     * @return the property or null if not there or property no integer
     */
    Integer getInteger(String key);

    /**
     * Get a Integer property or a default value when property cannot be found
     * in any configuration file.
     * 
     * @param key
     *            the key used in the configuration file.
     * @param defaultValue
     *            Default value returned, when value cannot be found in
     *            configuration.
     * @return the value of the key or the default value.
     */
    Integer getIntegerWithDefault(String key, Integer defaultValue);

    /**
     * 
     * @param key
     * @return the property or null if not there or property no boolean
     */
    Boolean getBoolean(String key);

    /**
     * Get a Boolean property or a default value when property cannot be found
     * in any configuration file.
     * 
     * @param key
     *            the key used in the configuration file.
     * @param defaultValue
     *            Default value returned, when value cannot be found in
     *            configuration.
     * @return the value of the key or the default value.
     */
    Boolean getBooleanWithDefault(String key, Boolean defaultValue);

    /**
     * The "die" method forces this key to be set. Otherwise a runtime exception
     * will be thrown.
     * 
     * @param key
     * @return the boolean or a RuntimeException will be thrown.
     */
    Boolean getBooleanOrDie(String key);

    /**
     * The "die" method forces this key to be set. Otherwise a runtime exception
     * will be thrown.
     * 
     * @param key
     * @return the Integer or a RuntimeException will be thrown.
     */
    Integer getIntegerOrDie(String key);

    /**
     * The "die" method forces this key to be set. Otherwise a runtime exception
     * will be thrown.
     * 
     * @param key
     * @return the String or a RuntimeException will be thrown.
     */
    String getOrDie(String key);

    /**
     * eg. key=myval1,myval2
     * 
     * Delimiter is a comma "," as outlined in the example above.
     * 
     * @return an array containing the values of that key or null if not found.
     */
    String[] getStringArray(String key);

    /**
     * Whether we are in dev mode
     * 
     * @return True if we are in dev mode
     */
    boolean isDev();

    /**
     * Whether we are in test mode
     * 
     * @return True if we are in test mode
     */
    boolean isTest();

    /**
     * Whether we are in prod mode
     * 
     * @return True if we are in prod mode
     */
    boolean isProd();

    /**
     * 
     * @return All properties that are currently loaded from internal and
     *         external files
     */
    Properties getAllCurrentProperties();

    /**
     * Get a File property or a default value when property cannot be found in
     * any configuration file.
     * The file object is constructed using <code>new File(basedir, value)</code>.
     * @param key the key
     * @param file the default file
     * @return the file object
     */
    File getFileWithDefault(String key, String file);

    /**
     * Get a File property or a default value when property cannot be found in
     * any configuration file.
     * The file object is constructed using <code>new File(basedir, value)</code>.
     * @param key the key
     * @param file the default file
     * @return the file object
     */
    File getFileWithDefault(String key, File file);
}
