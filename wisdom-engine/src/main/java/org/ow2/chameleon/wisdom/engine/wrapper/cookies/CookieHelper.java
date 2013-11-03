package org.ow2.chameleon.wisdom.engine.wrapper.cookies;

import com.google.common.base.Preconditions;
import io.netty.handler.codec.http.DefaultCookie;
import org.ow2.chameleon.wisdom.api.cookies.Cookie;
import org.ow2.chameleon.wisdom.api.cookies.Cookies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CookieHelper {
    private static final Logger log = LoggerFactory
            .getLogger(CookieHelper.class);

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

    public static org.ow2.chameleon.wisdom.api.cookies.Cookie convertNettyCookieToWisdomCookie(
            io.netty.handler.codec.http.Cookie cookie) {
        Preconditions.checkNotNull(cookie);
        String value = cookie.getValue();
        // Netty append some data at the end f the cookie:
        // -createdBy=wisdom&at=3+nov.+2013+11%3A52%3A15&___TS=1383475935779, path=/, maxAge=3600s, secure, HTTPOnly
        // We have to remove them
        if (value.contains(", path=")) {
            value = value.substring(0, value.indexOf(", path="));
        }

        org.ow2.chameleon.wisdom.api.cookies.Cookie.Builder builder
                = org.ow2.chameleon.wisdom.api.cookies.Cookie.builder(cookie.getName(), value);

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
