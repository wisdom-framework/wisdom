package org.ow2.chameleon.wisdom.engine.servlet.cookies;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;

public class CookieHelper {
    private static final Logger log = LoggerFactory
            .getLogger(CookieHelper.class);

    public static Cookie getCookie(String name, Cookie[] cookies) {

        Cookie returnCookie = null;

        if (cookies != null) {

            for (Cookie cookie : cookies) {

                if (cookie.getName().equals(name)) {
                    returnCookie = cookie;
                    break;
                }
            }

        }
        return returnCookie;
    }

    public static String getCookieValue(String name, Cookie[] cookies) {
        Cookie cookie = getCookie(name, cookies);
        if (cookie != null) {
            return cookie.getValue();
        }
        return null;
    }

    public static Cookie convertWisdomCookieToServletCookie(org.ow2.chameleon.wisdom.api.cookies.Cookie cookie) {
        Cookie servletCookie = new Cookie(cookie.name(), cookie.value());
        servletCookie.setMaxAge(cookie.maxAge());
        if (cookie.comment() != null) {
            servletCookie.setComment(cookie.comment());
        }
        if (cookie.domain() != null) {
            servletCookie.setDomain(cookie.domain());
        }
        if (cookie.isSecure()) {
            servletCookie.setSecure(true);
        }
        if (cookie.path() != null) {
            servletCookie.setPath(cookie.path());
        }
        if (cookie.isHttpOnly()) {
            SERVLET_COOKIE_FALLBACK_HANDLER.setHttpOnly(servletCookie);
        }
        return servletCookie;
    }
    
    public static org.ow2.chameleon.wisdom.api.cookies.Cookie convertServletCookieToWisdomCookie(
            javax.servlet.http.Cookie cookie) {
        Preconditions.checkNotNull(cookie);
        org.ow2.chameleon.wisdom.api.cookies.Cookie.Builder builder
            = org.ow2.chameleon.wisdom.api.cookies.Cookie.builder(cookie.getName(), cookie.getValue());
        

        builder.setMaxAge(cookie.getMaxAge());
        
        if (cookie.getComment() != null) {
            builder.setComment(cookie.getComment());
        }
        
        if (cookie.getDomain() != null) {
            builder.setDomain(cookie.getDomain());
        }

        builder.setSecure(cookie.getSecure());
        
        if (cookie.getPath() != null) {
            builder.setPath(cookie.getPath());
        }
        
        
        boolean isHttpOnly = SERVLET_COOKIE_FALLBACK_HANDLER.isHttpOnly(cookie);        
        builder.setHttpOnly(isHttpOnly);
        
        
        return builder.build();
    }

    public static void setHttpOnly(Cookie cookie) {
        SERVLET_COOKIE_FALLBACK_HANDLER.setHttpOnly(cookie);
    }

    /**
     * HTTP only is only available in Servlet 3 spec.
     */
    private interface ServletCookieFallbackHandler {
        void setHttpOnly(Cookie cookie);
        boolean isHttpOnly(Cookie cookie);
    }

    private static class Servlet3CookieFallbackHandler implements ServletCookieFallbackHandler {
        @Override
        public void setHttpOnly(Cookie cookie) {
            cookie.setHttpOnly(true);
        }
        
        @Override
        public boolean isHttpOnly(Cookie cookie) {
            return cookie.isHttpOnly();
        }
    }

    private static class Servlet25CookieFallbackHandler implements ServletCookieFallbackHandler {
        
        private boolean warningAlreadyPrintedOut = false;
        
        @Override
        public void setHttpOnly(Cookie cookie) {
            printWarning();
        }
        
        @Override
        public boolean isHttpOnly(Cookie cookie) {
            printWarning();
            return false;
        }
        
        private void printWarning() {
            
            //don't pollute log.
            if (!warningAlreadyPrintedOut) {
                
                log.warn("Running inside Servlet 2.5 container. " +
                		"Ignoring HttpSecure and HttpOnly for now.");
                
                warningAlreadyPrintedOut = true;
            }
            
        }
    }

    private static final ServletCookieFallbackHandler SERVLET_COOKIE_FALLBACK_HANDLER;

    static {
        ServletCookieFallbackHandler httpOnlySetter;
        try {
            Cookie.class.getMethod("setHttpOnly", boolean.class);
            httpOnlySetter = new Servlet3CookieFallbackHandler();
        } catch (NoSuchMethodException e) {
            httpOnlySetter = new Servlet25CookieFallbackHandler();
        }
        SERVLET_COOKIE_FALLBACK_HANDLER = httpOnlySetter;
    }

}
