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
import org.wisdom.api.cookies.Cookies;
import org.wisdom.api.cookies.FlashCookie;
import org.wisdom.api.cookies.SessionCookie;
import org.wisdom.api.router.Route;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Context Interface.
 */
public interface Context {

    static ThreadLocal<Context> CONTEXT = new ThreadLocal<>();

    /**
     * @return the context id (unique).
     */
    Long id();

    /**
     * @return the current request.
     */
    Request request();

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
    String path();

    /**
     * Returns the flash cookie. Flash cookies only live for one request. Good
     * uses are error messages to display. Almost everything else is bad use of
     * Flash Cookies.
     * <p>
     * A FlashCookie is usually not signed. Don't trust the content.
     *
     * @return the flash cookie of that request.
     */
    FlashCookie flash();

    /**
     * Returns the client side session. It is a cookie. Therefore you cannot
     * store a lot of information inside the cookie. This is by intention.
     * <p>
     * If you have the feeling that the session cookie is too small for what you
     * want to achieve thing again. Most likely your design is wrong.
     *
     * @return the Session of that request / response cycle.
     */
    SessionCookie session();

    /**
     * Get cookie from context.
     *
     * @param cookieName Name of the cookie to retrieve
     * @return the cookie with that name or null.
     */
    Cookie cookie(String cookieName);

    /**
     * Checks whether the context contains a given cookie.
     *
     * @param cookieName Name of the cookie to check for
     * @return {@code true} if the context has a cookie with that name.
     */
    boolean hasCookie(String cookieName);

    /**
     * Get all cookies from the context.
     *
     * @return the cookie with that name or null.
     */
    Cookies cookies();

    /**
     * Get the context path on which the application is running.
     *
     * @return the context-path with a leading "/" or "" if running on root
     */
    String contextPath();

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
    String parameter(String name);

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
    List<String> parameterMultipleValues(String name);

    /**
     * Same like {@link #parameter(String)}, but returns given defaultValue
     * instead of null in case parameter cannot be found.
     * <p>
     * The parameter is decoded by default.
     *
     * @param name         The name of the post or query parameter
     * @param defaultValue A default value if parameter not found.
     * @return The value of the parameter of the defaultValue if not found.
     */
    String parameter(String name, String defaultValue);

    /**
     * Same like {@link #parameter(String)}, but converts the parameter to
     * Integer if found.
     * <p>
     * The parameter is decoded by default.
     *
     * @param name The name of the post or query parameter
     * @return The value of the parameter or null if not found.
     */
    Integer parameterAsInteger(String name);

    /**
     * Same like {@link #parameter(String, String)}, but converts the
     * parameter to Integer if found.
     * <p>
     * The parameter is decoded by default.
     *
     * @param name         The name of the post or query parameter
     * @param defaultValue A default value if parameter not found.
     * @return The value of the parameter of the defaultValue if not found.
     */
    Integer parameterAsInteger(String name, Integer defaultValue);

    /**
     * Same like {@link #parameter(String)}, but converts the parameter to
     * Boolean if found.
     * <p>
     * The parameter is decoded by default.
     *
     * @param name The name of the post or query parameter
     * @return The value of the parameter or {@literal false} if not found.
     */
    Boolean parameterAsBoolean(String name);

    /**
     * Same like {@link #parameter(String)}, but converts the parameter to
     * Boolean if found.
     * <p>
     * The parameter is decoded by default.
     *
     * @param name         The name of the post or query parameter
     * @param defaultValue A default value if parameter not found.
     * @return The value of the parameter or the defaultValue if not found.
     */
    Boolean parameterAsBoolean(String name, boolean defaultValue);

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
    String parameterFromPath(String name);

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
    String parameterFromPathEncoded(String name);

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
    Integer parameterFromPathAsInteger(String key);

    /**
     * Get all the parameters from the request. This returns only the parameters from the 'query string'. Path
     * parameters can be retrieved using the {@link #parameterFromPath(String)} method.
     *
     * @return The parameters
     */
    Map<String, List<String>> parameters();

    /**
     * Get the (first) request header with the given name.
     *
     * @param name the name of the header
     * @return The header value
     */
    String header(String name);

    /**
     * Get all the request headers with the given name.
     *
     * @param name the name of the header
     * @return the header values
     */
    List<String> headers(String name);

    /**
     * Get all the headers from the request.
     *
     * @return The headers
     */
    Map<String, List<String>> headers();

    /**
     * Get the cookie value from the request, if defined.
     *
     * @param name The name of the cookie
     * @return The cookie value, or null if the cookie was not found
     */
    String cookieValue(String name);

    /**
     * Gets the request body parsed. You can register your own parsers depending on the request type.
     *
     * @param classOfT The class of the result.
     * @return The parsed request or null if something went wrong.
     * @see org.wisdom.api.content.BodyParser
     */
    <T> T body(Class<T> classOfT);

    /**
     * Gets the request body parsed. You can register your own parsers depending on the request type.
     * Unlike {@link #body(Class)}, this method supports generic type.
     *
     * @param classOfT The class of the result.
     * @return The parsed request or null if something went wrong.
     * @see  org.wisdom.api.content.BodyParser
     * @since 0.8.1
     */
    <T> T body(Class<T> classOfT, Type genericType);

    /**
     * Retrieves the request body as a String. If the request has no body, {@code null} is returned.
     *
     * @return the body as String
     */
    String body();

    /**
     * Retrieves the request body as a byte array. If the request has no body, {@code null} is returned.
     *
     * @return the body as byte array, as sent in the request
     * @since 0.7
     */
    byte[] raw();

    /**
     * Get the reader to read the request.
     * <p>
     *
     * @return The reader
     */
    BufferedReader reader() throws IOException;

    /**
     * Get the route for this context.
     *
     * @return The route
     */
    Route route();

    /**
     * Sets the context route.
     * Must only be called by the engine.
     *
     * @param route the route
     */
    void route(Route route);

    /**
     * Check if request is of type multipart. Important when you want to process
     * uploads for instance.
     * <p>
     * Also check out: http://commons.apache.org/fileupload/streaming.html
     *
     * @return true if request is of type multipart.
     */
    boolean isMultipart();

    /**
     * Gets the collection of uploaded files.
     *
     * @return the collection of files, {@literal empty} if no files.
     */
    Collection<? extends FileItem> files();

    /**
     * Gets the uploaded file having the given name.
     *
     * @param name the file name
     * @return the file object, {@literal null} if there are no file with this name
     */
    FileItem file(String name);

    /**
     * Gets the request attributes.
     *
     * @return the attributes.
     * @deprecated use {@link #form()} instead
     */
    @Deprecated
    Map<String, List<String>> attributes();

    /**
     * Gets the data sent to the server using an HTML Form.
     *
     * @return the form data
     */
    Map<String, List<String>> form();

}
