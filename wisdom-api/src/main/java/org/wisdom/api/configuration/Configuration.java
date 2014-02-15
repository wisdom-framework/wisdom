package org.wisdom.api.configuration;

import java.util.Map;
import java.util.Properties;

/**
 * Created by clement on 15/02/2014.
 */
public interface Configuration {
    /**
     * Gets a configuration object with all the properties starting with the given prefix.
     * @param prefix the prefix (without the ending `.`)
     * @return a configuration object with all properties with a name starting with `prefix.`,
     * or {@literal null} if no properties start with the given prefix.
     */
    Configuration getConfiguration(String prefix);


    /**
     * Get a String property or {@literal null} if it is not there...
     *
     * @param key the key used in the configuration file.
     * @return the property of null if not there
     */
    String get(String key);

    /**
     * Get a String property or a default value when property cannot be found in
     * any configuration file.
     *
     * @param key          the key used in the configuration file.
     * @param defaultValue Default value returned, when value cannot be found in
     *                     configuration.
     * @return the value of the key or the default value.
     */
    String getWithDefault(String key, String defaultValue);

    /**
     * Get a property as Integer or {@literal null} if not there / or if the property is not an integer
     *
     * @param key the key used in the configuration file.
     * @return the property or {@literal null} if not there or property no integer
     */
    Integer getInteger(String key);

    /**
     * Get a Integer property or a default value when property cannot be found
     * in any configuration file.
     *
     * @param key          the key used in the configuration file.
     * @param defaultValue Default value returned, when value cannot be found in
     *                     configuration.
     * @return the value of the key or the default value.
     */
    Integer getIntegerWithDefault(String key, Integer defaultValue);

    /**
     * Get a property as Boolean or {@literal null} if not there / or if the property is not an integer
     *
     * @param key the key used in the configuration file.
     * @return the property or {@literal null} if not there or property no boolean
     */
    Boolean getBoolean(String key);

    /**
     * Get a Boolean property or a default value when property cannot be found
     * in any configuration file.
     *
     * @param key          the key used in the configuration file.
     * @param defaultValue Default value returned, when value cannot be found in
     *                     configuration.
     * @return the value of the key or the default value.
     */
    Boolean getBooleanWithDefault(String key, Boolean defaultValue);

    /**
     * The "die" method forces this key to be set. Otherwise a runtime exception
     * will be thrown.
     *
     * @param key the key used in the configuration file.
     * @return the boolean or a RuntimeException will be thrown.
     */
    Boolean getBooleanOrDie(String key);

    /**
     * The "die" method forces this key to be set. Otherwise a runtime exception
     * will be thrown.
     *
     * @param key the key used in the configuration file.
     * @return the Integer or a RuntimeException will be thrown.
     */
    Integer getIntegerOrDie(String key);

    /**
     * The "die" method forces this key to be set. Otherwise a runtime exception
     * will be thrown.
     *
     * @param key the key used in the configuration file.
     * @return the String or a RuntimeException will be thrown.
     */
    String getOrDie(String key);

    /**
     * eg. key=myval1,myval2
     * <p/>
     * Delimiter is a comma "," as outlined in the example above.
     *
     * @return an array containing the values of that key or {@literal null} if not found.
     */
    String[] getStringArray(String key);

    /**
     * @return All properties that are currently loaded from internal and
     * external files
     */
    Properties asProperties();

    /**
     * @return All properties that are currently loaded from internal and
     *         external files
     */
    Map<String, Object> asMap();
}
