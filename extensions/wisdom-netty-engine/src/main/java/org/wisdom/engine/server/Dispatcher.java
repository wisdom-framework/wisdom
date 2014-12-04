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

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.akka.AkkaSystemService;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.content.ContentEngine;
import org.wisdom.api.crypto.Crypto;
import org.wisdom.api.engine.WisdomEngine;
import org.wisdom.api.http.websockets.WebSocketDispatcher;
import org.wisdom.api.http.websockets.WebSocketListener;
import org.wisdom.api.router.Router;

import java.util.*;

/**
 * The main entry point of the Wisdom Netty Engine.
 */
@Component
@Provides
@Instantiate
public class Dispatcher implements WebSocketDispatcher, WisdomEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(Dispatcher.class);

    /**
     * The Wisdom Server instance.
     */
    private WisdomServer wisdomServer;

    /**
     * The set of Web Socket Listeners used to dispatch data received on web sockets.
     */
    private List<WebSocketListener> listeners = new ArrayList<>();

    /**
     * The map of uri / list of channel context keeping a reference on all opened web sockets.
     */
    private Map<String, List<ChannelHandlerContext>> sockets = new HashMap<>();

    /**
     * The router service.
     */
    @Requires
    Router router;

    /**
     * The application configuration.
     */
    @Requires
    ApplicationConfiguration configuration;

    /**
     * The content parser.
     */
    @Requires
    ContentEngine parsers;

    /**
     * The crypto service.
     */
    @Requires
    Crypto crypto;

    /**
     * The akka system (used for async).
     */
    @Requires
    AkkaSystemService system;

    /**
     * Starts the server.
     */
    @Validate
    public void start() {
        ServiceAccessor accessor = new ServiceAccessor(crypto, configuration, router,
                parsers, system, this); //NOSONAR
        wisdomServer = new WisdomServer(accessor);
        // The starting is made in another thread:
        new Thread(new Runnable() {
            /**
             * Actually starts the server.
             */
            public void run() {
                try {
                    wisdomServer.start();
                } catch (InterruptedException e) {
                    LOGGER.error("Cannot start the Wisdom server", e);
                }
            }
        }).start();
    }

    /**
     * Stops the server.
     */
    @Invalidate
    public void stop() {
        wisdomServer.stop();
        sockets.clear();
        listeners.clear();
    }

    /**
     * Publishes the given message to all clients subscribed to the web socket specified using its url.
     *
     * @param url  the url of the web socket, must not be {@literal null}
     * @param data the data, must not be {@literal null}
     */
    @Override
    public void publish(String url, String data) {
        List<ChannelHandlerContext> channels;
        synchronized (this) {
            List<ChannelHandlerContext> ch = sockets.get(url);
            if (ch != null) {
                channels = new ArrayList<>(ch);
            } else {
                channels = Collections.emptyList();
            }
        }
        for (ChannelHandlerContext channel : channels) {
            channel.writeAndFlush(new TextWebSocketFrame(data));
        }
    }

    /**
     * Publishes the given message to all clients subscribed to the web socket specified using its url.
     *
     * @param url  the url of the web socket, must not be {@literal null}
     * @param data the data, must not be {@literal null}
     */
    @Override
    public synchronized void publish(String url, byte[] data) {
        List<ChannelHandlerContext> channels;
        synchronized (this) {
            List<ChannelHandlerContext> ch = sockets.get(url);
            if (ch != null) {
                channels = new ArrayList<>(ch);
            } else {
                channels = Collections.emptyList();
            }
        }
        for (ChannelHandlerContext channel : channels) {
            channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.copiedBuffer(data)));
        }
    }

    /**
     * A client subscribed to a web socket.
     *
     * @param url the url of the web sockets.
     * @param ctx the client channel.
     */
    public void addWebSocket(String url, ChannelHandlerContext ctx) {
        LOGGER.info("Adding web socket on {} bound to {}, {}", url, ctx, ctx.channel());
        List<WebSocketListener> webSocketListeners;
        synchronized (this) {
            List<ChannelHandlerContext> channels = sockets.get(url);
            if (channels == null) {
                channels = new ArrayList<>();
            }
            channels.add(ctx);
            sockets.put(url, channels);
            webSocketListeners = new ArrayList<>(this.listeners);
        }

        for (WebSocketListener listener : webSocketListeners) {
            listener.opened(url, id(ctx));
        }

    }

    /**
     * A client disconnected from a web socket.
     *
     * @param url the url of the web sockets.
     * @param ctx the client channel.
     */
    public void removeWebSocket(String url, ChannelHandlerContext ctx) {
        LOGGER.info("Removing web socket on {} bound to {}", url, ctx);
        List<WebSocketListener> webSocketListeners;
        synchronized (this) {
            List<ChannelHandlerContext> channels = sockets.get(url);
            if (channels != null) {
                channels.remove(ctx);
                if (channels.isEmpty()) {
                    sockets.remove(url);
                }
            }
            webSocketListeners = new ArrayList<>(this.listeners);
        }

        for (WebSocketListener listener : webSocketListeners) {
            listener.closed(url, id(ctx));
        }
    }

    /**
     * Registers a WebSocketListener. The listener will receive a 'open' notification for all clients connected to
     * web sockets.
     *
     * @param listener the listener, must not be {@literal null}
     */
    @Override
    public void register(WebSocketListener listener) {
        Map<String, List<ChannelHandlerContext>> copy;
        synchronized (this) {
            listeners.add(listener);
            copy = new HashMap<>(sockets);
        }

        // Call open on each opened web socket
        for (Map.Entry<String, List<ChannelHandlerContext>> entry : copy.entrySet()) {
            for (ChannelHandlerContext client : entry.getValue()) {
                listener.opened(entry.getKey(), id(client));
            }
        }
    }

    /**
     * Un-registers a web socket listeners.
     *
     * @param listener the listener, must not be {@literal null}
     */
    @Override
    public void unregister(WebSocketListener listener) {
        synchronized (this) {
            listeners.remove(listener);
        }
    }

    /**
     * Sends the given message to the client identify by its id and listening to the websocket having the given url.
     *
     * @param uri     the web socket url
     * @param client  the client id, retrieved from the {@link org.wisdom.api.http.websockets.WebSocketListener#opened
     *                (String, String)} method.
     * @param message the message to send
     */
    @Override
    public void send(String uri, String client, String message) {
        List<ChannelHandlerContext> channels;
        synchronized (this) {
            List<ChannelHandlerContext> ch = sockets.get(uri);
            if (ch != null) {
                channels = new ArrayList<>(ch);
            } else {
                channels = Collections.emptyList();
            }
        }
        for (ChannelHandlerContext channel : channels) {
            if (client.equals(id(channel))) {
                channel.writeAndFlush(new TextWebSocketFrame(message));
            }
        }
    }

    /**
     * Computes the client id for the given {@link io.netty.channel.ChannelHandlerContext}.
     *
     * @param ctx the client channel, must not be {@literal null}
     * @return the id
     */
    static String id(ChannelHandlerContext ctx) {
        return Integer.toOctalString(ctx.channel().hashCode());
    }

    /**
     * Sends the given message to the client identify by its id and listening to the websocket having the given url.
     *
     * @param uri     the web socket url
     * @param client  the client id, retrieved from the {@link org.wisdom.api.http.websockets.WebSocketListener#opened
     *                (String, String)} method.
     * @param message the message to send
     */
    @Override
    public void send(String uri, String client, byte[] message) {
        List<ChannelHandlerContext> channels;
        synchronized (this) {
            List<ChannelHandlerContext> ch = sockets.get(uri);
            if (ch != null) {
                channels = new ArrayList<>(ch);
            } else {
                channels = Collections.emptyList();
            }
        }
        for (ChannelHandlerContext channel : channels) {
            if (client.equals(id(channel))) {
                channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.copiedBuffer(message)));
            }
        }
    }

    /**
     * Method called when some data is received on a web socket. It delegates to the registered listeners.
     *
     * @param uri     the web socket url
     * @param content the data
     * @param ctx     the client channel
     */
    public void received(String uri, byte[] content, ChannelHandlerContext ctx) {
        List<WebSocketListener> localListeners;
        synchronized (this) {
            localListeners = new ArrayList<>(this.listeners);
        }

        for (WebSocketListener listener : localListeners) {
            listener.received(uri, id(ctx), content);
        }
    }

    /**
     * @return the hostname of the server.
     */
    @Override
    public String hostname() {
        return wisdomServer.hostname();
    }

    /**
     * @return the HTTP Port.
     */
    @Override
    public int httpPort() {
        return wisdomServer.httpPort();
    }

    /**
     * @return the HTTPS Port.
     */
    @Override
    public int httpsPort() {
        return wisdomServer.httpsPort();
    }
}
