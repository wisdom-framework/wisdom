package org.ow2.chameleon.wisdom.engine.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.concurrent.CancellationException;

/**
 * The Wisdom Server.
 */
public class WisdomServer {

    private final Logger logger = LoggerFactory.getLogger(WisdomServer.class);
    private final ServiceAccessor accessor;
    private Channel channel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public WisdomServer(ServiceAccessor accessor) {
        this.accessor = accessor;
    }

    public void start() throws InterruptedException {
        logger.info("Starting Wisdom server");
        int httpPort = accessor.configuration.getIntegerWithDefault("http.port", 9000);

        InetAddress address = null;
        if (System.getProperties().containsKey("http.port")) {
            httpPort = Integer.parseInt(System.getProperty("http.port"));
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

        // Configure the server.
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new WisdomServerInitializer(accessor));
            channel = b.bind(address, httpPort).sync().channel();
            // If something goes wrong, close everything. The following call wait until the channel is closed.
            channel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

    public void stop() {
        try {
            bossGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
            logger.info("Wisdom server stopped");
        } catch (InterruptedException e) {
            logger.warn("Cannot stop the Wisdom server gracefully", e);
        }
    }
}
