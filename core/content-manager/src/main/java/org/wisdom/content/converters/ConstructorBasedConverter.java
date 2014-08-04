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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * This 'default' converter tries to create objects using a constructor taking a single String argument.
 * Be aware that implementation must also handle the case where the input is {@literal null}.
 */
public final class ConstructorBasedConverter<T> implements ParameterConverter<T> {

    private final Constructor<T> constructor;
    private final Class<T> clazz;

    private ConstructorBasedConverter(Class<T> clazz, Constructor<T> constructor) {
        this.constructor = constructor;
        this.clazz = clazz;
    }

    /**
     * Checks whether the given class can be used by the {@link org.wisdom.content.converters
     * .ConstructorBasedConverter} (i.e. has a constructor taking a single String as argument). If so,
     * creates a new instance of converter for this type.
     *
     * @param clazz the class
     * @return a {@link org.wisdom.content.converters.ConstructorBasedConverter} if the given class is eligible,
     * {@literal null} otherwise.
     */
    public static <T> ConstructorBasedConverter<T> getIfEligible(Class<T> clazz) {
        try {
            final Constructor<T> constructor = clazz.getConstructor(String.class);
            if (! constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            return new ConstructorBasedConverter<>(clazz, constructor);
        } catch (NoSuchMethodException e) { //NOSONAR
            // The class does not have the right constructor, return null.
            return null;
        }

    }

    /**
     * Converts the given input to an object by using the constructor approach. Notice that the constructor must
     * expect receiving a {@literal null} value.
     *
     * @param input the input, can be {@literal null}
     * @return the instance of T
     * @throws IllegalArgumentException if the instance of T cannot be created from the input.
     */
    @Override
    public T fromString(String input) throws IllegalArgumentException {
        try {
            return constructor.newInstance(input);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            LoggerFactory.getLogger(this.getClass())
                    .error("Cannot create an instance of {} from \"{}\"",
                            constructor.getDeclaringClass().getName(),
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
