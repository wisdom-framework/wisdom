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
