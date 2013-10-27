package org.ow2.chameleon.wisdom.engine.server;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.stream.ChunkedStream;
import org.apache.commons.io.IOUtils;
import org.ow2.chameleon.wisdom.api.bodies.NoHttpBody;
import org.ow2.chameleon.wisdom.api.http.Context;
import org.ow2.chameleon.wisdom.api.http.Renderable;
import org.ow2.chameleon.wisdom.api.http.Result;
import org.ow2.chameleon.wisdom.api.http.Results;
import org.ow2.chameleon.wisdom.api.route.Route;
import org.ow2.chameleon.wisdom.engine.wrapper.ContextFromNetty;
import org.ow2.chameleon.wisdom.engine.wrapper.cookies.CookieHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * The Wisdom Channel Handler.
 * Every connection has it's own handler.
 */
public class WisdomHandler extends SimpleChannelInboundHandler<HttpObject> {

    // Disk if size exceed.
    private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

    static {
        DiskFileUpload.deleteOnExitTemporaryFile = true; // should delete file on exit (in normal exit)
        DiskFileUpload.baseDirectory = null; // system temp directory
        DiskAttribute.deleteOnExitTemporaryFile = true; // should delete file on exit (in normal exit)
        DiskAttribute.baseDirectory = null; // system temp directory
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(WisdomHandler.class);
    private final ServiceAccessor accessor;
    private ContextFromNetty context;
    private HttpRequest request;
    private HttpPostRequestDecoder decoder;

    public WisdomHandler(ServiceAccessor accessor) {
        this.accessor = accessor;
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
        ctx.write(response);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpRequest) {
            request = (HttpRequest) msg;
            initializeContext(ctx, request);
        }

        if (msg instanceof HttpContent) {
            if (context == null) {
                LOGGER.warn("Http Content receive before the request");
                return;
            }
            // Do we have a content ?
            // Only valid for put and post.
            if (request.getMethod().equals(HttpMethod.POST) || request.getMethod().equals(HttpMethod.PUT)) {
                if (decoder == null) {
                    decoder = new HttpPostRequestDecoder(factory, request);
                }
                context.decodeContent(request, (HttpContent) msg, decoder);
            }

            if (msg instanceof LastHttpContent) {
                // End of transmission.
                dispatch(ctx);
                cleanup();
            }

        }

//        else {
//            LOGGER.warn("Received message is not supported: {}", msg);
//        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (decoder != null) {
            decoder.cleanFiles();
        }
    }

    private void cleanup() {
        request = null;
        if (decoder != null) {
            decoder.destroy();
            decoder = null;
        }
        Context.context.remove();
        context = null;
    }

    private void initializeContext(ChannelHandlerContext ctx, HttpRequest req) {
        LOGGER.info("Attempt to serve " + req.getUri());
        // 1 build context
        context = new ContextFromNetty(accessor, ctx, req, null);
    }

    private void dispatch(ChannelHandlerContext ctx) {
        // 2 Register context
        Context.context.set(context);
        // 3 Get route for context
        // dump route
        for (Route r : accessor.router.getRoutes()) {
            System.out.println(r.getUrl() + " => " + r.getControllerObject());
        }
        Route route = accessor.router.getRouteFor(context.request().method(), context.path());
        Result result;
        try {

            if (route == null) {
                // 3.1 : no route to destination
                LOGGER.info("No route to " + context.path());
                result = Results.notFound();
            } else {
                // 3.2 : route found
                context.setRoute(route);
                result = invoke(route);
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
            }
        } finally {
            // Cleanup thread local
            Context.context.remove();
        }
    }

    private boolean writeResponse(ChannelHandlerContext ctx, HttpRequest request, Context context, Result result,
                                  boolean handleFlashAndSessionCookie) {
        // Decide whether to close the connection or not.
        boolean keepAlive = isKeepAlive(request);

        // Render the result.
        InputStream stream;
        boolean success = true;
        Renderable renderable = result.getRenderable();
        try {
            stream = renderable.render(context, result);
        } catch (Exception e) {
            LOGGER.error("Cannot render the response to " + request.getUri(), e);
            stream = new ByteArrayInputStream(NoHttpBody.EMPTY);
            success = false;
        }
        final InputStream content = stream;

        // Build the response object.
        HttpResponse response = new DefaultHttpResponse(
                request.getProtocolVersion(),
                getStatusFromResult(result, success));

        for (Map.Entry<String, String> header : result.getHeaders().entrySet()) {
            response.headers().set(header.getKey(), header.getValue());
        }

        String fullContentType = result.getFullContentType();
        if (fullContentType == null) {
            response.headers().set(CONTENT_TYPE, renderable.mimetype());
        } else {
            response.headers().set(CONTENT_TYPE, fullContentType);
        }

        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.headers().set(CONTENT_LENGTH, renderable.length());
            // Add keep alive header as per:
            // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
            response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }

        // copy cookies / flash and session
        if (handleFlashAndSessionCookie) {
            context.flash().save(context, result);
            context.session().save(context, result);
        }

        // copy cookies
        for (org.ow2.chameleon.wisdom.api.cookies.Cookie cookie : result.getCookies()) {
            response.headers().add(SET_COOKIE, CookieHelper
                    .convertWisdomCookieToNettyCookie(cookie));
        }

        // Write the response.
        ctx.write(response);

        // Write the content.
        ChannelFuture writeFuture;
        writeFuture = ctx.write(new ChunkedStream(content));
        writeFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                IOUtils.closeQuietly(content);
            }
        });

        // Decide whether to close the connection or not.
        if (keepAlive) {
            // Close the connection when the whole content is written out.
            writeFuture.addListener(ChannelFutureListener.CLOSE);
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
            return (Result) route.getControllerMethod().invoke(route.getControllerObject());
        } catch (Exception e) {
            return Results.internalServerError(e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
