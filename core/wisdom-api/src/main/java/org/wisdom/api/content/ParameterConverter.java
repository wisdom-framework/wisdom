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

/**
 * Service interface published by components able to transform a {@link java.lang.String} to the specified type.
 *
 * @param <T> the type created by this converter.
 */
public interface ParameterConverter<T> {

    /**
     * Converts the given input to an object of type T.
     *
     * @param input the input, can be {@literal null}
     * @return the instance of T
     * @throws IllegalArgumentException if the instance of T cannot be created from the input.
     */
    public T fromString(String input) throws IllegalArgumentException;

    /**
     * Gets the type created by this converter.
     *
     * @return the class of T
     */
    public Class<T> getType();

}
