package org.wisdom.engine.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.felix.ipojo.annotations.*;
import org.wisdom.akka.AkkaSystemService;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.content.ContentEngine;
import org.wisdom.api.crypto.Crypto;
import org.wisdom.api.engine.WisdomEngine;
import org.wisdom.api.error.ErrorHandler;
import org.wisdom.api.http.websockets.WebSocketDispatcher;
import org.wisdom.api.http.websockets.WebSocketListener;
import org.wisdom.api.router.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * The wisdom main servlet.
 */
@Component
@Provides
@Instantiate
public class Dispatcher implements WebSocketDispatcher, WisdomEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(Dispatcher.class);

    /**
     * The Wisdom Server instance.
     */
    private final WisdomServer wisdomServer;

    /**
     * The set of Web Socket Listeners used to dispatch data received on web sockets.
     */
    private List<WebSocketListener> listeners = new ArrayList<>();

    /**
     * The map of uri / list of channel context keeping a reference on all opened web sockets.
     */
    private Map<String, List<ChannelHandlerContext>> sockets = new HashMap<>();

    /**
     * The router service.
     */
    @Requires
    private Router router;

    /**
     * The application configuration.
     */
    @Requires
    private ApplicationConfiguration configuration;

    /**
     * The content parser.
     */
    @Requires
    private ContentEngine parsers;

    /**
     * The crypto service.
     */
    @Requires
    private Crypto crypto;

    /**
     * The akka system (used for async).
     */
    @Requires
    private AkkaSystemService system;

    /**
     * The error handler services.
     */
    @Requires(specification = ErrorHandler.class, optional = true)
    private List<ErrorHandler> handlers;

    public Dispatcher() throws InterruptedException {
        ServiceAccessor accessor = new ServiceAccessor(crypto, configuration, router,
                parsers, system, handlers, this); //NOSONAR
        wisdomServer = new WisdomServer(accessor);
    }

    @Validate
    public void start() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    wisdomServer.start();
                } catch (InterruptedException e) {
                    LOGGER.error("Cannot start the Wisdom server", e);
                }
            }
        }).start();
    }

    @Invalidate
    public void stop() {
        wisdomServer.stop();
        listeners.clear();
    }

    @Override
    public void publish(String url, String data) {
        List<ChannelHandlerContext> channels;
        synchronized (this) {
            List<ChannelHandlerContext> ch = sockets.get(url);
            if (ch != null) {
                channels = new ArrayList<>(ch);
            } else {
                channels = Collections.emptyList();
            }
        }
        for (ChannelHandlerContext channel : channels) {
            channel.writeAndFlush(new TextWebSocketFrame(data));
        }
    }

    @Override
    public synchronized void publish(String url, byte[] data) {
        List<ChannelHandlerContext> channels;
        synchronized (this) {
            List<ChannelHandlerContext> ch = sockets.get(url);
            if (ch != null) {
                channels = new ArrayList<>(ch);
            } else {
                channels = Collections.emptyList();
            }
        }
        for (ChannelHandlerContext channel : channels) {
            channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.copiedBuffer(data)));
        }
    }

    public void addWebSocket(String url, ChannelHandlerContext ctx) {
        LOGGER.info("Adding web socket on {} bound to {}", url, ctx);
        List<WebSocketListener> webSocketListeners;
        synchronized (this) {
            List<ChannelHandlerContext> channels = sockets.get(url);
            if (channels == null) {
                channels = new ArrayList<>();
            }
            channels.add(ctx);
            sockets.put(url, channels);
            webSocketListeners = new ArrayList<>(this.listeners);
        }

        for (WebSocketListener listener : webSocketListeners) {
            listener.opened(url);
        }

    }

    public void removeWebSocket(String url, ChannelHandlerContext ctx) {
        LOGGER.info("Removing web socket on {} bound to {}", url, ctx);
        List<WebSocketListener> webSocketListeners;
        synchronized (this) {
            List<ChannelHandlerContext> channels = sockets.get(url);
            if (channels != null) {
                channels.remove(ctx);
                if (channels.isEmpty()) {
                    sockets.remove(url);
                }
            }
            webSocketListeners = new ArrayList<>(this.listeners);
        }

        for (WebSocketListener listener : webSocketListeners) {
            listener.closed(url);
        }
    }

    @Override
    public void register(WebSocketListener listener) {
        synchronized (this) {
            listeners.add(listener);
        }
    }

    @Override
    public void unregister(WebSocketListener listener) {
        synchronized (this) {
            listeners.remove(listener);
        }
    }

    public void received(String uri, byte[] content) {
        List<WebSocketListener> localListeners;
        synchronized (this) {
            localListeners = new ArrayList<>(this.listeners);
        }

        for (WebSocketListener listener : localListeners) {
            listener.received(uri, content);
        }
    }

    @Override
    public String hostname() {
        return wisdomServer.hostname();
    }

    @Override
    public int httpPort() {
        return wisdomServer.httpPort();
    }

    @Override
    public int httpsPort() {
        return wisdomServer.httpsPort();
    }
}
