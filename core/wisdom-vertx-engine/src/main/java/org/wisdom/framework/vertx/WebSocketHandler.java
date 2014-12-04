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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.ServerWebSocket;

/**
 * Handles web socket frames.
 */
public class WebSocketHandler implements Handler<ServerWebSocket> {

    /**
     * The logger.
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(WebSocketHandler.class);

    /**
     * The structure used to access services.
     */
    private final ServiceAccessor accessor;

    /**
     * Creates an instance of {@link org.wisdom.framework.vertx.WebSocketHandler}
     *
     * @param accessor the service accessor
     */
    public WebSocketHandler(ServiceAccessor accessor) {
        this.accessor = accessor;
    }

    /**
     * Handles a web socket connection.
     *
     * @param socket the opening socket.
     */
    @Override
    public void handle(final ServerWebSocket socket) {
        LOGGER.info("New web socket connection {}, {}", socket, socket.uri());
        accessor.getDispatcher().addWebSocket(socket.path(), socket);

        socket.closeHandler(new Handler<Void>() {
            /**
             * Handles the closing of an open socket.
             * @param event irrelevant
             */
            @Override
            public void handle(Void event) {
                LOGGER.info("Web Socket closed {}, {}", socket, socket.uri());
                accessor.getDispatcher().removeWebSocket(socket.path(), socket);
            }
        });

        socket.dataHandler(new Handler<Buffer>() {
            /**
             * Handles a web socket frames (message)
             * @param event the data
             */
            @Override
            public void handle(Buffer event) {
                accessor.getDispatcher().received(socket.path(), event.getBytes(), socket);
            }
        });

    }
}
