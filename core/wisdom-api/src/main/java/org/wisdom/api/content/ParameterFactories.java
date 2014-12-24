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


import org.wisdom.api.http.Context;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Set;

/**
 * This interface is exposed by the Parameter Engine implementation as a service.
 * It allows retrieving the converter to create object from a specific type from a String representation, or create
 * object from an HTTP context.
 * <p>
 * Implementations aggregates the available {@link ParameterConverter} and chooses the 'right'
 * one. It also aggregates the available {@link org.wisdom.api.content.ParameterFactory} and chooses the 'right' one.
 * In other words, this service is a facade selecting the right provider.
 */
public interface ParameterFactories {

    /**
     * Creates an instance of T from the given input. If the target type is an array or a collection,
     * the input string is considered as a coma-separated list of values. Individual values are trimmed before being
     * processed.
     *
     * @param input        the input String, may be {@literal null}
     * @param rawType      the target class
     * @param type         the type representation of the raw type, may contains metadata about generics
     * @param defaultValue the default value if any
     * @param <T>          the type of object to create
     * @return the created object
     * @throws IllegalArgumentException if there are no converter available for the type T at the moment
     */
    <T> T convertValue(String input, Class<T> rawType, Type type, String defaultValue) throws IllegalArgumentException;

    /**
     * Creates an instance of T from the given input. Unlike {@link #convertValue(String, Class,
     * java.lang.reflect.Type, String)}, this method support multi-value parameters. If the target type cannot be
     * multi-valued (so not an array or a collection), only the first value is considered (other are ignored).
     *
     * @param input        the input Strings, may be {@literal null} or empty
     * @param rawType      the target class
     * @param type         the type representation of the raw type, may contains metadata about generics
     * @param defaultValue the default value if any
     * @param <T>          the type of object to create
     * @return the created object
     * @throws IllegalArgumentException if there are no converter available for the type T at the moment
     */
    <T> T convertValues(Collection<String> input, Class<T> rawType, Type type, String defaultValue) throws
            IllegalArgumentException;

    /**
     * Creates an instance of T from the given HTTP content. Unlike converters, it does not handler generics or
     * collections.
     *
     * @param context the HTTP content
     * @param type    the class to instantiate
     * @param <T>     the type ot the returned object
     * @return the created object
     * @throws IllegalArgumentException if there are no {@link org.wisdom.api.content.ParameterFactory} available for
     *                                  the type T, or if the instantiation failed.
     */
    <T> T newInstance(Context context, Class<T> type) throws IllegalArgumentException;

    /**
     * Gets the current set of classes that can be instantiated using an available
     * {@link org.wisdom.api.content.ParameterFactory}. This set if dynamic, the returned collection is an immutable
     * copy of a snapshot.
     *
     * @return the set of classes that can be instantiated using an available {@link org.wisdom.api.content
     * .ParameterFactory} service.
     */
    Set<Class> getTypesHandledByFactories();

}
