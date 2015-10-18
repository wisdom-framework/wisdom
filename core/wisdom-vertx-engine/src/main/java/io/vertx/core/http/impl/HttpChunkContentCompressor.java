/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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
package io.vertx.core.http.impl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpContentCompressor;

/**
 * Copy of the original vert.x class but extends a different HTTPCompressor class to allow us to
 * customize the content encoding.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class HttpChunkContentCompressor extends WisdomHttpContentCompressor {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
            throws Exception {
        if (msg instanceof ByteBuf) {
            // convert ByteBuf to HttpContent to make it work with compression.
            // This is needed as we use the
            // ChunkedWriteHandler to send files when compression is enabled.
            msg = new DefaultHttpContent((ByteBuf) msg);
        }
        super.write(ctx, msg, promise);
    }

}
