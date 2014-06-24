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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.api.http.Status;
import org.wisdom.test.parents.FakeContext;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class AkkaBootstrapTest {

    private AkkaBootstrap akka;
    private FakeContext http;

    @Before
    public void setUp() throws ClassNotFoundException {
        BundleContext context = mock(BundleContext.class);
        Bundle bundle = mock(Bundle.class);
        doAnswer(new Answer<Class<?>>() {
            @Override
            public Class<?> answer(InvocationOnMock invocation) throws Throwable {
                return this.getClass().getClassLoader().loadClass((String) invocation.getArguments()[0]);
            }
        }).when(bundle).loadClass(anyString());
        when(context.getBundle()).thenReturn(bundle);
        akka = new AkkaBootstrap(context);

        http = new FakeContext();
        Context.CONTEXT.set(http);
    }

    @After
    public void tearDown() {
        Context.CONTEXT.remove();
        akka.stop();
    }

    @Test
    public void startAndStop() throws ClassNotFoundException {
        assertThat(akka.system()).isNull();
        akka.start();
        assertThat(akka.system()).isNotNull();
        akka.stop();
        assertThat(akka.system()).isNull();
    }

    @Test
    public void testDispatchResult() throws Exception {
        akka.start();
        Future<Result> future = akka.dispatchResult(new Callable<Result>() {
            @Override
            public Result call() throws Exception {
                // check we have a HTTP Context
                if (Context.CONTEXT.get() == null) {
                    return Results.internalServerError();
                }
                if (Context.CONTEXT.get() != http) {
                    return Results.badRequest();
                }
                return Results.ok();
            }
        });
        Await.result(future, Duration.apply(10, TimeUnit.SECONDS));
        assertThat(future.value().get().get().getStatusCode()).isEqualTo(Status.OK);
    }

    @Test
    public void testDispatchUsingCustomContext() throws Exception {
        akka.start();
        final FakeContext ctx = new FakeContext();
        Future<Result> future = akka.dispatchResultWithContext(new Callable<Result>() {
            @Override
            public Result call() throws Exception {
                // check we have a HTTP Context
                if (Context.CONTEXT.get() == null) {
                    return Results.internalServerError();
                }
                if (Context.CONTEXT.get() != ctx) {
                    return Results.badRequest();
                }
                return Results.ok();
            }
        }, ctx);
        Await.result(future, Duration.apply(10, TimeUnit.SECONDS));
        assertThat(future.value().get().get().getStatusCode()).isEqualTo(Status.OK);
    }

    @Test
    public void testDispatchWithFromThread() throws Exception {
        akka.start();
        Future<Result> future = akka.dispatch(new Callable<Result>() {
            @Override
            public Result call() throws Exception {
                // check we have a HTTP Context
                if (Context.CONTEXT.get() == null) {
                    return Results.internalServerError();
                }
                if (Context.CONTEXT.get() != http) {
                    return Results.badRequest();
                }
                return Results.ok();
            }
        }, akka.fromThread());
        Await.result(future, Duration.apply(10, TimeUnit.SECONDS));
        assertThat(future.value().get().get().getStatusCode()).isEqualTo(Status.OK);
    }

}