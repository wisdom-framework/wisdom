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

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpVersion;
import org.slf4j.LoggerFactory;
import org.wisdom.api.bodies.NoHttpBody;
import org.wisdom.api.content.ContentSerializer;
import org.wisdom.api.http.*;

import java.io.InputStream;

/**
 * A set of utility methods used to handle HTTP requests.
 */
public class HttpUtils {

    /**
     * The 'CLOSE' connection value.
     */
    public static final String CLOSE = "close";

    /**
     * The 'KEEP-ALIVE' connection value.
     */
    public static final String KEEP_ALIVE = "keep-alive";

    /**
     * Checks whether the given request should be closed or not once completed.
     *
     * @param request the request
     * @return {@code true} if the connection is marked as {@literal keep-alive}, and so must not be closed. {@code
     * false} otherwise. Notice that if not set in the request, the default value depends on the HTTP version.
     */
    public static boolean isKeepAlive(HttpServerRequest request) {
        String connection = request.headers().get(HeaderNames.CONNECTION);
        if (connection != null && connection.equalsIgnoreCase(CLOSE)) {
            return false;
        }
        if (request.version() == HttpVersion.HTTP_1_1) {
            return !CLOSE.equalsIgnoreCase(connection);
        } else {
            return KEEP_ALIVE.equalsIgnoreCase(connection);
        }
    }

    /**
     * Gets the HTTP Status (code) for the given result and indication on a state of failure.
     *
     * @param result  the result
     * @param success whether or not the result was computed correctly.
     * @return the HTTP code, {@link Status#BAD_REQUEST} if {@literal success} is {@code false}.
     */
    public static int getStatusFromResult(Result result, boolean success) {
        if (!success) {
            return Status.BAD_REQUEST;
        } else {
            return result.getStatusCode();
        }
    }

    /**
     * Processes the given result. This method returns either the "rendered renderable",
     * but also applies required serialization if any.
     *
     * @param accessor   the service accessor
     * @param context    the current HTTP context
     * @param renderable the renderable object
     * @param result     the computed result
     * @return the stream of the result
     * @throws Exception if the result cannot be rendered.
     */
    public static InputStream processResult(ServiceAccessor accessor, Context context, Renderable renderable,
                                            Result result) throws Exception {
        if (renderable.requireSerializer()) {
            ContentSerializer serializer = null;
            if (result.getContentType() != null) {
                serializer = accessor.getContentEngines().getContentSerializerForContentType(result
                        .getContentType());
            }
            if (serializer == null) {
                // Try with the Accept type
                serializer = accessor.getContentEngines().getBestSerializer(context.request().mediaTypes());
                if (serializer != null) {
                    // Set CONTENT_TYPE
                    result.with(HeaderNames.CONTENT_TYPE, serializer.getContentType());
                }
            }

            if (serializer != null) {
                serializer.serialize(renderable);
            } else {
                LoggerFactory.getLogger(HttpHandler.class)
                        .error("Cannot find a serializer to handle the request (explicit content type: {}, " +
                                        "accept media types: {}), returning content as String",
                                result.getContentType(),
                                context.request().mediaTypes());
                if (renderable.content() != null) {
                    renderable.setSerializedForm(renderable.content().toString());
                    result.with(HeaderNames.CONTENT_TYPE, "text/plain");
                } else {
                    renderable = NoHttpBody.INSTANCE;
                    result.with(HeaderNames.CONTENT_TYPE, "text/plain");
                }
            }
        }
        return renderable.render(context, result);
    }

    /**
     * A http content type should contain a character set like
     * "application/json; charset=utf-8".
     * <p>
     * If you only want to get "application/json" you can use this method.
     * <p>
     * See also: http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.7.1
     *
     * @param rawContentType "application/json; charset=utf-8" or "application/json"
     * @return only the contentType without charset. Eg "application/json"
     */
    public static String getContentTypeFromContentTypeAndCharacterSetting(String rawContentType) {
        if (rawContentType.contains(";")) {
            return rawContentType.split(";")[0];
        } else {
            return rawContentType;
        }
    }

    /**
     * Checks whether the current request is either using the "POST" or "PUT" HTTP methods. This method let checks if
     * the request can except a {@literal multipart} body or not.
     *
     * @param request the request
     * @return {@code true} if the request use either "POST" or "PUT", {@code false} otherwise.
     */
    public static boolean isPostOrPut(HttpServerRequest request) {
        return request.method().name().equalsIgnoreCase(HttpMethod.POST.name())
                || request.method().name().equalsIgnoreCase(HttpMethod.PUT.name());
    }
}
