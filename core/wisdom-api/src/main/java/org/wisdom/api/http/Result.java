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

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.w3c.dom.Document;
import org.wisdom.api.bodies.*;
import org.wisdom.api.cookies.Cookie;
import org.wisdom.api.utils.DateUtil;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * A result is an object returned by a controller action.
 * It will be merge with the response.
 */
public class Result implements Status {

    /**
     * The status code.
     */
    private int statusCode;
    /**
     * The content.
     */
    private Renderable<?> content;
    /**
     * Something like: "utf-8" => will be appended to the content-type. eg
     * "text/html; charset=utf-8"
     */
    private Charset charset;
    /**
     * The headers.
     */
    private Map<String, String> headers;
    /**
     * The cookies.
     */
    private List<Cookie> cookies;

    /**
     * A result. Sets utf-8 as charset and status code by default.
     * Refer to {@link Status#OK}, {@link Status#NO_CONTENT} and so on
     * for some short cuts to predefined results.
     *
     * @param statusCode The status code to set for the result. Shortcuts to the code at: {@link Status#OK}
     */
    public Result(int statusCode) {
        this();
        this.statusCode = statusCode;
    }

    /**
     * A result. Sets utf-8 as charset and status code by default.
     * Refer to {@link Status#OK}, {@link Status#NO_CONTENT} and so on
     * for some short cuts to predefined results.
     */
    public Result() {
        this.headers = Maps.newHashMap();
        this.cookies = Lists.newArrayList();
    }

    /**
     * Gets the result's content.
     *
     * @return the content.
     */
    public Renderable<?> getRenderable() {
        return content;
    }

    /**
     * Sets this renderable as object to render. Usually this renderable
     * does rendering itself and will not call any templating engine.
     *
     * @param renderable The renderable that will handle everything after returning the result.
     * @return This result for chaining.
     */
    public Result render(Renderable<?> renderable) {
        this.content = renderable;
        return this;
    }

    /**
     * Sets the content of the current result to the given object.
     *
     * @param object the object
     * @return the current result
     */
    public Result render(Object object) {
        if (object instanceof Renderable) {
            this.content = (Renderable<?>) object;
        } else {
            this.content = new RenderableObject(object);
        }
        return this;
    }

    /**
     * Sets the content of the current result to the given object node. It also sets the content-type header to json
     * and the charset to UTF-8.
     *
     * @param node the content
     * @return the current result
     */
    public Result render(JsonNode node) {
        this.content = new RenderableJson(node);
        json();
        return this;
    }

    /**
     * Sets the content of the current result to the JSONP response using the given padding (callback). It also sets
     * the content-type header to JavaScript and the charset to UTF-8.
     *
     * @param node the content
     * @return the current result
     */
    public Result render(String padding, JsonNode node) {
        this.content = new RenderableJsonP(padding, node);
        setContentType(MimeTypes.JAVASCRIPT);
        charset = Charsets.UTF_8;
        return this;
    }

    /**
     * Sets the content of the current result to the given XML document. It also sets the content-type header to XML
     * and the charset to UTF-8.
     *
     * @param document the content
     * @return the current result
     */
    public Result render(Document document) {
        this.content = new RenderableXML(document);
        xml();
        return this;
    }

    /**
     * Sets the content of the current result to the given content.
     *
     * @param content the content
     * @return the current result
     */
    public Result render(String content) {
        this.content = new RenderableString(content);
        return this;
    }

    /**
     * Sets the content of the current result to the given content.
     *
     * @param content the content
     * @return the current result
     */
    public Result render(CharSequence content) {
        this.content = new RenderableString(content);
        return this;
    }

    /**
     * Sets the content of the current result to the given content.
     *
     * @param content the content
     * @return the current result
     */
    public Result render(StringBuilder content) {
        this.content = new RenderableString(content);
        return this;
    }

    /**
     * Sets the content of the current result to the given content.
     *
     * @param content the content
     * @return the current result
     */
    public Result render(StringBuffer content) {
        this.content = new RenderableString(content);
        return this;
    }

    /**
     * Gets the current value of the {@literal Content-Type} header.
     *
     * @return the current {@literal Content-Type} value, {@literal null} if not set
     */
    public String getContentType() {
        return headers.get(HeaderNames.CONTENT_TYPE);
    }

    /**
     * Sets the value of the {@literal Content-Type} header.
     *
     * @param contentType the value
     */
    private void setContentType(String contentType) {
        // The content type may contain the charset, parse it and set it.
        if (contentType != null && contentType.contains("charset=")) {
            charset = Charset.forName(contentType.substring(contentType.indexOf("charset=") + 8).trim());
        }
        headers.put(HeaderNames.CONTENT_TYPE, contentType);
    }

    /**
     * @return Charset of the current result that will be used. Will be "utf-8"
     * by default.
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * @return Set the charset of the result. Is "utf-8" by default.
     */
    public Result with(Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * @return the full content-type containing the mime type and the charset if set.
     */
    public String getFullContentType() {
        if (getContentType() == null) {
            // Will use the renderable content type.
            return null;
        }
        Charset localCharset = getCharset();
        if (localCharset == null || getContentType().contains("charset")) {
            return getContentType();
        } else {
            return getContentType() + "; charset=" + localCharset.displayName();
        }
    }

    /**
     * Sets the content type. Must not contain any WRONG charset:
     * "text/html; charset=utf8".
     * <p>
     * If you want to set the charset use method {@link Result#with(Charset)};
     *
     * @param contentType (without encoding) something like "text/html" or
     *                    "application/json", must not be {@literal null}.
     * @return the current result
     */
    public Result as(String contentType) {
        setContentType(contentType);
        return this;
    }

    /**
     * Gets the current headers.
     * All modification to the result modifies the result headers.
     *
     * @return the current headers
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Sets a header. If this header was already set, the value is overridden.
     *
     * @param headerName    the header name
     * @param headerContent the header value
     * @return the current result.
     */
    public Result with(String headerName, String headerContent) {
        headers.put(headerName, headerContent);
        return this;
    }

    /**
     * Returns cookie with that name or {@literal null}.
     *
     * @param cookieName Name of the cookie
     * @return The cookie or null if not found.
     */
    public Cookie getCookie(String cookieName) {

        for (Cookie cookie : getCookies()) {
            if (cookie.name().equals(cookieName)) {
                return cookie;
            }
        }

        return null;
    }

    /**
     * Gets the list of cookies.
     * Modifications to the returned list modified the result's cookie.
     *
     * @return the list of cookies
     */
    public List<Cookie> getCookies() {
        return cookies;
    }

    /**
     * Adds the given cookie to the current result.
     *
     * @param cookie the cookie
     * @return the current result
     */
    public Result with(Cookie cookie) {
        cookies.add(cookie);
        return this;
    }

    /**
     * Removes the given header, session or flash data or cookie (name) from the current result.
     * This method behaves as follows:
     * <ol>
     * <li>Check whether `name` is a header, if so removes it and returns</li>
     * <li>Check whether we have a current HTTP context, if so check whether `name` is a stored in the session or
     * in the flash. If so, it removes the data and returns
     * </li>
     * <li>Check whether `name` is a cookie, if so removes it and returns</li>
     * </ol>
     *
     * @param name the header, session key, flash key or cookie's name to remove
     * @return the current result
     */
    public Result without(String name) {
        String v = headers.remove(name);
        if (v == null) {
            Context context = current(false);
            if (context != null) {
                // Lookup into session and flash
                if (context.session().remove(name) == null) {
                    if (context.flash().remove(name)) {
                        return this;
                    }
                } else {
                    return this;
                }
            }
            // It may be a cookie
            discard(name);
        }
        return this;
    }

    public Result withoutCompression(){
        headers.put(HeaderNames.X_WISDOM_DISABLED_ENCODING_HEADER, "true");
        return this;
    }

    /**
     * Discards the given cookie. The cookie max-age is set to 0, so is going to be invalidated.
     *
     * @param name the name of the cookie
     * @return the current result
     */
    public Result discard(String name) {
        Cookie cookie = getCookie(name);
        if (cookie != null) {
            cookies.remove(cookie);
            cookies.add(Cookie.builder(cookie).setMaxAge(0).build());
        } else {
            cookies.add(Cookie.builder(name, "").setMaxAge(0).build());
        }
        return this;
    }

    /**
     * Discards the given cookies. For each cookie, the max-age is set fo 0, so is going to be invalidated.
     *
     * @param names the names of the cookies
     * @return the current result
     */
    public Result discard(String... names) {
        for (String n : names) {
            discard(n);
        }
        return this;
    }

    /**
     * Gets the result's status code.
     *
     * @return the status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Set the status of this result.
     * Refer to {@link Status#OK}, {@link Status#NO_CONTENT} and so on
     * for some short cuts to predefined results.
     *
     * @param statusCode The status code. Result ({@link Status#OK}) provides some helpers.
     * @return The result you executed the method on for method chaining.
     */
    public Result status(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    /**
     * A redirect that uses 303 - SEE OTHER.
     *
     * @param url The url used as redirect target.
     * @return A nicely configured result with status code 303 and the url set
     * as Location header.
     */
    public Result redirect(String url) {
        status(Status.SEE_OTHER);
        with(HeaderNames.LOCATION, url);
        return this;
    }

    /**
     * A redirect that uses 307 see other.
     *
     * @param url The url used as redirect target.
     * @return A nicely configured result with status code 307 and the url set
     * as Location header.
     */
    public Result redirectTemporary(String url) {
        status(Status.TEMPORARY_REDIRECT);
        with(HeaderNames.LOCATION, url);
        return this;
    }

    /**
     * Sets the content type of this result to {@link MimeTypes#HTML}.
     *
     * @return the same result where you executed this method on. But the content type is now {@link MimeTypes#HTML}.
     */
    public Result html() {
        setContentType(MimeTypes.HTML);
        charset = Charsets.UTF_8;
        return this;
    }

    /**
     * Sets the content type of this result to {@link MimeTypes#JSON}.
     *
     * @return the same result where you executed this method on. But the content type is now {@link MimeTypes#JSON}.
     */
    public Result json() {
        setContentType(MimeTypes.JSON);
        charset = Charsets.UTF_8;
        // If we already have a String content, we must set the type.
        // The renderable object checks whether or not the given String is a valid JSON string,
        // or if a transformation is required.
        if (getRenderable() instanceof RenderableString) {
            ((RenderableString) getRenderable()).setType(MimeTypes.JSON);
        }
        return this;
    }

    /**
     * Set the content type of this result to {@link MimeTypes#XML}.
     *
     * @return the same result where you executed this method on. But the content type is now {@link MimeTypes#XML}.
     */
    public Result xml() {
        setContentType(MimeTypes.XML);
        charset = Charsets.UTF_8;
        // If we already have a String content, we must set the type.
        // The renderable object checks whether or not the given String is a valid XML string,
        // or if a transformation is required.
        if (getRenderable() instanceof RenderableString) {
            ((RenderableString) getRenderable()).setType(MimeTypes.XML);
        }
        return this;
    }

    /**
     * This function sets
     * <p>
     * Cache-Control: no-cache, no-store
     * Date: (current date)
     * Expires: 1970
     * <p>
     * => it therefore effectively forces the browser and every proxy in between
     * not to cache content.
     * <p>
     * See also https://devcenter.heroku.com/articles/increasing-application-performance-with-http-cache-headers
     *
     * @return this result for chaining.
     */
    public Result noCache() {
        with(HeaderNames.CACHE_CONTROL, HeaderNames.NOCACHE_VALUE);
        with(HeaderNames.DATE, DateUtil.formatForHttpHeader(System.currentTimeMillis()));
        with(HeaderNames.EXPIRES, DateUtil.formatForHttpHeader(0L));
        return this;
    }

    /**
     * Sets the content of the current result to "No Content" if the result has no content set.
     *
     * @return the current result
     */
    public Result noContentIfNone() {
        if (content == null) {
            content = NoHttpBody.INSTANCE;
        }
        return this;
    }

    /**
     * Convenient method to retrieve the current HTTP context.
     *
     * @param fail whether or no we should fail (i.e. throw an {@link java.lang.IllegalStateException}) if there are
     *             no HTTP context
     * @return the HTTP context, {@code null} if none
     */
    private Context current(boolean fail) {
        Context context = Context.CONTEXT.get();
        if (context == null && fail) {
            throw new IllegalStateException("No context");
        }
        return context;
    }

    /**
     * Adds the given key-value pair to the current session. This method requires a current HTTP context. If none, a
     * {@link java.lang.IllegalStateException} is thrown.
     *
     * @param key   the key
     * @param value the value
     * @return the current result
     */
    public Result addToSession(String key, String value) {
        current(true).session().put(key, value);
        return this;
    }

    /**
     * Adds the given key-value pair to the current flash. This method requires a current HTTP context. If none, a
     * {@link java.lang.IllegalStateException} is thrown.
     *
     * @param key   the key
     * @param value the value
     * @return the current result
     */
    public Result addToFlash(String key, String value) {
        current(true).flash().put(key, value);
        return this;
    }
}
