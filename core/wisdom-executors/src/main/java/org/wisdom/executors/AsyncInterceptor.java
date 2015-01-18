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
package org.wisdom.executors;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.annotations.scheduler.Async;
import org.wisdom.api.concurrent.ManagedScheduledExecutorService;
import org.wisdom.api.exceptions.HttpException;
import org.wisdom.api.http.AsyncResult;
import org.wisdom.api.http.Result;
import org.wisdom.api.interception.Interceptor;
import org.wisdom.api.interception.RequestContext;

import java.util.concurrent.Callable;

@Component
@Provides(specifications = Interceptor.class)
@Instantiate
public class AsyncInterceptor extends Interceptor<Async> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncInterceptor.class);

    @Requires(filter="(name=" + ManagedScheduledExecutorService.SYSTEM + ")", proxy = false)
    protected ManagedScheduledExecutorService scheduler;

    private static class ResultRetriever implements Callable<Result> {

        private final RequestContext context;
        private final Object lock = new Object();
        private volatile Thread currentThread;
        private volatile boolean hasStarted = false; // currentThread != null
        private volatile boolean hasTimedOut = false;
        private volatile boolean wasSuccessful = false;
        private final Async configuration;

        public ResultRetriever(RequestContext context, Async configuration) {
            this.context = context;
            this.configuration = configuration;
        }

        @Override
        public Result call() throws Exception {
            if (hasTimedOut) {
                timeout();
            }
            synchronized (lock) {
                this.currentThread = Thread.currentThread();
                hasStarted = true;
            }
            Result res = null;
            Exception excp = null;
            try {
                res = context.proceed();
                synchronized (lock) {
                    wasSuccessful = true;
                }
            } catch (Exception e) {
                synchronized (lock) {
                    if (!hasTimedOut) {
                        excp = e;
                    }
                }
            } finally {
                synchronized (lock) {
                    if (!wasSuccessful && hasTimedOut) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Call on " + context.route().getUrl()
                                    + " was cancelled because it took more than " + configuration.timeout() + " "
                                    + configuration.unit().toString());
                        }
                        timeout();
                    }
                }

            }
            if (excp != null) {
                throw new HttpException(Result.INTERNAL_SERVER_ERROR, "Computation error", excp);
            }
            if (res == null) {
                throw new HttpException(Result.INTERNAL_SERVER_ERROR, "Computation error");
            }
            return res;

        }

        public void timeout() {
            throw new HttpException(Result.GATEWAY_TIMEOUT, "Request timeout");
        }

        public void setTimeout() {
            synchronized (lock) {
                if (!wasSuccessful) {
                    hasTimedOut = true;
                    if (hasStarted) {
                        Thread t = currentThread;
                        if (t != null) {
                            try {
                                t.interrupt();
                            } catch (SecurityException se) {
                                LOGGER.debug("Could not interrupt thread because of SecurityException", se);
                                throw se;
                            } catch (Throwable throwable) {
                                // We don't need this thread anymore
                            }
                        }

                    }
                }
            }
        }
    }

    /**
     * Wrap the action method as an asynchronous method. The result is computed asynchronously and returned to the
     * client once computed. Optionally a timeout can be set to return an error if the result takes too much time to
     * be computed.
     *
     * @param configuration the interception configuration
     * @param context       the interception context
     * @return an async result wrapping the action method invocation.
     */
    @Override
    public Result call(final Async configuration, final RequestContext context) throws Exception {
        Callable<Result> callable = new Callable<Result>() {
            @Override
            public Result call() throws Exception {
                return context.proceed();
            }
        };

        if (configuration.timeout() > 0) {
            final ResultRetriever proceeder = new ResultRetriever(context, configuration);
            scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    proceeder.setTimeout();
                }
            }, configuration.timeout(), configuration.unit());
            callable = proceeder;
        }
        return new AsyncResult(callable);
    }

    /**
     * Gets the annotation class configuring the current interceptor.
     *
     * @return the annotation
     */
    @Override
    public Class<Async> annotation() {
        return Async.class;
    }
}
