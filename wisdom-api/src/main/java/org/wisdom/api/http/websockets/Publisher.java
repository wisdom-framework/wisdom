package org.wisdom.api.http.websockets;

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
     * Sends a message to a specific client connected to a websocket.
     * The method does nothing if their is no client matching the id connected on the websocket.
     *
     * @param uri     the websocket's url
     * @param client  the client id, received in the 'opened' callback
     * @param message the message
     */
    public void send(String uri, String client, String message);

    /**
     * Sends data to a specific client connected to a websocket.
     * The method does nothing if their is no client matching the id connected on the websocket.
     *
     * @param uri     the websocket's url
     * @param client  the client id, received in the 'opened' callback
     * @param message the data
     */
    public void send(String uri, String client, byte[] message);

    //TODO Extend with other types such as json....
    // publish(message(o).as(Json).on(uri))
}
