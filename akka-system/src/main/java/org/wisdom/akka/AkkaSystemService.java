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
package org.wisdom.akka;

import java.io.InputStream;
import java.util.concurrent.Callable;

import org.wisdom.api.http.Context;
import org.wisdom.api.http.Result;

import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import akka.actor.ActorSystem;

/**
 * A service to access the wisdom actor system and ease the dispatching of task.
 */
public interface AkkaSystemService {
    /**
     * @return the Wisdom actor system.
     */
    ActorSystem system();

    /**
     * Dispatches the given task using an execution context preserving the given HTTP Context and the thread context
     * classloader.
     * @param callable the classloader
     * @param context the context
     * @return the future
     */
    Future<Result> dispatchResultWithContext(Callable<Result> callable, Context context);

    /**
     * Dispatches the given task using an execution context preserving the current HTTP Context and the thread context
     * classloader.
     * @param callable the classloader
     * @return the future
     */
    Future<Result> dispatchResult(Callable<Result> callable);
    
    Future<InputStream> dispatchInputStream(Callable<InputStream> callable);

    /**
     * Dispatches the given task. The task is executed using the given execution context.
     * @param callable the task
     * @param ctx the execution context
     * @param <T> the expected type of result.
     * @return the enqueued task.
     */
    <T> Future<T> dispatch(Callable<T> callable, ExecutionContext ctx);

    /**
     * Gets an Akka execution context preserving the HTTP Context and thread context classloader of the caller thread.
     * @return the execution context.
     */
    public ExecutionContext fromThread();
}
