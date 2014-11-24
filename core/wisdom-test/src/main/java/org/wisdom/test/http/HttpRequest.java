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
package org.wisdom.test.http;

import org.apache.commons.codec.binary.Base64;
import org.wisdom.api.http.HttpMethod;

import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * A class allowing the instantiation of HTTP Requests.
 */
public class HttpRequest extends BaseRequest {

    /**
     * The HTTP method of the request.
     */
    private HttpMethod httpMethod;
    /**
     * The url os the request.
     */
    protected String url;

    /**
     * The headers of the request.
     */
    private Map<String, String> headers = new HashMap<>();

    /**
     * The payload of the request.
     */
    protected Body body;

    /**
     * Creates a new request.
     *
     * @param method the method, must not be {@literal null}
     * @param url    the url, must not be {@literal null}
     */
    public HttpRequest(HttpMethod method, String url) {
        this.httpMethod = method;
        try {
            this.url = parseUrl(url).toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        super.httpRequest = this;
    }

    private URL parseUrl(String s) throws Exception {
        return new URI(s.replaceAll("\\s+", "%20")).toURL();
    }

    /**
     * Configures the authentication credentials. It uses basic HTTP authentication.
     *
     * @param username the username
     * @param password the password
     * @return the current request
     */
    public HttpRequest basicAuth(String username, String password) {
        String key = username + ":" + password;
        String encoded = new String(Base64.encodeBase64(key.getBytes(Charset.forName(UTF_8))), Charset.forName(UTF_8));
        header("Authorization", "Basic " + encoded);
        return this;
    }

    /**
     * Adds a header to the request. If the header was already set, the value is overridden.
     *
     * @param name  the header's name, must not be {@literal null}
     * @param value the header's value, must not be {@literal null}
     * @return the current request
     */
    public HttpRequest header(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    /**
     * Adds a set of headers to the headers of the request. If one of the given header is already set,
     * the value is overridden.
     *
     * @param headers the headers to add to the current headers.
     * @return the current request.
     */
    public HttpRequest headers(Map<String, String> headers) {
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                header(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    /**
     * @return the HTTP method.
     */
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return a copy of the current headers, or an empty map if none are set.
     */
    public Map<String, String> getHeaders() {
        if (headers == null) {
            return new HashMap<>();
        }
        return new HashMap<>(headers);
    }

    /**
     * @return the current body, {@literal null} if none are set.
     */
    public Body getBody() {
        return body;
    }

}

