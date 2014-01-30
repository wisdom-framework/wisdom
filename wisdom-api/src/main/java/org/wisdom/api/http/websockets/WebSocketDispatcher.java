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
