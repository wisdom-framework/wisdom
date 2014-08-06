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

import java.util.Map;
import java.util.Properties;

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
     * @return the String or a RuntimeException will be thrown.
     */
    String getOrDie(String key);

    /**
     * Gets the array of values. Values are split using comma.
     * eg. key=myval1,myval2
     * <p/>
     * Delimiter is a comma "," as outlined in the example above.
     *
     * @return an array containing the values of that key or {@literal null} if not found.
     */
    String[] getStringArray(String key);

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
