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
package org.wisdom.engine.server;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

/**
 * A set of methods writing common results in the channel.
 */
public class CommonResponses {

    /**
     * Return that we are not compatible with the requested the web socket version.
     *
     * @param channel the channel
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
     * @param channel the channel
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
