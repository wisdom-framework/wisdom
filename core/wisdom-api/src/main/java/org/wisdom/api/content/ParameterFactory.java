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

/**
 * Service interface published by components able to create an instance of the specified type from the current HTTP
 * Context - {@link org.wisdom.api.http.Context}.
 *
 * @param <T> the type created by this converter.
 */
public interface ParameterFactory<T> {

    /**
     * Creates a new instance of {@code T} from the given HTTP Context.
     *
     * @param context the HTTP context
     * @return the instance of T
     * @throws IllegalArgumentException if the instance of T cannot be created from the given context.
     */
    public T newInstance(Context context) throws IllegalArgumentException;

    /**
     * Gets the type created by this factory.
     *
     * @return the class of T
     */
    public Class<T> getType();

}
