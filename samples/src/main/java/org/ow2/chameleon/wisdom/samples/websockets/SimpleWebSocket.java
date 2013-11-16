package org.ow2.chameleon.wisdom.samples.websockets;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.ow2.chameleon.wisdom.api.Controller;
import org.ow2.chameleon.wisdom.api.DefaultController;
import org.ow2.chameleon.wisdom.api.annotations.*;
import org.ow2.chameleon.wisdom.api.http.websockets.Publisher;

/**
 * A very simple controller handling web sockets.
 */
@Component
@Provides(specifications=Controller.class)
@Instantiate
public class SimpleWebSocket extends DefaultController {

    @Requires
    Publisher publisher;

    @Opened("/assets/{name}")
    public void open(@Parameter("name") String name) {
        System.out.println("Web socket opened => " + name);
    }

    @Closed("/assets/{name}")
    public void close(@Parameter("name") String name) {
        System.out.println("Web socket closed => " + name);
    }

    @OnMessage("/assets/{name}")
    public void onMessage(@Body Message message, @Parameter("name") String name) {
        System.out.println("Receiving message on " + name + " : " + message.message);
        publisher.publish("/assets/" + name, message.message.toUpperCase());
    }
}
