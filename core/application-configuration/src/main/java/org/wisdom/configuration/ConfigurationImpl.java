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

import com.google.common.collect.ImmutableList;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigResolveOptions;
import org.wisdom.api.configuration.Configuration;
import org.wisdom.api.content.ParameterFactories;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * An implementation of the configuration object based on Apache Commons Configuration.
 * Unlike the main application configuration, this implementation does not used a logger.
 */
public class ConfigurationImpl implements Configuration {

    private static final String ERROR_KEYNOTFOUND = "Key %s does not exist. Please include it in your application.conf. " +
            "Otherwise this application will not work";
    protected static final String ERROR_NOSUCHKEY = "No such key \"";

    /**
     * The parameter converter service, must be a proxy.
     */
    protected ParameterFactories converters;

    private Config configuration;

    /**
     * Creates an instance of {@link org.wisdom.configuration.ConfigurationImpl}.
     *
     * @param configuration the underlying configuration
     */
    public ConfigurationImpl(ParameterFactories converters, Config configuration) {
        this(converters);
        this.configuration = configuration;
    }

    protected ConfigurationImpl(ParameterFactories converters) {
        this.converters = converters;
        // This constructor requires an invocation of setConfiguration.
    }

    protected void setConfiguration(Config configuration) {
        this.configuration = configuration;
    }

    protected Config getConfiguration() {
        return configuration;
    }


    /**
     * Get a String property or null if it is not there...
     *
     * @param key the key
     * @return the property of null if not there
     */
    @Override
    public String get(final String key) {
        return retrieve(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return configuration.getString(key);
            }
        }, null);
    }

    /**
     * Checks whether the configuration object define a value at the given name / path.
     * @param key the key / path
     * @return {@code true} if the configuration has a non-null value, {@code false} otherwise
     */
    public boolean has(String key) {
        return configuration.hasPath(key);
    }

    private <T> T retrieve(Callable<T> callable, T defaultValue) {
        try {
            T v = callable.call();
            return v != null ? v : defaultValue;
        } catch (ConfigException.Missing e) {
            return defaultValue;
        } catch (Exception e) {
            throw new RuntimeException(e);
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
    public String getWithDefault(final String key, String defaultValue) {
        return retrieve(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return configuration.getString(key);
            }
        }, defaultValue);
    }

    /**
     * Get a property as Integer or null if not there / or property no integer.
     *
     * @param key the key
     * @return the property or {@literal null} if not there or property no integer
     */
    @Override
    public Integer getInteger(final String key) {
        return retrieve(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return configuration.getInt(key);
            }
        }, null);
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
    public Integer getIntegerWithDefault(final String key, Integer defaultValue) {

        return retrieve(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return configuration.getInt(key);
            }
        }, defaultValue);
    }

    /**
     * Get a property as Double or {@literal null} if not there / or if the property is not an integer.
     *
     * @param key the key used in the configuration file.
     * @return the property or {@literal null} if not there or property no integer
     */
    @Override
    public Double getDouble(final String key) {
        return retrieve(new Callable<Double>() {
            @Override
            public Double call() throws Exception {
                return configuration.getDouble(key);
            }
        }, null);
    }

    /**
     * Get a Double property or a default value when property cannot be found
     * in any configuration file.
     *
     * @param key          the key used in the configuration file.
     * @param defaultValue Default value returned, when value cannot be found in
     *                     configuration.
     * @return the value of the key or the default value.
     */
    @Override
    public Double getDoubleWithDefault(final String key, Double defaultValue) {
        return retrieve(new Callable<Double>() {
            @Override
            public Double call() throws Exception {
                return configuration.getDouble(key);
            }
        }, defaultValue);
    }

    /**
     * @param key the key
     * @return the property or null if not there or property no boolean
     */
    @Override
    public Boolean getBoolean(final String key) {

        return retrieve(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return configuration.getBoolean(key);
            }
        }, null);
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
    public Boolean getBooleanWithDefault(final String key, Boolean defaultValue) {

        return retrieve(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return configuration.getBoolean(key);
            }
        }, defaultValue);
    }

    @Override
    public Long getLong(final String key) {
        return retrieve(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return configuration.getLong(key);
            }
        }, null);

    }

    @Override
    public Long getLongWithDefault(final String key, Long defaultValue) {
        return retrieve(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return configuration.getLong(key);
            }
        }, defaultValue);
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
     * @param key the key used in the configuration file.
     * @return the Double or a RuntimeException will be thrown.
     */
    @Override
    public Double getDoubleOrDie(String key) {
        Double value = getDouble(key);
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
     * {@inheritDoc}
     */
    public Long getDuration(final String key, final TimeUnit unit) {
        return retrieve(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return configuration.getDuration(key, unit);
            }
        }, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getDuration(final String key, final TimeUnit unit, long defaultValue) {
        return retrieve(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return configuration.getDuration(key, unit);
            }
        }, defaultValue);
    }

    /**
     * {@inheritDoc}
     */
    public Long getBytes(final String key) {
        return retrieve(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return configuration.getBytes(key);
            }
        }, null);
    }

    /**
     * {@inheritDoc}
     */
    public Long getBytes(final String key, long defaultValue) {
        return retrieve(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return configuration.getBytes(key);
            }
        }, defaultValue);
    }

    /**
     * Retrieves the values as a array of String, the format is: key=[myval1,myval2].
     *
     * @return an array containing the values of that key or empty if not found.
     */
    @Override
    public String[] getStringArray(final String key) {
        List<String> list = getList(key);
        return list.toArray(new String[list.size()]);
    }

    //TODO List of other type.

    /**
     * Retrieves the values as a list of String, the format is: key=[myval1,myval2].
     *
     * @param key the key the key used in the configuration file.
     * @return an list containing the values of that key or empty if not found.
     */
    @Override
    public List<String> getList(final String key) {
        return retrieve(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                try {
                    return configuration.getStringList(key);
                } catch (ConfigException.WrongType e) {
                    // Not a list.
                    String s = get(key);
                    if (s != null) {
                        return ImmutableList.of(s);
                    } else {
                        throw new IllegalArgumentException("Cannot create a list for the key '" + key + "'", e);
                    }
                }
            }
        }, Collections.<String>emptyList());
    }

    /**
     * @return All properties that are currently loaded from internal and
     * external files
     */
    @Override
    public Properties asProperties() {
        Properties properties = new Properties();
        properties.putAll(asMap());
        return properties;
    }

    /**
     * @return All properties that are currently loaded from internal and
     * external files
     */
    @Override
    public Map<String, Object> asMap() {
        return configuration
                .resolve(ConfigResolveOptions.defaults().setUseSystemEnvironment(true).setAllowUnresolved(true))
                .root()
                .unwrapped();
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
        try {
            Config value = configuration.getConfig(prefix);
            return new ConfigurationImpl(converters, value);
        } catch (ConfigException.Missing e) {
            // Ignore the exception.
            return null;
        }
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        String value = get(key);
        if (value == null) {
            return null;
        }
        return converters.convertValue(value, clazz, clazz, null);
    }

    /**
     * Get a custom type property. The object is created using the
     * {@link org.wisdom.api.content.ParameterFactories} strategy. This "die" method forces this key to be set.
     * Otherwise a runtime exception will be thrown.
     *
     * @param key   the key the key used in the configuration file.
     * @param clazz the class of the object to create
     * @return the created object. The object cannot be created (because the property is missing,
     * or because the conversion failed) a {@link RuntimeException} is thrown.
     */
    @Override
    public <T> T getOrDie(String key, Class<T> clazz) {
        T val = get(key, clazz);
        if (val == null) {
            throw new IllegalArgumentException(String.format(ERROR_KEYNOTFOUND, key));
        }
        return val;
    }

    @Override
    public <T> T get(String key, Class<T> clazz, T defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        return converters.convertValue(value, clazz, clazz, null);
    }

    @Override
    public <T> T get(String key, Class<T> clazz, String defaultValueAsString) {
        String value = get(key);
        return converters.convertValue(value, clazz, clazz, defaultValueAsString);
    }
}
