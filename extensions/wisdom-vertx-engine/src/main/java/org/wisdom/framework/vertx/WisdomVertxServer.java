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
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServer;
import org.wisdom.akka.AkkaSystemService;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.content.ContentEngine;
import org.wisdom.api.crypto.Crypto;
import org.wisdom.api.router.Router;
import org.wisdom.framework.vertx.ssl.SSLServerContext;


/**
 * Created by clement on 20/07/2014.
 */
@Component
@Instantiate
public class WisdomVertxServer {


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

    @Validate
    public void start() {
        LOGGER.info("Starting the vert.x server");
        //TODO The asynchronous start may interfere with the port selection.
        http = vertx.createHttpServer().requestHandler(new HttpHandler(vertx, accessor)).listen(8080);
        https = vertx.createHttpServer()
                .setSSL(true)
                .setSSLContext(SSLServerContext.getInstance(accessor).serverContext())
                .requestHandler(new HttpHandler(vertx, accessor)).listen(8082);
    }

    @Invalidate
    public void stop() {
        LOGGER.info("Stopping the vert.x server");
        http.close();
        https.close();
    }
}
