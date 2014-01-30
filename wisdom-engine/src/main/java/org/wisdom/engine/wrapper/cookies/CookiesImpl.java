package org.wisdom.engine.wrapper.cookies;

import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;

import java.util.Map;
import java.util.Set;

import org.wisdom.api.cookies.Cookie;
import org.wisdom.api.cookies.Cookies;

import com.google.common.collect.Maps;

/**
 * Implementation of cookies based on HTTP Servlet Cookies
 */
public class CookiesImpl implements Cookies {

    private Map<String, Cookie> cookies = Maps.newTreeMap();

    public CookiesImpl(HttpRequest request) {
        Set<io.netty.handler.codec.http.Cookie> localCookies;
        String value = request.headers().get(HttpHeaders.Names.COOKIE);
        if (value != null) {
            localCookies = CookieDecoder.decode(value);
            for (io.netty.handler.codec.http.Cookie cookie : localCookies) {
                this.cookies.put(cookie.getName(), CookieHelper.convertNettyCookieToWisdomCookie(cookie));
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
