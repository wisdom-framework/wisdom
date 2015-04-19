/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSServer;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.configuration.Configuration;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.framework.vertx.ssl.SSLServerContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Class representing the server configuration and configuring the server.
 */
public class Server {

    /**
     * Random used to generate random port.
     */
    private static Random random = new Random();

    /**
     * The name of the server.
     */
    private final String name;

    /**
     * The interface / host on which the server is bound. By default all interfaces are bound.
     */
    private final String host;

    /**
     * The logger.
     */
    private final Logger logger; // NOSONAR the name is configured per instance.

    /**
     * The vert.x instance.
     */
    private final Vertx vertx;

    /**
     * The service accessor.
     */
    private final ServiceAccessor accessor;

    /**
     * The application configuration.
     */
    private final ApplicationConfiguration configuration;

    /**
     * The listened port, updated once the server is bound (that's why the field is not final).
     */
    private int port;

    /**
     * whether or not SSL is enabled.
     */
    private final boolean ssl;

    /**
     * whether or not the mutual authentication is enabled.
     */
    private final boolean authentication;

    /**
     * The list of accepted patterns.
     */
    private List<Pattern> allow;

    /**
     * The list of denied patterns
     */
    private List<Pattern> deny;

    /**
     * The url on which the request is redirected when the request is denied. By default, if not set a `FORBIDDEN`
     * result is returned.
     */
    private String onDenied;

    /**
     * The HTTP server.
     */
    private HttpServer http;

    /**
     * The list of SockJS servers.
     */
    private List<SockJSServer> sockjs = new ArrayList<>();
    private long encodingMinBound;
    private long encodingMaxBound;

    /**
     * Creates the default HTTP server (listening on port 9000 / `http.port`), no SSL, no mutual authentication,
     * accept all requests.
     *
     * @param accessor the service accessor
     * @param vertx    the vertx singleton
     * @return the configured server (not bound, not started)
     */
    public static Server defaultHttp(ServiceAccessor accessor, Vertx vertx) {
        return new Server(
                accessor,
                vertx,
                "default-http",
                accessor.getConfiguration().getIntegerWithDefault("http.port", 9000),
                false, false,
                null,
                Collections.<String>emptyList(), Collections.<String>emptyList(), null);
    }

    /**
     * Creates the default HTTPS server (listening on port 9001 / `https.port`), SSL enabled, no mutual authentication,
     * accept all requests.
     *
     * @param accessor the service accessor
     * @param vertx    the vertx singleton
     * @return the configured server (not bound, not started)
     */
    public static Server defaultHttps(ServiceAccessor accessor, Vertx vertx) {
        return new Server(
                accessor,
                vertx,
                "default-https",
                accessor.getConfiguration().getIntegerWithDefault("https.port", 9001),
                true, false,
                null,
                Collections.<String>emptyList(), Collections.<String>emptyList(), null);
    }

    /**
     * Creates a new server from the given configuration object.
     *
     * @param accessor      the service accessor
     * @param vertx         the vertx singleton
     * @param name          the server name
     * @param configuration the configuration
     * @return the configured server (not bound, not started)
     */
    public static Server from(ServiceAccessor accessor,
                              Vertx vertx,
                              String name,
                              Configuration configuration) {
        return new Server(
                accessor,
                vertx,
                name,
                configuration.getIntegerOrDie("port"),
                configuration.getBooleanWithDefault("ssl", false),
                configuration.getBooleanWithDefault("authentication", false),
                configuration.get("host"),
                configuration.getList("allow"),
                configuration.getList("deny"),
                configuration.get("onDenied")
        );
    }

    /**
     * Creates a new server.
     *
     * @param accessor       the service accessor
     * @param vertx          the vertx singleton
     * @param name           the server name
     * @param port           the port
     * @param ssl            whether or not SSL is enabled
     * @param host           the listened interface
     * @param allow          the set of path with wildcards accepted by the server
     * @param deny           the set of path with wildcards rejected by the server
     * @param authentication whether or not mutual authentication is enabled
     * @param onDenied       the redirection URL if a request is denied by the server
     */
    public Server(ServiceAccessor accessor,
                  Vertx vertx,
                  String name, int port,
                  boolean ssl, boolean authentication,
                  String host,
                  List<String> allow, List<String> deny, String onDenied) {
        Preconditions.checkNotNull(accessor);
        Preconditions.checkNotNull(vertx);
        Preconditions.checkNotNull(name);
        this.accessor = accessor;
        this.configuration = accessor.getConfiguration();
        this.vertx = vertx;
        this.name = name;

        if (host == null) {
            this.host = "0.0.0.0";
        } else {
            this.host = host;
        }

        this.port = port;
        this.ssl = ssl;
        this.authentication = authentication;

        List<Pattern> allowedPatterns = new ArrayList<>();
        List<Pattern> deniedPatterns = new ArrayList<>();

        for (String a : allow) {
            allowedPatterns.add(Pattern.compile(a.trim().replace(".", "\\.").replace("*", ".*")));
        }

        for (String a : deny) {
            deniedPatterns.add(Pattern.compile(a.trim().replace(".", "\\.").replace("*", ".*")));
        }

        this.allow = allowedPatterns;
        this.deny = deniedPatterns;
        this.onDenied = onDenied;

        this.logger = LoggerFactory.getLogger("server-" + name);
    }

    /**
     * Starts the server. The server is going to try to listen on the given host / port. Startup is asynchronous. You
     * can pull {@link #port()} to know when the server has successfully be bound (in case of a random port).
     */
    public void bind() {
        logger.info("Starting server {}", name);
        bind(port);
    }

    private void bind(int p) {
        // Get port number.
        final int thePort = pickAPort(port);
        http = vertx.createHttpServer()
                .requestHandler(new HttpHandler(vertx, accessor, this))
                .websocketHandler(new WebSocketHandler(accessor, this));

        if (ssl) {
            http.setSSL(true)
                    .setSSLContext(SSLServerContext.getInstance(accessor).serverContext());
            if (authentication) {
                http.setClientAuthRequired(true);
            }
        }

        for (String prefix : configuration.getList("vertx.sockjs.prefixes")) {
            configureSockJsServer(prefix, vertx.createSockJSServer(http));
        }

        if (hasCompressionEnabled()) {
            http.setCompressionSupported(true);
        }

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

        http.listen(thePort, host, new Handler<AsyncResult<HttpServer>>() {
            @Override
            public void handle(AsyncResult<HttpServer> event) {
                if (event.succeeded()) {
                    logger.info("Wisdom is going to serve HTTP requests on port {}.", thePort);
                    port = thePort;
                } else if (port == 0) {
                    logger.debug("Cannot bind on port {} (port already used probably)", thePort, event.cause());
                    bind(0);
                } else {
                    logger.error("Cannot bind on port {} (port already used probably)", thePort, event.cause());
                }
            }
        });
    }

    private void configureSockJsServer(String prefix, SockJSServer sockJSServer) {
        JsonObject config = new JsonObject()
                .putString("prefix", prefix)
                .putNumber("session_timeout",
                        configuration.getDuration("vertx.sockjs.timeout", TimeUnit.MILLISECONDS, 5000))
                .putNumber("heartbeat_period",
                        configuration.getDuration("vertx.sockjs.heartbeat", TimeUnit.MILLISECONDS, 5000))
                .putNumber("max_bytes_streaming",
                        configuration.getBytes("vertx.sockjs.max", 128 * 1024))
                .putString("library_url",
                        configuration.getWithDefault("vertx.sockjs.library",
                                "http://cdn.jsdelivr.net/sockjs/0.3.4/sockjs.min.js"));
        sockJSServer.installApp(config, new SockJsHandler(accessor, prefix));
        sockjs.add(sockJSServer);
    }

    /**
     * Checks whether the given path is accepted or rejected by the current server.
     *
     * @param path the path
     * @return {@code true} if the path is accepted, {@code false} otherwise.
     */
    public boolean accept(String path) {
        if (allow.isEmpty() && deny.isEmpty()) {
            return true;
        }
        // Check if the path is denied
        for (Pattern p : deny) {
            if (p.matcher(path).matches()) {
                return false;
            }
        }

        // Check if the path is accepted
        for (Pattern p : allow) {
            if (p.matcher(path).matches()) {
                return true;
            }
        }

        // Denied by default.
        return !deny.isEmpty();
    }

    public Result getOnDeniedResult() {
        if (onDenied == null) {
            return Results.forbidden();
        } else {
            return Results.redirect(onDenied);
        }
    }

    private int pickAPort(int port) {
        if (port == 0) {
            if (random == null) {
                random = new Random();
            }
            port = 9000 + random.nextInt(10000);
            logger.debug("Random port lookup - Trying with {}", port);
        }
        return port;
    }


    /**
     * Gets the server's name.
     *
     * @return the name
     */
    public String name() {
        return name;
    }

    /**
     * Stops / Closes the server.
     */
    public void close() {
        for (SockJSServer server : sockjs) {
            server.close();
        }

        http.close(new AsyncResultHandler<Void>() {
            @Override
            public void handle(AsyncResult<Void> event) {
                logger.info("The server '{}' has been stopped (bound port: {})", name, port);
            }
        });
    }

    /**
     * Gets whether or not SSL is enabled on the current server.
     *
     * @return {@code true} if SSL is enabled, {@code false} otherwise
     */
    public boolean ssl() {
        return ssl;
    }

    /**
     * Gets the port listen by the server.
     *
     * @return the listened port, 0 is not bound yet.
     */
    public int port() {
        return port;
    }

    /**
     * Gets the host on which the server is bound.
     *
     * @return the host, {@code 0.0.0.0} means all network interfaces
     */
    public String host() {
        return host;
    }

    /**
     * @return whether or not the compression is enabled.
     */
    public boolean hasCompressionEnabled() {
        return configuration.getBooleanWithDefault("vertx.compression", true);
    }

    /**
     * @return the threshold below which the content should not be encoded. By default
     * it's {@link ApplicationConfiguration#DEFAULT_ENCODING_MIN_SIZE} bytes.
     */
    public long getEncodingMinBound() {
        return configuration.getBytes(ApplicationConfiguration.ENCODING_MIN_SIZE,
                ApplicationConfiguration.DEFAULT_ENCODING_MIN_SIZE);
    }

    /**
     * @return the threshold above which the content should not be encoded. By default
     * it's {@link ApplicationConfiguration#DEFAULT_ENCODING_MAX_SIZE} bytes.
     */
    public long getEncodingMaxBound() {
        return configuration.getBytes(ApplicationConfiguration.ENCODING_MAX_SIZE,
                ApplicationConfiguration.DEFAULT_ENCODING_MAX_SIZE);
    }
}
