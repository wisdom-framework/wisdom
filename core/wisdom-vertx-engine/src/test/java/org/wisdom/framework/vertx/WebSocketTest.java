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

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import org.junit.After;
import org.junit.Test;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.exceptions.ExceptionMapper;
import org.wisdom.api.http.websockets.WebSocketListener;
import org.wisdom.api.router.Router;
import org.wisdom.framework.vertx.file.DiskFileUpload;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test the web socket management.
 */
public class WebSocketTest extends VertxBaseTest {

    private WisdomVertxServer server;

    @After
    public void tearDown() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    @Test
    public void testPingPong() throws InterruptedException, IOException {
        prepareServer();

        Spy spy = new Spy(server);
        server.register(spy);

        final CountDownLatch done = new CountDownLatch(1);
        final StringBuilder marker = new StringBuilder();

        HttpClient client = vertx.createHttpClient(new HttpClientOptions().setDefaultHost("localhost")
                .setDefaultPort(server.httpPort()));

        client.websocket("/some-uri", ws -> {
            ws.handler(event -> {
                if (event.toString().equals("pong")) {
                    marker.append("pong");
                    done.countDown();
                }
            });
            ws.write(Buffer.buffer("ping"));
        });

        done.await(30, TimeUnit.SECONDS);
        assertThat(marker).containsOnlyOnce("pong");
        client.close();
    }

    @Test
    public void testMultipleP2PPingPong() throws InterruptedException, IOException {
        prepareServer();

        Spy spy = new Spy(server);
        server.register(spy);

        // Now start bunch of clients
        int num = NUMBER_OF_CLIENTS;
        final CountDownLatch startSignal = new CountDownLatch(1);
        final CountDownLatch doneSignal = new CountDownLatch(num);

        for (int i = 1; i < num + 1; ++i) {
            final int id = i;
            executor.submit(() -> {
                try {
                    startSignal.await();
                    HttpClientOptions options = new HttpClientOptions()
                            .setDefaultHost("localhost")
                            .setDefaultPort(server.httpPort());
                    vertx.createHttpClient(options)
                            .websocket("/some-uri", ws -> {
                                ws.handler(event -> {
                                    if (event.toString().equals("" + id)) {
                                        success(id);
                                    } else {
                                        fail(id);
                                    }
                                    doneSignal.countDown();
                                });
                                ws.write(Buffer.buffer("" + id));
                            });
                } catch (Throwable ex) {
                    ex.printStackTrace();
                    fail(id);
                }
            });
        }

        startSignal.countDown();      // let all threads proceed
        doneSignal.await(60, TimeUnit.SECONDS);           // wait for all to finish

        assertThat(failure).isEmpty();
        assertThat(success).hasSize(num);
    }

    @Test
    public void testBroadcast() throws InterruptedException, IOException {
        prepareServer();

        // Now start bunch of clients
        int num = NUMBER_OF_CLIENTS;
        final CountDownLatch preparedSignal = new CountDownLatch(num);

        final CountDownLatch doneSignal = new CountDownLatch(num);

        for (int i = 1; i < num + 1; ++i) {
            final int id = i;
            executor.submit(() -> {
                try {
                    HttpClientOptions options = new HttpClientOptions()
                            .setDefaultHost("localhost")
                            .setDefaultPort(server.httpPort());
                    HttpClient client = vertx.createHttpClient(options);

                    client.websocket("/some-uri", ws -> {
                        preparedSignal.countDown();
                        ws.handler(event -> {
                            if (event.toString().equals("hello")) {
                                success(id);
                            } else {
                                fail(id);
                            }
                            doneSignal.countDown();
                        });
                    });
                } catch (Throwable ex) {
                    ex.printStackTrace();
                    fail(id);
                }
            });
        }

       // startSignal.countDown();      // let all threads proceed
        preparedSignal.await(30, TimeUnit.SECONDS);
        server.publish("/some-uri", "hello");

        doneSignal.await(60, TimeUnit.SECONDS);           // wait for all to finish

        assertThat(failure).isEmpty();
        assertThat(success).hasSize(num);
    }

    private void prepareServer() throws InterruptedException, IOException {
        // Prepare the configuration
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getIntegerWithDefault(eq("vertx.http.port"), anyInt())).thenReturn(0);
        when(configuration.getIntegerWithDefault(eq("vertx.https.port"), anyInt())).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.receiveBufferSize", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.sendBufferSize", -1)).thenReturn(-1);
        when(configuration.getLongWithDefault("http.upload.disk.threshold", DiskFileUpload.MINSIZE)).thenReturn
                (DiskFileUpload.MINSIZE);
        when(configuration.getLongWithDefault("http.upload.max", -1L)).thenReturn(-1L);
        when(configuration.getIntegerWithDefault("vertx.acceptBacklog", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.maxWebSocketFrameSize", -1)).thenReturn(-1);
        when(configuration.getStringArray("wisdom.websocket.subprotocols")).thenReturn(new String[0]);
        when(configuration.getStringArray("vertx.websocket-subprotocols")).thenReturn(new String[0]);

        // Configure the server.
        server = new WisdomVertxServer();
        server.accessor = new ServiceAccessor(
                null,
                configuration,
                mock(Router.class),
                getMockContentEngine(),
                executor,
                server,
                Collections.<ExceptionMapper>emptyList()
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
