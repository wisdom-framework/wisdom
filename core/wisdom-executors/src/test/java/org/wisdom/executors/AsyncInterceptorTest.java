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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.wisdom.api.annotations.scheduler.Async;
import org.wisdom.api.concurrent.ManagedExecutorService;
import org.wisdom.api.concurrent.ManagedFutureTask;
import org.wisdom.api.exceptions.HttpException;
import org.wisdom.api.http.AsyncResult;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.api.http.Status;
import org.wisdom.api.interception.RequestContext;
import org.wisdom.api.router.Route;
import org.wisdom.test.parents.FakeConfiguration;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Checks the Async Interceptor.
 */
public class AsyncInterceptorTest {

    AsyncInterceptor interceptor = new AsyncInterceptor();
    ManagedExecutorService executor = new ManagedExecutorServiceImpl("test", new FakeConfiguration(Collections.<String, Object>emptyMap()), null);

    @Before
    public void setUp() throws ClassNotFoundException {
        BundleContext context = mock(BundleContext.class);
        Bundle bundle = mock(Bundle.class);
        when(bundle.getBundleId()).thenReturn(1L);
        when(context.getBundle()).thenReturn(bundle);
        doAnswer(
                new Answer<Class>() {
                    @Override
                    public Class answer(InvocationOnMock invocation) throws Throwable {
                        return AsyncInterceptorTest.class.getClassLoader().loadClass((String) invocation.getArguments()[0]);
                    }
                }
        ).when(bundle).loadClass(anyString());
        interceptor.executor = executor;
    }

    @After
    public void tearDown() {
        executor.shutdownNow();
    }

    @Test
    public void testWithoutTimeout() throws Exception {
        RequestContext rc = mock(RequestContext.class);
        Route route = mock(Route.class);
        when(route.getUrl()).thenReturn("/");
        when(rc.proceed()).thenReturn(new Result(Status.OK));

        Async async = mock(Async.class);
        when(async.timeout()).thenReturn(0l);
        when(async.unit()).thenReturn(TimeUnit.SECONDS);

        Result result = interceptor.call(async, rc);
        assertThat(result).isInstanceOf(AsyncResult.class);

        final int[] code = {0};
        ManagedFutureTask<Result> r = executor.submit(((AsyncResult) result).callable())
                .onSuccess(new ManagedFutureTask.SuccessCallback<Result>() {
                    @Override
                    public void onSuccess(ManagedFutureTask<Result> future, Result result) {
                        code[0] = result.getStatusCode();
                    }
                });
        Thread.sleep(100);
        assertThat(code[0]).isEqualTo(200);
    }

    @Test
    public void testWithTimeout() throws Exception {
        RequestContext rc = mock(RequestContext.class);
        when(rc.proceed()).thenReturn(new Result(Status.OK));

        Async async = mock(Async.class);
        when(async.timeout()).thenReturn(1l);
        when(async.unit()).thenReturn(TimeUnit.SECONDS);

        Result result = interceptor.call(async, rc);
        assertThat(result).isInstanceOf(AsyncResult.class);

        final int[] code = {0};
        ManagedFutureTask<Result> r = executor.submit(((AsyncResult) result).callable())
                .onSuccess(new ManagedFutureTask.SuccessCallback<Result>() {
                    @Override
                    public void onSuccess(ManagedFutureTask<Result> future, Result result) {
                        code[0] = result.getStatusCode();
                    }
                });

        Thread.sleep(100);
        assertThat(code[0]).isEqualTo(200);
    }

    @Test
    public void testWithFunctionalError() throws Exception {
        RequestContext rc = mock(RequestContext.class);
        doAnswer(new Answer<Result>() {

            @Override
            public Result answer(InvocationOnMock invocation) throws Throwable {
                throw new IllegalAccessException("Bad, but expected");
            }
        }).when(rc).proceed();
        Route route = mock(Route.class);
        when(route.getUrl()).thenReturn("/");
        when(rc.route()).thenReturn(route);

        Async async = mock(Async.class);
        when(async.timeout()).thenReturn(10l);
        when(async.unit()).thenReturn(TimeUnit.SECONDS);

        Result result = interceptor.call(async, rc);
        assertThat(result).isInstanceOf(AsyncResult.class);

        final Throwable[] errors = {null};

        ManagedFutureTask<Result> r = executor.submit(((AsyncResult) result).callable())
                .onFailure(new ManagedFutureTask.FailureCallback<Result>() {
                    @Override
                    public void onFailure(ManagedFutureTask<Result> future, Throwable throwable) {
                        errors[0] = throwable;
                    }
                });

        Thread.sleep(100);
        assertThat(errors[0]).isNotNull().isInstanceOf(HttpException.class);
        assertThat(errors[0].getCause().getMessage())
                .contains("Bad, but expected");
    }

    @Test
    public void testWithTimeoutReached() throws Exception {
        RequestContext rc = mock(RequestContext.class);
        doAnswer(new Answer<Result>() {

            @Override
            public Result answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(1000);
                return Results.ok("Done");
            }
        }).when(rc).proceed();
        Route route = mock(Route.class);
        when(route.getUrl()).thenReturn("/");
        when(rc.route()).thenReturn(route);

        Async async = mock(Async.class);
        // Must be below the thread.sleep from the action.
        when(async.timeout()).thenReturn(10l);
        when(async.unit()).thenReturn(TimeUnit.MILLISECONDS);

        Result result = interceptor.call(async, rc);
        assertThat(result).isInstanceOf(AsyncResult.class);

        final Result[] retrieved = {null};
        final Throwable[] errors = {null};

        ManagedFutureTask<Result> r = executor.submit(((AsyncResult) result).callable())
                .onSuccess(new ManagedFutureTask.SuccessCallback<Result>() {
                    @Override
                    public void onSuccess(ManagedFutureTask<Result> future, Result result) {
                        retrieved[0] = result;
                    }
                })
                .onFailure(new ManagedFutureTask.FailureCallback() {
                    @Override
                    public void onFailure(ManagedFutureTask future, Throwable throwable) {
                        errors[0] = throwable;
                    }
                });
        Thread.sleep(100);
        assertThat(retrieved[0]).isNull();
        assertThat(errors[0]).isNotNull().isInstanceOf(HttpException.class);
        assertThat(errors[0].getMessage())
                .contains("Request timeout");
    }


}
