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
package org.wisdom.api.http;

import java.util.concurrent.Callable;

/**
 * An extension of result instructing the engine to render the result asynchronously. That means that the result
 * computation is delegated to another thread (and not the request thread), and is written and sent to the client
 * when the computation is completed.
 */
public class AsyncResult extends Result {

    /**
     * The callable computing the result.
     */
    private final Callable<Result> callable;

    /**
     * Creates a new asynchronous result.
     *
     * @param callable the callable that computes the result. This wrapped code is executed in another thread. This
     *                 callable must not be {@literal null}.
     */
    public AsyncResult(Callable<Result> callable) {
        this.callable = callable;
    }

    /**
     * @return the callable.
     */
    public Callable<Result> callable() {
        return callable;
    }
}
