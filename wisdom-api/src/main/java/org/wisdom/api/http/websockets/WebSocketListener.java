package org.wisdom.api.http.websockets;

/**
 * Classes implementing this interface should register themselves on {@link WebSocketDispatcher} to receive
 * notification when client are opening, closing web sockets or sending data.
 */
public interface WebSocketListener {

    public void received(String uri, byte[] content);

    public void opened(String uri);

    public void closed(String uri);

}
