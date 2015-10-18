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

import com.google.common.base.Preconditions;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import org.wisdom.api.cookies.Cookie;
import org.wisdom.api.cookies.Cookies;

/**
 * Utility method to handle cookies.
 */
public class CookieHelper {

    private static final String PATH = ", path=";

    private CookieHelper() {
        //Unused
    }

    /**
     * Converts the Wisdom's cookie to a Netty's cookie.
     *
     * @param cookie the Wisdom's cookie
     * @return the Netty's cookie with the same metadata and content than the input cookie.
     */
    public static DefaultCookie convertWisdomCookieToNettyCookie(Cookie cookie) {
        DefaultCookie nettyCookie = new DefaultCookie(cookie.name(), cookie.value());
        nettyCookie.setMaxAge(cookie.maxAge());
        // Comments are not supported anymore by netty.
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

    /**
     * Converts the Netty's cookie to a Wisdom's cookie.
     *
     * @param cookie the Netty's cookie
     * @return the Wisdom's cookie with the same metadata and content than the input cookie.
     */
    public static Cookie convertNettyCookieToWisdomCookie(DefaultCookie cookie) {
        Preconditions.checkNotNull(cookie);
        String value = cookie.value();
//        // Netty append some data at the end f the cookie:
//        // -createdBy=wisdom&at=3+nov.+2013+11%3A52%3A15&___TS=1383475935779, path=/, maxAge=3600s, secure, HTTPOnly
//        // We have to remove them
//        // TODO Do we really need this ? It was probably related to the cookie encoding issue.
//        if (value.contains(PATH)) {
//            value = value.substring(0, value.indexOf(PATH));
//        }

        Cookie.Builder builder
                = Cookie.builder(cookie.name(), value);

        builder.setMaxAge(cookie.maxAge());

        if (cookie.domain() != null) {
            builder.setDomain(cookie.domain());
        }

        builder.setSecure(cookie.isSecure());

        if (cookie.path() != null) {
            builder.setPath(cookie.path());
        }

        builder.setHttpOnly(cookie.isHttpOnly());

        return builder.build();
    }

    /**
     * Gets the value of the cookie having the given name. The cookie is looked from the given cookies set.
     *
     * @param name    the name of the cookie
     * @param cookies the set of cookie
     * @return the value of the cookie, {@literal null} if there are no cookie with the given name in the cookies set.
     */
    public static String getCookieValue(String name, Cookies cookies) {
        Cookie c = cookies.get(name);
        if (c != null) {
            return c.value();
        }
        return null;
    }
}
