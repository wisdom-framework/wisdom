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

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.http.Context;

/**
 * Handles web socket frames.
 */
public class WebSocketHandler implements Handler<ServerWebSocket> {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketHandler.class);

    /**
     * The structure used to access services.
     */
    private final ServiceAccessor accessor;
    private final Vertx vertx;

    /**
     * The server configuration.
     */
    private final Server configuration;

    /**
     * Creates an instance of {@link org.wisdom.framework.vertx.WebSocketHandler}
     *
     * @param accessor the service accessor
     * @param server the server configuration - used to check whether or not the message should be
     *                            allowed or denied
     */
    public WebSocketHandler(Vertx vertx, ServiceAccessor accessor, Server server) {
        this.accessor = accessor;
        this.configuration = server;
        this.vertx = vertx;
    }

    /**
     * Handles a web socket connection.
     *
     * @param socket the opening socket.
     */
    @Override
    public void handle(final ServerWebSocket socket) {
        LOGGER.info("New web socket connection {}, {}", socket, socket.uri());

        final ContextFromVertx context = new ContextFromVertx(vertx, vertx.getOrCreateContext(), accessor, socket.headers());

        if (! configuration.accept(socket.uri())) {
            LOGGER.warn("Web Socket connection denied on {} by {}", socket.uri(), configuration.name());
            return;
        }

        final Socket sock = new Socket(socket);
        Context.CONTEXT.set(context);
        //TODO Propagate context in case listeners use executor on socket opening ?
        accessor.getDispatcher().addSocket(socket.path(), sock);
        Context.CONTEXT.remove();

        socket.closeHandler(event -> {
            LOGGER.info("Web Socket closed {}, {}", socket, socket.uri());
            Context.CONTEXT.set(context);
            //TODO Propagate context in case listeners use executor on socket closing ?
            accessor.getDispatcher().removeSocket(socket.path(), sock);
            Context.CONTEXT.remove();
        });

        socket.handler(event -> {
            //Context has to be propagated as WebSocketRouter (default WebSocketListener) execute registered routes in an executor
            accessor.getDispatcher().received(socket.path(), event.getBytes(), sock, context);
        });

    }
}
