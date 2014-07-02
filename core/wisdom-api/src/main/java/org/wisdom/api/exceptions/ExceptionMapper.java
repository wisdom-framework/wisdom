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
package org.wisdom.api.exceptions;

import org.wisdom.api.http.Result;

/**
 * Service interface exposed to build custom HTTP {@link org.wisdom.api.http.Result} from instance of exception
 * belonging to a specific class.
 *
 * @param <X> the class of the exception handled by this {@link org.wisdom.api.exceptions.ExceptionMapper}
 */
public interface ExceptionMapper<X extends Exception> {

    /**
     * Gets the class of the exception instances that are handled by this mapper.
     *
     * @return the class
     */
    public Class<X> getExceptionClass();

    /**
     * Maps the instance of exception to a {@link org.wisdom.api.http.Result}.
     *
     * @param exception the exception
     * @return the HTTP result.
     */
    public Result toResult(X exception);
}
