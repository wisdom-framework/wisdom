package org.wisdom.api.http;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.wisdom.api.bodies.NoHttpBody;
import org.wisdom.api.bodies.RenderableJson;
import org.wisdom.api.bodies.RenderableObject;
import org.wisdom.api.bodies.RenderableString;
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
    public Result render(Renderable renderable) {
        this.content = renderable;
        return this;
    }

    public Result render(Object object) {
        if (object instanceof Renderable) {
            this.content = (Renderable) object;
        } else {
            this.content = new RenderableObject(object);
        }
        return this;
    }

    public Result render(Exception e) {
        this.content = new RenderableObject(e);
        return this;
    }

    /**
     *
     */
    public Result render(ObjectNode node) {
        this.content = new RenderableJson(node);
        return this;
    }

    /**
     *
     */
    public Result render(String content) {
        this.content = new RenderableString(content);
        return this;
    }

    public Result render(CharSequence content) {
        this.content = new RenderableString(content);
        return this;
    }

    public Result render(StringBuilder content) {
        this.content = new RenderableString(content);
        return this;
    }

    public Result render(StringBuffer content) {
        this.content = new RenderableString(content);
        return this;
    }

    public String getContentType() {
        return headers.get(HeaderNames.CONTENT_TYPE);
    }
    
    private void setContentType(String contentType){
        headers.put(HeaderNames.CONTENT_TYPE, contentType);
    }

    /**
     * @return Charset of the current result that will be used. Will be "utf-8"
     *         by default.
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
            return null; // Will use the renderable content type.
        }
        Charset charset = getCharset();
        if (charset == null) {
            return getContentType();
        } else {
            return getContentType() + "; " + charset.displayName();
        }
    }

    /**
     * Sets the content type. Must not contain any charset WRONG:
     * "text/html; charset=utf8".
     * <p/>
     * If you want to set the charset use method {@link Result#with(Charset)};
     *
     * @param contentType (without encoding) something like "text/html" or
     *                    "application/json"
     */
    public Result as(String contentType) {
        setContentType(contentType);
        return this;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Result with(String headerName, String headerContent) {
        headers.put(headerName, headerContent);
        return this;
    }

    /**
     * Returns cookie with that name or null.
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

    public List<Cookie> getCookies() {
        return cookies;
    }

    public Result with(Cookie cookie) {
        cookies.add(cookie);
        return this;
    }

    public Result without(String name) {
        String v = headers.remove(name);
        if (v == null  && getCookie(name) != null) {
            // It may be a cookie
            discard(name);
        }
        return this;
    }

    public Result discard(String name) {
        cookies.add(Cookie.builder(name, "").setMaxAge(0).build());
        return this;
    }

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
     * A redirect that uses 303 see other.
     *
     * @param url The url used as redirect target.
     * @return A nicely configured result with status code 303 and the url set
     *         as Location header.
     */
    public Result redirect(String url) {
        status(Status.SEE_OTHER);
        with(Response.LOCATION, url);
        return this;
    }

    /**
     * A redirect that uses 307 see other.
     *
     * @param url The url used as redirect target.
     * @return A nicely configured result with status code 307 and the url set
     *         as Location header.
     */
    public Result redirectTemporary(String url) {
        status(Status.TEMPORARY_REDIRECT);
        with(Response.LOCATION, url);
        return this;
    }

    /**
     * Set the content type of this result to {@link MimeTypes#HTML}.
     *
     * @return the same result where you executed this method on. But the content type is now {@link MimeTypes#HTML}.
     */
    public Result html() {
        setContentType(MimeTypes.HTML);
        charset = Charsets.UTF_8;
        return this;
    }

    /**
     * Set the content type of this result to {@link MimeTypes#JSON}.
     *
     * @return the same result where you executed this method on. But the content type is now {@link MimeTypes#JSON}.
     */
    public Result json() {
        setContentType(MimeTypes.JSON);
        charset = Charsets.UTF_8;
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
        return this;
    }

    /**
     * This function sets
     * <p/>
     * Cache-Control: no-cache, no-store
     * Date: (current date)
     * Expires: 1970
     * <p/>
     * => it therefore effectively forces the browser and every proxy in between
     * not to cache content.
     * <p/>
     * See also https://devcenter.heroku.com/articles/increasing-application-performance-with-http-cache-headers
     *
     * @return this result for chaining.
     */
    public Result noCache() {
        with(Response.CACHE_CONTROL, Response.NOCACHE_VALUE);
        with(Response.DATE, DateUtil.formatForHttpHeader(System.currentTimeMillis()));
        with(Response.EXPIRES, DateUtil.formatForHttpHeader(0L));
        return this;
    }

    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    public Result noContentIfNone() {
        if (content == null) {
            content = new NoHttpBody();
        }
        return this;
    }
}
