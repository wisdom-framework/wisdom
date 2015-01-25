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

import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.ServerWebSocket;
import org.wisdom.api.concurrent.ManagedExecutorService;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.content.ContentEngine;
import org.wisdom.api.crypto.Crypto;
import org.wisdom.api.engine.WisdomEngine;
import org.wisdom.api.http.websockets.WebSocketDispatcher;
import org.wisdom.api.http.websockets.WebSocketListener;
import org.wisdom.api.router.Router;
import org.wisdom.framework.vertx.ssl.SSLServerContext;

import java.net.InetAddress;
import java.util.*;


/**
 * The main entry point of the Vert.x engine for Wisdom. This component is responsible for the creation of the
 * different server and their configuration. It also exposed the {@link org.wisdom.api.engine.WisdomEngine} and
 * {@link org.wisdom.api.http.websockets.WebSocketDispatcher} services.
 */
@Component
@Provides
@Instantiate
public class WisdomVertxServer implements WebSocketDispatcher, WisdomEngine {


    private final static Logger LOGGER = LoggerFactory.getLogger(WisdomVertxServer.class);


    /**
     * The set of Web Socket Listeners used to dispatch data received on web sockets.
     */
    private List<WebSocketListener> listeners = new ArrayList<>();

    /**
     * The map of uri / list of channel context keeping a reference on all opened web sockets.
     */
    private Map<String, List<ServerWebSocket>> sockets = new HashMap<>();

    @Requires
    Vertx vertx;

    @Requires
    private Router router;

    @Requires
    ApplicationConfiguration configuration;

    @Requires
    private Crypto crypto;

    @Requires
    private ContentEngine engine;

    @Requires(filter = "(name=" + ManagedExecutorService.SYSTEM + ")")
    private ManagedExecutorService executor;

    ServiceAccessor accessor = new ServiceAccessor(crypto, configuration, router, engine, executor, this);

    private HttpServer http;
    private HttpServer https;
    private Integer httpPort;
    private Integer httpsPort;
    private InetAddress address;

    /**
     * This random object is initialized on demand and is used to generate random port.
     */
    private Random random;

    /**
     * Starts the servers (HTTP and HTTPS).
     * The actual start is asynchronous.
     */
    @Validate
    public void start() {
        LOGGER.info("Starting the vert.x server");
        // Check whether we have a specific vertx configuration, if not try the global one, and if not use default.
        httpPort = accessor.getConfiguration().getIntegerWithDefault(
                "vertx.http.port",
                accessor.getConfiguration().getIntegerWithDefault(ApplicationConfiguration.HTTP_PORT, 9000));
        httpsPort = accessor.getConfiguration().getIntegerWithDefault(
                "vertx.https.port",
                accessor.getConfiguration().getIntegerWithDefault(ApplicationConfiguration.HTTPS_PORT, -1));

        initializeInetAddress();

        if (httpPort != -1) {
            bindHttp(httpPort);
        }
        if (httpsPort != -1) {
            bindHttps(httpsPort);
        }
    }


    private void bindHttp(int port) {
        // Get port number.
        final int thePort = pickAPort(port);
        http = vertx.createHttpServer()
                .requestHandler(new HttpHandler(vertx, accessor))
                .websocketHandler(new WebSocketHandler(accessor));

        if (configuration.getIntegerWithDefault("vertx.acceptBacklog", -1) != -1) {
            http.setAcceptBacklog(configuration.getInteger("vertx.acceptBacklog"));
        }
        if (configuration.getIntegerWithDefault("vertx.maxWebSocketFrameSize", -1) != -1) {
            http.setMaxWebSocketFrameSize(configuration.getInteger("vertx.maxWebSocketFrameSize"));
        }
        if (configuration.getStringArray("wisdom.websocket.subprotocols").length > 0) {
            http.setWebSocketSubProtocols(configuration.getStringArray("wisdom.websocket.subprotocols"));
        }
        if (configuration.getStringArray("vertx.websocket-subprotocols").length > 0) {
            http.setWebSocketSubProtocols(configuration.getStringArray("vertx.websocket-subprotocols"));
        }
        if (configuration.getIntegerWithDefault("vertx.receiveBufferSize", -1) != -1) {
            http.setReceiveBufferSize(configuration.getInteger("vertx.receiveBufferSize"));
        }
        if (configuration.getIntegerWithDefault("vertx.sendBufferSize", -1) != -1) {
            http.setSendBufferSize(configuration.getInteger("vertx.sendBufferSize"));
        }

        http.listen(thePort, new Handler<AsyncResult<HttpServer>>() {
            @Override
            public void handle(AsyncResult<HttpServer> event) {
                if (event.succeeded()) {
                    LOGGER.info("Wisdom is going to serve HTTP requests on port {}.", thePort);
                    httpPort = thePort;
                } else if (httpPort == 0) {
                    LOGGER.debug("Cannot bind on port {} (port already used probably)", thePort, event.cause());
                    bindHttp(0);
                } else {
                    LOGGER.error("Cannot bind on port {} (port already used probably)", thePort, event.cause());
                }
            }
        });
    }

    private void bindHttps(int port) {
        // Get port number.
        final int thePort = pickAPort(port);
        https = vertx.createHttpServer()
                .setSSL(true)
                .setSSLContext(SSLServerContext.getInstance(accessor).serverContext())
                .requestHandler(new HttpHandler(vertx, accessor))
                .websocketHandler(new WebSocketHandler(accessor));

        if (configuration.getIntegerWithDefault("vertx.acceptBacklog", -1) != -1) {
            https.setAcceptBacklog(configuration.getInteger("vertx.acceptBacklog"));
        }
        if (configuration.getIntegerWithDefault("vertx.maxWebSocketFrameSize", -1) != -1) {
            https.setMaxWebSocketFrameSize(configuration.getInteger("vertx.maxWebSocketFrameSize"));
        }
        if (configuration.get("wisdom.websocket.subprotocols") != null) {
            https.setWebSocketSubProtocols(configuration.getStringArray("wisdom.websocket.subprotocols"));
        }
        if (configuration.getIntegerWithDefault("vertx.receiveBufferSize", -1) != -1) {
            https.setReceiveBufferSize(configuration.getInteger("vertx.receiveBufferSize"));
        }
        if (configuration.getIntegerWithDefault("vertx.sendBufferSize", -1) != -1) {
            https.setSendBufferSize(configuration.getInteger("vertx.sendBufferSize"));
        }


        https.listen(thePort, new Handler<AsyncResult<HttpServer>>() {
            @Override
            public void handle(AsyncResult<HttpServer> event) {
                if (event.succeeded()) {
                    httpsPort = thePort;
                    LOGGER.info("Wisdom is going to serve HTTPS requests on port {}.", httpsPort);
                } else if (httpsPort == 0) {
                    LOGGER.debug("Cannot bind on port {} (port already used probably)", thePort, event.cause());
                    bindHttps(0);
                } else {
                    LOGGER.error("Cannot bind on port {} (port already used probably)", thePort, event.cause());
                }
            }
        });
    }

    private int pickAPort(int port) {
        if (port == 0) {
            if (random == null) {
                random = new Random();
            }
            port = 9000 + random.nextInt(10000);
            LOGGER.debug("Random port lookup - Trying with {}", port);
        }
        return port;
    }

    private void initializeInetAddress() {
        address = null;
        try {
            if (accessor.getConfiguration().get("http.address") != null) {
                address = InetAddress.getByName(accessor.getConfiguration().get("http.address"));
            }
        } catch (Exception e) {
            LOGGER.error("Could not understand http.address", e);
            onError();
        }
    }

    private void onError() {
        System.exit(-1); //NOSONAR
    }


    /**
     * Stops the different servers.
     */
    @Invalidate
    public void stop() {
        listeners.clear();
        LOGGER.info("Stopping the vert.x server");
        if (http != null) {
            http.close(new AsyncResultHandler<Void>() {
                @Override
                public void handle(AsyncResult<Void> event) {
                    LOGGER.info("The HTTP server has been stopped");
                }
            });
        }
        if (https != null) {
            https.close(new AsyncResultHandler<Void>() {
                @Override
                public void handle(AsyncResult<Void> event) {
                    LOGGER.info("The HTTPS server has been stopped");
                }
            });
        }
    }

    /**
     * @return the hostname.
     */
    public String hostname() {
        if (address == null) {
            return "localhost";
        } else {
            return address.getHostName();
        }
    }

    /**
     * @return the HTTP port on which the current HTTP server is bound. {@literal -1} means that the HTTP connection
     * is not enabled.
     */
    public synchronized int httpPort() {
        return httpPort;
    }

    /**
     * @return the HTTP port on which the current HTTPS server is bound. {@literal -1} means that the HTTPS connection
     * is not enabled.
     */
    public synchronized int httpsPort() {
        return httpsPort;
    }

    /**
     * Publishes the given message to all clients subscribed to the web socket specified using its url.
     *
     * @param url  the url of the web socket, must not be {@literal null}
     * @param data the data, must not be {@literal null}
     */
    @Override
    public void publish(String url, String data) {
        List<ServerWebSocket> sockets;
        synchronized (this) {
            List<ServerWebSocket> ch = this.sockets.get(url);
            if (ch != null) {
                sockets = new ArrayList<>(ch);
            } else {
                sockets = Collections.emptyList();
            }
        }
        for (ServerWebSocket socket : sockets) {
            vertx.eventBus().publish(socket.textHandlerID(), data);
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
        List<ServerWebSocket> sockets;
        synchronized (this) {
            List<ServerWebSocket> ch = this.sockets.get(url);
            if (ch != null) {
                sockets = new ArrayList<>(ch);
            } else {
                sockets = Collections.emptyList();
            }
        }
        for (ServerWebSocket socket : sockets) {
            vertx.eventBus().publish(socket.binaryHandlerID(), data);
        }
    }

    /**
     * A client subscribed to a web socket.
     *
     * @param url    the url of the web sockets.
     * @param socket the client channel.
     */
    public void addWebSocket(String url, ServerWebSocket socket) {
        LOGGER.info("Adding web socket on {} bound to {}", url, socket);
        List<WebSocketListener> webSocketListeners;
        synchronized (this) {
            List<ServerWebSocket> channels = sockets.get(url);
            if (channels == null) {
                channels = new ArrayList<>();
            }
            channels.add(socket);
            sockets.put(url, channels);
            webSocketListeners = new ArrayList<>(this.listeners);
        }

        for (WebSocketListener listener : webSocketListeners) {
            listener.opened(url, id(socket));
        }
    }

    /**
     * A client disconnected from a web socket.
     *
     * @param url the url of the web sockets.
     * @param ctx the client channel.
     */
    public void removeWebSocket(String url, ServerWebSocket ctx) {
        LOGGER.info("Removing web socket on {} bound to {}", url, ctx.path());
        List<WebSocketListener> webSocketListeners;
        synchronized (this) {
            List<ServerWebSocket> channels = sockets.get(url);
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
        Map<String, List<ServerWebSocket>> copy;
        synchronized (this) {
            listeners.add(listener);
            copy = new HashMap<>(sockets);
        }

        // Call open on each opened web socket
        for (Map.Entry<String, List<ServerWebSocket>> entry : copy.entrySet()) {
            for (ServerWebSocket client : entry.getValue()) {
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
        List<ServerWebSocket> sockets;
        synchronized (this) {
            List<ServerWebSocket> ch = this.sockets.get(uri);
            if (ch != null) {
                sockets = new ArrayList<>(ch);
            } else {
                sockets = Collections.emptyList();
            }
        }
        for (ServerWebSocket socket : sockets) {
            if (client.equals(id(socket))) {
                vertx.eventBus().publish(socket.textHandlerID(), message);
            }
        }
    }

    /**
     * Computes the client id for the given {@link org.vertx.java.core.http.ServerWebSocket}.
     *
     * @param socket the socket.
     * @return the id
     */
    static String id(ServerWebSocket socket) {
        return Integer.toOctalString(socket.hashCode());
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
        List<ServerWebSocket> sockets;
        synchronized (this) {
            List<ServerWebSocket> ch = this.sockets.get(uri);
            if (ch != null) {
                sockets = new ArrayList<>(ch);
            } else {
                sockets = Collections.emptyList();
            }
        }
        for (ServerWebSocket socket : sockets) {
            if (client.equals(id(socket))) {
                vertx.eventBus().publish(socket.binaryHandlerID(), new Buffer(message));
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
    public void received(String uri, byte[] content, ServerWebSocket ctx) {
        List<WebSocketListener> localListeners;
        synchronized (this) {
            localListeners = new ArrayList<>(this.listeners);
        }

        for (WebSocketListener listener : localListeners) {
            listener.received(uri, id(ctx), content);
        }
    }
}
