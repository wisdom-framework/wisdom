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
 * A service exposed by entities willing to configure the execution environment
 * of a thread (such as {@link java.lang.ThreadLocal}.
 * <p>
 * An instance of {@link org.wisdom.api.concurrent.ExecutionContext} is created
 * when the task is submitted (in the caller thread) and applied before the
 * execution of the task (in the task's execution thread). The context in
 * un-applied when the task completes (also called in the task's execution
 * thread).
 */
public interface ExecutionContextService {

    /**
     * The name of the execution context service
     *
     * @return the name, such as "http context" or "transaction"
     */
    public String name();

    /**
     * Creates an {link ExecutionContext} that is applied before the task
     * execution. It should store the data to set up in the task's thread.
     *
     * @return the execution context, {@code null} if the caller thread does not
     * have meaningful data to be stored and restored.
     */
    public ExecutionContext prepare();

}
