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

import akka.dispatch.OnComplete;
import io.netty.handler.codec.http.ServerCookieEncoder;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.streams.Pump;
import org.wisdom.api.bodies.NoHttpBody;
import org.wisdom.api.content.ContentCodec;
import org.wisdom.api.http.*;
import org.wisdom.api.router.Route;
import org.wisdom.framework.vertx.cookies.CookieHelper;
import scala.concurrent.Future;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Handles HTTP Request. Don't forget that request may arrive as chunk.
 */
public class HttpHandler implements Handler<HttpServerRequest> {

    /**
     * The server name.
     */
    private static final String SERVER_NAME = "Wisdom-Framework/" + BuildConstants.WISDOM_VERSION + " VertX/" +
            BuildConstants.VERTX_VERSION;

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpHandler.class);
    private final ServiceAccessor accessor;
    private final Vertx vertx;


    /**
     * Creates the handler.
     *
     * @param vertx    the vertx singleton
     * @param accessor the accessor
     */
    public HttpHandler(Vertx vertx, ServiceAccessor accessor) {
        this.accessor = accessor;
        this.vertx = vertx;
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
    public void handle(HttpServerRequest request) {
        LOGGER.debug("A request has arrived on the server : {} {}", request.method(), request.path());
        final ContextFromVertx context = new ContextFromVertx(vertx, accessor, request);
        request.endHandler(new VoidHandler() {
            public void handle() {
                // Notifies the context that the request has been read, we start the dispatching.
                context.ready();
                // Dispatch.
                dispatch(context, (RequestFromVertx) context.request());
            }
        });
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
        Route route = accessor.getRouter().getRouteFor(context.request().method(), context.path());
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
            AsyncResult result) {
        Future<Result> future = accessor.getSystem().dispatchResultWithContext(result.callable(), context);
        future.onComplete(new OnComplete<Result>() {
            /**
             * Called when the result is computed. It writes the response.
             *
             * @param failure the failure caught when the result was computed
             * @param result the successfully computed result.
             */
            public void onComplete(Throwable failure, Result result) {
                if (failure != null) {
                    //We got a failure, handle it here
                    writeResponse(context, request, Results.internalServerError(failure), false, true);
                } else {
                    // We got a result, write it here.
                    writeResponse(context, request, result, true, true);
                }
            }
        }, accessor.getSystem().fromThread());
    }

    private void writeResponse(
            ContextFromVertx context,
            RequestFromVertx request,
            Result result,
            boolean handleFlashAndSessionCookie,
            boolean fromAsync) {


        //Retrieve the renderable object.
        Renderable<?> renderable = result.getRenderable();
        if (renderable == null) {
            renderable = new NoHttpBody();
        }

        InputStream stream;
        boolean success = true;
        try {
            // Process the result, and apply serialization is required.
            stream = HttpUtils.processResult(accessor, context, renderable, result);
        } catch (Exception e) {
            LOGGER.error("Cannot render the response to " + request.uri(), e);
            stream = new ByteArrayInputStream(NoHttpBody.EMPTY);
            success = false;
        }

        if (accessor.getContentEngines().getContentEncodingHelper().shouldEncode(context, result, renderable)) {
            ContentCodec codec = null;

            for (String encoding :
                    accessor.getContentEngines().getContentEncodingHelper()
                            .parseAcceptEncodingHeader(context.request().getHeader(HeaderNames.ACCEPT_ENCODING))) {
                codec = accessor.getContentEngines().getContentCodecForEncodingType(encoding);
                if (codec != null) break;
            }

            // We found a codec.
            if (codec != null) {
                result.with(HeaderNames.CONTENT_ENCODING, codec.getEncodingType());
                proceedAsyncEncoding(context, request, codec, stream, result, success,
                        handleFlashAndSessionCookie,
                        fromAsync);
                return;
            }
            //No encoding possible, do the finalize
        }

        finalizeWriteReponse(context, request.getVertxRequest(),
                result, stream, success, handleFlashAndSessionCookie, fromAsync);
    }

    private void proceedAsyncEncoding(
            final ContextFromVertx httpContext,
            final RequestFromVertx request,
            final ContentCodec codec,
            final InputStream stream,
            final Result result,
            final boolean success,
            final boolean handleFlashAndSessionCookie,
            final boolean fromAsync) {

        vertx.runOnContext(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                InputStream is = null;
                try {
                    is = codec.encode(stream);
                    finalizeWriteReponse(httpContext, request.getVertxRequest(),
                            result, is, success, handleFlashAndSessionCookie, true);
                } catch (IOException e) {
                    e.printStackTrace();
                    //TODO Error handling here...
                }

            }
        });
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
     * @param fromAsync                   a flag indicating that the request was handled asynchronously
     */
    private void finalizeWriteReponse(
            final ContextFromVertx context,
            final HttpServerRequest request,
            Result result,
            InputStream stream,
            boolean success,
            boolean handleFlashAndSessionCookie,
            boolean fromAsync) {

        Renderable<?> renderable = result.getRenderable();
        if (renderable == null) {
            renderable = new NoHttpBody();
        }
        // Decide whether to close the connection or not.
        boolean keepAlive = HttpUtils.isKeepAlive(request);

        // Build the response object.
        final HttpServerResponse response = request.response();
        for (Map.Entry<String, String> header : result.getHeaders().entrySet()) {
            response.putHeader(header.getKey(), header.getValue());
        }

        if (!result.getHeaders().containsKey(HeaderNames.SERVER)) {
            // Add the server metadata
            response.putHeader(HeaderNames.SERVER, SERVER_NAME);
        }

        String fullContentType = result.getFullContentType();
        if (fullContentType == null) {
            response.putHeader(HeaderNames.CONTENT_TYPE, renderable.mimetype());
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
            final String encoded = ServerCookieEncoder.encode(CookieHelper.convertWisdomCookieToNettyCookie(cookie));
            // Here we use the 'add' method to add a new value to the header.
            response.headers().add(HeaderNames.SET_COOKIE, encoded);
        }
        response.setStatusCode(HttpUtils.getStatusFromResult(result, success));
        if (renderable.mustBeChunked()) {
            LOGGER.debug("Building the chunked response for {} {} ({})", request.method(), request.uri(), context);
            if (renderable.length() > 0 && !response.headers().contains(HeaderNames.CONTENT_LENGTH)) {
                response.putHeader(HeaderNames.CONTENT_LENGTH, Long.toString(renderable.length()));
            }

            if (! response.headers().contains(HeaderNames.CONTENT_TYPE)) {
                // No content is not legal, set default to binary.
                response.putHeader(HeaderNames.CONTENT_TYPE, MimeTypes.BINARY);
            }

            // Can't determine the size, so switch to chunked.
            response.setChunked(true);
            response.putHeader(HeaderNames.TRANSFER_ENCODING, "chunked");
            // In addition, we can't keep the connection open.
            response.putHeader(HeaderNames.CONNECTION, "close");

            final AsyncInputStream s = new AsyncInputStream(vertx, accessor.getSystem().system(), stream);
            final Pump pump = Pump.createPump(s, response);
            s.endHandler(new Handler<Void>() {
                             @Override
                             public void handle(Void event) {
                                 context.vertxContext().runOnContext(new Handler<Void>() {
                                     @Override
                                     public void handle(Void event) {
                                         response.end();
                                         response.close();
                                         cleanup(context);
                                     }
                                 });
                             }
                         }
            );
            s.exceptionHandler(new Handler<Throwable>() {
                                   @Override
                                   public void handle(Throwable event) {
                                       context.vertxContext().runOnContext(new Handler<Void>() {
                                           @Override
                                           public void handle(Void event) {
                                               LOGGER.error("Cannot read the result stream", event);
                                               response.close();
                                               cleanup(context);
                                           }
                                       });
                                   }
                               }
            );
            context.vertxContext().runOnContext(new Handler<Void>() {
                @Override
                public void handle(Void event) {
                    pump.start();
                }
            });

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
            response.write(new Buffer(cont));
            if (HttpUtils.isKeepAlive(request)) {
                response.end();
            } else {
                response.end();
                response.close();
            }
            cleanup(context);
        }
    }

}
