/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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
package org.wisdom.test.parents;

import org.wisdom.api.configuration.Configuration;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * A simple, flat, without conversion support implementation of configuration. For
 * testing purpose only.
 */
public class FakeConfiguration implements Configuration {

    private final Map<String, Object> internals;

    public FakeConfiguration(Map<String, Object> map) {
        this.internals = map;
    }

    /**
     * Gets a sub-configuration (instance of
     * {@link org.wisdom.test.parents.FakeConfiguration}) stored at the key 'prefix'
     *
     * @param prefix the key
     */
    @Override
    public Configuration getConfiguration(String prefix) {
        return (Configuration) internals.get(prefix);
    }

    /**
     * Checks whether the configuration object define a value at the given name / path. Be aware that this
     * implementation does not check for sub-configuration.
     *
     * @param key the key / path
     * @return {@code true} if the configuration has a non-null value, {@code false} otherwise
     * @since 0.7
     */
    @Override
    public boolean has(String key) {
        return internals.containsKey(key);
    }

    /**
     * Get a String property or {@literal null} if it is not there.
     *
     * @param key the key used in the configuration file.
     * @return the property of null if not there
     */
    @Override
    public String get(String key) {
        return (String) internals.get(key);
    }

    /**
     * Get a custom type property or {@literal null} if it's not there. No conversion
     * made in this implementation
     *
     * @param key   the key the key used in the configuration file.
     * @param clazz the class of the object (must match the stored object)
     * @return the created object, {@code null} if not there
     */
    @Override
    public <T> T get(String key, Class<T> clazz) {
        return (T) internals.get(key);
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
        T result = get(key, clazz);
        if (result == null) {
            throw new RuntimeException("Missing key " + key);
        }
        return result;
    }

    /**
     * Get a custom type property or the given default value if it's not there. The object is created using the
     * {@link org.wisdom.api.content.ParameterFactories} strategy.
     *
     * @param key          the key the key used in the configuration file.
     * @param clazz        the class of the object to create
     * @param defaultValue the object returned if the property is missing
     * @return the created object, or the given default object if not there
     */
    @Override
    public <T> T get(String key, Class<T> clazz, T defaultValue) {
        T result = get(key, clazz);
        if (result == null) {
            return defaultValue;
        }
        return result;
    }

    /**
     * Get a custom type property or the given default value if it's not there. The object is created using the
     * {@link org.wisdom.api.content.ParameterFactories} strategy.
     *
     * @param key                  the key the key used in the configuration file.
     * @param clazz                the class of the object to create
     * @param defaultValueAsString the 'string' format of the object returned if the property is missing. The object
     *                             is built using the parameter converters service.
     * @return the created object, or the given default object if not there
     */
    @Override
    public <T> T get(String key, Class<T> clazz, String defaultValueAsString) {
        T result = get(key, clazz);
        if (result == null) {
            throw new UnsupportedOperationException("No conversion in FakeConfiguration");
        }
        return result;
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
        String result = (String) internals.get(key);
        if (result == null) {
            return defaultValue;
        }
        return result;
    }

    /**
     * Get a property as Integer or {@literal null} if not there / or if the property is not an integer.
     *
     * @param key the key used in the configuration file.
     * @return the property or {@literal null} if not there or property no integer
     */
    @Override
    public Integer getInteger(String key) {
        return get(key, Integer.class);
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
        return get(key, Integer.class, defaultValue);
    }

    /**
     * Get a property as Double or {@literal null} if not there / or if the property is not an integer.
     *
     * @param key the key used in the configuration file.
     * @return the property or {@literal null} if not there or property no integer
     * @since 0.7
     */
    @Override
    public Double getDouble(String key) {
        return get(key, Double.class);
    }

    /**
     * Get a Double property or a default value when property cannot be found
     * in any configuration file.
     *
     * @param key          the key used in the configuration file.
     * @param defaultValue Default value returned, when value cannot be found in
     *                     configuration.
     * @return the value of the key or the default value.
     * @since 0.7
     */
    @Override
    public Double getDoubleWithDefault(String key, Double defaultValue) {
        return get(key, Double.class, defaultValue);
    }

    /**
     * Get a property as Boolean or {@literal null} if not there or if the property is not an integer.
     *
     * @param key the key used in the configuration file.
     * @return the property or {@literal null} if not there or property no boolean
     */
    @Override
    public Boolean getBoolean(String key) {
        return get(key, Boolean.class);
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
        return get(key, Boolean.class, defaultValue);
    }

    /**
     * Get a property as Long or null if not there or if the property is not a long.
     *
     * @param key the key used in the configuration file.
     * @return the property or null if not there or property no long
     */
    @Override
    public Long getLong(String key) {
        return get(key, Long.class);
    }

    /**
     * Get a Long property or a default value when property cannot be found
     * in any configuration file.
     *
     * @param key          the key used in the configuration file.
     * @param defaultValue Default value returned, when value cannot be found in
     *                     configuration.
     * @return the value of the key or the default value.
     */
    @Override
    public Long getLongWithDefault(String key, Long defaultValue) {
        return get(key, Long.class, defaultValue);
    }

    /**
     * The "die" method forces this key to be set. Otherwise a runtime exception
     * will be thrown.
     *
     * @param key the key used in the configuration file.
     * @return the Long or a RuntimeException will be thrown.
     */
    @Override
    public Long getLongOrDie(String key) {
        return getOrDie(key, Long.class);
    }

    /**
     * The "die" method forces this key to be set. Otherwise a runtime exception
     * will be thrown.
     *
     * @param key the key used in the configuration file.
     * @return the boolean or a RuntimeException will be thrown.
     */
    @Override
    public Boolean getBooleanOrDie(String key) {
        return getOrDie(key, Boolean.class);
    }

    /**
     * The "die" method forces this key to be set. Otherwise a runtime exception
     * will be thrown.
     *
     * @param key the key used in the configuration file.
     * @return the Integer or a RuntimeException will be thrown.
     */
    @Override
    public Integer getIntegerOrDie(String key) {
        return getOrDie(key, Integer.class);
    }

    /**
     * The "die" method forces this key to be set. Otherwise a runtime exception
     * will be thrown.
     *
     * @param key the key used in the configuration file.
     * @return the Double or a RuntimeException will be thrown.
     * @since 0.7
     */
    @Override
    public Double getDoubleOrDie(String key) {
        return getOrDie(key, Double.class);
    }

    /**
     * The "die" method forces this key to be set. Otherwise a runtime exception
     * will be thrown.
     *
     * @param key the key used in the configuration file.
     * @return the String or a RuntimeException will be thrown.
     */
    @Override
    public String getOrDie(String key) {
        String r = get(key);
        if (r == null) {
            throw new RuntimeException("Missing key : " + key);
        }
        return r;
    }

    /**
     * Gets a property for a duration. It retrieves the amount of 'unit' for the duration written in the configuration.
     * For instance, if the configuration contains "2s", and you want to retrieve it as milliseconds (unit), it returns
     * 2000.
     * <p>
     * Are supported:
     * <ul>
     * <li>ns, nanosecond, nanoseconds</li>
     * <li>us, microsecond, microseconds</li>
     * <li>ms, millisecond, milliseconds</li>
     * <li>s, second, seconds</li>
     * <li>m, minute, minutes</li>
     * <li>h, hour, hours</li>
     * <li>d, day, days</li>
     * </ul>
     * <p>
     * <strong>No conversion in this implementation !</strong>
     * </p>
     * <p>
     * <strong>Data must be a long</strong>
     * </p>
     *
     * @param key  the key used in the configuration file.
     * @param unit the time unit
     * @return the duration converted to the given units, {@code null} if not found.
     * @since 0.7
     */
    @Override
    public Long getDuration(String key, TimeUnit unit) {
        return getLong(key);
    }

    /**
     * Gets a property for a duration. It retrieves the amount of 'unit' for the duration written in the configuration.
     * For instance, if the configuration contains "2s", and you want to retrieve it as milliseconds (unit), it returns
     * 2000.
     * <p>
     * Are supported:
     * <ul>
     * <li>ns, nanosecond, nanoseconds</li>
     * <li>us, microsecond, microseconds</li>
     * <li>ms, millisecond, milliseconds</li>
     * <li>s, second, seconds</li>
     * <li>m, minute, minutes</li>
     * <li>h, hour, hours</li>
     * <li>d, day, days</li>
     * </ul>
     * <p>
     * <strong>No conversion in this implementation !</strong>
     *
     * @param key          the key used in the configuration file.
     * @param unit         the time unit
     * @param defaultValue the default value to return if the configuration does not contain the given key
     * @return the duration converted to the given units, {@code defaultValue} if not found.
     * @since 0.7
     */
    @Override
    public Long getDuration(String key, TimeUnit unit, long defaultValue) {
        return getLongWithDefault(key, defaultValue);
    }

    /**
     * Gets a property for a size in bytes. It retrieves the amount of 'bytes' for the size written in the
     * configuration. This is made to avoid the misleading powers of 1024 with powers of 1000.
     * For instance, if the configuration contains "2kB", it returns 2000. But, if the configuration contains "2K",
     * it returns 2048.
     * <p>
     * For single bytes, exactly these strings are supported:
     * <ul><li>B, b, byte, bytes</li></ul>
     * For powers of ten, exactly these strings are supported:
     * <ul>
     * <li>kB, kilobyte, kilobytes</li>
     * <li>MB, megabyte, megabytes</li>
     * <li>GB, gigabyte, gigabytes</li>
     * <li>TB, terabyte, terabytes</li>
     * <li>PB, petabyte, petabytes</li>
     * <li>EB, exabyte, exabytes</li>
     * <li>ZB, zettabyte, zettabytes</li>
     * <li>YB, yottabyte, yottabytes</li>
     * </ul>
     * For powers of two, exactly these strings are supported:
     * <ul>
     * <li>K, k, Ki, KiB, kibibyte, kibibytes</li>
     * <li>M, m, Mi, MiB, mebibyte, mebibytes</li>
     * <li>G, g, Gi, GiB, gibibyte, gibibytes</li>
     * <li>T, t, Ti, TiB, tebibyte, tebibytes</li>
     * <li>P, p, Pi, PiB, pebibyte, pebibytes</li>
     * <li>E, e, Ei, EiB, exbibyte, exbibytes</li>
     * <li>Z, z, Zi, ZiB, zebibyte, zebibytes</li>
     * <li>Y, y, Yi, YiB, yobibyte, yobibytes</li>
     * </ul>
     * <p>
     * * <strong>No conversion in this implementation !</strong>
     *
     * @param key the key used in the configuration file.
     * @return the amount of bytes, {@code null} if not found.
     * @since 0.7
     */
    @Override
    public Long getBytes(String key) {
        return getLong(key);
    }

    /**
     * Gets a property for a size in bytes. It retrieves the amount of 'bytes' for the size written in the
     * configuration. This is made to avoid the misleading powers of 1024 with powers of 1000.
     * For instance, if the configuration contains "2kB", it returns 2000. But, if the configuration contains "2K",
     * it returns 2048.
     * <p>
     * For single bytes, exactly these strings are supported:
     * <ul><li>B, b, byte, bytes</li></ul>
     * For powers of ten, exactly these strings are supported:
     * <ul>
     * <li>kB, kilobyte, kilobytes</li>
     * <li>MB, megabyte, megabytes</li>
     * <li>GB, gigabyte, gigabytes</li>
     * <li>TB, terabyte, terabytes</li>
     * <li>PB, petabyte, petabytes</li>
     * <li>EB, exabyte, exabytes</li>
     * <li>ZB, zettabyte, zettabytes</li>
     * <li>YB, yottabyte, yottabytes</li>
     * </ul>
     * For powers of two, exactly these strings are supported:
     * <ul>
     * <li>K, k, Ki, KiB, kibibyte, kibibytes</li>
     * <li>M, m, Mi, MiB, mebibyte, mebibytes</li>
     * <li>G, g, Gi, GiB, gibibyte, gibibytes</li>
     * <li>T, t, Ti, TiB, tebibyte, tebibytes</li>
     * <li>P, p, Pi, PiB, pebibyte, pebibytes</li>
     * <li>E, e, Ei, EiB, exbibyte, exbibytes</li>
     * <li>Z, z, Zi, ZiB, zebibyte, zebibytes</li>
     * <li>Y, y, Yi, YiB, yobibyte, yobibytes</li>
     * </ul>
     *
     * @param key          the key used in the configuration file.
     * @param defaultValue the default value to return if the configuration does not contain the given key
     * @return the amount of bytes, {@code defaultValue} if not found.
     * @since 0.7
     */
    @Override
    public Long getBytes(String key, long defaultValue) {
        return getLongWithDefault(key, defaultValue);
    }

    /**
     * Gets the array of values. Must have been stored as an array.
     *
     * @param key the key
     * @return an array containing the values of that key or empty if not found.
     */
    @Override
    public String[] getStringArray(String key) {
        String[] array = (String[]) internals.get(key);
        if (array == null) {
            return new String[0];
        }
        return array;
    }

    /**
     * Gets the list of values. Must have been stored as a list.
     *
     * @param key the key
     * @return an list containing the values of that key or empty if not found.
     */
    @Override
    public List<String> getList(String key) {
        List<String> list =  (List<String>) internals.get(key);
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }

    /**
     * @return All properties that are currently loaded from internal and
     * external files.
     */
    @Override
    public Properties asProperties() {
        Properties properties = new Properties();
        properties.putAll(internals);
        return properties;
    }

    /**
     * @return All properties that are currently loaded from internal and
     * external files. Modifying the returned map, does not modify the configuration.
     */
    @Override
    public Map<String, Object> asMap() {
        return new HashMap<>(internals);
    }
}
