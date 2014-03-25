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

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Service to send messages on web sockets.
 */
public interface Publisher {

    /**
     * Publishes a message to all clients connected to a websocket.
     *
     * @param uri     the websocket's url
     * @param message the message
     */
    public void publish(String uri, String message);

    /**
     * Publishes data to all clients connected to a websocket.
     *
     * @param uri     the websocket's url
     * @param message the data
     */
    public void publish(String uri, byte[] message);

    /**
     * Publishes a message to all clients connected to a websocket.
     *
     * @param uri     the websocket's url
     * @param message the message
     */
    public void publish(String uri, JsonNode message);

    /**
     * Sends a message to a specific client connected to a websocket.
     * The method does nothing if there is no client matching the id connected on the websocket.
     *
     * @param uri     the websocket's url
     * @param client  the client id, received in the 'opened' callback
     * @param message the message
     */
    public void send(String uri, String client, String message);

    /**
     * Sends a message to a specific client connected to a websocket.
     * The method does nothing if there is no client matching the id connected on the websocket.
     *
     * @param uri     the websocket's url
     * @param client  the client id, received in the 'opened' callback
     * @param message the message
     */
    public void send(String uri, String client, JsonNode message);

    /**
     * Sends data to a specific client connected to a websocket.
     * The method does nothing if there is no client matching the id connected on the websocket.
     *
     * @param uri     the websocket's url
     * @param client  the client id, received in the 'opened' callback
     * @param message the data
     */
    public void send(String uri, String client, byte[] message);
}
