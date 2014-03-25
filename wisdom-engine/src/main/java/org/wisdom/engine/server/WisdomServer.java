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
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

/**
 * The Wisdom Server.
 */
public class WisdomServer {
    
    private static final String KEY_HTTP_ADDRESS = "http.address";

    private static final Logger LOGGER = LoggerFactory.getLogger("wisdom-engine");
    private final ServiceAccessor accessor;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelGroup group;
    private int httpPort;
    private int httpsPort;
    private InetAddress address;

    public WisdomServer(ServiceAccessor accessor) {
        this.accessor = accessor;
    }

    public void start() throws InterruptedException {
        LOGGER.info("Starting Wisdom server");
        httpPort = accessor.getConfiguration().getIntegerWithDefault(ApplicationConfiguration.HTTP_PORT, 9000);
        httpsPort = accessor.getConfiguration().getIntegerWithDefault(ApplicationConfiguration.HTTPS_PORT, -1);

        address = null;
        if (System.getProperties().containsKey(ApplicationConfiguration.HTTP_PORT)) {
            httpPort = Integer.parseInt(System.getProperty(ApplicationConfiguration.HTTP_PORT));
        }
        if (System.getProperties().containsKey(ApplicationConfiguration.HTTPS_PORT)) {
            httpsPort = Integer.parseInt(System.getProperty(ApplicationConfiguration.HTTPS_PORT));
        }

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

        group = new DefaultChannelGroup("wisdom-channels", GlobalEventExecutor.INSTANCE);
        // Configure the server.
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        try {
            //HTTP
            if (httpPort != -1) {
                LOGGER.info("Wisdom is going to serve HTTP requests on port " + httpPort);
                ServerBootstrap http = new ServerBootstrap();
                http.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new WisdomServerInitializer(accessor, false));
                group.add(http.bind(address, httpPort).sync().channel());
            }

            //HTTPS
            if (httpsPort != -1) {
                LOGGER.info("Wisdom is going to serve HTTPS requests on port " + httpsPort);
                ServerBootstrap https = new ServerBootstrap();
                https.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new WisdomServerInitializer(accessor, true));
                group.add(https.bind(address, httpsPort).sync().channel());
            }
        } catch (Exception e) {
            LOGGER.error("Cannot initialize Wisdom", e);
            group.close().sync();
            bossGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
            onError();
        }
    }

    private void onError() {
        System.exit(-1); //NOSONAR
    }

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

    public String hostname() {
        if (address == null) {
            return "localhost";
        } else {
            return address.getHostName();
        }
    }

    public int httpPort() {
        return httpPort;
    }


    public int httpsPort() {
        return httpsPort;
    }
}
