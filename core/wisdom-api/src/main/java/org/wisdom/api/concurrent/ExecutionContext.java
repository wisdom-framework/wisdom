/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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
package org.wisdom.api.concurrent;

/**
 * {link ExecutionContext} represents the context that need to be applied before
 * the execution of a task. It's made to migrate data stored in
 * {@link java.lang.ThreadLocal} when submitting a task to an executor.
 * <p>
 * The execution context is applied before the task execution in the execution
 * thread and unapply after. Notice that instances of
 * {@link org.wisdom.api.concurrent.ExecutionContext} are made in the caller
 * thread using a {@link org.wisdom.api.concurrent.ExecutionContextService}.
 */
public interface ExecutionContext {

    /**
     * Applies the execution context. It sets up the environment.
     */
    public void apply();

    /**
     * Cleans the execution context. It removes everything that has been made in
     * the {@link #apply()} method.
     */
    public void unapply();
}
