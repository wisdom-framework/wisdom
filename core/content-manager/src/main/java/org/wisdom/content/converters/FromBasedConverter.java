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
package org.wisdom.content.converters;

import org.slf4j.LoggerFactory;
import org.wisdom.api.content.ParameterConverter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * This 'default' converter tries to create objects using a static 'from' method taking a single String argument.
 * This converter is particularly convenient for builders.
 *
 * @param <T> the built type.
 */
public final class FromBasedConverter<T> implements ParameterConverter<T> {

    public static final String FROM = "from";
    private final Method method;
    private final Class<T> clazz;

    private FromBasedConverter(Class<T> clazz, Method method) {
        this.clazz = clazz;
        this.method = method;
    }

    /**
     * Checks whether the given class can be used by the {@link org.wisdom.content.converters
     * .FromBasedConverter} (i.e. has a static 'from' method taking a single String as argument). If so,
     * creates a new instance of converter for this type.
     *
     * @param clazz the class
     * @return a {@link org.wisdom.content.converters.FromBasedConverter} if the given class is eligible,
     * {@literal null} otherwise.
     */
    public static <T> FromBasedConverter<T> getIfEligible(Class<T> clazz) {
        try {
            final Method method = clazz.getMethod(FROM, String.class);
            if (Modifier.isStatic(method.getModifiers())) {
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                return new FromBasedConverter<>(clazz, method);
            } else {
                // The from method is present but it must be static.
                return null;
            }
        } catch (NoSuchMethodException e) { //NOSONAR
            // The class does not have the right method, return null.
            return null;
        }

    }

    /**
     * Converts the given input to an object by using the 'from' method. Notice that the method may
     * receive a {@literal null} value.
     *
     * @param input the input, can be {@literal null}
     * @return the instance of T
     * @throws IllegalArgumentException if the instance of T cannot be created from the input.
     */
    @Override
    public T fromString(String input) throws IllegalArgumentException {
        try {
            return clazz.cast(method.invoke(null, input));
        } catch (IllegalAccessException | InvocationTargetException e) {
            LoggerFactory.getLogger(this.getClass())
                    .error("Cannot create an instance of {} from \"{}\" using the 'from' method",
                            method.getDeclaringClass().getName(),
                            input,
                            e);
            if (e.getCause() != null) {
                throw new IllegalArgumentException(e.getCause());
            } else {
                throw new IllegalArgumentException(e);
            }
        }
    }

    @Override
    public Class<T> getType() {
        return clazz;
    }
}
