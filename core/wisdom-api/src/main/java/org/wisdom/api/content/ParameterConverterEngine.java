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
package org.wisdom.api.content;


import java.lang.reflect.Type;
import java.util.Collection;

/**
 * This interface is exposed by the Parameter Converter Engine implementation as a service.
 * It allows retrieving the converter to create object from a specific type from a String representation.
 * <p>
 * Implementations aggregates the available {@link org.wisdom.api.content.ParameterConverter} and chooses the 'right'
 * one.
 */
public interface ParameterConverterEngine {

    /**
     * Creates an instance of T from the given input. It the target type is an array or a collection,
     * the input string is considered as a coma-separated list of values. Individual values are trimmed before being
     * processed.
     *
     * @param input        the input String, may be {@literal null}
     * @param rawType      the target class
     * @param type         the type representation of the raw type, may contains metadata about generics
     * @param defaultValue the default value if any
     * @param <T>          the type of object to create
     * @return the created object
     * @throws java.lang.IllegalArgumentException if there are no converter available from the type T at the moment
     */
    <T> T convertValue(String input, Class<T> rawType, Type type, String defaultValue) throws IllegalArgumentException;

    /**
     * Creates an instance of T from the given input. Unlike {@link #convertValue(java.lang.String, Class,
     * java.lang.reflect.Type, String)}, this method support multi-value parameters. If the target type cannot be
     * multi-valued (so not an array or a collection), only the first value is considered (other are ignored).
     *
     * @param input        the input Strings, may be {@literal null} or empty
     * @param rawType      the target class
     * @param type         the type representation of the raw type, may contains metadata about generics
     * @param defaultValue the default value if any
     * @param <T>          the type of object to create
     * @return the created object
     * @throws java.lang.IllegalArgumentException if there are no converter available from the type T at the moment
     */
    <T> T convertValues(Collection<String> input, Class<T> rawType, Type type, String defaultValue) throws
            IllegalArgumentException;

}
