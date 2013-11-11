package org.ow2.chameleon.wisdom.engine.server;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.*;

/**
 * Created with IntelliJ IDEA.
 * User: clement
 * Date: 11/11/2013
 * Time: 16:55
 * To change this template use File | Settings | File Templates.
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
