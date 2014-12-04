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
package org.wisdom.engine.server;

import com.google.common.base.Charsets;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.junit.After;
import org.junit.Test;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.content.ContentEncodingHelper;
import org.wisdom.api.content.ContentEngine;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.Renderable;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.websockets.WebSocketListener;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.Router;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test the dispatcher.
 */
public class DispatcherTest {

    private Dispatcher dispatcher;

    @After
    public void tearDown() {
        if (dispatcher != null) {
            dispatcher.stop();
            dispatcher = null;
        }
    }

    @Test
    public void testServerStartSequence() throws InterruptedException, IOException {
        // Prepare the configuration
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getIntegerWithDefault(eq("netty.http.port"), anyInt())).thenReturn(0);
        when(configuration.getIntegerWithDefault(eq("netty.https.port"), anyInt())).thenReturn(0);

        // Prepare an empty router.
        Router router = mock(Router.class);

        ContentEncodingHelper encodingHelper = new ContentEncodingHelper() {

            @Override
            public List<String> parseAcceptEncodingHeader(String headerContent) {
                return new ArrayList<String>();
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

        dispatcher = new Dispatcher();
        dispatcher.configuration = configuration;
        dispatcher.router = router;
        dispatcher.parsers = contentEngine;

        dispatcher.start();
        // Wait for initialization.
        Thread.sleep(5000);

        int http = dispatcher.httpPort();
        int https = dispatcher.httpsPort();

        HttpURLConnection connection = null;
        int responseCode = 0;
        for (int i = 0; i < 10; i++) {
            URL url = new URL("http://localhost:" + dispatcher.httpPort() + "/test");
            try {
                connection = (HttpURLConnection) url.openConnection();
                responseCode = connection.getResponseCode();
                if (responseCode != 0) {
                    break;
                }
            } catch (IOException e) {
                // Probably not yet started, waiting.
                // Wait a maximum of 20 seconds, should be enough on most machine.
                Thread.sleep(2000);
            }
        }
        // Here either the server has started, or something really bad happened.
        assertThat(connection).isNotNull();
        assertThat(responseCode).isEqualTo(404);

        assertThat(dispatcher.hostname()).isEqualTo("localhost");
        assertThat(dispatcher.httpPort()).isGreaterThan(8080);
        assertThat(dispatcher.httpsPort()).isGreaterThan(8081);
    }

    @Test
    public void testWebSocketDispatching() throws InterruptedException {
        // Prepare the configuration
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getIntegerWithDefault(eq("netty.http.port"), anyInt())).thenReturn(-1);
        when(configuration.getIntegerWithDefault(eq("netty.https.port"), anyInt())).thenReturn(-1);

        // Prepare an empty router.
        Router router = mock(Router.class);

        ContentEncodingHelper encodingHelper = new ContentEncodingHelper() {

            @Override
            public List<String> parseAcceptEncodingHeader(String headerContent) {
                return new ArrayList<String>();
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

        dispatcher = new Dispatcher();
        dispatcher.configuration = configuration;
        dispatcher.router = router;
        dispatcher.parsers = contentEngine;

        dispatcher.start();
        // Wait for initialization.
        Thread.sleep(1000);

        final ChannelHandlerContext channelHandlerContext = mock(ChannelHandlerContext.class);
        when(channelHandlerContext.channel()).thenReturn(mock(io.netty.channel.Channel.class));
        dispatcher.addWebSocket("/hello", channelHandlerContext);

        MyWebSocketListener listener = new MyWebSocketListener();
        dispatcher.register(listener);
        // The listener should have been notified.
        assertThat(listener.opened).isNotNull();

        dispatcher.received("/hello", "message".getBytes(Charsets.UTF_8), channelHandlerContext);

        // The listener should have received the message.
        assertThat(listener.lastMessage).isEqualTo("message");
        assertThat(listener.lastClient).isNotNull();
        assertThat(listener.closed).isNull();

        dispatcher.received("/hello", "message2".getBytes(Charsets.UTF_8), channelHandlerContext);
        assertThat(listener.lastMessage).isEqualTo("message2");
        assertThat(listener.lastClient).isNotNull();
        assertThat(listener.closed).isNull();

        dispatcher.removeWebSocket("/hello", channelHandlerContext);
        assertThat(listener.closed).isNotNull();

    }

    @Test
    public void testWebSocketWithMultiClients() throws InterruptedException {
        // Prepare the configuration
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getIntegerWithDefault(eq("netty.http.port"), anyInt())).thenReturn(-1);
        when(configuration.getIntegerWithDefault(eq("netty.https.port"), anyInt())).thenReturn(-1);

        // Prepare an empty router.
        Router router = mock(Router.class);

        ContentEncodingHelper encodingHelper = new ContentEncodingHelper() {

            @Override
            public List<String> parseAcceptEncodingHeader(String headerContent) {
                return new ArrayList<String>();
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

        dispatcher = new Dispatcher();
        dispatcher.configuration = configuration;
        dispatcher.router = router;
        dispatcher.parsers = contentEngine;

        dispatcher.start();
        // Wait for initialization.
        Thread.sleep(1000);

        MyWebSocketListener listener = new MyWebSocketListener();
        dispatcher.register(listener);

        final ChannelHandlerContext client1 = mock(ChannelHandlerContext.class);
        final ChannelHandlerContext client2 = mock(ChannelHandlerContext.class);
        when(client1.channel()).thenReturn(mock(io.netty.channel.Channel.class));
        when(client2.channel()).thenReturn(mock(io.netty.channel.Channel.class));

        dispatcher.addWebSocket("/hello", client1);
        // The listener should have been notified.
        assertThat(listener.opened).isNotNull();
        dispatcher.received("/hello", "message".getBytes(Charsets.UTF_8), client1);

        // The listener should have received the message.
        assertThat(listener.lastMessage).isEqualTo("message");
        assertThat(listener.lastClient).isEqualTo(Integer.toOctalString(client1.channel().hashCode()));

        dispatcher.addWebSocket("/hello", client2);
        dispatcher.received("/hello", "message2".getBytes(Charsets.UTF_8), client2);
        assertThat(listener.lastMessage).isEqualTo("message2");
        assertThat(listener.lastClient).isEqualTo(Integer.toOctalString(client2.channel().hashCode()));

        dispatcher.removeWebSocket("/hello", client1);
        dispatcher.removeWebSocket("/hello", client2);
        assertThat(listener.closed).isNotNull();
    }

    @Test
    public void testWebSocketSending() throws InterruptedException {
        // Prepare the configuration
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getIntegerWithDefault(eq("netty.http.port"), anyInt())).thenReturn(-1);
        when(configuration.getIntegerWithDefault(eq("netty.https.port"), anyInt())).thenReturn(-1);

        // Prepare an empty router.
        Router router = mock(Router.class);

        ContentEncodingHelper encodingHelper = new ContentEncodingHelper() {

            @Override
            public List<String> parseAcceptEncodingHeader(String headerContent) {
                return new ArrayList<String>();
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

        dispatcher = new Dispatcher();
        dispatcher.configuration = configuration;
        dispatcher.router = router;
        dispatcher.parsers = contentEngine;

        dispatcher.start();
        // Wait for initialization.
        Thread.sleep(1000);

        MyWebSocketListener listener = new MyWebSocketListener();
        dispatcher.register(listener);

        final ChannelHandlerContext client1 = mock(ChannelHandlerContext.class);
        when(client1.channel()).thenReturn(mock(io.netty.channel.Channel.class));

        dispatcher.addWebSocket("/hello", client1);
        // The listener should have been notified.
        assertThat(listener.opened).isNotNull();
        dispatcher.received("/hello", "message".getBytes(Charsets.UTF_8), client1);

        // The listener should have received the message.
        assertThat(listener.lastMessage).isEqualTo("message");
        assertThat(listener.lastClient).isEqualTo(Dispatcher.id(client1));

        dispatcher.send("/hello", Dispatcher.id(client1), "response");
        verify(client1, times(1)).writeAndFlush(any(TextWebSocketFrame.class));

        // Write on missing client.
        dispatcher.send("/hello", "missing", "response");
        // Still 1
        verify(client1, times(1)).writeAndFlush(any(TextWebSocketFrame.class));

        dispatcher.publish("/hello", "yep !");
        verify(client1, times(2)).writeAndFlush(any(TextWebSocketFrame.class));
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
