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

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.sockjs.SockJSSocket;

/**
 * A class abstracting the differences between the WebSocket API and the SockJs API.
 */
public class Socket {

    /**
     * The underlying socket, either a {@link org.vertx.java.core.http.ServerWebSocket} or
     * a {@link org.wisdom.framework.vertx.SockJsHandler}.
     */
    private final Object delegate;

    /**
     * Creates an instance of {@link org.wisdom.framework.vertx.Socket} delegating to
     * a {@link org.vertx.java.core.http.ServerWebSocket} instance.
     *
     * @param delegate the delegate
     */
    public Socket(ServerWebSocket delegate) {
        this.delegate = delegate;
    }

    /**
     * Creates an instance of {@link org.wisdom.framework.vertx.Socket} delegating to
     * a {@link org.vertx.java.core.sockjs.SockJSSocket} instance.
     *
     * @param delegate the delegate
     */
    public Socket(SockJSSocket delegate) {
        this.delegate = delegate;
    }

    private String getWriteHandlerId() {
        if (delegate instanceof ServerWebSocket) {
            return ((ServerWebSocket) delegate).textHandlerID();
        } else if (delegate instanceof SockJSSocket) {
            return ((SockJSSocket) delegate).writeHandlerID();
        }
        throw new IllegalArgumentException("Unsupported socket type " + delegate);
    }

    private String getBinaryWriteHandlerId() {
        if (delegate instanceof ServerWebSocket) {
            return ((ServerWebSocket) delegate).binaryHandlerID();
        } else if (delegate instanceof SockJSSocket) {
            return ((SockJSSocket) delegate).writeHandlerID();
        }
        throw new IllegalArgumentException("Unsupported socket type " + delegate);
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
        if (delegate instanceof ServerWebSocket) {
            return ((ServerWebSocket) delegate).path();
        } else if (delegate instanceof SockJSSocket) {
            return ((SockJSSocket) delegate).uri();
        }
        throw new IllegalArgumentException("Unsupported socket type " + delegate);
    }

    /**
     * Sends a text frame on the socket.
     *
     * @param message the message
     * @param bus     the Vert.x event bus.
     */
    public void publish(String message, EventBus bus) {
        if (delegate instanceof ServerWebSocket) {
            bus.publish(getWriteHandlerId(), message);
        } else if (delegate instanceof SockJSSocket) {
            bus.publish(getWriteHandlerId(), new Buffer().appendString(message));
        }
    }

    /**
     * Sends a binary frame on the socket.
     *
     * @param message the message
     * @param bus     the Vert.x event bus.
     */
    public void publish(byte[] message, EventBus bus) {
        if (delegate instanceof ServerWebSocket) {
            bus.publish(getBinaryWriteHandlerId(), new Buffer().appendBytes(message));
        } else if (delegate instanceof SockJSSocket) {
            bus.publish(getBinaryWriteHandlerId(), new Buffer().appendBytes(message));
        }
    }
}
