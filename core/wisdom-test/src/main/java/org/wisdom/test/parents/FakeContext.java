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
package org.wisdom.test.parents;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.wisdom.api.cookies.Cookie;
import org.wisdom.api.cookies.Cookies;
import org.wisdom.api.cookies.FlashCookie;
import org.wisdom.api.cookies.SessionCookie;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.FileItem;
import org.wisdom.api.http.Request;
import org.wisdom.api.router.Route;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A fake implementation of the context.
 */
public class FakeContext implements Context {

    /**
     * The context id.
     */
    private static AtomicLong counter = new AtomicLong();

    /**
     * The context's id.
     */
    private Long id;

    /**
     * The form data.
     */
    private Map<String, List<String>> form = Maps.newHashMap();

    /**
     * The parameters from path and query.
     */
    private Map<String, List<String>> parameters = Maps.newHashMap();

    /**
     * The session cookie.
     */
    private SessionCookie session = new FakeSessionCookie();

    /**
     * The flash cookie.
     */
    private FlashCookie flash = new FakeFlashCookie();

    /**
     * The payload.
     */
    private Object body;

    /**
     * The headers of the request.
     */
    private Map<String, List<String>> headers = new HashMap<>();

    /**
     * The path of the request.
     */
    private String path;

    /**
     * The cookies.
     */
    private FakeCookies cookies = new FakeCookies();

    /**
     * The upload files.
     */
    private Map<String, FileItem> files = new HashMap<>();

    /**
     * The fake request.
     */
    private FakeRequest request = new FakeRequest(this);

    /**
     * The route.
     */
    private Route route;

    /**
     * Creates a new instance of {@link FakeContext}.
     */
    public FakeContext() {
        id = counter.getAndIncrement();
    }

    /**
     * @return the context id.
     */
    @Override
    public Long id() {
        return id;
    }

    /**
     * @return the fake request associated to this context.
     */
    @Override
    public Request request() {
        return request;
    }

    /**
     * @return the path, {@literal null} if not set.
     */
    @Override
    public String path() {
        return path;
    }

    /**
     * @return the flash cookie.
     */
    @Override
    public FlashCookie flash() {
        return flash;
    }

    /**
     * @return the session cookie.
     */
    @Override
    public SessionCookie session() {
        return session;
    }

    /**
     * Gets a specific cookie.
     *
     * @param cookieName Name of the cookie to retrieve
     * @return the cookie, {@literal null} if not set.
     */
    @Override
    public Cookie cookie(String cookieName) {
        return cookies.get(cookieName);
    }

    /**
     * Checks whether the cookie is set.
     *
     * @param cookieName Name of the cookie to check for
     * @return {@literal true} if the cookie is set, {@literal false} otherwise.
     */
    @Override
    public boolean hasCookie(String cookieName) {
        return cookies.get(cookieName) != null;
    }

    /**
     * @return the (fake) cookies.
     */
    @Override
    public Cookies cookies() {
        return cookies;
    }

    /**
     * Adds a cookie. Except the value, others cookie's data are meaningless.
     *
     * @param name  the name, must not be {@literal null}
     * @param value the value
     * @return the current fake request
     */
    public FakeContext setCookie(String name, String value) {
        cookies.add(name, value);
        return this;
    }

    /**
     * Adds a cookie.
     *
     * @param cookie the cookie, must not be {@literal null}
     * @return the current fake request
     */
    public FakeContext setCookie(Cookie cookie) {
        cookies.add(cookie);
        return this;
    }

    /**
     * @return {@literal null}
     */
    @Override
    public String contextPath() {
        return null;
    }

    /**
     * Gets the value of the given parameter.
     *
     * @param name The key of the parameter
     * @return the value, {@literal null} if not set
     */
    @Override
    public String parameter(String name) {
        if (parameters.get(name) != null) {
            return parameters.get(name).get(0);
        }
        return null;
    }

    /**
     * Gets all the values of the given parameter.
     *
     * @param name The key of the parameter
     * @return the list of value, empty if none.
     */
    @Override
    public List<String> parameterMultipleValues(String name) {
        List<String> values = parameters.get(name);
        if (values == null) {
            return Collections.emptyList();
        } else {
            return values;
        }
    }

    /**
     * Gets the value of the given parameter.
     *
     * @param name         The key of the parameter
     * @param defaultValue the default value, if the parameter is not set
     * @return the value, the default value if not set
     */
    @Override
    public String parameter(String name, String defaultValue) {
        String v = parameter(name);
        if (v == null) {
            return defaultValue;
        }
        return v;
    }

    /**
     * Gets the value of the given parameter.
     *
     * @param name The key of the parameter
     * @return the value, {@literal null} if not set
     */
    @Override
    public Integer parameterAsInteger(String name) {
        String v = parameter(name);
        if (v != null) {
            return Integer.parseInt(v);
        }
        return null;
    }

    /**
     * Gets the value of the given parameter.
     *
     * @param name         The key of the parameter
     * @param defaultValue the default value, if the parameter is not set
     * @return the value, the default value if not set
     */
    @Override
    public Integer parameterAsInteger(String name, Integer defaultValue) {
        String v = parameter(name);
        if (v != null) {
            return Integer.parseInt(v);
        }
        return defaultValue;
    }

    /**
     * Gets the value of the given parameter.
     *
     * @param name The key of the parameter
     * @return the value, {@literal null} if not set
     */
    @Override
    public Boolean parameterAsBoolean(String name) {
        String v = parameter(name);
        if (v != null) {
            return Boolean.parseBoolean(v);
        }
        return null;  //NOSONAR return null to distinguish the case where the parameter is not set.
    }

    /**
     * Gets the value of the given parameter.
     *
     * @param name         The key of the parameter
     * @param defaultValue the default value, if the parameter is not set
     * @return the value, the default value if not set
     */
    @Override
    public Boolean parameterAsBoolean(String name, boolean defaultValue) {
        String v = parameter(name);
        if (v != null) {
            return Boolean.parseBoolean(v);
        }
        return defaultValue;
    }

    /**
     * Gets the value of the given parameter.
     *
     * @param name The key of the parameter
     * @return the value, {@literal null} if not set
     */
    @Override
    public String parameterFromPath(String name) {
        return parameter(name);
    }

    /**
     * Gets the value of the given parameter.
     *
     * @param name The key of the parameter
     * @return the value, {@literal null} if not set
     */
    @Override
    public String parameterFromPathEncoded(String name) {
        return parameter(name);
    }

    /**
     * Gets the value of the given parameter.
     *
     * @param name The key of the parameter
     * @return the value, {@literal null} if not set
     */
    @Override
    public Integer parameterFromPathAsInteger(String name) {
        return parameterAsInteger(name);
    }

    /**
     * @return all parameters.
     */
    @Override
    public Map<String, List<String>> parameters() {
        return parameters;
    }

    /**
     * Gets the value of the header.
     *
     * @param name the header's name
     * @return the value, {@literal null} if not set
     */
    @Override
    public String header(String name) {
        List<String> v = headers.get(name);
        if (v != null && !v.isEmpty()) {
            return v.get(0);
        }
        return null;
    }

    /**
     * Gets all values of the header.
     *
     * @param name the header's name
     * @return the values, {@literal empty} if none
     */
    @Override
    public List<String> headers(String name) {
        if (headers.containsKey(name)) {
            return headers.get(name);
        }
        return Collections.emptyList();
    }

    /**
     * @return the set of set headers.
     */
    @Override
    public Map<String, List<String>> headers() {
        return headers;
    }

    /**
     * A method getting the value stored in a cookie.
     *
     * @param name The name of the cookie
     * @return the stored value, {@literal null} if the cookie is not set, or empty
     */
    @Override
    public String cookieValue(String name) {
        Cookie cookie = cookie(name);
        if (cookie != null) {
            return cookie.value();
        }
        return null;
    }

    /**
     * Gets the body.
     *
     * @param classOfT The class of the result.
     * @param <T>      the body's class
     * @return the body object.
     */
    @Override
    public <T> T body(Class<T> classOfT) {
        return (T) body;
    }

    /**
     * Gets the body.
     *
     * @param classOfT the class of the result.
     * @param genericType the generic signature of the type (ignored in this implementation)
     * @param <T> the body's class
     * @return the body object.
     */
    @Override
    public <T> T body(Class<T> classOfT, Type genericType) {
        return body(classOfT);
    }


    /**
     * @return the raw body, {@literal null} if not set.
     */
    @Override
    public String body() {
        if (body != null) {
            return body.toString();
        }
        return null;
    }

    /**
     * Returns the byte array of the String form of the body object, {@code null} if none.
     *
     * @return the body as byte array, as sent in the request
     */
    @Override
    public byte[] raw() {
        if (body != null) {
            return body.toString().getBytes(); //NOSONAR use default charset.
        }
        return null;
    }

    /**
     * @return a reader on the body to retrieve it as stream, {@literal null} if no body.
     * @throws IOException if the body cannot be read.
     */
    @Override
    public BufferedReader reader() throws IOException {
        String body = body();
        if (body != null) {
            return new BufferedReader(new StringReader(body));
        }
        return null;
    }

    /**
     * Not supported in tests.
     *
     * @return {@literal null}
     */
    @Override
    public Route route() {
        return route;
    }

    /**
     * Not supported in tests.
     *
     * @param route the route ignored
     */
    @Override
    public void route(Route route) {
        this.route = route;
    }

    /**
     * Sets the context's path.
     *
     * @param path the path
     * @return the current fake context
     */
    public FakeContext setPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * @return {@literal true} if some form data or files are attached to the current context,
     * {@literal false} otherwise.
     */
    @Override
    public boolean isMultipart() {
        return !form.isEmpty() || !files.isEmpty();
    }

    /**
     * @return the file items, empty if none.
     */
    @Override
    public Collection<? extends FileItem> files() {
        return files.values();
    }

    /**
     * Gets a file item.
     *
     * @param name the file name
     * @return the file item, {@literal null} if no file item with the given name was added to the context.
     */
    @Override
    public FileItem file(String name) {
        return files.get(name);
    }

    /**
     * @return the form data.
     * @deprecated
     */
    @Override
    @Deprecated
    public Map<String, List<String>> attributes() {
        return form();
    }

    /**
     * @return the form data.
     */
    @Override
    public Map<String, List<String>> form() {
        return form;
    }

    /**
     * Sets the value of an attribute.
     *
     * @param name  the name
     * @param value the value
     * @return the current Fake Context
     * @deprecated use {@link #setFormField(String, String)} instead.
     */
    @Deprecated
    public FakeContext setAttribute(String name, String value) {
        return setFormField(name, value);
    }

    /**
     * Sets the value of a form field.
     *
     * @param name  the name
     * @param value the value
     * @return the current Fake Context
     */
    public FakeContext setFormField(String name, String value) {
        List<String> values = form.get(name);
        if (values == null) {
            values = new ArrayList<>();
            form.put(name, values);
        }
        values.add(value);
        return this;
    }

    /**
     * Sets the value of a form field.
     *
     * @param name  the name
     * @param values the values
     * @return the current Fake Context
     */
    public FakeContext setFormField(String name, String... values) {
        List<String> v = form.get(name);
        if (v == null) {
            v = new ArrayList<>();
            form.put(name, v);
        }
        Collections.addAll(v, values);
        return this;
    }

    /**
     * Sets a parameter.
     *
     * @param name  the name
     * @param value the value
     * @return the current Fake Context
     */
    public FakeContext setParameter(String name, String value) {
        List<String> list = Lists.newArrayList();
        list.add(value);
        setParameter(name, list);
        return this;
    }

    /**
     * Sets a parameter values.
     *
     * @param name   the name
     * @param values the values
     * @return the current Fake Context
     */
    public FakeContext setParameter(String name, List<String> values) {
        parameters.put(name, values);
        return this;
    }

    /**
     * Sets the body.
     *
     * @param body the body
     * @return the current Fake Context
     */
    public FakeContext setBody(Object body) {
        this.body = body;
        return this;
    }

    /**
     * Sets a header.
     *
     * @param name  the name
     * @param value the value
     * @return the current fake context
     */
    public FakeContext setHeader(String name, String value) {
        List<String> list = headers.get(name);
        if (list == null) {
            list = new ArrayList<>();
            headers.put(name, list);
        }
        list.add(value);
        return this;
    }

    /**
     * Sets a header.
     *
     * @param name   the name
     * @param values the values
     * @return the current fake context
     */
    public FakeContext setHeader(String name, String... values) {
        List<String> list = new ArrayList<>(Arrays.asList(values));
        headers.put(name, list);
        return this;
    }

    /**
     * Adds an uploaded files.
     *
     * @param name the name of the field in the form uploading the file.
     * @param file the file object
     * @return the current fake context
     * @deprecated use {@link #setFormField(String, java.io.File)} instead.
     */
    @Deprecated
    public FakeContext setAttribute(String name, File file) {
        return setFormField(name, file);
    }

    /**
     * Adds an uploaded files.
     *
     * @param name the name of the field in the form uploading the file.
     * @param file the file object
     * @return the current fake context
     */
    public FakeContext setFormField(String name, File file) {
        files.put(name, new FakeFileItem(file, name));
        return this;
    }

    /**
     * Adds data to the session.
     *
     * @param key   the key
     * @param value the value
     * @return the current fake context
     */
    public FakeContext addToSession(String key, String value) {
        session.put(key, value);
        return this;
    }

    /**
     * Adds data to the (incoming) flash scope.
     *
     * @param key   the key
     * @param value the value
     * @return the current fake context
     */
    public FakeContext addToFlash(String key, String value) {
        flash.put(key, value);
        return this;
    }

    /**
     * Retrieves the fake request.
     * @return the fake request
     */
    public FakeRequest getFakeRequest() {
        return request;
    }
}
