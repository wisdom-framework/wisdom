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
package org.wisdom.api.http;

import org.wisdom.api.cookies.Cookie;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The HTTP response.
 */
public class Response implements HeaderNames {


    private final Map<String,String> headers = new HashMap<String,String>();
    private final List<Cookie> cookies = new ArrayList<Cookie>();
    private final OutputStream stream = new ByteArrayOutputStream();

    /**
     * Adds a new header to the response.
     *
     * @param name The name of the header. Must not be null.
     * @param value The value of the header. Must not be null.
     */
    public void setHeader(String name, String value) {
        this.headers.put(name, value);
    }

    /**
     * Gets the current response headers.
     */
    public Map<String,String> getHeaders() {
        return headers;
    }

    /**
     * Sets the content-type of the response.
     *
     * @param contentType The content type.  Must not be null.
     */
    public void setContentType(String contentType) {
        setHeader(CONTENT_TYPE, contentType);
    }

    /**
     * Set a new transient cookie with path “/”<br />
     * For example:
     * <pre>
     * response().setCookie("theme", "blue");
     * </pre>
     * @param name Cookie name.  Must not be null.
     * @param value Cookie value.
     */
    public void setCookie(String name, String value) {
        setCookie(name, value, null); //NOSONAR
    }

    /**
     * Set a new cookie with path “/”
     * @param name Cookie name.  Must not be null.
     * @param value Cookie value.
     * @param maxAge Cookie duration (null for a transient cookie and 0 or less for a cookie that expires now).
     */
    public void setCookie(String name, String value, Integer maxAge) {
        setCookie(name, value, maxAge, "/");
    }

    /**
     * Set a new cookie
     * @param name Cookie name.  Must not be null.
     * @param value Cookie value
     * @param maxAge Cookie duration (null for a transient cookie and 0 or less for a cookie that expires now)
     * @param path Cookie path
     */
    public void setCookie(String name, String value, Integer maxAge, String path) {
        setCookie(name, value, maxAge, path, null);
    }

    /**
     * Set a new cookie
     * @param name Cookie name.  Must not be null.
     * @param value Cookie value
     * @param maxAge Cookie duration (null for a transient cookie and 0 or less for a cookie that expires now)
     * @param path Cookie path
     * @param domain Cookie domain
     */
    public void setCookie(String name, String value, Integer maxAge, String path, String domain) {
        setCookie(name, value, maxAge, path, domain, false, false);
    }

    /**
     * Set a new cookie
     * @param name Cookie name.  Must not be null.
     * @param value Cookie value
     * @param maxAge Cookie duration (null for a transient cookie and 0 or less for a cookie that expires now)
     * @param path Cookie path
     * @param domain Cookie domain
     * @param secure Whether the cookie is secured (for HTTPS requests)
     * @param httpOnly Whether the cookie is HTTP only (i.e. not accessible from client-side JavaScript code)
     */
    public void setCookie(String name, String value, Integer maxAge, String path, String domain, boolean secure, boolean httpOnly) {
        cookies.add(new Cookie(name, value, "", domain, maxAge, path, secure, httpOnly));
    }

    /**
     * Discard cookies along this result<br />
     * For example:
     * <pre>
     * response().discardCookies("theme");
     * </pre>
     *
     * This only discards cookies on the default path ("/") with no domain and that didn't have secure set.  To
     * discard other cookies, use the discardCookie method.
     *
     * @param names Names of the cookies to discard.  Must not be null.
     * @deprecated Use the discardCookie methods instead
     */
    @Deprecated
    public void discardCookies(String... names) {
        for (String name: names) {
            discardCookie(name);
        }
    }

    /**
     * Discard a cookie on the default path ("/") with no domain and that's not secure
     *
     * @param name The name of the cookie to discard.  Must not be null.
     */
    public void discardCookie(String name) {
        discardCookie(name, "/", null, false);
    }

    /**
     * Discard a cookie on the give path with no domain and not that's secure
     *
     * @param name The name of the cookie to discard.  Must not be null.
     * @param path The path of the cookie te discard, may be null
     */
    public void discardCookie(String name, String path) {
        discardCookie(name, path, null, false);
    }

    /**
     * Discard a cookie on the given path and domain that's not secure
     *
     * @param name The name of the cookie to discard.  Must not be null.
     * @param path The path of the cookie te discard, may be null
     * @param domain The domain of the cookie to discard, may be null
     */
    public void discardCookie(String name, String path, String domain) {
        discardCookie(name, path, domain, false);
    }

    /**
     * Discard a cookie in this result
     *
     * @param name The name of the cookie to discard.  Must not be null.
     * @param path The path of the cookie te discard, may be null
     * @param domain The domain of the cookie to discard, may be null
     * @param secure Whether the cookie to discard is secure
     */
    public void discardCookie(String name, String path, String domain, boolean secure) {
        cookies.add(new Cookie(name, "", "", domain, -86400, path, secure, false));
    }

    public Map<String, Cookie> cookies() {
        Map<String, Cookie> map = new HashMap<String, Cookie>();
        for (Cookie cookie : cookies) {
            map.put(cookie.name(), cookie);
        }
        return map;
    }

    /**
     * Gets the output stream of the response. Used to write the response body.
     * @return the output stream
     */
    public OutputStream stream() {
        return stream;
    }
}
