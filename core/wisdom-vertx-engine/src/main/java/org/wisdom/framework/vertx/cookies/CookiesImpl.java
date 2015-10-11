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
package org.wisdom.framework.vertx.cookies;

import com.google.common.collect.Maps;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.vertx.core.http.HttpServerRequest;
import org.wisdom.api.cookies.Cookie;
import org.wisdom.api.cookies.Cookies;

import java.util.Map;
import java.util.Set;

/**
 * Implementation of cookies based on HTTP Cookies.
 */
public class CookiesImpl implements Cookies {

    private Map<String, Cookie> cookies = Maps.newTreeMap();

    public CookiesImpl(HttpServerRequest request) {
        Set<io.netty.handler.codec.http.cookie.Cookie> localCookies;
        String value = request.headers().get(HttpHeaders.Names.COOKIE);
        if (value != null) {
            localCookies = ServerCookieDecoder.LAX.decode(value);
            for (io.netty.handler.codec.http.cookie.Cookie cookie : localCookies) {
                this.cookies.put(cookie.name(), CookieHelper.convertNettyCookieToWisdomCookie((DefaultCookie) cookie));
            }
        }
    }

    /**
     * @param name Name of the cookie to retrieve
     * @return the cookie that is associated with the given name, or null if there is no such cookie
     */
    @Override
    public Cookie get(String name) {
        return cookies.get(name);
    }
}
