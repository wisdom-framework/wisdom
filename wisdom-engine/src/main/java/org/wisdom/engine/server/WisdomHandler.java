package org.wisdom.engine.server;

import akka.dispatch.OnComplete;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.stream.ChunkedStream;
import org.apache.commons.io.IOUtils;
import org.wisdom.api.bodies.NoHttpBody;
import org.wisdom.api.content.ContentSerializer;
import org.wisdom.api.error.ErrorHandler;
import org.wisdom.api.http.*;
import org.wisdom.api.router.Route;
import org.wisdom.engine.wrapper.ContextFromNetty;
import org.wisdom.engine.wrapper.cookies.CookieHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Future;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;

/**
 * The Wisdom Channel Handler.
 * Every connection has it's own handler.
 */
public class WisdomHandler extends SimpleChannelInboundHandler<Object> {

    // Disk if size exceed.
    private static final HttpDataFactory DATA_FACTORY = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
    private static final Logger LOGGER = LoggerFactory.getLogger("wisdom-engine");
    private final ServiceAccessor accessor;
    private WebSocketServerHandshaker handshaker;

    static {
        DiskFileUpload.deleteOnExitTemporaryFile = true; // should delete file on exit (in normal exit)
        DiskFileUpload.baseDirectory = null; // system temp directory
        DiskAttribute.deleteOnExitTemporaryFile = true; // should delete file on exit (in normal exit)
        DiskAttribute.baseDirectory = null; // system temp directory
    }

    private ContextFromNetty context;
    private HttpRequest request;
    private HttpPostRequestDecoder decoder;

    public WisdomHandler(ServiceAccessor accessor) {
        this.accessor = accessor;
    }

    private static String getWebSocketLocation(HttpRequest req) {
        //TODO Support wss
        return "ws://" + req.headers().get(HOST) + req.getUri();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpObject) {
            handleHttpRequest(ctx, (HttpObject) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof CloseWebSocketFrame) {
            accessor.getDispatcher().removeWebSocket(strip(handshaker.uri()), ctx);
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        if (frame instanceof TextWebSocketFrame) {
            accessor.getDispatcher().received(strip(handshaker.uri()), ((TextWebSocketFrame) frame).text().getBytes());
        } else if (frame instanceof BinaryWebSocketFrame) {
            accessor.getDispatcher().received(strip(handshaker.uri()), frame.content().array());
        }
    }

    private static String strip(String uri) {
        try {
            return new URI(uri).getRawPath();
        } catch (URISyntaxException e) { //NOSONAR
            return null;
        }
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, HttpObject req) {
        if (req instanceof HttpRequest) {
            request = (HttpRequest) req;
            context = new ContextFromNetty(accessor, ctx, request);
            switch (handshake(ctx)) {
                case HANDSHAKE_UNSUPPORTED:
                    CommonResponses.sendUnsupportedWebSocketVersionResponse(ctx.channel());
                    return;
                case HANDSHAKE_ERROR :
                    CommonResponses.sendWebSocketHandshakeErrorResponse(ctx.channel());
                    return;
                case HANDSHAKE_OK :
                    // Handshake ok, just return
                    return;
                case NO_HANDSHAKE :
                default:
                    // No handshake attempted, continue.
            }
        }

        if (req instanceof HttpContent) {
            // Only valid for put and post.
            if (request.getMethod().equals(HttpMethod.POST) || request.getMethod().equals(HttpMethod.PUT)) {
                if (decoder == null) {
                    decoder = new HttpPostRequestDecoder(DATA_FACTORY, request);
                }
                context.decodeContent(request, (HttpContent) req, decoder);
            }
        }

        if (req instanceof LastHttpContent) {
            // End of transmission.
            boolean isAsync = dispatch(ctx);

            if (!isAsync) {
                cleanup();
            }
        }

    }

    /**
     * Constant telling that the websocket handshake has not be attempted as the request did not include the headers.
     */
    private final static int NO_HANDSHAKE = 0;
    /**
     Constant telling that the websocket handshake has been made successfully.
     */
    private final static int HANDSHAKE_OK = 1;
    /**
     Constant telling that the websocket handshake has been attempted but failed.
     */
    private final static int HANDSHAKE_ERROR = 2;
    /**
     * Constant telling that the websocket handshake has failed because the version specified in the request is not
     * supported. In this case the error method is already written in the channel.
     */
    private final static int HANDSHAKE_UNSUPPORTED = 3;

    /**
     * Manages the websocket handshake.
     *
     * @param ctx the current context
     * @return an integer representing the handshake state.
     */
    private int handshake(ChannelHandlerContext ctx) {
        if (HttpHeaders.Values.UPGRADE.equalsIgnoreCase(request.headers().get(CONNECTION))
                || HttpHeaders.Values.WEBSOCKET.equalsIgnoreCase(request.headers().get(HttpHeaders.Names.UPGRADE))) {
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                    getWebSocketLocation(request), null, false);
            handshaker = wsFactory.newHandshaker(request);
            if (handshaker == null) {
                return HANDSHAKE_UNSUPPORTED;
            } else {
                try {
                    handshaker.handshake(ctx.channel(), new FakeFullHttpRequest(request));
                    accessor.getDispatcher().addWebSocket(strip(handshaker.uri()), ctx);
                    LOGGER.debug("Handshake completed on {}", strip(handshaker.uri()));
                    return HANDSHAKE_OK;
                } catch (Exception e) {
                    LOGGER.error("The websocket handshake failed for {}", getWebSocketLocation(request), e);
                    return HANDSHAKE_ERROR;
                }
            }
        }
        return NO_HANDSHAKE;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // Do we have a web socket opened ?
        if (handshaker != null) {
            accessor.getDispatcher().removeWebSocket(strip(handshaker.uri()), ctx);
            handshaker = null;
        }

        if (decoder != null) {
            try {
                decoder.cleanFiles();
                decoder.destroy();
            } catch (IllegalStateException e) { //NOSONAR
                // Decoder already destroyed.
            } finally {
                decoder = null;
            }
        }
    }

    private void cleanup() {
        // Release all resources, especially uploaded file.
        context.cleanup();
        request = null;
        if (decoder != null) {
            try {
                decoder.cleanFiles();
                decoder.destroy();
            } catch (IllegalStateException e) { //NOSONAR
                // Decoder already destroyed.
            } finally {
                decoder = null;
            }
        }
        Context.CONTEXT.remove();
        context = null;
    }

    private boolean dispatch(ChannelHandlerContext ctx) {
        LOGGER.debug("Dispatching {} {}", context.request().method(), context.path());
        // 2 Register context
        Context.CONTEXT.set(context);
        // 3 Get route for context
        Route route = accessor.getRouter().getRouteFor(context.request().method(), context.path());
        Result result;

        if (route == null) {
            // 3.1 : no route to destination

            // If we open a websocket in the same request, just ignore it.
            if (handshaker != null) {
                return false;
            }

            LOGGER.info("No route to serve {} {}", context.request().method(), context.path());
            result = Results.notFound();
            for (ErrorHandler handler : accessor.getHandlers()) {
                result = handler.onNoRoute(
                        org.wisdom.api.http.HttpMethod.from(context.request().method()),
                        context.path());
            }
        } else {
            // 3.2 : route found
            context.setRoute(route);
            result = invoke(route);
            if (result instanceof AsyncResult) {
                // Asynchronous operation in progress.
                handleAsyncResult(ctx, request, context, (AsyncResult) result);
                return true;
            }
        }

        try {
            writeResponse(ctx, request, context, result, true);
        } catch (Exception e) {
            LOGGER.error("Cannot write response", e);
            result = Results.internalServerError(e);
            try {
                writeResponse(ctx, request, context, result, false);
            } catch (Exception e1) {
                LOGGER.error("Cannot even write the error response...", e1);
                // Ignore.
            }
        } finally {
            // Cleanup thread local
            Context.CONTEXT.remove();
        }
        return false;
    }

    /**
     * Handling an async result.
     * The controller has returned an async task ( {@link java.util.concurrent.Callable} ) that will be computed
     * asynchronously using the Akka system dispatcher.
     * The callable is not called using the Netty worker thread.
     *
     * @param ctx     the channel context
     * @param request the request
     * @param context the context
     * @param result  the async result
     */
    private void handleAsyncResult(final ChannelHandlerContext ctx, final HttpRequest request, final Context context,
                                   AsyncResult result) {
        Future<Result> future = accessor.getSystem().dispatch(result.callable(), context);
        future.onComplete(new OnComplete<Result>() {
            public void onComplete(Throwable failure, Result result) {
                if (failure != null) {
                    //We got a failure, handle it here
                    writeResponse(ctx, request, context, Results.internalServerError(failure), false);
                } else {
                    // We got a result, write it here.
                    writeResponse(ctx, request, context, result, true);
                }
                cleanup();
            }
        }, accessor.getSystem().fromThread());
    }

    private InputStream processResult(Result result) throws Exception {
        Renderable<?> renderable = result.getRenderable();
        if (renderable == null) {
            renderable = new NoHttpBody();
        }

        if (renderable.requireSerializer()) {
            ContentSerializer serializer = null;
            if (result.getContentType() != null) {
                serializer = accessor.getContentEngines().getContentSerializerForContentType(result
                        .getContentType());
            }
            if (serializer == null) {
                // Try with the Accept type
                String fromRequest = context.request().contentType();
                serializer = accessor.getContentEngines().getContentSerializerForContentType(fromRequest);
            }

            if (serializer != null) {
                serializer.serialize(renderable);
            }
        }
        return renderable.render(context, result);
    }

    private boolean writeResponse(final ChannelHandlerContext ctx, final HttpRequest request, Context context,
                                  Result result,
                                  boolean handleFlashAndSessionCookie) {
        //TODO Refactor this method.

        // Decide whether to close the connection or not.
        boolean keepAlive = isKeepAlive(request);

        // Render the result.
        InputStream stream;
        boolean success = true;
        Renderable<?> renderable = result.getRenderable();
        if (renderable == null) {
            renderable = new NoHttpBody();
        }
        try {
            stream = processResult(result);
        } catch (Exception e) {
            LOGGER.error("Cannot render the response to " + request.getUri(), e);
            stream = new ByteArrayInputStream(NoHttpBody.EMPTY);
            success = false;
        }
        final InputStream content = stream;

        // Build the response object.
        HttpResponse response;
        Object res;
        final boolean isChunked = renderable.mustBeChunked();
        if (isChunked) {
            response = new DefaultHttpResponse(request.getProtocolVersion(), getStatusFromResult(result, success));
            if (renderable.length() > 0) {
                response.headers().set(CONTENT_LENGTH, renderable.length());
            }
            // Can't determine the size, so switch to chunked.
            response.headers().set(TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);
            // In addition, we can't keep the connection open.
            response.headers().set(CONNECTION, HttpHeaders.Values.CLOSE);
            //keepAlive = false;
            res = new ChunkedStream(content);
        } else {
            DefaultFullHttpResponse resp = new DefaultFullHttpResponse(request.getProtocolVersion(),
                    getStatusFromResult(result, success));
            byte[] cont = new byte[0];
            try {
                cont = IOUtils.toByteArray(content);
            } catch (IOException e) {
                LOGGER.error("Cannot copy the response to " + request.getUri(), e);
            }
            resp.headers().set(CONTENT_LENGTH, cont.length);
            res = Unpooled.copiedBuffer(cont);
            if (keepAlive) {
                // Add keep alive header as per:
                // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
                resp.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            }
            resp.content().writeBytes(cont);
            response = resp;
        }

        for (Map.Entry<String, String> header : result.getHeaders().entrySet()) {
            response.headers().set(header.getKey(), header.getValue());
        }

        String fullContentType = result.getFullContentType();
        if (fullContentType == null) {
            response.headers().set(CONTENT_TYPE, renderable.mimetype());
        } else {
            response.headers().set(CONTENT_TYPE, fullContentType);
        }

        // copy cookies / flash and session
        if (handleFlashAndSessionCookie) {
            context.flash().save(context, result);
            context.session().save(context, result);
        }

        // copy cookies
        for (org.wisdom.api.cookies.Cookie cookie : result.getCookies()) {
            response.headers().add(SET_COOKIE, CookieHelper
                    .convertWisdomCookieToNettyCookie(cookie));
        }

        // Send the response and close the connection if necessary.
        ctx.write(response);

        final ChannelFuture writeFuture;
        if (isChunked) {
            writeFuture = ctx.write(res);
        } else {
            writeFuture = ctx.writeAndFlush(res);
        }
        writeFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                IOUtils.closeQuietly(content);
            }
        });

        if (isChunked) {
            // Write the end marker
            ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            if (! keepAlive) {
                // Close the connection when the whole content is written out.
                lastContentFuture.addListener(ChannelFutureListener.CLOSE);
            }
        } else {
            if (! keepAlive) {
                // Close the connection when the whole content is written out.
                writeFuture.addListener(ChannelFutureListener.CLOSE);
            }
        }

        return keepAlive;

    }

    private HttpResponseStatus getStatusFromResult(Result result, boolean success) {
        if (!success) {
            return HttpResponseStatus.BAD_REQUEST;
        } else {
            return HttpResponseStatus.valueOf(result.getStatusCode());
        }
    }

    private Result invoke(Route route) {
        try {
            return route.invoke();
        } catch (Throwable e) { //NOSONAR
            if (e.getCause() != null) {
                // We don't really care about the parent exception, dump the cause only.
                LOGGER.error("An error occurred during route invocation", e.getCause());
            } else {
                LOGGER.error("An error occurred during route invocation", e);
            }
            // invoke error handlers
            Result result = null;
            for (ErrorHandler handler : accessor.getHandlers()) {
                result = handler.onError(context, route, e);
            }
            if (result == null) {
                if (e.getCause() != null) {
                    result = Results.internalServerError(e.getCause());
                } else {
                    result = Results.internalServerError(e);
                }
            }
            return result;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("Exception caught in channel", cause);
        ctx.close();
    }
}
