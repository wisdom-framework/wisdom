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
package org.wisdom.framework.vertx;

import akka.actor.ActorSystem;
import org.apache.http.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultVertxFactory;
import org.wisdom.akka.AkkaSystemService;
import org.wisdom.akka.impl.HttpExecutionContext;
import org.wisdom.api.content.ContentEncodingHelper;
import org.wisdom.api.content.ContentEngine;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.Renderable;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Route;
import scala.concurrent.Future;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Utility methods used in test.
 */
public class VertxBaseTest {

    public static final int NUMBER_OF_CLIENTS;

    public static final Random RANDOM;

    static {
        NUMBER_OF_CLIENTS = Integer.getInteger("vertx.test.clients", 100);
        RANDOM = new Random();
    }

    ActorSystem actor = ActorSystem.create();
    protected AkkaSystemService system;

    DefaultVertxFactory factory = new DefaultVertxFactory();
    Vertx vertx = factory.createVertx();

    ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_CLIENTS);

    List<Integer> success = new ArrayList<>();
    List<Integer> failure = new ArrayList<>();

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        system = mock(AkkaSystemService.class);
        when(system.system()).thenReturn(actor);
        when(system.fromThread()).thenReturn(new HttpExecutionContext(actor.dispatcher(), Context.CONTEXT.get(),
                Thread.currentThread().getContextClassLoader()));
        doAnswer(new Answer<Future<Result>>() {
            @Override
            public Future<Result> answer(InvocationOnMock invocation) throws Throwable {
                Callable<Result> callable = (Callable<Result>) invocation.getArguments()[0];
                Context context = (Context) invocation.getArguments()[1];

                return akka.dispatch.Futures.future(callable,
                        new HttpExecutionContext(actor.dispatcher(), context,
                                Thread.currentThread().getContextClassLoader()));
            }
        }).when(system).dispatchResultWithContext(any(Callable.class), any(Context.class));
    }

    public static boolean isOk(HttpResponse response) {
        return response != null  && response.getStatusLine().getStatusCode() == 200;
    }

    public static boolean isOk(int status) {
        return status == 200;
    }

    public static boolean containsExactly(byte[] content, byte[] expected) {
        if (content.length != expected.length) {
            return false;
        }
        for (int i = 0; i < content.length; i++) {
            if (content[i] != expected[i]) {
                return false;
            }
        }
        return true;
    }

    @After
    public void tearDown() {
        if (vertx != null) {
            vertx.stop();
        }

        failure.clear();
        success.clear();

        actor.shutdown();
        executor.shutdownNow();
    }

    public synchronized void success(int id) {
        success.add(id);
    }

    public synchronized void fail(int id) {
        failure.add(id);
    }



    static ContentEngine getMockContentEngine() {
        ContentEncodingHelper encodingHelper = new ContentEncodingHelper() {

            @Override
            public List<String> parseAcceptEncodingHeader(String headerContent) {
                return new ArrayList<>();
            }

            @Override
            public boolean shouldEncodeWithRoute(Route route) {
                return true;
            }

            @Override
            public boolean shouldEncodeWithSize(Route route,
                                                Renderable<?> renderable) {
                return true;
            }

            @Override
            public boolean shouldEncodeWithMimeType(Renderable<?> renderable) {
                return true;
            }

            @Override
            public boolean shouldEncode(Context context, Result result,
                                        Renderable<?> renderable) {
                return false;
            }

            @Override
            public boolean shouldEncodeWithHeaders(Map<String, String> headers) {
                return false;
            }
        };
        ContentEngine contentEngine = mock(ContentEngine.class);
        when(contentEngine.getContentEncodingHelper()).thenReturn(encodingHelper);
        return contentEngine;
    }
}
