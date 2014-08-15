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
package org.wisdom.framework.vertx;

import akka.dispatch.OnComplete;
import io.netty.handler.codec.http.ServerCookieEncoder;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.streams.Pump;
import org.wisdom.api.bodies.NoHttpBody;
import org.wisdom.api.content.ContentCodec;
import org.wisdom.api.http.*;
import org.wisdom.api.router.Route;
import org.wisdom.framework.vertx.cookies.CookieHelper;
import scala.concurrent.Future;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Created by clement on 11/08/2014.
 */
public class WebSocketHandler implements Handler<ServerWebSocket> {


    private final static Logger LOGGER = LoggerFactory.getLogger(WebSocketHandler.class);
    private final ServiceAccessor accessor;


    public WebSocketHandler(ServiceAccessor accessor) {
        this.accessor = accessor;
    }

    @Override
    public void handle(final ServerWebSocket socket) {
        LOGGER.info("New web socket connection {}, {}", socket, socket.uri());
        accessor.getDispatcher().addWebSocket(socket.path(), socket);
        socket.closeHandler(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                LOGGER.info("Web Socket closed {}, {}", socket, socket.uri());
                accessor.getDispatcher().removeWebSocket(socket.path(), socket);
            }
        });
        socket.dataHandler(new Handler<Buffer>() {
            @Override
            public void handle(Buffer event) {
                LOGGER.info("receiving : " + event.toString() + " on " + socket.path());
                accessor.getDispatcher().received(socket.path(), event.getBytes(), socket);
            }
        });

    }
}
