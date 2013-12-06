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

    private static final Logger logger = LoggerFactory.getLogger("wisdom-engine");
    private final ServiceAccessor accessor;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelGroup group;

    public WisdomServer(ServiceAccessor accessor) {
        this.accessor = accessor;
    }

    public void start() throws InterruptedException {
        logger.info("Starting Wisdom server");
        int httpPort = accessor.configuration.getIntegerWithDefault(ApplicationConfiguration.HTTP_PORT, 9000);
        int httpsPort = accessor.configuration.getIntegerWithDefault(ApplicationConfiguration.HTTPS_PORT, -1);

        InetAddress address = null;
        if (System.getProperties().containsKey(ApplicationConfiguration.HTTP_PORT)) {
            httpPort = Integer.parseInt(System.getProperty(ApplicationConfiguration.HTTP_PORT));
        }
        if (System.getProperties().containsKey(ApplicationConfiguration.HTTPS_PORT)) {
            httpsPort = Integer.parseInt(System.getProperty(ApplicationConfiguration.HTTPS_PORT));
        }

        try {
            if (accessor.configuration.get("http.address") != null) {
                address = InetAddress.getByName(accessor.configuration.get("http.address"));
            }
            if (System.getProperties().containsKey("http.address")) {
                address = InetAddress.getByName(System.getProperty("http.address"));
            }
        } catch (Exception e) {
            logger.error("Could not understand http.address", e);
            System.exit(-1);
        }

        group = new DefaultChannelGroup("wisdom-channels", GlobalEventExecutor.INSTANCE);
        // Configure the server.
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        try {
            //HTTP
            if (httpPort != -1) {
                logger.info("Wisdom is going to serve HTTP requests on port " + httpPort);
                ServerBootstrap http = new ServerBootstrap();
                http.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new WisdomServerInitializer(accessor, false));
                group.add(http.bind(address, httpPort).sync().channel());
            }

            //HTTPS
            if (httpsPort != -1) {
                logger.info("Wisdom is going to serve HTTPS requests on port " + httpsPort);
                ServerBootstrap https = new ServerBootstrap();
                https.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new WisdomServerInitializer(accessor, true));
                group.add(https.bind(address, httpsPort).sync().channel());
            }
        } catch (Exception e) {
            logger.error("Cannot initialize Wisdom", e);
            group.close().sync();
            bossGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
            System.exit(-1);
        }
    }

    public void stop() {
        try {
            group.close().sync();
            bossGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
            logger.info("Wisdom server has been stopped gracefully");
        } catch (InterruptedException e) {
            logger.warn("Cannot stop the Wisdom server gracefully", e);
        }
    }
}
