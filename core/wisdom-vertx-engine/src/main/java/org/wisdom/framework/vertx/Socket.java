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


import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.ServerWebSocket;

/**
 * A class abstracting the differences between the WebSocket API and the SockJs API.
 * TODO: Add sockjs support back.
 */
public class Socket {

    /**
     * The underlying socket, a {@link ServerWebSocket} instance.
     */
    private final ServerWebSocket delegate;

    /**
     * Creates an instance of {@link org.wisdom.framework.vertx.Socket} delegating to
     * a {@link ServerWebSocket} instance.
     *
     * @param delegate the delegate
     */
    public Socket(ServerWebSocket delegate) {
        this.delegate = delegate;
    }


    private String getWriteHandlerId() {
        return delegate.textHandlerID();
    }

    private String getBinaryWriteHandlerId() {
        return delegate.binaryHandlerID();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Socket socket = (Socket) o;
        return delegate.equals(socket.delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    /**
     * Gets the socket path. For SockJS, it returns the full URI.
     *
     * @return the socket path
     */
    public String path() {
        return delegate.path();
    }

    /**
     * Sends a text frame on the socket.
     *
     * @param message the message
     * @param bus     the Vert.x event bus.
     */
    public void publish(String message, EventBus bus) {
        bus.publish(getWriteHandlerId(), message);
    }

    /**
     * Sends a binary frame on the socket.
     *
     * @param message the message
     * @param bus     the Vert.x event bus.
     */
    public void publish(byte[] message, EventBus bus) {
        bus.publish(getBinaryWriteHandlerId(), Buffer.buffer(message));
    }
}
