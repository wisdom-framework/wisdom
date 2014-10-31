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

import akka.dispatch.OnComplete;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.wisdom.api.annotations.scheduler.Async;
import org.wisdom.api.annotations.scheduler.Every;
import org.wisdom.api.exceptions.HttpException;
import org.wisdom.api.http.*;
import org.wisdom.api.interception.RequestContext;
import org.wisdom.api.scheduler.Scheduled;
import scala.concurrent.Future;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Checks the Async Interceptor.
 */
public class AsyncInterceptorTest {

    AkkaScheduler scheduler = new AkkaScheduler();
    AsyncInterceptor interceptor = new AsyncInterceptor();
    private AkkaBootstrap akka;

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
        akka = new AkkaBootstrap(context);
        akka.start();
        scheduler.akka = akka;
        interceptor.akka = akka;
    }

    @After
    public void tearDown() {
        ((AkkaBootstrap) scheduler.akka).stop();
    }

    @Test
    public void testWithoutTimeout() throws Exception {
        RequestContext rc = mock(RequestContext.class);
        when(rc.proceed()).thenReturn(new Result(Status.OK));

        Async async = mock(Async.class);
        when(async.timeout()).thenReturn(0l);
        when(async.unit()).thenReturn(TimeUnit.SECONDS);

        Result result = interceptor.call(async, rc);
        assertThat(result).isInstanceOf(AsyncResult.class);

        Future<Result> r = akka.dispatch(((AsyncResult) result).callable(), akka.fromThread());
        final int[] code = {0};
        r.onComplete(new OnComplete<Result>() {

            @Override
            public void onComplete(Throwable failure, Result success) throws Throwable {
                code[0] = success.getStatusCode();
            }
        }, akka.fromThread());

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

        Future<Result> r = akka.dispatch(((AsyncResult) result).callable(), akka.fromThread());
        final int[] code = {0};
        r.onComplete(new OnComplete<Result>() {

            @Override
            public void onComplete(Throwable failure, Result success) throws Throwable {
                code[0] = success.getStatusCode();
            }
        }, akka.fromThread());

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

        Async async = mock(Async.class);
        when(async.timeout()).thenReturn(10l);
        when(async.unit()).thenReturn(TimeUnit.SECONDS);

        Result result = interceptor.call(async, rc);
        assertThat(result).isInstanceOf(AsyncResult.class);

        Future<Result> r = akka.dispatch(((AsyncResult) result).callable(), akka.fromThread());
        final Result[] retrieved = {null};
        final Throwable[] errors = {null};
        r.onComplete(new OnComplete<Result>() {

            @Override
            public void onComplete(Throwable failure, Result success) throws Throwable {
                retrieved[0] = success;
                errors[0] = failure;
            }
        }, akka.fromThread());

        Thread.sleep(100);
        assertThat(retrieved[0]).isNull();
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

        Async async = mock(Async.class);
        // Must be below the thread.sleep from the action.
        when(async.timeout()).thenReturn(10l);
        when(async.unit()).thenReturn(TimeUnit.MILLISECONDS);

        Result result = interceptor.call(async, rc);
        assertThat(result).isInstanceOf(AsyncResult.class);

        Future<Result> r = akka.dispatch(((AsyncResult) result).callable(), akka.fromThread());
        final Result[] retrieved = {null};
        final Throwable[] errors = {null};
        r.onComplete(new OnComplete<Result>() {

            @Override
            public void onComplete(Throwable failure, Result success) throws Throwable {
                retrieved[0] = success;
                errors[0] = failure;
            }
        }, akka.fromThread());

        Thread.sleep(100);
        assertThat(retrieved[0]).isNull();
        assertThat(errors[0]).isNotNull().isInstanceOf(HttpException.class);
        assertThat(errors[0].getCause().getMessage())
                .contains("Request timeout");
    }


}
