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

    public WisdomServerInitializer(final ServiceAccessor accessor, final boolean secure) throws KeyStoreException {
        this.accessor = accessor;
        this.secure = secure;
    }

    @Override
    public void initChannel(final SocketChannel ch) throws Exception {
        // Create a default pipeline implementation.
        final ChannelPipeline pipeline = ch.pipeline();
        if (secure) {
            final SSLEngine engine = SSLServerContext
                    .getInstance(accessor).serverContext().createSSLEngine();
            engine.setUseClientMode(false);
            setClientAuthenticationMode(engine);
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
    
    private void setClientAuthenticationMode(final SSLEngine engine) {
        final String clientCertificate = accessor.getConfiguration().get("https.clientCertificate");
        if (clientCertificate != null)
        {
            switch (clientCertificate.toLowerCase())
            {
                case "needs":
                    engine.setNeedClientAuth(true);
                    break;
                case "wants":
                    engine.setWantClientAuth(true);
                    break;
                default:
                    break;
            }
        }
    }

}

