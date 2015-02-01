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
package org.wisdom.samples.websockets;

import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.websockets.Publisher;

/**
 * A very simple controller handling SockJS.
 */
@Controller
public class SimpleSockJS extends DefaultController {

    @Requires
    Publisher publisher;

    @Requires
    Json json;

    @Opened("/chat")
    public void open(@Parameter("client") String client) {
        System.out.println("socket opened on /chat by " + client);
        publisher.send("/chat", client, "Welcome on the chat !");
    }

    @Closed("/chat")
    public void close() {
        System.out.println("socket closed => /chat");
    }

    @OnMessage("/chat")
    public void onMessage(@Body Message message) {
        System.out.println("Receiving message : " + message.message);
        publisher.publish("/chat", json.toJson(message.message.toUpperCase()));
    }
}
