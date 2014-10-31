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

import akka.dispatch.Futures;
import akka.dispatch.OnSuccess;
import com.google.common.collect.ImmutableList;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.akka.AkkaSystemService;
import org.wisdom.api.annotations.scheduler.Async;
import org.wisdom.api.exceptions.HttpException;
import org.wisdom.api.http.AsyncResult;
import org.wisdom.api.http.Result;
import org.wisdom.api.interception.Interceptor;
import org.wisdom.api.interception.RequestContext;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.Callable;

@Component
@Provides(specifications = Interceptor.class)
@Instantiate
public class AsyncInterceptor extends Interceptor<Async> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncInterceptor.class);

    @Requires
    protected AkkaSystemService akka;

    private static class ResultRetriever implements Callable<Result> {

        private RequestContext context;
        private volatile Thread currentThread;

        public ResultRetriever(RequestContext context) {
            this.context = context;
        }

        @Override
        public Result call() throws Exception {
            this.currentThread = Thread.currentThread();
            return context.proceed();
        }

        public Thread getThread() {
            return currentThread;
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
    public Result call(final Async configuration, final RequestContext context) {
        return new AsyncResult(new Callable<Result>() {
            @Override
            public Result call() throws Exception {
                try {
                    if (configuration.timeout() > 0) {
                        final ResultRetriever resultRetriever = new ResultRetriever(context);
                        final Future<Result> resultFuture = Futures.future(resultRetriever, akka.fromThread());

                        Future<Result> timeoutFuture = Futures.future(new Callable<Result>() {
                            @Override
                            public Result call() throws Exception {
                                configuration.unit().sleep(configuration.timeout());
                                throw new HttpException(Result.GATEWAY_TIMEOUT, "Request timeout");
                            }
                        }, akka.fromThread());

                        timeoutFuture.onSuccess(new OnSuccess<Result>() {
                            @Override
                            public final void onSuccess(Result r) {
                                if (resultFuture.isCompleted()) {
                                    return;
                                }
                                Thread t = resultRetriever.getThread();
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
                        }, akka.fromThread());

                        Future<Result> firstCompleted = Futures.firstCompletedOf(
                                ImmutableList.of(timeoutFuture, resultFuture), akka.fromThread());
                        return Await.result(firstCompleted, Duration.Inf());
                    }
                    return context.proceed();
                } catch (InterruptedException ie) {
                    throw new HttpException(Result.RESET_CONTENT, "Interrupted process", ie);
                } catch (Exception t) {
                    throw new HttpException(Result.INTERNAL_SERVER_ERROR, "Computation error", t);
                }
            }
        });
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
