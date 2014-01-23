package org.wisdom.engine.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.security.KeyStoreException;

import javax.net.ssl.SSLEngine;

import org.wisdom.engine.ssl.SSLServerContext;

/**
 * Initializes the pipeline.
 */
public class WisdomServerInitializer extends ChannelInitializer<SocketChannel> {

    private final ServiceAccessor accessor;
    private final boolean secure;

    public WisdomServerInitializer(ServiceAccessor accessor, boolean secure) throws KeyStoreException {
        this.accessor = accessor;
        this.secure = secure;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = ch.pipeline();
        if (secure) {
            SSLEngine engine = SSLServerContext
                    .getInstance(accessor.getConfiguration().getBaseDir()).serverContext().createSSLEngine();
            engine.setUseClientMode(false);
            pipeline.addLast("ssl", new SslHandler(engine));
        }

        pipeline.addLast("decoder", new HttpRequestDecoder());

        // Uncomment the following line if you don't want to handle HttpChunks.
        //p.addLast("aggregator", new HttpObjectAggregator(65536));
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());

        // The wisdom handler.
        pipeline.addLast("handler", new WisdomHandler(accessor));

    }

}

