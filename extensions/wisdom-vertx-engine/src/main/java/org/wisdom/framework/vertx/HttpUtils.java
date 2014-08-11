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

import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpVersion;
import org.wisdom.api.content.ContentSerializer;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.Renderable;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Status;

import java.io.InputStream;

/**
 * Created by clement on 11/08/2014.
 */
public class HttpUtils {
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

    static int getStatusFromResult(Result result, boolean success) {
        if (!success) {
            return Status.BAD_REQUEST;
        } else {
            return result.getStatusCode();
        }
    }

    static InputStream processResult(ServiceAccessor accessor, Context context, Renderable renderable,
                                     Result result) throws Exception {
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
}
