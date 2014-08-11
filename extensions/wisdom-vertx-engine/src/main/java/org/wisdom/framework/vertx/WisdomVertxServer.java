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
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServer;
import org.wisdom.akka.AkkaSystemService;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.content.ContentEngine;
import org.wisdom.api.crypto.Crypto;
import org.wisdom.api.engine.WisdomEngine;
import org.wisdom.api.router.Router;
import org.wisdom.framework.vertx.ssl.SSLServerContext;

import java.net.InetAddress;
import java.util.Random;


/**
 * Created by clement on 20/07/2014.
 */
@Component
@Instantiate
public class WisdomVertxServer implements WisdomEngine {


    private final static Logger LOGGER = LoggerFactory.getLogger(WisdomVertxServer.class);

    private HttpServer http;

    @Requires
    private Vertx vertx;

    @Requires
    private Router router;

    @Requires
    private ApplicationConfiguration configuration;

    @Requires
    private Crypto crypto;

    @Requires
    private ContentEngine engine;

    @Requires
    private AkkaSystemService system;

    final ServiceAccessor accessor = new ServiceAccessor(crypto, configuration, router, engine, system);
    private HttpServer https;
    private Integer httpPort;
    private Integer httpsPort;
    private InetAddress address;
    private Random random;

    @Validate
    public void start() {
        LOGGER.info("Starting the vert.x server");
        httpPort = accessor.getConfiguration().getIntegerWithDefault("vertx.http.port", 8080);
        httpsPort = accessor.getConfiguration().getIntegerWithDefault("vertx.https.port", -1);

        initializeInetAddress();

        bindHttp(httpPort);
        bindHttps(httpsPort);
    }


    private void bindHttp(int port) {
        // Get port number.
        final int thePort = pickAPort(port);
        http = vertx.createHttpServer()
                .requestHandler(new HttpHandler(vertx, accessor))
                .listen(port, new Handler<AsyncResult<HttpServer>>() {
                    @Override
                    public void handle(AsyncResult<HttpServer> event) {
                        if (event.succeeded()) {
                            httpPort = thePort;
                            LOGGER.info("Wisdom is going to serve HTTP requests on port " + httpPort);
                        } else if (httpPort == 0) {
                            LOGGER.debug("Cannot bind on port {} (port already used probably)", thePort, event.cause());
                            bindHttp(0);
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
                .listen(port, new Handler<AsyncResult<HttpServer>>() {
                    @Override
                    public void handle(AsyncResult<HttpServer> event) {
                        if (event.succeeded()) {
                            httpPort = thePort;
                            LOGGER.info("Wisdom is going to serve HTTP requests on port " + httpPort);
                        } else if (httpPort == 0) {
                            LOGGER.debug("Cannot bind on port {} (port already used probably)", thePort, event.cause());
                            bindHttp(0);
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


    @Invalidate
    public void stop() {
        LOGGER.info("Stopping the vert.x server");
        http.close();
        https.close();
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
    public int httpPort() {
        return httpPort;
    }

    /**
     * @return the HTTP port on which the current HTTPS server is bound. {@literal -1} means that the HTTPS connection
     * is not enabled.
     */
    public int httpsPort() {
        return httpsPort;
    }
}
