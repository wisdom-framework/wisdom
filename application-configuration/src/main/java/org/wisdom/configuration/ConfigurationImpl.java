package org.wisdom.configuration;

import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.MapConfiguration;
import org.wisdom.api.configuration.Configuration;

import java.util.*;

/**
 * An implementation of the configuration object based on Apache Commons Configuration.
 * Unlike the main application configuration, this implementation does not used a logger.
 */
public class ConfigurationImpl implements Configuration {
    
    private static final String ERROR_KEYNOTFOUND = "Key %s does not exist. Please include it in your application.conf. " +
            "Otherwise this application will not work";
    protected static final String ERROR_NOSUCHKEY = "No such key \"";

    private org.apache.commons.configuration.Configuration configuration;

    public ConfigurationImpl(org.apache.commons.configuration.Configuration configuration) {
        this.configuration = configuration;
    }

    protected ConfigurationImpl() {
        // This constructor requires an invocation of setConfiguration.
    }

    protected void setConfiguration(org.apache.commons.configuration.Configuration configuration) {
        this.configuration = configuration;
    }

    protected org.apache.commons.configuration.Configuration getConfiguration() {
        return configuration;
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
     * Get a property as Integer or null if not there / or property no integer
     *
     * @param key the key
     * @return the property or {@literal null} if not there or property no integer
     */
    @Override
    public Integer getInteger(String key) {
        Integer v = Integer.getInteger(key);
        if (v == null) {
            try {
                return configuration.getInt(key);
            } catch (NoSuchElementException e) { //NOSONAR
                return null;
            }
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
            try {
                return configuration.getBoolean(key);
            } catch (NoSuchElementException e) { //NOSONAR
                return null;
            }
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
    
    @Override
    public Long getLong(String key) {
        Long v = Long.getLong(key);
        if (v == null) {
            try {
                return configuration.getLong(key);
            } catch (NoSuchElementException e) { //NOSONAR
                return null;
            }
        } else {
            return v;
        }
    }

    @Override
    public Long getLongWithDefault(String key, Long defaultValue) {
        Long value = Long.getLong(key);
        if (value == null) {
            return configuration.getLong(key, defaultValue);
        }
        return value;
    }
    
    @Override
    public Long getLongOrDie(String key) {
        Long value = Long.getLong(key);
        if (value == null) {
            throw new IllegalArgumentException(String.format(ERROR_KEYNOTFOUND, key));
        } else {
            return value;
        }
    }

    /**
     * The "die" method forces this key to be set. Otherwise a runtime exception
     * will be thrown.
     *
     * @param key the key
     * @return the boolean or a IllegalArgumentException will be thrown.
     */
    @Override
    public Boolean getBooleanOrDie(String key) {
        Boolean value = getBoolean(key);

        if (value == null) {
            throw new IllegalArgumentException(String.format(ERROR_KEYNOTFOUND, key));
        } else {
            return value;
        }
    }

    /**
     * The "die" method forces this key to be set. Otherwise a runtime exception
     * will be thrown.
     *
     * @param key the key
     * @return the Integer or a IllegalArgumentException will be thrown.
     */
    @Override
    public Integer getIntegerOrDie(String key) {
        Integer value = getInteger(key);

        if (value == null) {
            throw new IllegalArgumentException(String.format(ERROR_KEYNOTFOUND, key));
        } else {
            return value;
        }
    }

    /**
     * The "die" method forces this key to be set. Otherwise a runtime exception
     * will be thrown.
     *
     * @param key the key
     * @return the String or a IllegalArgumentException will be thrown.
     */
    @Override
    public String getOrDie(String key) {
        String value = get(key);

        if (value == null) {
            throw new IllegalArgumentException(String.format(ERROR_KEYNOTFOUND, key));
        } else {
            return value;
        }
    }

    /**
     * eg. key=myval1,myval2
     * <p/>
     * Delimiter is a comma ",".
     *
     * @return an array containing the values of that key or null if not found.
     */
    @Override
    public String[] getStringArray(String key) {
        return configuration.getStringArray(key);
    }

    /**
     * @return All properties that are currently loaded from internal and
     * external files
     */
    @Override
    public Properties asProperties() {
        return ConfigurationConverter.getProperties(configuration);
    }

    /**
     * @return All properties that are currently loaded from internal and
     * external files
     */
    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        Iterator<String> keys = configuration.getKeys();
        while (keys.hasNext()) {
            String key = keys.next();
            map.put(key, configuration.getProperty(key));
        }
        return map;
    }

    /**
     * Gets a configuration object with all the properties starting with the given prefix.
     *
     * @param prefix the prefix (without the ending `.`)
     * @return a configuration object with all properties with a name starting with `prefix.`,
     * or {@literal null} if no properties start with the given prefix.
     */
    @Override
    public Configuration getConfiguration(String prefix) {
        Map<String, Object> map = new LinkedHashMap<>();
        org.apache.commons.configuration.Configuration configuration = getConfiguration();
        Iterator<String> keys = configuration.getKeys(prefix);
        while (keys != null  && keys.hasNext()) {
            String key = keys.next();
            // Remove the prefix from the keys.
            if (key.length() > prefix.length()) {
                String newKey = key.substring(prefix.length() +1);
                map.put(newKey, configuration.getProperty(key));
            }
            // Else the key was the prefix, we skip it.
        }
        if (map.isEmpty()) {
            return null;
        } else {
            return new ConfigurationImpl(new MapConfiguration(map));
        }
    }
}
