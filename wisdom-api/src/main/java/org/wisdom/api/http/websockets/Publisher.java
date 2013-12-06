package org.wisdom.api.http.websockets;

/**
 * Service to send messages on web sockets.
 */
public interface Publisher {

    public void publish(String uri, String message);

    public void publish(String uri, byte[] message);

    //TODO Extend with other types such as json....
    // publish(message(o).as(Json).on(uri))
}
