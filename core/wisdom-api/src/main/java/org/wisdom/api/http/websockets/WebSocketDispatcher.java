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
package org.wisdom.api.http.websockets;

/**
 * Service exposed by the engine to receive and publish data on web sockets.
 */
public interface WebSocketDispatcher {

    /**
     * Asks the engine to dispatch the given data on the web socket having the given url.
     * The message is dispatched to all clients listening the web socket. The message is sent as text.
     *
     * @param url  the url of the web socket, must not be {@literal null}
     * @param data the data, must not be {@literal null}
     */
    public void publish(String url, String data);

    /**
     * Asks the engine to dispatch the given data on the web socket having the given url.
     * The message is dispatched to all clients listening the web socket. The message is sent as binary.
     *
     * @param url  the url of the web socket, must not be {@literal null}
     * @param data the data, must not be {@literal null}
     */
    public void publish(String url, byte[] data);

    /**
     * Registers a listener to be notified when a client connects to a web socket, disconnects and send a message.
     *
     * @param listener the listener, must not be {@literal null}
     */
    public void register(WebSocketListener listener);

    /**
     * Un-registers a listener. Once called, the listener will no more receive any notifications.
     *
     * @param listener the listener, must not be {@literal null}
     */
    public void unregister(WebSocketListener listener);

    /**
     * Asks the engine to send a text message to a specific client listening to a web socket.
     *
     * @param uri     the web socket url
     * @param client  the client id, retrieved from the {@link org.wisdom.api.http.websockets.WebSocketListener#opened
     *                (String, String)} method.
     * @param message the message to send
     */
    public void send(String uri, String client, String message);

    /**
     * Asks the engine to send a binary message to a specific client listening to a web socket.
     *
     * @param uri     the web socket url
     * @param client  the client id, retrieved from the {@link org.wisdom.api.http.websockets.WebSocketListener#opened
     *                (String, String)} method.
     * @param message the message to send
     */
    public void send(String uri, String client, byte[] message);
}
