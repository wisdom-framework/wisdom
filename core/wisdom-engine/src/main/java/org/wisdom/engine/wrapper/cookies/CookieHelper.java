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
package org.wisdom.engine.wrapper.cookies;

import io.netty.handler.codec.http.DefaultCookie;

import org.wisdom.api.cookies.Cookie;
import org.wisdom.api.cookies.Cookies;

import com.google.common.base.Preconditions;

public class CookieHelper {
    
    private static final String PATH = ", path=";
    
    private CookieHelper(){
        //Unused
    }

    public static Cookie getCookie(String name, Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.name().equals(name)) {
                    return cookie;
                }
            }
        }
        return null;
    }

    public static io.netty.handler.codec.http.Cookie getCookie(String name, io.netty.handler.codec.http.Cookie[] cookies) {
        if (cookies != null) {
            for (io.netty.handler.codec.http.Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie;
                }
            }
        }
        return null;
    }

    public static String getCookieValue(String name, io.netty.handler.codec.http.Cookie[] cookies) {
        io.netty.handler.codec.http.Cookie cookie = getCookie(name, cookies);
        if (cookie != null) {
            return cookie.getValue();
        }
        return null;
    }

    public static io.netty.handler.codec.http.Cookie convertWisdomCookieToNettyCookie(Cookie cookie) {
        io.netty.handler.codec.http.Cookie nettyCookie = new DefaultCookie(cookie.name(), cookie.value());
        nettyCookie.setMaxAge(cookie.maxAge());
        if (cookie.comment() != null) {
            nettyCookie.setComment(cookie.comment());
        }
        if (cookie.domain() != null) {
            nettyCookie.setDomain(cookie.domain());
        }
        if (cookie.isSecure()) {
            nettyCookie.setSecure(true);
        }
        if (cookie.path() != null) {
            nettyCookie.setPath(cookie.path());
        }
        if (cookie.isHttpOnly()) {
            nettyCookie.setHttpOnly(true);
        }
        return nettyCookie;
    }

    public static org.wisdom.api.cookies.Cookie convertNettyCookieToWisdomCookie(
            io.netty.handler.codec.http.Cookie cookie) {
        Preconditions.checkNotNull(cookie);
        String value = cookie.getValue();
        // Netty append some data at the end f the cookie:
        // -createdBy=wisdom&at=3+nov.+2013+11%3A52%3A15&___TS=1383475935779, path=/, maxAge=3600s, secure, HTTPOnly
        // We have to remove them
        // TODO Do we really need this ? It was probably related to the cookie encoding issue.
        if (value.contains(PATH)) {
            value = value.substring(0, value.indexOf(PATH));
        }

        org.wisdom.api.cookies.Cookie.Builder builder
                = org.wisdom.api.cookies.Cookie.builder(cookie.getName(), value);

        builder.setMaxAge(cookie.getMaxAge());

        if (cookie.getComment() != null) {
            builder.setComment(cookie.getComment());
        }

        if (cookie.getDomain() != null) {
            builder.setDomain(cookie.getDomain());
        }

        builder.setSecure(cookie.isSecure());

        if (cookie.getPath() != null) {
            builder.setPath(cookie.getPath());
        }

        builder.setHttpOnly(cookie.isHttpOnly());

        return builder.build();
    }

    public static String getCookieValue(String name, Cookies cookies) {
        Cookie c = cookies.get(name);
        if (c != null) {
            return c.value();
        }
        return null;
    }
}
