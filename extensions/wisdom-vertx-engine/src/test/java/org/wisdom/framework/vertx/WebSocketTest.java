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
import org.junit.After;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.WebSocket;
import org.vertx.java.core.impl.DefaultVertxFactory;
import org.wisdom.akka.AkkaSystemService;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.content.ContentEncodingHelper;
import org.wisdom.api.content.ContentEngine;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.Renderable;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.websockets.WebSocketListener;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.Router;
import org.wisdom.framework.vertx.file.DiskFileUpload;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by clement on 18/08/2014.
 */
public class WebSocketTest {

    private WisdomVertxServer server;

    ActorSystem actor = ActorSystem.create();

    DefaultVertxFactory factory = new DefaultVertxFactory();
    Vertx vertx = factory.createVertx();

    private AkkaSystemService system;

    private List<Integer> success = new ArrayList<>();
    private List<Integer> failure = new ArrayList<>();

    public synchronized void success(int id) {
        success.add(id);
    }

    public synchronized void fail(int id) {
        failure.add(id);
    }

    @After
    public void tearDown() {
        success.clear();
        failure.clear();
    }

    @Test
    public void testPingPong() throws InterruptedException {
        prepareServer();

        Spy spy = new Spy(server);
        server.register(spy);

        final CountDownLatch done = new CountDownLatch(1);
        final StringBuilder marker = new StringBuilder();

        HttpClient client = vertx.createHttpClient().setHost("localhost").setPort(server.httpPort());

        client.connectWebsocket("/some-uri", new Handler<WebSocket>() {
            public void handle(WebSocket ws) {
                ws.dataHandler(new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer event) {
                        if (event.toString().equals("pong")) {
                            marker.append("pong");
                            done.countDown();
                        }
                    }
                });
                ws.write(new Buffer("ping"));
            }
        });

        done.await(30, TimeUnit.SECONDS);
        assertThat(marker).containsOnlyOnce("pong");
        client.close();
    }

    @Test
    public void testMultipleP2PPingPong() throws InterruptedException {
        prepareServer();

        Spy spy = new Spy(server);
        server.register(spy);

        // Now start bunch of clients
        int num = 100;
        final CountDownLatch startSignal = new CountDownLatch(1);
        final CountDownLatch doneSignal = new CountDownLatch(num);

        for (int i = 1; i < num + 1; ++i) {
            final int id = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        startSignal.await();
                        HttpClient client = vertx.createHttpClient().setHost("localhost").setPort(server.httpPort());

                        client.connectWebsocket("/some-uri", new Handler<WebSocket>() {
                            public void handle(WebSocket ws) {
                                ws.dataHandler(new Handler<Buffer>() {
                                    @Override
                                    public void handle(Buffer event) {
                                        if (event.toString().equals("" + id)) {
                                            success(id);
                                        } else {
                                            fail(id);
                                        }
                                        doneSignal.countDown();
                                    }
                                });
                                ws.write(new Buffer("" + id));
                            }
                        });
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                        fail(id);
                    }
                }
            }).start();
        }

        startSignal.countDown();      // let all threads proceed
        doneSignal.await(60, TimeUnit.SECONDS);           // wait for all to finish

        assertThat(failure).isEmpty();
        assertThat(success).hasSize(num);
    }

    @Test
    public void testBroadcast() throws InterruptedException {
        prepareServer();

        // Now start bunch of clients
        int num = 100;
        final CountDownLatch startSignal = new CountDownLatch(1);
        final CountDownLatch preparedSignal = new CountDownLatch(num);

        final CountDownLatch doneSignal = new CountDownLatch(num);

        for (int i = 1; i < num + 1; ++i) {
            final int id = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                       // startSignal.await();
                        HttpClient client = vertx.createHttpClient().setHost("localhost").setPort(server.httpPort());

                        client.connectWebsocket("/some-uri", new Handler<WebSocket>() {
                            public void handle(WebSocket ws) {
                                preparedSignal.countDown();
                                ws.dataHandler(new Handler<Buffer>() {
                                    @Override
                                    public void handle(Buffer event) {
                                        if (event.toString().equals("hello")) {
                                            success(id);
                                        } else {
                                            fail(id);
                                        }
                                        doneSignal.countDown();
                                    }
                                });
                            }
                        });
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                        fail(id);
                    }
                }
            }).start();
        }

       // startSignal.countDown();      // let all threads proceed
        preparedSignal.await(30, TimeUnit.SECONDS);
        server.publish("/some-uri", "hello");

        doneSignal.await(60, TimeUnit.SECONDS);           // wait for all to finish

        assertThat(failure).isEmpty();
        assertThat(success).hasSize(num);
    }

    private void prepareServer() throws InterruptedException {
        // Prepare the configuration
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getIntegerWithDefault(eq("vertx.http.port"), anyInt())).thenReturn(0);
        when(configuration.getIntegerWithDefault(eq("vertx.https.port"), anyInt())).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.receiveBufferSize", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.sendBufferSize", -1)).thenReturn(-1);
        when(configuration.getLongWithDefault("http.upload.disk.threshold", DiskFileUpload.MINSIZE)).thenReturn
                (DiskFileUpload.MINSIZE);
        when(configuration.getLongWithDefault("http.upload.max", -1l)).thenReturn(-1l);
        when(configuration.getIntegerWithDefault("vertx.acceptBacklog", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.maxWebSocketFrameSize", -1)).thenReturn(-1);
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

        // Configure the server.
        server = new WisdomVertxServer();
        server.accessor = new ServiceAccessor(
                null,
                configuration,
                mock(Router.class),
                contentEngine,
                system,
                server
        );
        server.vertx = vertx;
        server.configuration = configuration;
        server.start();

        VertxHttpServerTest.waitForStart(server);
    }

    private class Spy implements WebSocketListener {

        private final WisdomVertxServer server;

        public Spy(WisdomVertxServer server) {
            this.server = server;
        }

        @Override
        public void received(String uri, String client, byte[] content) {
            if (new String(content).equalsIgnoreCase("ping")) {
                server.send(uri, client, "pong");
            } else {
                // Echo
                server.send(uri, client, content);
            }
        }

        @Override
        public void opened(String uri, String client) {

        }

        @Override
        public void closed(String uri, String client) {

        }
    }
}
