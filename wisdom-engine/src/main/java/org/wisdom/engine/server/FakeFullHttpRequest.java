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

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.*;

/**
 * A class implementing Full HTTP Request based on a regular HTTP Request. This class is required because the
 * web socket handshaker needs a full http request. However it only used data from a regular http request.
 *
 * In Wisdom, we don't have a full http request while doing the handshake.
 */
public class FakeFullHttpRequest implements FullHttpRequest {

    private final HttpRequest request;

    public FakeFullHttpRequest(HttpRequest request) {
        this.request = request;
    }

    @Override
    public HttpMethod getMethod() {
        return request.getMethod();
    }

    @Override
    public FullHttpRequest setMethod(HttpMethod method) {
        return this;
    }

    @Override
    public String getUri() {
        return request.getUri();
    }

    @Override
    public FullHttpRequest setUri(String uri) {
        return this;
    }

    @Override
    public HttpHeaders trailingHeaders() {
        return null;
    }

    @Override
    public ByteBuf content() {
        return null;
    }

    @Override
    public FullHttpRequest copy() {
        return null;
    }

    @Override
    public HttpContent duplicate() {
        return null;
    }

    @Override
    public FullHttpRequest retain(int increment) {
        return null;
    }

    @Override
    public boolean release() {
        return false;
    }

    @Override
    public boolean release(int i) {
        return false;
    }

    @Override
    public int refCnt() {
        return 0;
    }

    @Override
    public FullHttpRequest retain() {
        return null;
    }

    @Override
    public FullHttpRequest setProtocolVersion(HttpVersion version) {
        return this;
    }

    @Override
    public HttpVersion getProtocolVersion() {
        return request.getProtocolVersion();
    }

    @Override
    public HttpHeaders headers() {
        return request.headers();
    }

    @Override
    public DecoderResult getDecoderResult() {
        return request.getDecoderResult();
    }

    @Override
    public void setDecoderResult(DecoderResult result) {
        // Nothing.
    }


}
