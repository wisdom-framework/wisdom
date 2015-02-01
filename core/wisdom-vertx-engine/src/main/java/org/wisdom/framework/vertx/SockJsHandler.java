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
import org.vertx.java.core.sockjs.SockJSSocket;

/**
 * Handles SockJS frames.
 */
public class SockJsHandler implements Handler<SockJSSocket> {

    /**
     * The logger.
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(SockJsHandler.class);

    /**
     * The structure used to access services.
     */
    private final ServiceAccessor accessor;

    /**
     * The prefix handled by this SockJs server.
     */
    private final String prefix;

    /**
     * Creates an instance of {@link org.wisdom.framework.vertx.SockJsHandler}
     *
     * @param accessor the service accessor
     * @param prefix the prefix
     */
    public SockJsHandler(ServiceAccessor accessor, String prefix) {
        this.accessor = accessor;
        this.prefix = prefix;
    }

    /**
     * Handles a SockJS connection.
     *
     * @param socket the opening socket.
     */
    @Override
    public void handle(final SockJSSocket socket) {
        LOGGER.info("New sockJS connection {}, {}", socket, socket.uri());
        final Socket sock = new Socket(socket);
        accessor.getDispatcher().addSocket(prefix, sock);
        socket.endHandler(new Handler<Void>() {
            /**
             * Handles the closing of an open socket.
             * @param event irrelevant
             */
            @Override
            public void handle(Void event) {
                LOGGER.info("Socket JS closed {}, {}", socket, socket.uri());
                accessor.getDispatcher().removeSocket(prefix, sock);
            }
        });

        socket.dataHandler(new Handler<Buffer>() {
            /**
             * Handles a web socket frames (message)
             * @param event the data
             */
            @Override
            public void handle(Buffer event) {
                accessor.getDispatcher().received(prefix, event.getBytes(), sock);
            }
        });

    }
}
