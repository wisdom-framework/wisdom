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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.configuration.ApplicationConfiguration;

import java.net.InetAddress;
import java.security.KeyStoreException;
import java.util.Random;

/**
 * The Wisdom Server.
 */
public class WisdomServer {

    private static final String KEY_HTTP_ADDRESS = "http.address";

    private static final Logger LOGGER = LoggerFactory.getLogger("wisdom-netty-engine");
    private final ServiceAccessor accessor;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelGroup group;
    private int httpPort;
    private int httpsPort;
    private InetAddress address;

    /**
     * Creates a new instance of the Wisdom Server.
     * @param accessor the structure letting access services.
     */
    public WisdomServer(ServiceAccessor accessor) {
        this.accessor = accessor;
    }

    /**
     * Starts the server.
     * @throws InterruptedException if the server is interrupted.
     */
    public void start() throws InterruptedException {
        LOGGER.info("Starting Netty server");
        httpPort = accessor.getConfiguration().getIntegerWithDefault("netty.http.port", 8080);
        httpsPort = accessor.getConfiguration().getIntegerWithDefault("netty.https.port", -1);

        initializeInetAddress();

        group = new DefaultChannelGroup("wisdom-channels", GlobalEventExecutor.INSTANCE);
        // Configure the server.
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        try {
            // Here we need to start the different channels.
            // Negative ports disable the channels. Usually, we use -1.
            // 0 indicates a random port.
            initializeHTTP();
            initializeHTTPS();
        } catch (Exception e) {
            LOGGER.error("Cannot initialize Wisdom", e);
            group.close().sync();
            bossGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
            onError();
        }
    }

    private void initializeHTTPS() throws Exception {
        //HTTPS
        if (httpsPort == 0) {
            Random random = new Random();
            for (int i = 0; httpsPort == 0 && i < 30; i++) {
                int port = 8080 + random.nextInt(10000);
                try {
                    LOGGER.debug("Random port lookup - Trying with {}", port);
                    bind(port, true);
                    httpsPort = port;
                    LOGGER.info("Wisdom is going to serve HTTPS requests on port " + httpsPort);
                } catch (Exception e) {
                    LOGGER.debug("Cannot bind on port {} (port already used probably)", port, e);
                }
            }

            // If the port is still 0, we were not able to bind on any port.
            if (httpsPort == 0) {
                throw new IllegalStateException("Cannot find a free port for HTTPS after 30 tries");
            }
        } else if (httpsPort >= 0) {
            bind(httpsPort, true);
            LOGGER.info("Wisdom is going to serve HTTPS requests on port " + httpsPort);
        }
    }

    private void initializeHTTP() throws Exception {
        // HTTP
        if (httpPort == 0) {
            Random random = new Random();
            for (int i = 0; httpPort == 0  && i < 30; i++) {
                int port = 8080 + random.nextInt(10000);
                try {
                    LOGGER.debug("Random port lookup - Trying with {}", port);
                    bind(port, false);
                    httpPort = port;
                    LOGGER.info("Wisdom is going to serve HTTP requests on port " + httpPort);
                } catch (Exception e) {
                    LOGGER.debug("Cannot bind on port {} (port already used probably)", port, e);
                }
            }

            // If the port is still 0, we were not able to bind on any port.
            if (httpPort == 0) {
                throw new IllegalStateException("Cannot find a free port for HTTP after 30 tries");
            }
        } else if (httpPort >= 0) {
            bind(httpPort, false);
            LOGGER.info("Wisdom is going to serve HTTP requests on port " + httpPort);
        }
    }

    private void initializeInetAddress() {
        address = null;
        try {
            if (accessor.getConfiguration().get(KEY_HTTP_ADDRESS) != null) {
                address = InetAddress.getByName(accessor.getConfiguration().get(KEY_HTTP_ADDRESS));
            }
            if (System.getProperties().containsKey(KEY_HTTP_ADDRESS)) {
                address = InetAddress.getByName(System.getProperty(KEY_HTTP_ADDRESS));
            }
        } catch (Exception e) {
            LOGGER.error("Could not understand http.address", e);
            onError();
        }
    }

    private void bind(int port, boolean secure) throws KeyStoreException, InterruptedException {
        ServerBootstrap http = new ServerBootstrap();
        http.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new WisdomServerInitializer(accessor, secure));
        group.add(http.bind(address, port).sync().channel());
    }

    private void onError() {
        System.exit(-1); //NOSONAR
    }

    /**
     * Stops the server.
     */
    public void stop() {
        try {
            group.close().sync();
            bossGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
            LOGGER.info("Wisdom server has been stopped gracefully");
        } catch (InterruptedException e) {
            LOGGER.warn("Cannot stop the Wisdom server gracefully", e);
        }
    }

    /**
     * @return the hostname.
     */
    public String hostname() {
        if (address == null) {
            return "localhost";
        } else {
            return address.getHostName();
        }
    }

    /**
     * @return the HTTP port on which the current HTTP server is bound. {@literal -1} means that the HTTP connection
     * is not enabled.
     */
    public int httpPort() {
        return httpPort;
    }

    /**
     * @return the HTTP port on which the current HTTPS server is bound. {@literal -1} means that the HTTPS connection
     * is not enabled.
     */
    public int httpsPort() {
        return httpsPort;
    }
}
