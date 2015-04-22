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
import org.wisdom.api.concurrent.ManagedExecutorService;
import org.wisdom.api.concurrent.ManagedFutureTask;
import org.wisdom.api.exceptions.HttpException;
import org.wisdom.api.http.AsyncResult;
import org.wisdom.api.http.Result;
import org.wisdom.api.interception.Interceptor;
import org.wisdom.api.interception.RequestContext;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * The interceptor managing {@link Async} actions.
 */
@Component
@Provides(specifications = Interceptor.class)
@Instantiate
public class AsyncInterceptor extends Interceptor<Async> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncInterceptor.class);

    @Requires(filter = "(name=" + ManagedExecutorService.SYSTEM + ")", proxy = false)
    protected ManagedExecutorService executor;

    private class ResultRetriever implements Callable<Result> {

        private final RequestContext context;
        private final Async configuration;

        /**
         * Creates a {@link org.wisdom.executors.AsyncInterceptor.ResultRetriever}.
         *
         * @param context       the context
         * @param configuration the annotation configuration
         */
        public ResultRetriever(RequestContext context, Async configuration) {
            this.context = context;
            this.configuration = configuration;
        }

        @Override
        public Result call() throws Exception {
            final ManagedFutureTask<Result> task = executor.submit(new Callable<Result>() {
                @Override
                public Result call() throws Exception {
                    return context.proceed();
                }
            });

            Result result;
            try {
                result = task.get(configuration.timeout(), configuration.unit());
            } catch (TimeoutException e) {
                LOGGER.debug("Call on {} was cancelled because it took more than {} {}",
                        context.route().getUrl(),
                        configuration.unit(),
                        configuration.timeout()
                );
                // Interrupt the computation if supported.
                task.cancel(true);
                throw new HttpException(Result.GATEWAY_TIMEOUT, "Request timeout");
            } catch (InterruptedException e) {
                LOGGER.debug("Call on {} was interrupted", context.route().getUrl());
                throw new HttpException(Result.GATEWAY_TIMEOUT, "Request timeout");
            } catch (ExecutionException e) {
                throw new HttpException(Result.INTERNAL_SERVER_ERROR, "Computation error", e);
            }

            if (result == null) {
                throw new HttpException(Result.INTERNAL_SERVER_ERROR, "Computation error");
            }

            return result;
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
        Callable<Result> callable;
        if (configuration.timeout() > 0) {
            callable = new ResultRetriever(context, configuration);
        } else {
            callable = new Callable<Result>() {
                @Override
                public Result call() throws Exception {
                    return context.proceed();
                }
            };
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
