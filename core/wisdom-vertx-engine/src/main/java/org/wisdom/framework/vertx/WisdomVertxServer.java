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

import io.vertx.core.Vertx;
import io.vertx.core.spi.VerticleFactory;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.concurrent.ManagedExecutorService;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.configuration.Configuration;
import org.wisdom.api.content.ContentEngine;
import org.wisdom.api.crypto.Crypto;
import org.wisdom.api.engine.WisdomEngine;
import org.wisdom.api.exceptions.ExceptionMapper;
import org.wisdom.api.http.websockets.WebSocketDispatcher;
import org.wisdom.api.http.websockets.WebSocketListener;
import org.wisdom.api.router.Router;

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


    private static final Logger LOGGER = LoggerFactory.getLogger(WisdomVertxServer.class);

    /**
     * The set of Web Socket Listeners used to dispatch data received on web sockets.
     */
    private List<WebSocketListener> listeners = new ArrayList<>();

    /**
     * The map of uri / list of channel context keeping a reference on all opened web sockets.
     */
    private Map<String, List<Socket>> socketsByUri = new HashMap<>();

    /**
     * The vertx singleton.
     */
    @Requires
    Vertx vertx;

    /**
     * The router.
     */
    @Requires
    private Router router;

    /**
     * The configuration.
     */
    @Requires
    ApplicationConfiguration configuration;

    /**
     * The crypto service.
     */
    @Requires
    private Crypto crypto;

    /**
     * The content engine.
     */
    @Requires
    private ContentEngine engine;

    /**
     * The thread pool used by the server (system).
     */
    @Requires(filter = "(name=" + ManagedExecutorService.SYSTEM + ")")
    private ManagedExecutorService executor;

    /**
     * The exception mappers.
     */
    @Requires(specification = ExceptionMapper.class, optional = true)
    private Collection<ExceptionMapper> mappers;

    /**
     * The accessor to get all the services.
     */
    ServiceAccessor accessor = new ServiceAccessor(crypto, configuration, router,
            engine, executor, this, mappers); //NOSONAR

    private InetAddress address;

    protected List<Server> servers = new ArrayList<>(2);
    private String deploymentId;

    /**
     * Starts the servers (HTTP and HTTPS).
     * The actual start is asynchronous.
     */
    @Validate
    public synchronized void start() {

        LOGGER.info("Starting the vert.x server");

        // Check whether we have a specific vertx configuration, if not try the global one, and if not use default.
        int httpPort = accessor.getConfiguration().getIntegerWithDefault(
                "vertx.http.port",
                accessor.getConfiguration().getIntegerWithDefault(ApplicationConfiguration.HTTP_PORT, 9000));
        int httpsPort = accessor.getConfiguration().getIntegerWithDefault(
                "vertx.https.port",
                accessor.getConfiguration().getIntegerWithDefault(ApplicationConfiguration.HTTPS_PORT, -1));

        initializeInetAddress();


        // Parse server configuration if any
        Configuration servers = configuration.getConfiguration("vertx.servers");
        if (servers == null) {
            if (httpPort != -1) {
                LOGGER.info("Configuring default HTTP Server");
                this.servers.add(Server.defaultHttp(accessor, vertx));
            }
            if (httpsPort != -1) {
                LOGGER.info("Configuring default HTTPS Server");
                this.servers.add(Server.defaultHttps(accessor, vertx));
            }
        } else {
            // Custom configuration
            for (String name : servers.asMap().keySet()) {
                LOGGER.info("Configuring server {}", name);
                this.servers.add(Server.from(accessor, vertx, name,
                        servers.getConfiguration(name)));
            }
        }

        // Check whether or not the wisdom-internal verticle factory is already registered
        boolean found = false;
        for (VerticleFactory factory : vertx.verticleFactories()) {
            if (factory.prefix().equalsIgnoreCase("wisdom-internal")) {
                found = true;
            }
        }

        if (!found) {
            vertx.registerVerticleFactory(new WisdomInternalVerticleFactory(accessor, this.servers));
        }

        vertx.runOnContext(v -> vertx.deployVerticle("wisdom-internal:wisdom", ar -> {
            LOGGER.info("Wisdom verticle deployed : " + ar.result());
            deploymentId = ar.result();
        }));
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

        vertx.runOnContext(v -> {
            if (deploymentId != null) {
                vertx.undeploy(deploymentId, ar -> LOGGER.info("Wisdom verticle un-deployed"));
            }
        });

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
        for (Server server : servers) {
            if (!server.ssl()) {
                return server.port();
            }
        }
        return -1;
    }

    /**
     * @return the HTTP port on which the current HTTPS server is bound. {@literal -1} means that the HTTPS connection
     * is not enabled.
     */
    public synchronized int httpsPort() {
        for (Server server : servers) {
            if (server.ssl()) {
                return server.port();
            }
        }
        return -1;
    }

    /**
     * Publishes the given message to all clients subscribed to the socket (either a web socket of a SockJS socket)
     * specified using its url. For SockJS, it must match one of the configured prefix.
     *
     * @param url  the url of the web socket, must not be {@literal null}
     * @param data the data, must not be {@literal null}
     */
    @Override
    public void publish(String url, String data) {
        List<Socket> sockets;
        synchronized (this) {
            List<Socket> ch = this.socketsByUri.get(url);
            if (ch != null) {
                sockets = new ArrayList<>(ch);
            } else {
                sockets = Collections.emptyList();
            }
        }
        for (Socket socket : sockets) {
            socket.publish(data, vertx.eventBus());
        }
    }

    /**
     * Publishes the given message to all clients subscribed to the socket ((either a web socket of a SockJS socket))
     * specified using its url. For SockJS, it must match one of the configured prefix.
     *
     * @param url  the url of the socket, must not be {@literal null}
     * @param data the data, must not be {@literal null}
     */
    @Override
    public synchronized void publish(String url, byte[] data) {
        List<Socket> sockets;
        synchronized (this) {
            List<Socket> ch = this.socketsByUri.get(url);
            if (ch != null) {
                sockets = new ArrayList<>(ch);
            } else {
                sockets = Collections.emptyList();
            }
        }
        for (Socket socket : sockets) {
            socket.publish(data, vertx.eventBus());
        }
    }

    /**
     * A client subscribed to a socket (either a web socket of a SockJS socket).
     *
     * @param url    the url of the web sockets.
     * @param socket the client channel.
     */
    public void addSocket(String url, Socket socket) {
        LOGGER.info("Adding web socket on {} bound to {}", url, socket);
        List<WebSocketListener> webSocketListeners;
        synchronized (this) {
            List<Socket> channels = socketsByUri.get(url);
            if (channels == null) {
                channels = new ArrayList<>();
            }
            channels.add(socket);
            socketsByUri.put(url, channels);
            webSocketListeners = new ArrayList<>(this.listeners);
        }

        for (WebSocketListener listener : webSocketListeners) {
            listener.opened(url, id(socket));
        }
    }

    /**
     * A client disconnected from a socket (either a web socket of a SockJS socket).
     *
     * @param url    the url of the web sockets.
     * @param socket the client channel.
     */
    public void removeSocket(String url, Socket socket) {
        LOGGER.info("Removing web socket on {} bound to {}", url, socket.path());
        List<WebSocketListener> webSocketListeners;
        synchronized (this) {
            List<Socket> channels = socketsByUri.get(url);
            if (channels != null) {
                channels.remove(socket);
                if (channels.isEmpty()) {
                    socketsByUri.remove(url);
                }
            }
            webSocketListeners = new ArrayList<>(this.listeners);
        }

        for (WebSocketListener listener : webSocketListeners) {
            listener.closed(url, id(socket));
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
        Map<String, List<Socket>> copy;
        synchronized (this) {
            listeners.add(listener);
            copy = new HashMap<>(socketsByUri);
        }

        // Call open on each opened web socket
        for (Map.Entry<String, List<Socket>> entry : copy.entrySet()) {
            for (Socket client : entry.getValue()) {
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
        List<Socket> sockets;
        synchronized (this) {
            List<Socket> ch = this.socketsByUri.get(uri);
            if (ch != null) {
                sockets = new ArrayList<>(ch);
            } else {
                sockets = Collections.emptyList();
            }
        }
        for (Socket socket : sockets) {
            if (client.equals(id(socket))) {
                socket.publish(message, vertx.eventBus());
            }
        }
    }

    /**
     * Computes the client id for the given {@link org.wisdom.framework.vertx.Socket}.
     *
     * @param socket the socket.
     * @return the id
     */
    static String id(Socket socket) {
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
        List<Socket> sockets;
        synchronized (this) {
            List<Socket> ch = this.socketsByUri.get(uri);
            if (ch != null) {
                sockets = new ArrayList<>(ch);
            } else {
                sockets = Collections.emptyList();
            }
        }
        for (Socket socket : sockets) {
            if (client.equals(id(socket))) {
                socket.publish(message, vertx.eventBus());
            }
        }
    }

    /**
     * Method called when some data is received on a web socket. It delegates to the registered listeners.
     *
     * @param uri     the web socket url
     * @param content the data
     * @param socket  the client channel
     */
    public void received(String uri, byte[] content, Socket socket) {
        List<WebSocketListener> localListeners;
        synchronized (this) {
            localListeners = new ArrayList<>(this.listeners);
        }

        for (WebSocketListener listener : localListeners) {
            listener.received(uri, id(socket), content);
        }
    }
}
