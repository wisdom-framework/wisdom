package org.ow2.chameleon.wisdom.engine.servlet.cookies;

import com.google.common.collect.Maps;
import org.ow2.chameleon.wisdom.api.cookies.Cookie;
import org.ow2.chameleon.wisdom.api.cookies.Cookies;

import javax.servlet.http.HttpServletRequest;
import java.util.TreeMap;

/**
 * Implementation of cookies based on HTTP Servlet Cookies
 */
public class CookiesImpl implements Cookies {

    private TreeMap<String, Cookie> cookies = Maps.newTreeMap();

    public CookiesImpl(HttpServletRequest request) {
        javax.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (javax.servlet.http.Cookie cookie : cookies) {
                this.cookies.put(cookie.getName(), CookieHelper.convertServletCookieToWisdomCookie(cookie));
            }
        }

    }

    /**
     * @param name Name of the cookie to retrieve
     * @return the cookie that is associated with the given name, or null if there is no such cookie
     */
    @Override
    public Cookie get(String name) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
