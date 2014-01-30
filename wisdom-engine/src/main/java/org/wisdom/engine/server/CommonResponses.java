package org.wisdom.engine.server;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

/**
 * A set of methods writing common results in the channel
 */
public class CommonResponses {

    /**
     * Return that we are not compatible with the requested the web socket version.
     *
     * @param channel
     *            Channel
     */
    public static void sendUnsupportedWebSocketVersionResponse(Channel channel) {
        HttpResponse res = new DefaultHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.UPGRADE_REQUIRED);
        res.headers().set(HttpHeaders.Names.SEC_WEBSOCKET_VERSION, WebSocketVersion.V13.toHttpHeaderValue());
        channel.write(res);
    }

    /**
     * Return that we were not able to complete the handshake.
     *
     * @param channel
     *            Channel
     */
    public static void sendWebSocketHandshakeErrorResponse(Channel channel) {
        DefaultFullHttpResponse res = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.BAD_REQUEST);
        res.headers().set(HttpHeaders.Names.SEC_WEBSOCKET_VERSION, WebSocketVersion.V13.toHttpHeaderValue());
        res.content().writeBytes("Error during websocket handshake".getBytes());
        channel.write(res);
    }
}
