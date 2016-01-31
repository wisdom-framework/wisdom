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
package org.wisdom.framework.vertx;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.streams.Pump;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.bodies.NoHttpBody;
import org.wisdom.api.concurrent.ManagedFutureTask;
import org.wisdom.api.exceptions.ExceptionMapper;
import org.wisdom.api.exceptions.HttpException;
import org.wisdom.api.http.*;
import org.wisdom.api.router.Route;
import org.wisdom.framework.vertx.cookies.CookieHelper;
import org.wisdom.framework.vertx.file.DiskFileUpload;
import org.wisdom.framework.vertx.file.MixedFileUpload;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handles HTTP Request. Don't forget that request may arrive as chunk.
 */
public class HttpHandler implements Handler<HttpServerRequest> {

    /**
     * The server name.
     */
    private static final String SERVER_NAME = "Wisdom-Framework/" + BuildConstants.WISDOM_VERSION + " Vert.x/" +
            BuildConstants.VERTX_VERSION;

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpHandler.class);

    private final ServiceAccessor accessor;
    private final Vertx vertx;
    private final Server server;

    /**
     * Creates the handler.
     *
     * @param vertx    the vertx singleton
     * @param accessor the accessor
     * @param server   the server configuration - used to check whether or not the message should be
     *                 allowed or denied
     */
    public HttpHandler(Vertx vertx, ServiceAccessor accessor, Server server) {
        this.accessor = accessor;
        this.vertx = vertx;
        this.server = server;
    }

    /**
     * Handles a new HTTP request.
     * The actual reading of the request is delegated to the {@link org.wisdom.framework.vertx.ContextFromVertx} and
     * {@link org.wisdom.framework.vertx.RequestFromVertx} classes. However, the close handler is set here and
     * trigger the request dispatch (i.e. Wisdom processing).
     *
     * @param request the request
     */
    @Override
    public void handle(final HttpServerRequest request) {
        LOGGER.debug("A request has arrived on the server : {} {}", request.method(), request.path());
        final ContextFromVertx context = new ContextFromVertx(vertx, vertx.getOrCreateContext(), accessor, request);

        if (!server.accept(request.path())) {
            LOGGER.warn("Request on {} denied by {}", request.path(), server.name());
            writeResponse(context, (RequestFromVertx) context.request(),
                    server.getOnDeniedResult(),
                    false,
                    true);
        } else {
            Buffer raw = Buffer.buffer(0);
            RequestFromVertx req = (RequestFromVertx) context.request();
            AtomicBoolean error = new AtomicBoolean();
            if (HttpUtils.isPostOrPut(request)) {
                request.setExpectMultipart(true);
                request.uploadHandler(upload -> req.getFiles().add(new MixedFileUpload(context.vertx(), upload,
                        accessor.getConfiguration().getLongWithDefault("http.upload.disk.threshold", DiskFileUpload.MINSIZE),
                        accessor.getConfiguration().getLongWithDefault("http.upload.max", -1L),
                        r -> {
                            request.uploadHandler(null);
                            request.handler(null);
                            error.set(true);
                            writeResponse(context, req, r, false, true);
                        })
                ));
            }

            int maxBodySize =
                    accessor.getConfiguration().getIntegerWithDefault("request.body.max.size", 100 * 1024);
            request.handler(event -> {
                if (event == null) {
                    return;
                }

                // To avoid we run out of memory we cut the read body to 100Kb. This can be configured using the
                // "request.body.max.size" property.
                boolean exceeded = raw.length() >= maxBodySize;

                // We may have the content in different HTTP message, check if we already have a content.
                // Issue #257.
                if (!exceeded) {
                    raw.appendBuffer(event);
                } else {
                    // Remove the handler as we stop reading the request.
                    request.handler(null);
                    error.set(true);
                    writeResponse(context, req, new Result(Status.PAYLOAD_TOO_LARGE)
                            .render("Body size exceeded - request cancelled")
                                    .as(MimeTypes.TEXT),
                            false, true);
                }
            });

            request.endHandler(event -> {
                if (error.get()) {
                    // Error already written.
                    return;
                }
                req.setRawBody(raw);
                // Notifies the context that the request has been read, we start the dispatching.
                if (context.ready()) {
                    // Dispatch.
                    dispatch(context, (RequestFromVertx) context.request());
                } else {
                    writeResponse(context, req,
                            Results.badRequest("Request processing failed"), false, true);
                }
            });
        }
    }

    /**
     * The request is now completed, clean everything.
     *
     * @param context the context
     */
    private static void cleanup(ContextFromVertx context) {
        // Release all resources, especially uploaded file.
        if (context != null) {
            context.cleanup();
        }
        Context.CONTEXT.remove();
    }


    private void dispatch(ContextFromVertx context, RequestFromVertx request) {
        LOGGER.debug("Dispatching {} {}", context.request().method(), context.path());
        // 2 Register context
        Context.CONTEXT.set(context);
        // 3 Get route for context
        Route route = accessor.getRouter().getRouteFor(context.request().method(), context.path(), request);
        Result result;

        if (route == null) {
            // 3.1 : no route to destination
            // Should never return null, but an unbound route instead.
            LOGGER.error("The router has returned 'null' instead of an unbound route for " + context.path());
            result = Results.notFound();
        } else {
            // 3.2 : route found
            context.route(route);
            result = invoke(route);

            if (result instanceof AsyncResult) {
                // Asynchronous operation in progress.
                handleAsyncResult(context, request, (AsyncResult) result);
                return;
            }
        }

        // Synchronous processing or not found.
        try {
            writeResponse(context, request, result, true, false);
        } catch (Exception e) {
            LOGGER.error("Cannot write response", e);
            result = Results.internalServerError(e);
            try {
                writeResponse(context, request, result, false, false);
            } catch (Exception e1) {
                LOGGER.error("Cannot even write the error response...", e1);
                // Ignore.
            }
        }
        // If we reach this point, it means we did not write anything... Annoying.
    }

    private Result invoke(Route route) {
        try {
            return route.invoke();
        } catch (Throwable e) { //NOSONAR
            if (e.getCause() != null) {
                // We don't really care about the parent exception, dump the cause only.
                LOGGER.error("An error occurred during route invocation", e.getCause());
                return Results.internalServerError(e.getCause());
            } else {
                LOGGER.error("An error occurred during route invocation", e);
                return Results.internalServerError(e);
            }
        }
    }

    private void handleAsyncResult(
            final ContextFromVertx context,
            final RequestFromVertx request,
            final AsyncResult asyncResult) {

        ManagedFutureTask<Result> future = accessor.getExecutor().submit(asyncResult.callable());
        Futures.addCallback(future, new FutureCallback<Result>() {
            @Override
            public void onSuccess(Result result) {
                // We got a result, write it here.
                // Merge the headers of the initial result and the async results.
                final Map<String, String> headers = result.getHeaders();
                for (Map.Entry<String, String> header : asyncResult.getHeaders().entrySet()) {
                    if (!headers.containsKey(header.getKey())) {
                        headers.put(header.getKey(), header.getValue());
                    }
                }
                writeResponse(context, request, result, true, false);
            }

            @Override
            public void onFailure(Throwable t) {
                //We got a failure, handle it here

                // Check whether it's a HTTPException
                if (t instanceof HttpException) {
                    writeResponse(context, request, ((HttpException) t).toResult(), false, false);
                    return;
                }

                // Check if we have a mapper
                if (t instanceof Exception) {
                    ExceptionMapper mapper = accessor.getExceptionMapper((Exception) t);
                    if (mapper != null) {
                        writeResponse(context, request, mapper.toResult((Exception) t), false, false);
                        return;
                    }
                }

                writeResponse(context, request, Results.internalServerError(t), false, false);
            }
        }/*, MoreExecutors.directExecutor()*/);
        //TODO Which executor should we use here ?
    }

    private void writeResponse(
            ContextFromVertx context,
            RequestFromVertx request,
            Result result,
            boolean handleFlashAndSessionCookie,
            boolean closeConnection) {
        //Retrieve the renderable object.
        Renderable<?> renderable = result.getRenderable();
        if (renderable == null) {
            renderable = NoHttpBody.INSTANCE;
        }

        InputStream stream;
        boolean success = true;
        try {
            // Process the result, and apply serialization if required.
            stream = HttpUtils.processResult(accessor, context, renderable, result);
        } catch (Exception e) {
            LOGGER.error("Cannot render the response to " + request.uri(), e);
            stream = new ByteArrayInputStream(NoHttpBody.empty());
            success = false;
        }

        // If the content is too big or too small, disable encoding.
        // First get the length of the content, it can be either the length of the renderable object. If not set, we
        // have to check whether or not the length is given in the header.
        long length = renderable.length();
        if (length == 0 && result.getHeaders().get(HeaderNames.CONTENT_LENGTH) != null) {
            length = Long.valueOf(result.getHeaders().get(HeaderNames.CONTENT_LENGTH));
        }

        // Check whether the length is not in range.
        if (length != 0 && shouldEncodingBeDisabledForResponse(length, result)) {
            LOGGER.debug("Disabling encoding for {} - size ({} bytes) not in range",
                    request.path(), length);
            result.withoutCompression();
        }

        finalizeWriteReponse(context, request.getVertxRequest(),
                result, stream, success, handleFlashAndSessionCookie, closeConnection);
    }

    /**
     * This method must be called in a Vert.X context. It finalizes the response and send it to the client.
     *
     * @param context                     the HTTP context
     * @param request                     the Vert.x request
     * @param result                      the computed result
     * @param stream                      the stream of the result
     * @param success                     a flag indicating whether or not the request was successfully handled
     * @param handleFlashAndSessionCookie if the flash and session cookie need to be send with the response
     * @param closeConnection             whehter or not the (underlying) TCP connection must be closed
     */
    private void finalizeWriteReponse(
            final ContextFromVertx context,
            final HttpServerRequest request,
            Result result,
            InputStream stream,
            boolean success,
            boolean handleFlashAndSessionCookie,
            boolean closeConnection) {

        Renderable<?> renderable = result.getRenderable();
        if (renderable == null) {
            renderable = NoHttpBody.INSTANCE;
        }
        // Decide whether to close the connection or not.
        boolean keepAlive = HttpUtils.isKeepAlive(request);

        // Build the response object.
        final HttpServerResponse response = request.response();

        // Copy headers from the result
        for (Map.Entry<String, String> header : result.getHeaders().entrySet()) {
            response.putHeader(header.getKey(), header.getValue());
        }

        if (!result.getHeaders().containsKey(HeaderNames.SERVER)) {
            // Add the server metadata
            response.putHeader(HeaderNames.SERVER, SERVER_NAME);
        }

        String fullContentType = result.getFullContentType();
        if (fullContentType == null) {
            if (renderable.mimetype() != null) {
                response.putHeader(HeaderNames.CONTENT_TYPE, renderable.mimetype());
            }
        } else {
            response.putHeader(HeaderNames.CONTENT_TYPE, fullContentType);
        }

        // copy cookies / flash and session
        if (handleFlashAndSessionCookie) {
            context.flash().save(context, result);
            context.session().save(context, result);
        }

        // copy cookies
        for (org.wisdom.api.cookies.Cookie cookie : result.getCookies()) {
            // Encode cookies:
            final String encoded = ServerCookieEncoder.LAX.encode(
                    CookieHelper.convertWisdomCookieToNettyCookie(cookie));
            // Here we use the 'add' method to add a new value to the header.
            response.headers().add(HeaderNames.SET_COOKIE, encoded);
        }
        response.setStatusCode(HttpUtils.getStatusFromResult(result, success));
        if (renderable.mustBeChunked()) {
            LOGGER.debug("Building the chunked response for {} {} ({})", request.method(), request.uri(), context);
            if (renderable.length() > 0 && !response.headers().contains(HeaderNames.CONTENT_LENGTH)) {
                response.putHeader(HeaderNames.CONTENT_LENGTH, Long.toString(renderable.length()));
            }

            if (!response.headers().contains(HeaderNames.CONTENT_TYPE)) {
                // No content is not legal, set default to binary.
                response.putHeader(HeaderNames.CONTENT_TYPE, MimeTypes.BINARY);
            }

            // Can't determine the size, so switch to chunked.
            response.setChunked(true);
            response.putHeader(HeaderNames.TRANSFER_ENCODING, "chunked");
            // In addition, we can't keep the connection open.
            response.putHeader(HeaderNames.CONNECTION, "close");

            final AsyncInputStream s = new AsyncInputStream(vertx, accessor.getExecutor(), stream);
            s.setContext(context.vertxContext());
            final Pump pump = Pump.pump(s, response);
            s.endHandler(event -> context.vertxContext().runOnContext(event1 -> {
                        LOGGER.debug("Ending chunked response for {}", request.uri());
                        response.end();
                        response.close();
                        cleanup(context);
                    })
            );
            s.exceptionHandler(event -> context.vertxContext().runOnContext(event1 -> {
                        LOGGER.error("Cannot read the result stream", event1);
                        response.close();
                        cleanup(context);
                    })
            );
            context.vertxContext().runOnContext(event -> pump.start());

        } else {
            byte[] cont = new byte[0];
            try {
                cont = IOUtils.toByteArray(stream);
            } catch (IOException e) {
                LOGGER.error("Cannot copy the response to {}", request.uri(), e);
            }

            if (!response.headers().contains(HeaderNames.CONTENT_LENGTH)) {
                // Because of the HEAD implementation, if the length is already set, do not update it.
                // (HEAD would mean no content)
                response.putHeader(HeaderNames.CONTENT_LENGTH, Long.toString(cont.length));
            }

            if (keepAlive) {
                // Add keep alive header as per:
                // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
                response.putHeader(HeaderNames.CONNECTION, "keep-alive");
            }
            response.write(Buffer.buffer(cont));
            if (HttpUtils.isKeepAlive(request) && !closeConnection) {
                response.end();
            } else {
                response.end();
                response.close();
            }
            cleanup(context);
        }
    }

    private boolean shouldEncodingBeDisabledForResponse(long length, Result result) {
        return server.hasCompressionEnabled()
                && (
                length < server.getEncodingMinBound() // Too small
                        || length > server.getEncodingMaxBound() // Too big
        );
    }

}
