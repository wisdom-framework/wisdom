package org.ow2.chameleon.wisdom.engine.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * Initializes the pipeline.
 */
public class WisdomServerInitializer extends ChannelInitializer<SocketChannel> {

    private final ServiceAccessor accessor;

    public WisdomServerInitializer(ServiceAccessor accessor) {
        this.accessor = accessor;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline p = ch.pipeline();

        // Uncomment the following line if you want HTTPS
        //SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
        //engine.setUseClientMode(false);
        //p.addLast("ssl", new SslHandler(engine));

        p.addLast("decoder", new HttpRequestDecoder());
        // Uncomment the following line if you don't want to handle HttpChunks.
        //p.addLast("aggregator", new HttpObjectAggregator(1048576));
        p.addLast("encoder", new HttpResponseEncoder());
        p.addLast("chunkedWriter", new ChunkedWriteHandler());

        p.addLast("handler", new WisdomHandler(accessor));

        //p.addLast("deflater", new HttpContentCompressor());
    }


}
