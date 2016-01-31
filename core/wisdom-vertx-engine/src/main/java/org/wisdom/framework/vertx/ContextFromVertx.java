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
package org.wisdom.framework.vertx;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.vertx.core.*;
import io.vertx.core.http.HttpServerRequest;
import org.apache.commons.io.IOUtils;
import org.wisdom.api.content.BodyParser;
import org.wisdom.api.cookies.Cookie;
import org.wisdom.api.cookies.Cookies;
import org.wisdom.api.cookies.FlashCookie;
import org.wisdom.api.cookies.SessionCookie;
import org.wisdom.api.http.*;
import org.wisdom.api.http.Context;
import org.wisdom.api.router.Route;
import org.wisdom.framework.vertx.cookies.CookieHelper;
import org.wisdom.framework.vertx.cookies.FlashCookieImpl;
import org.wisdom.framework.vertx.cookies.SessionCookieImpl;
import org.wisdom.framework.vertx.file.VertxFileUpload;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An implementation of {@link org.wisdom.api.http.Context} for Vert.x.
 */
public class ContextFromVertx implements Context {

    private static AtomicLong ids = new AtomicLong();
    private final long id;
    private final ServiceAccessor services;
    private final FlashCookieImpl flash;
    private final SessionCookieImpl session;
    private final Vertx vertx;


    private /*not final*/ Route route;
    /**
     * the request object, created lazily.
     */
    private RequestFromVertx request;
    private io.vertx.core.Context vertxContext;


    /**
     * Creates a new context.
     *
     * @param accessor a structure containing the used services.
     * @param req      the incoming HTTP Request.
     */
    public ContextFromVertx(Vertx vertx, io.vertx.core.Context vertxContext, ServiceAccessor accessor, HttpServerRequest req) {
        id = ids.getAndIncrement();
        services = accessor;
        request = new RequestFromVertx(req);
        this.vertx = vertx;
        flash = new FlashCookieImpl(accessor.getConfiguration());
        session = new SessionCookieImpl(accessor.getCrypto(), accessor.getConfiguration());
        flash.init(this);
        session.init(this);

        if (vertxContext == null) {
            throw new IllegalArgumentException("Creating a context from vert.x outside of an event loop");
        } else {
            this.vertxContext = vertxContext;
        }
    }


    /**
     * The context id (unique).
     */
    @Override
    public Long id() {
        return id;
    }

    /**
     * Returns the current request.
     */
    @Override
    public Request request() {
        return request;
    }

    /**
     * Returns the path that the controller should act upon.
     * <p>
     * For instance in servlets you could have something like a context prefix.
     * /myContext/app
     * <p>
     * If your route only defines /app it will work as the requestpath will
     * return only "/app". A context path is not returned.
     * <p>
     * It does NOT decode any parts of the url.
     * <p>
     * Interesting reads: -
     * http://www.lunatech-research.com/archives/2009/02/03/
     * what-every-web-developer-must-know-about-url-encoding -
     * http://stackoverflow
     * .com/questions/966077/java-reading-undecoded-url-from-servlet
     *
     * @return The the path as seen by the server. Does exclude any container
     * set context prefixes. Not decoded.
     */
    @Override
    public String path() {
        return request().path();
    }

    /**
     * Returns the flash cookie. Flash cookies only live for one request. Good
     * uses are error messages to display. Almost everything else is bad use of
     * Flash Cookies.
     * <p>
     * A FlashCookie is usually not signed. Don't trust the content.
     *
     * @return the flash cookie of that request.
     */
    @Override
    public FlashCookie flash() {
        return flash;
    }

    /**
     * Returns the client side session. It is a cookie. Therefore you cannot
     * store a lot of information inside the cookie. This is by intention.
     * <p>
     * If you have the feeling that the session cookie is too small for what you
     * want to achieve thing again. Most likely your design is wrong.
     *
     * @return the Session of that request / response cycle.
     */
    @Override
    public SessionCookie session() {
        return session;
    }

    /**
     * Get cookie from context.
     *
     * @param cookieName Name of the cookie to retrieve
     * @return the cookie with that name or null.
     */
    @Override
    public Cookie cookie(String cookieName) {
        return request().cookie(cookieName);
    }

    /**
     * Checks whether the context contains a given cookie.
     *
     * @param cookieName Name of the cookie to check for
     * @return {@code true} if the context has a cookie with that name.
     */
    @Override
    public boolean hasCookie(String cookieName) {
        return request().cookie(cookieName) != null;
    }

    /**
     * Get all cookies from the context.
     *
     * @return the cookie with that name or null.
     */
    @Override
    public Cookies cookies() {
        return request().cookies();
    }

    /**
     * Get the context path on which the application is running.
     *
     * @return the context-path with a leading "/" or "" if running on root
     */
    @Override
    public String contextPath() {
        return "";
    }

    /**
     * Get the parameter with the given key from the request. The parameter may
     * either be a query parameter, or in the case of form submissions, may be a
     * form parameter.
     * <p>
     * When the parameter is multivalued, returns the first value.
     * <p>
     * The parameter is decoded by default.
     *
     * @param name The key of the parameter
     * @return The value, or null if no parameter was found.
     * @see #parameterMultipleValues
     */
    @Override
    public String parameter(String name) {
        return request.parameter(name);
    }

    @Override
    public Map<String, List<String>> attributes() {
        return form();
    }

    /**
     * Gets the data sent to the server using an HTML Form.
     *
     * @return the form data
     */
    @Override
    public Map<String, List<String>> form() {
        return request.getFormData();
    }

    /**
     * Get the parameter with the given key from the request. The parameter may
     * either be a query parameter, or in the case of form submissions, may be a
     * form parameter.
     * <p>
     * The parameter is decoded by default.
     *
     * @param name The key of the parameter
     * @return The values, possibly an empty list.
     */
    @Override
    public List<String> parameterMultipleValues(String name) {
        return request.parameterMultipleValues(name);
    }

    /**
     * Like {@link #parameter(String)}, but returns given defaultValue
     * instead of null in case parameter cannot be found.
     * <p>
     * The parameter is decoded by default.
     *
     * @param name         The name of the parameter
     * @param defaultValue A default value if parameter not found.
     * @return The value of the parameter of the defaultValue if not found.
     */
    @Override
    public String parameter(String name, String defaultValue) {
        return request.parameter(name, defaultValue);
    }

    /**
     * Like {@link #parameter(String)}, but converts the parameter to
     * Integer if found.
     * <p>
     * The parameter is decoded by default.
     *
     * @param name The name of the parameter
     * @return The value of the parameter or null if not found.
     */
    @Override
    public Integer parameterAsInteger(String name) {
        return request.parameterAsInteger(name);
    }

    /**
     * Like {@link #parameter(String, String)}, but converts the
     * parameter to Integer if found.
     * <p>
     * The parameter is decoded by default.
     *
     * @param name         The name of the parameter
     * @param defaultValue A default value if parameter not found.
     * @return The value of the parameter of the defaultValue if not found.
     */
    @Override
    public Integer parameterAsInteger(String name, Integer defaultValue) {
        return request.parameterAsInteger(name, defaultValue);
    }

    /**
     * Like {@link #parameter(String)}, but converts the
     * parameter to Boolean if found.
     * <p>
     * The parameter is decoded by default.
     *
     * @param name The name parameter
     * @return The value of the parameter of the defaultValue if not found.
     */
    @Override
    public Boolean parameterAsBoolean(String name) {
        return request.parameterAsBoolean(name);
    }

    /**
     * Like {@link #parameter(String, String)}, but converts the
     * parameter to Boolean if found.
     * <p>
     * The parameter is decoded by default.
     *
     * @param name         The name of the parameter
     * @param defaultValue A default value if parameter not found.
     * @return The value of the parameter of the defaultValue if not found.
     */
    @Override
    public Boolean parameterAsBoolean(String name, boolean defaultValue) {
        return request.parameterAsBoolean(name, defaultValue);
    }

    /**
     * Get the path parameter for the given key.
     * <p>
     * The parameter will be decoded based on the RFCs.
     * <p>
     * Check out http://docs.oracle.com/javase/6/docs/api/java/net/URI.html for
     * more information.
     *
     * @param name The name of the path parameter in a route. Eg
     *             /{myName}/rest/of/url
     * @return The decoded path parameter, or null if no such path parameter was
     * found.
     */
    @Override
    public String parameterFromPath(String name) {
        String encodedParameter = route.getPathParametersEncoded(
                path()).get(name);
        if (encodedParameter == null) {
            return null;
        } else {
            // #514 - If the encoded parameter contains : it should be encoded manually.
            // Some library don't meaning that the URI creation fails as : is not allowed.
            if (encodedParameter.contains(":")) {
                encodedParameter = encodedParameter.replace(":", "%3A");
            }
            return URI.create(encodedParameter).getPath();
        }
    }

    /**
     * Get the path parameter for the given key.
     * <p>
     * Returns the raw path part. That means you can get stuff like:
     * blue%2Fred%3Fand+green
     *
     * @param name The name of the path parameter in a route. Eg
     *             /{myName}/rest/of/url
     * @return The encoded (!) path parameter, or null if no such path parameter
     * was found.
     */
    @Override
    public String parameterFromPathEncoded(String name) {
        return route.getPathParametersEncoded(path()).get(name);
    }

    /**
     * Get the path parameter for the given key and convert it to Integer.
     * <p>
     * The parameter will be decoded based on the RFCs.
     * <p>
     * Check out http://docs.oracle.com/javase/6/docs/api/java/net/URI.html for
     * more information.
     *
     * @param key the key of the path parameter
     * @return the numeric path parameter, or null of no such path parameter is
     * defined, or if it cannot be parsed to int
     */
    @Override
    public Integer parameterFromPathAsInteger(String key) {
        String parameter = parameterFromPath(key);
        if (parameter == null) {
            return null;
        } else {
            return Integer.parseInt(parameter);
        }
    }

    /**
     * Get all the parameters from the request (query string). Path parameters can be retrieved using the {@link
     * #parameterFromPath(String)} method. This method does not check the formData.
     *
     * @return The parameters
     */
    @Override
    public Map<String, List<String>> parameters() {
        return request.parameters();
    }

    /**
     * Get the (first) request header with the given name.
     *
     * @return The header value
     */
    @Override
    public String header(String name) {
        List<String> list = request.headers().get(name);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    /**
     * Get all the request headers with the given name.
     *
     * @return the header values
     */
    @Override
    public List<String> headers(String name) {
        return request.headers().get(name);
    }

    /**
     * Get all the headers from the request.
     *
     * @return The headers
     */
    @Override
    public Map<String, List<String>> headers() {
        return request.headers();
    }

    /**
     * Get the cookie value from the request, if defined.
     *
     * @param name The name of the cookie
     * @return The cookie value, or null if the cookie was not found
     */
    @Override
    public String cookieValue(String name) {
        return CookieHelper.getCookieValue(name, request().cookies());
    }

    /**
     * This will give you the request body nicely parsed. You can register your
     * own parsers depending on the request type.
     * <p>
     *
     * @param classOfT The class of the result.
     * @return The parsed request or null if something went wrong.
     */
    @Override
    public <T> T body(Class<T> classOfT) {
        return body(classOfT, null);
    }

    /**
     * This will give you the request body nicely parsed. You can register your
     * own parsers depending on the request type.
     * <p>
     *
     * @param classOfT The class of the result.
     * @return The parsed request or null if something went wrong.
     */
    @Override
    public <T> T body(Class<T> classOfT, Type genericType) {
        String rawContentType = request().contentType();

        // If the Content-type: xxx header is not set we return null.
        // we cannot parse that request.
        if (rawContentType == null) {
            return null;
        }

        // If Content-type is application/json; charset=utf-8 we split away the charset
        // application/json
        String contentTypeOnly = HttpUtils.getContentTypeFromContentTypeAndCharacterSetting(
                rawContentType);

        BodyParser parser = services.getContentEngines().getBodyParserEngineForContentType(contentTypeOnly);

        if (parser == null) {
            return null;
        }

        return parser.invoke(this, classOfT, genericType);
    }

    /**
     * Retrieves the request body as a String. If the request has no body, {@code null} is returned.
     *
     * @return the body as String
     */
    public String body() {
        return request.getRawBodyAsString();
    }

    /**
     * Retrieves the request body as a byte array. If the request has no body, {@code null} is returned.
     *
     * @return the body as byte array, as sent in the request
     */
    @Override
    public byte[] raw() {
        return request.getRawBody();
    }

    /**
     * Gets the reader to read the request.
     *
     * @return The reader
     */
    @Override
    public BufferedReader reader() throws IOException {
        byte[] raw = request.getRawBody();
        if (raw != null) {
            return IOUtils.toBufferedReader(new InputStreamReader(new ByteArrayInputStream(raw)));
        }
        return null;
    }

    /**
     * Get the route for this context.
     *
     * @return The route
     */
    @Override
    public Route route() {
        return route;
    }

    /**
     * Sets the route associated with the current context.
     *
     * @param route the route
     */
    public void route(Route route) {
        // Can be called only once, with a non null route.
        Preconditions.checkState(this.route == null);
        Preconditions.checkNotNull(route);
        this.route = route;
    }

    /**
     * Check if request is of type multipart. Important when you want to process
     * uploads for instance.
     * <p>
     * Also check out: http://commons.apache.org/fileupload/streaming.html
     *
     * @return true if request is of type multipart.
     */
    @Override
    public boolean isMultipart() {
        return MimeTypes.MULTIPART.equals(request.getHeader(HeaderNames.CONTENT_TYPE));
    }

    /**
     * Gets the collection of uploaded files.
     *
     * @return the collection of files, {@literal empty} if no files.
     */
    @Override
    public Collection<? extends FileItem> files() {
        return request.getFiles();
    }

    /**
     * Gets the uploaded file having a form's field matching the given name.
     *
     * @param name the name of the field of the form that have uploaded the file
     * @return the file object, {@literal null} if there are no file with this name
     */
    @Override
    public FileItem file(String name) {
        for (FileItem item : request.getFiles()) {
            // FileItem that do not have an real file attached, should not be returned
            if (item.field().equals(name) && !Strings.isNullOrEmpty(item.name())) {
                return item;
            }
        }
        return null;
    }

    /**
     * Releases uploaded files.
     */
    public void cleanup() {
        request.getFiles().forEach(VertxFileUpload::cleanup);
    }

    /**
     * Callback called when the request has been read completely.
     */
    public boolean ready() {
        // Store the vertx context, used to finalize the response
        vertxContext = vertx.getOrCreateContext();
        return request.ready();
    }

    /**
     * @return the vertx singleton.
     */
    public Vertx vertx() {
        return vertx;
    }

    /**
     * @return the vertx context that will be used to write the response to the client.
     */
    public io.vertx.core.Context vertxContext() {
        return vertxContext;
    }

    @Override
    public String toString() {
        return "context-" + id + " / " + vertxContext;
    }

}
