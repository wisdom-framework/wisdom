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
package org.wisdom.api.configuration;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Configuration object used to retrieve values.
 * It offers a set of convenient method to avoid having to parse the results.
 */
public interface Configuration {
    /**
     * Gets a configuration object with all the properties starting with the given prefix.
     *
     * @param prefix the prefix (without the ending `.`)
     * @return a configuration object with all properties with a name starting with `prefix.`,
     * or {@literal null} if no properties start with the given prefix.
     */
    Configuration getConfiguration(String prefix);

    /**
     * Checks whether the configuration object define a value at the given name / path.
     *
     * @param key the key / path
     * @return {@code true} if the configuration has a non-null value, {@code false} otherwise
     * @since 0.7
     */
    boolean has(String key);

    /**
     * Get a String property or {@literal null} if it is not there.
     *
     * @param key the key used in the configuration file.
     * @return the property of null if not there
     */
    String get(String key);

    /**
     * Get a custom type property or {@literal null} if it's not there. The object is created using the
     * {@link org.wisdom.api.content.ParameterFactories} strategy.
     *
     * @param key   the key the key used in the configuration file.
     * @param clazz the class of the object to create
     * @param <T>   the type of the object to create
     * @return the created object, {@code null} if not there
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * Get a custom type property. The object is created using the
     * {@link org.wisdom.api.content.ParameterFactories} strategy. This "die" method forces this key to be set.
     * Otherwise a runtime exception will be thrown.
     *
     * @param key   the key the key used in the configuration file.
     * @param clazz the class of the object to create
     * @param <T>   the type of the object to create
     * @return the created object. The object cannot be created (because the property is missing,
     * or because the conversion failed) a {@link java.lang.RuntimeException} is thrown.
     */
    <T> T getOrDie(String key, Class<T> clazz);

    /**
     * Get a custom type property or the given default value if it's not there. The object is created using the
     * {@link org.wisdom.api.content.ParameterFactories} strategy.
     *
     * @param key          the key the key used in the configuration file.
     * @param clazz        the class of the object to create
     * @param defaultValue the object returned if the property is missing
     * @param <T>          the type of the object to create
     * @return the created object, or the given default object if not there
     */
    <T> T get(String key, Class<T> clazz, T defaultValue);

    /**
     * Get a custom type property or the given default value if it's not there. The object is created using the
     * {@link org.wisdom.api.content.ParameterFactories} strategy.
     *
     * @param key                  the key the key used in the configuration file.
     * @param clazz                the class of the object to create
     * @param defaultValueAsString the 'string' format of the object returned if the property is missing. The object
     *                             is built using the parameter converters service.
     * @param <T>                  the type of the object to create
     * @return the created object, or the given default object if not there
     */
    <T> T get(String key, Class<T> clazz, String defaultValueAsString);

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
     * Get a property as Integer or {@literal null} if not there / or if the property is not an integer.
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
     * Get a property as Double or {@literal null} if not there / or if the property is not an integer.
     *
     * @param key the key used in the configuration file.
     * @return the property or {@literal null} if not there or property no integer
     * @since 0.7
     */
    Double getDouble(String key);

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
    Double getDoubleWithDefault(String key, Double defaultValue);

    /**
     * Get a property as Boolean or {@literal null} if not there or if the property is not an integer.
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
     * Get a property as Long or null if not there or if the property is not a long.
     *
     * @param key the key used in the configuration file.
     * @return the property or null if not there or property no long
     */
    Long getLong(String key);

    /**
     * Get a Long property or a default value when property cannot be found
     * in any configuration file.
     *
     * @param key          the key used in the configuration file.
     * @param defaultValue Default value returned, when value cannot be found in
     *                     configuration.
     * @return the value of the key or the default value.
     */
    Long getLongWithDefault(String key, Long defaultValue);

    /**
     * The "die" method forces this key to be set. Otherwise a runtime exception
     * will be thrown.
     *
     * @param key the key used in the configuration file.
     * @return the Long or a RuntimeException will be thrown.
     */
    Long getLongOrDie(String key);

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
     * @return the Double or a RuntimeException will be thrown.
     * @since 0.7
     */
    Double getDoubleOrDie(String key);

    /**
     * The "die" method forces this key to be set. Otherwise a runtime exception
     * will be thrown.
     *
     * @param key the key used in the configuration file.
     * @return the String or a RuntimeException will be thrown.
     */
    String getOrDie(String key);

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
     *
     * @param key  the key used in the configuration file.
     * @param unit the time unit
     * @return the duration converted to the given units, {@code null} if not found.
     * @since 0.7
     */
    Long getDuration(final String key, final TimeUnit unit);

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
     *
     * @param key          the key used in the configuration file.
     * @param unit         the time unit
     * @param defaultValue the default value to return if the configuration does not contain the given key
     * @return the duration converted to the given units, {@code defaultValue} if not found.
     * @since 0.7
     */
    Long getDuration(final String key, final TimeUnit unit, long defaultValue);

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
     * @param key the key used in the configuration file.
     * @return the amount of bytes, {@code null} if not found.
     * @since 0.7
     */
    Long getBytes(final String key);

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
    Long getBytes(final String key, long defaultValue);


    /**
     * Gets the array of values. Values are split using comma.
     * eg. key=myval1,myval2
     * <p>
     * Delimiter is a comma "," as outlined in the example above. Each values is 'trimmed'.
     *
     * @param key the key used in the configuration file.
     * @return an array containing the values of that key or empty if not found.
     */
    String[] getStringArray(String key);

    /**
     * Gets the list of values. Values are split using comma.
     * eg. key=myval1,myval2
     * <p>
     * Delimiter is a comma "," as outlined in the example above. Each values is 'trimmed'.
     *
     * @param key the key used in the configuration file.
     * @return an list containing the values of that key or empty if not found.
     */
    List<String> getList(String key);

    /**
     * @return All properties that are currently loaded from internal and
     * external files.
     */
    Properties asProperties();

    /**
     * @return All properties that are currently loaded from internal and
     * external files.
     */
    Map<String, Object> asMap();
}
