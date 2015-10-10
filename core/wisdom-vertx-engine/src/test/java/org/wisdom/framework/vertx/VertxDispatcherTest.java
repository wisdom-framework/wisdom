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

import com.google.common.base.Charsets;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.content.ContentEngine;
import org.wisdom.api.exceptions.ExceptionMapper;
import org.wisdom.api.http.websockets.WebSocketListener;
import org.wisdom.api.router.Router;
import org.wisdom.framework.vertx.ssl.SSLServerContext;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test the server.
 */
public class VertxDispatcherTest extends VertxBaseTest {

    private WisdomVertxServer server;

    Vertx vertx = Vertx.vertx();

    @After
    public void tearDown() throws InterruptedException {
        if (server != null) {
            server.stop();
            server = null;
        }

        CountDownLatch latch = new CountDownLatch(1);
        if (vertx != null) {
            vertx.close(v -> {
                latch.countDown();
            });
        }
        latch.await();
        FileUtils.deleteQuietly(new File("target/junk/conf/conf/fake.keystore"));
    }

    public void prepareHttps() throws KeyManagementException, NoSuchAlgorithmException {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = (hostname, session) -> true;

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

    }


    @Test
    public void testServerStartSequence() throws InterruptedException, IOException, NoSuchAlgorithmException, KeyManagementException {
        prepareHttps();
        prepareServer();
        VertxHttpServerTest.waitForStart(server);
        VertxBaseTest.waitForHttpsStart(server);

        int http = server.httpPort();
        int https = server.httpsPort();

        HttpURLConnection connection = null;
        int responseCode = 0;
        for (int i = 0; i < 10; i++) {
            URL url = new URL("http://localhost:" + http + "/test");
            try {
                connection = (HttpURLConnection) url.openConnection();
                responseCode = connection.getResponseCode();
            } catch (IOException e) {
                // Probably not yet started, waiting.
                // Wait a maximum of 20 seconds, should be enough on most machine.
                Thread.sleep(2000);
            }
        }
        System.out.println("1");
        // Here either the server has started, or something really bad happened.
        assertThat(connection).isNotNull();
        assertThat(responseCode).isEqualTo(404);
        System.out.println("2");
        connection = null;
        responseCode = 0;
        for (int i = 0; i < 10; i++) {
            URL url = new URL("https://localhost:" + https + "/test");
            try {
                connection = (HttpURLConnection) url.openConnection();

                responseCode = connection.getResponseCode();
            } catch (IOException e) {
                // Probably not yet started, waiting.
                // Wait a maximum of 20 seconds, should be enough on most machine.
                Thread.sleep(2000);
            }
        }
        System.out.println("3");
        // Here either the server has started, or something really bad happened.
        assertThat(connection).isNotNull();
        assertThat(responseCode).isEqualTo(404);

        assertThat(server.hostname()).isEqualTo("localhost");
        assertThat(server.httpPort()).isGreaterThan(9000);
        assertThat(server.httpsPort()).isGreaterThan(9001);
    }

    @Test
    public void testWebSocketDispatching() throws InterruptedException {
        // Prepare the configuration
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getIntegerWithDefault(eq("vertx.http.port"), anyInt())).thenReturn(-1);
        when(configuration.getIntegerWithDefault(eq("vertx.https.port"), anyInt())).thenReturn(-1);

        // Prepare an empty router.
        Router router = mock(Router.class);
        ContentEngine contentEngine = mock(ContentEngine.class);

        // Configure the server.
        server = new WisdomVertxServer();
        server.accessor = new ServiceAccessor(
                null,
                configuration,
                router,
                contentEngine,
                null,
                null,
                Collections.<ExceptionMapper>emptyList()
        );
        server.configuration = configuration;
        server.vertx = vertx;

        server.start();

        final ServerWebSocket socket = mock(ServerWebSocket.class);
        final Socket sock = new Socket(socket);
        server.addSocket("/hello", sock);

        MyWebSocketListener listener = new MyWebSocketListener();
        server.register(listener);
        // The listener should have been notified.
        assertThat(listener.opened).isNotNull();

        server.received("/hello", "message".getBytes(Charsets.UTF_8), sock);

        // The listener should have received the message.
        assertThat(listener.lastMessage).isEqualTo("message");
        assertThat(listener.lastClient).isNotNull();
        assertThat(listener.closed).isNull();

        server.received("/hello", "message2".getBytes(Charsets.UTF_8), sock);
        assertThat(listener.lastMessage).isEqualTo("message2");
        assertThat(listener.lastClient).isNotNull();
        assertThat(listener.closed).isNull();

        server.removeSocket("/hello", sock);
        assertThat(listener.closed).isNotNull();

    }

    @Test
    public void testWebSocketWithMultiClients() throws InterruptedException, IOException {
        // Prepare the configuration
        prepareServer();

        final ServerWebSocket socket1 = mock(ServerWebSocket.class);
        final Socket sock1 = new Socket(socket1);
        final ServerWebSocket socket2 = mock(ServerWebSocket.class);
        final Socket sock2 = new Socket(socket2);

        MyWebSocketListener listener = new MyWebSocketListener();
        server.register(listener);


        server.addSocket("/hello", sock1);
        // The listener should have been notified.
        assertThat(listener.opened).isNotNull();
        server.received("/hello", "message".getBytes(Charsets.UTF_8), sock1);

        // The listener should have received the message.
        assertThat(listener.lastMessage).isEqualTo("message");
        assertThat(listener.lastClient).isEqualTo(Integer.toOctalString(socket1.hashCode()));

        server.addSocket("/hello", sock2);
        server.received("/hello", "message2".getBytes(Charsets.UTF_8), sock2);
        assertThat(listener.lastMessage).isEqualTo("message2");
        assertThat(listener.lastClient).isEqualTo(Integer.toOctalString(socket2.hashCode()));

        server.removeSocket("/hello", sock1);
        server.removeSocket("/hello", sock2);
        assertThat(listener.closed).isNotNull();
    }

    @Test
    public void testWebSocketSending() throws InterruptedException, IOException {
        prepareServer();


        final ServerWebSocket socket1 = mock(ServerWebSocket.class);
        final Socket sock = new Socket(socket1);
        when(socket1.textHandlerID()).thenReturn("/hello");

        MyWebSocketListener listener = new MyWebSocketListener();
        server.register(listener);


        server.addSocket("/hello", sock);
        // The listener should have been notified.
        assertThat(listener.opened).isNotNull();
        server.received("/hello", "message".getBytes(Charsets.UTF_8), sock);

        // The listener should have received the message.
        assertThat(listener.lastMessage).isEqualTo("message");
        assertThat(listener.lastClient).isEqualTo(WisdomVertxServer.id(sock));

        server.send("/hello", WisdomVertxServer.id(sock), "response");
        // Write on missing client.
        server.send("/hello", "missing", "response");
        server.publish("/hello", "yep !");

    }

    private void prepareServer() throws IOException, InterruptedException {
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getIntegerWithDefault(eq("vertx.http.port"), anyInt())).thenReturn(0);
        when(configuration.getIntegerWithDefault(eq("vertx.https.port"), anyInt())).thenReturn(0);
        when(configuration.getIntegerWithDefault("vertx.acceptBacklog", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.receiveBufferSize", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.sendBufferSize", -1)).thenReturn(-1);
        when(configuration.getStringArray("wisdom.websocket.subprotocols")).thenReturn(new String[0]);
        when(configuration.getStringArray("vertx.websocket-subprotocols")).thenReturn(new String[0]);
        when(configuration.getBaseDir()).thenReturn(new File("target/junk/conf"));

        // Prepare an empty router.
        Router router = mock(Router.class);

        ContentEngine contentEngine = mock(ContentEngine.class);

        // Configure the server.
        server = new WisdomVertxServer();
        server.accessor = new ServiceAccessor(
                null,
                configuration,
                router,
                contentEngine,
                null,
                null,
                Collections.<ExceptionMapper>emptyList()
        );
        server.configuration = configuration;
        server.vertx = vertx;

        server.start();

        waitForStart(server);
    }

    private class MyWebSocketListener implements WebSocketListener {
        String lastMessage;
        String lastClient;
        String opened;
        String closed;

        @Override
        public void received(String uri, String client, byte[] content) {
            this.lastMessage = new String(content, Charsets.UTF_8);
            this.lastClient = client;
        }

        @Override
        public void opened(String uri, String client) {
            this.opened = client;
        }

        @Override
        public void closed(String uri, String client) {
            this.closed = client;
        }
    }
}
