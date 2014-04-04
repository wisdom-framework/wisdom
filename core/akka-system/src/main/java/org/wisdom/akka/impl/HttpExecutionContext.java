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
package org.wisdom.akka.impl;

import org.wisdom.api.http.Context;
import scala.concurrent.ExecutionContext;
import scala.concurrent.ExecutionContextExecutor;

/**
 * An Akka execution context delegating to another one but setting the http context and thread context class loader.
 */
public class HttpExecutionContext implements ExecutionContextExecutor {

    private final ExecutionContext delegate;
    private final Context context;
    private final ClassLoader tccl;

    /**
     * Creates the Http Execution Context.
     *
     * @param delegate the delegate context.
     * @param context  the HTTP Context to use
     * @param tccl     the TTCL to use
     */
    public HttpExecutionContext(ExecutionContext delegate, Context context, ClassLoader tccl) {
        this.delegate = delegate;
        this.context = context;
        this.tccl = tccl;
    }

    /**
     * Prepares the environment.
     *
     * @return the execution context
     */
    @Override
    public ExecutionContext prepare() {
        return delegate.prepare();
    }

    /**
     * Executes the given runnable within the configured execution context.
     * This method sets the HTTP Context and TCCL to the configured one, executes the runnable,
     * and restore the original execution context (HTTP Context and TCCL).
     *
     * @param runnable the runnable to execute
     */
    @Override
    public void execute(final Runnable runnable) {
        delegate.execute(new Runnable() {
            @Override
            public void run() {
                Thread thread = Thread.currentThread();
                // Store the TCCL and the current context.
                // They will be restored after the execution.
                final ClassLoader oldContextClassLoader = thread.getContextClassLoader();
                final Context oldHttpContext = Context.CONTEXT.get();
                thread.setContextClassLoader(tccl);
                Context.CONTEXT.set(context);
                try {
                    // We call the run method directly as wrapping the given runnable within another 'enhanced'
                    // runnable preparing the execution environment.
                    runnable.run(); //NOSONAR
                } finally {
                    // Restore the execution environment.
                    thread.setContextClassLoader(oldContextClassLoader);
                    Context.CONTEXT.set(oldHttpContext);
                }
            }
        });
    }

    /**
     * Reports that an asynchronous computation failed.
     */
    @Override
    public void reportFailure(Throwable t) {
        delegate.reportFailure(t);
    }
}
