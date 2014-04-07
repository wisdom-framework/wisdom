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
 * Classes implementing this interface should register themselves on {@link WebSocketDispatcher} to receive
 * notification when client are opening, closing web sockets or sending data.
 */
public interface WebSocketListener {

    /**
     * Callback invoked when data is received on the web socket identified by its url.
     *
     * @param uri     the url of the web socket
     * @param client  the client id
     * @param content the received content
     */
    public void received(String uri, String client, byte[] content);

    /**
     * Callback invoked when a new client connects on a web socket identified by its url.
     *
     * @param uri    the url of the web socket
     * @param client the client id
     */
    public void opened(String uri, String client);

    /**
     * Callback invoked when a new client closes the connection to a web socket identified by its url.
     *
     * @param uri    the url of the web socket
     * @param client the client id
     */
    public void closed(String uri, String client);

}
