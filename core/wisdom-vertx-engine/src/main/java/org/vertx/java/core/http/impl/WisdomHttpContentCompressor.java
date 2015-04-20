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
/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.vertx.java.core.http.impl;

import io.netty.handler.codec.http.*;
import org.wisdom.api.http.HeaderNames;

/**
 * Compresses an {@link HttpMessage} and an {@link HttpContent} in {@code gzip} or
 * {@code deflate} encoding while respecting the {@code "Accept-Encoding"} header.
 * If there is no matching encoding, no compression is done.  For more
 * information on how this handler modifies the message, please refer to
 * {@link HttpContentEncoder}.
 * <p>
 * <p>
 * This class is a copy from the Netty class, extended with a way to disabled encoding when the HTTP handler decide to.
 */
public class WisdomHttpContentCompressor extends HttpContentCompressor {

    /**
     * Creates a new handler with the default compression level (<tt>6</tt>),
     * default window size (<tt>15</tt>) and default memory level (<tt>8</tt>).
     */
    public WisdomHttpContentCompressor() {
        super(6);
    }

    /**
     * Begins the encoding. This method checks whether the encoding should be disabled, and if so return {@code null}.
     * Otherwise, is calls {@link HttpContentCompressor#beginEncode(HttpResponse, String)}.
     *
     * @param response       the response
     * @param acceptEncoding the {@code ACCEPT-ENCODING} header value
     * @return the result, or {@code null} if the encoding is disabled
     * @throws Exception encoding fails
     */
    @Override
    protected Result beginEncode(HttpResponse response, String acceptEncoding) throws Exception {
        String disabledEncoding = response.headers().get(HeaderNames.X_WISDOM_DISABLED_ENCODING_HEADER);
        if (disabledEncoding != null) {
            response.headers().remove(HeaderNames.X_WISDOM_DISABLED_ENCODING_HEADER);
            return null;
        }
        return super.beginEncode(response, acceptEncoding);

    }
}
