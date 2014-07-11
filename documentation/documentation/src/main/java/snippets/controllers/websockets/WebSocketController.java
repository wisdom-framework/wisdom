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
package snippets.controllers.websockets;

import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.annotations.scheduler.Every;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.websockets.Publisher;
import org.wisdom.api.scheduler.Scheduled;

import java.util.Random;

@Controller
public class WebSocketController extends DefaultController implements Scheduled {

    // tag::reception[]
    @OnMessage("/socket")
    public void onMessage(@Body String message) {
        logger().info("Message received : {}", message);
    }
    // end::reception[]

    // tag::receptionWithJson[]
    @OnMessage("/jsonSocket")
    public void onJsonMessage(@Body Data data) {
        logger().info("Data received : name: {}, age: {}", data.name, data.age);
    }
    // end::receptionWithJson[]

    // tag::parameter[]
    @OnMessage("/socket/{name}")
    public void messageOnSeveralSockets(@Parameter("name") String name, @Body String message) {
        logger().info("Message received on {} : {}", name, message);
    }
    // end::parameter[]

    // tag::client[]
    @OnMessage("/socket")
    public void identifyClient(@Parameter("client") String client, @Body String message) {
        logger().info("Message received from {} : {}", client, message);
    }
    // end::client[]


    // tag::send[]
    @Requires
    Publisher publisher;

    @OnMessage("/echo")
    public void echo(@Parameter("client") String client, @Body String message) {
        logger().info("Message received : {}", message);
        publisher.send("/echo", client, message.toUpperCase());
    }
    // end::send[]

    // tag::publish[]
    @OnMessage("/echo-all")
    public void echoAll(@Body String message) {
        logger().info("Message received : {}", message);
        publisher.publish("/echo-all", message.toUpperCase());
    }
    // end::publish[]

    Random random = new Random();

    // tag::binary[]
    @Every("1h")
    public void binary() {
        byte[] bytes = new byte[5];
        random.nextBytes(bytes);
        logger().info("Message dispatching binary : {}", bytes);
        publisher.publish("/binary", bytes);
    }
    // end::binary[]


    // tag::json[]
    @Requires
    Json json;

    @Every("1h")
    public void sendJson() {
        publisher.publish("/ws/json", json.newObject().put("name", "hello").put("date", System.currentTimeMillis()));
    }
    // end::json[]

    // tag::notification[]
    @Opened("/ws/json")
    public void opened(@Parameter("client") String client) {
        logger().info("Client connection on web socket {}", client);
    }

    @Closed("/ws/json")
    public void closed(@Parameter("client") String client) {
        logger().info("Client disconnection on web socket {}", client);
    }
    // end::notification[]


}
