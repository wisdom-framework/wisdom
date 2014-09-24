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
package org.wisdom.api.model;

/**
 * The CommittedHandler is called when the {@link FluentTransaction} has been successfully committed.
 *
 * @author barjo
 */
public interface CommittedHandler<T> {
    /**
     * The transaction has been successfully committed.
     * See {@link FluentTransaction.Intermediate#with(java.util.concurrent.Callable)}.
     *
     * @param result, The result of transaction content callable.
     */
    void committed(T result);
}
