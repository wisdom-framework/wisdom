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
import org.apache.commons.io.IOUtils;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.http.HttpVersion;
import org.vertx.java.core.streams.Pump;
import org.wisdom.akka.AkkaSystemService;
import org.wisdom.api.bodies.NoHttpBody;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.content.ContentCodec;
import org.wisdom.api.content.ContentEngine;
import org.wisdom.api.content.ContentSerializer;
import org.wisdom.api.crypto.Crypto;
import org.wisdom.api.http.*;
import org.wisdom.api.http.Context;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.Router;
import scala.concurrent.Future;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.Executors;


/**
 * Created by clement on 20/07/2014.
 */
@Component
@Instantiate
public class WisdomVertxServer {

    private static final String SERVER_NAME = "Wisdom-Framework/" + BuildConstants.WISDOM_VERSION + " VertX/" +
            BuildConstants.VERTX_VERSION_KEY;


    private final static Logger LOGGER = LoggerFactory.getLogger(WisdomVertxServer.class);

    private HttpServer server;

    @Requires
    private Vertx vertx;

    @Requires
    private Router router;

    @Requires
    private ApplicationConfiguration configuration;

    @Requires
    private Crypto crypto;

    @Requires
    private ContentEngine engine;

    @Requires
    private AkkaSystemService system;

    final ServiceAccessor accessor = new ServiceAccessor(crypto, configuration, router, engine, system);

    @Validate
    public void start() {
        LOGGER.info("Starting the vert.x server");
        //TODO The asynchronous start may interfere with the port selection.
        server = vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest request) {
                LOGGER.info("A request has arrived on the server : {} {}", request.method(), request.path());
                final ContextFromVertx context = new ContextFromVertx(accessor, request);
                request.endHandler(new VoidHandler() {
                    public void handle() {
                        context.ready();
                        boolean isAsync = dispatch(context, (RequestFromVertx) context.request());
                        if (!isAsync) {
                            cleanup(context);
                        }
                    }
                });

            }
        }).listen(8080);

    }

    private void cleanup(ContextFromVertx context) {
        // Release all resources, especially uploaded file.
        if (context != null) {
            context.cleanup();
        }
        Context.CONTEXT.remove();
    }

    private boolean dispatch(ContextFromVertx context, RequestFromVertx request) {
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

            // If we open a websocket in the same request, just ignore it.
// TODO
//            if (handshaker != null) {
//                return false;
//            }
            result = Results.notFound();
        } else {
            // 3.2 : route found
            context.route(route);
            result = invoke(route);

            // We have this weird case where we don't have controller (unbound), but are just there to complete the
            // websocket handshake.
// TODO
//            if (route.isUnbound() && handshaker != null) {
//                return false;
//            }

            if (result instanceof AsyncResult) {
                // Asynchronous operation in progress.
                handleAsyncResult(context, request, (AsyncResult) result);
                return true;
            }
        }

        // Synchronous processing.
        try {
            return writeResponse(context, request, result, true, false);
        } catch (Exception e) {
            LOGGER.error("Cannot write response", e);
            result = Results.internalServerError(e);
            try {
                return writeResponse(context, request, result, false, false);
            } catch (Exception e1) {
                LOGGER.error("Cannot even write the error response...", e1);
                // Ignore.
            }
        }
        // If we reach this point, it means we did not write anything... Annoying.
        return false;
    }

    @Invalidate
    public void stop() {
        LOGGER.info("Stopping the vert.x server");
        server.close();
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

    private InputStream processResult(Context context, Result result) throws Exception {
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

    private boolean writeResponse(
            ContextFromVertx context,
            RequestFromVertx request,
            Result result,
            boolean handleFlashAndSessionCookie,
            boolean fromAsync) {
        //TODO Refactor this method.

        LOGGER.info("Writing response for " + result);
        // Render the result.
        InputStream stream;
        boolean success = true;
        Renderable<?> renderable = result.getRenderable();
        if (renderable == null) {
            renderable = new NoHttpBody();
        }
        try {
            stream = processResult(context, result);
        } catch (Exception e) {
            LOGGER.error("Cannot render the response to " + request.uri(), e);
            stream = new ByteArrayInputStream(NoHttpBody.EMPTY);
            success = false;
        }

        if (accessor.getContentEngines().getContentEncodingHelper().shouldEncode(context, result, renderable)) {
            ContentCodec codec = null;

            for (String encoding : accessor.getContentEngines().getContentEncodingHelper().parseAcceptEncodingHeader(context.request().getHeader(HeaderNames.ACCEPT_ENCODING))) {
                codec = accessor.getContentEngines().getContentCodecForEncodingType(encoding);
                if (codec != null) break;
            }

            if (codec != null) { // Encode Async
                result.with(HeaderNames.CONTENT_ENCODING, codec.getEncodingType());
                proceedAsyncEncoding(context, request, codec, stream, result, success,
                        handleFlashAndSessionCookie,
                        fromAsync);
                return true;
            }
            //No encoding possible, do the finalize
        }

        return finalizeWriteReponse(context, request.getVertxRequest(),
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
                    //TODO Error handling here...
                }

            }
        });
    }

    private boolean finalizeWriteReponse(
            ContextFromVertx context,
            HttpServerRequest request,
            Result result,
            InputStream stream,
            boolean success,
            boolean handleFlashAndSessionCookie,
            boolean fromAsync) {

        LOGGER.info("Finalizing the response");
        Renderable<?> renderable = result.getRenderable();
        if (renderable == null) {
            renderable = new NoHttpBody();
        }
        // Decide whether to close the connection or not.
        boolean keepAlive = isKeepAlive(request);

        // Build the response object.
        final HttpServerResponse response = request.response();
        for (Map.Entry<String, String> header : result.getHeaders().entrySet()) {
            response.putHeader(header.getKey(), header.getValue());
        }

        if (!result.getHeaders().containsKey(HeaderNames.SERVER)) {
            // Add the server metadata
            response.headers().set(HeaderNames.SERVER, SERVER_NAME);
        }

        String fullContentType = result.getFullContentType();
        if (fullContentType == null) {
            response.headers().set(HeaderNames.CONTENT_TYPE, renderable.mimetype());
        } else {
            response.headers().set(HeaderNames.CONTENT_TYPE, fullContentType);
        }


        boolean isChunked = renderable.mustBeChunked();

        byte[] cont = new byte[0];

        if (isChunked) {
            LOGGER.info("Building the chunked response");
            response.setStatusCode(getStatusFromResult(result, success));
            if (renderable.length() > 0) {
                response.putHeader(HeaderNames.CONTENT_LENGTH, Long.toString(renderable.length()));
            }
            // Can't determine the size, so switch to chunked.
            response.setChunked(true);
            response.putHeader(HeaderNames.TRANSFER_ENCODING, "chunked");
            // In addition, we can't keep the connection open.
            response.putHeader(HeaderNames.CONNECTION, "close");

            //TODO Which executor do we want to use ?
            final AsyncInputStream s = new AsyncInputStream(vertx, Executors.newSingleThreadExecutor(), stream);
            s.endHandler(new Handler<Void>() {
                             @Override
                             public void handle(Void event) {
                                 System.out.println("Closing, " + s.transferredBytes());
                                 response.end();
                             }
                         }
            );
            s.exceptionHandler(new Handler<Throwable>() {
                                   @Override
                                   public void handle(Throwable event) {
                                       event.printStackTrace();
                                       response.end();
                                   }
                               }
            );
            Pump.createPump(s, response).start();
        } else {
            LOGGER.info("Building the response");
            response.setStatusCode(getStatusFromResult(result, success));
            try {
                cont = IOUtils.toByteArray(stream);
            } catch (IOException e) {
                LOGGER.error("Cannot copy the response to " + request.uri(), e);
            }
            response.putHeader(HeaderNames.CONTENT_LENGTH, Long.toString(cont.length));
            if (keepAlive) {
                // Add keep alive header as per:
                // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
                response.putHeader(HeaderNames.CONNECTION, "keep-alive");
            }
            LOGGER.info("Writing " + cont.length);
            response.write(new Buffer(cont));
            response.close();
        }


        // copy cookies / flash and session
        //TODO
//        if (handleFlashAndSessionCookie) {
//            httpContext.flash().save(httpContext, result);
//            httpContext.session().save(httpContext, result);
//        }

//        // copy cookies
//        for (org.wisdom.api.cookies.Cookie cookie : result.getCookies()) {
//            // Encode cookies:
//            final String encode = ServerCookieEncoder.encode(CookieHelper.convertWisdomCookieToNettyCookie(cookie));
//            response.headers().add(SET_COOKIE, encode);
//        }

        // Send the response and close the connection if necessary.
//        response.closeHandler(new Handler<Void>() {
//            @Override
//            public void handle(Void event) {
//                IOUtils.closeQuietly(content);
//            }
//        });


        if (fromAsync) {
            cleanup(context);
        }

        return false;
    }

    public static boolean isKeepAlive(HttpServerRequest request) {
        String connection = request.headers().get("Connection");
        if (connection != null && connection.equalsIgnoreCase("close")) {
            return false;
        }

        if (request.version() == HttpVersion.HTTP_1_1) {
            return !"close".equalsIgnoreCase(connection);
        } else {
            return "keep-alive".equalsIgnoreCase(connection);
        }
    }

    private int getStatusFromResult(Result result, boolean success) {
        if (!success) {
            return Status.BAD_REQUEST;
        } else {
            return result.getStatusCode();
        }
    }

}
