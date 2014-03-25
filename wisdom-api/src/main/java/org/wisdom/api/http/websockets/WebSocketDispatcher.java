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

    public void publish(String url, String data);
    public void publish(String url, byte[] data);

    public void register(WebSocketListener listener);
    public void unregister(WebSocketListener listener);

    public void send(String uri, String client, String message);
    public void send(String uri, String client, byte[] message);
}
