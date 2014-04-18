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
 * A very simple controller handling web sockets.
 */
@Controller
public class SimpleWebSocket extends DefaultController {

    @Requires
    Publisher publisher;

    @Requires
    Json json;

    @Opened("/ws/{name}")
    public void open(@Parameter("name") String name) {
        System.out.println("Web socket opened => " + name);
    }

    @Closed("/ws/{name}")
    public void close(@Parameter("name") String name) {
        System.out.println("Web socket closed => " + name);
    }

    @OnMessage("/ws/{name}")
    public void onMessage(@Body Message message, @Parameter("name") String name) {
        System.out.println("Receiving message on " + name + " : " + message.message);
        publisher.publish("/ws/" + name, json.toJson(message.message.toUpperCase()));
    }
}
