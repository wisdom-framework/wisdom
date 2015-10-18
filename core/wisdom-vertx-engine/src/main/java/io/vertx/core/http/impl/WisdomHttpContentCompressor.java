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

import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpResponse;
import org.wisdom.api.http.HeaderNames;

/**
 * Extends the {@link HttpContentCompressor} to check whether or not the compression is disabled.
 * If so, it skip the compression step.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class WisdomHttpContentCompressor extends HttpContentCompressor {

    @Override
    protected Result beginEncode(HttpResponse response, String acceptEncoding) throws Exception {
        String disabled = response.headers().get(HeaderNames.X_WISDOM_DISABLED_ENCODING_HEADER);
        if ("true".equals(disabled)) {
            response.headers().remove(HeaderNames.X_WISDOM_DISABLED_ENCODING_HEADER);
            return null;
        }
        return super.beginEncode(response, acceptEncoding);
    }
}
