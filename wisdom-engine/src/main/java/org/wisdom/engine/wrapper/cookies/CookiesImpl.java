package org.wisdom.engine.wrapper.cookies;

import com.google.common.collect.Maps;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import org.wisdom.api.cookies.Cookie;
import org.wisdom.api.cookies.Cookies;

import java.util.Set;
import java.util.TreeMap;

/**
 * Implementation of cookies based on HTTP Servlet Cookies
 */
public class CookiesImpl implements Cookies {

    private TreeMap<String, Cookie> cookies = Maps.newTreeMap();

    public CookiesImpl(HttpRequest request) {
        Set<io.netty.handler.codec.http.Cookie> cookies;
        String value = request.headers().get(HttpHeaders.Names.COOKIE);
        if (value != null) {
            cookies = CookieDecoder.decode(value);
            for (io.netty.handler.codec.http.Cookie cookie : cookies) {
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
