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

import com.fasterxml.jackson.databind.JsonNode;
import org.wisdom.api.http.HeaderNames;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;


/**
 * A class making easy the creation of HTTP Request with a payload (body).
 */
public class HttpRequestWithBody extends HttpRequest {

    /**
     * Creates the request.
     *
     * @param method the HTTP method
     * @param url    the url
     */
    public HttpRequestWithBody(HttpMethod method, String url) {
        super(method, url);
    }

    /**
     * Adds a header to the request.
     *
     * @param name  the header's name
     * @param value the header's value
     * @return the current request
     */
    @Override
    public HttpRequestWithBody header(String name, String value) {
        super.header(name, value);
        return this;
    }

    /**
     * Adds a set of headers to the headers of the request. If one of the given header is already set,
     * the value is overridden.
     *
     * @param headers the headers to add to the current headers.
     * @return the current request.
     */
    public HttpRequestWithBody headers(Map<String, String> headers) {
        super.headers(headers);
        return this;
    }

    /**
     * Configures the authentication credentials. It uses basic HTTP authentication.
     *
     * @param username the username
     * @param password the password
     * @return the current request
     */
    public HttpRequestWithBody basicAuth(String username, String password) {
        super.basicAuth(username, password);
        return this;
    }

    /**
     * Adds a form's field to the request.
     *
     * @param name  the field's name
     * @param value the field's value
     * @return the multipart body usable to add other fields to the content.
     */
    public MultipartBody field(String name, Object value) {
        MultipartBody body = new MultipartBody(this).field(name, (value == null) ? "" : value.toString());
        this.body = body;
        return body;
    }

    /**
     * Adds a 'upload' file to the request.
     *
     * @param name the field's name
     * @param file the file to upload
     * @return the multipart body usable to add other fields to the content.
     */
    public MultipartBody field(String name, File file) {
        MultipartBody body = new MultipartBody(this).field(name, file);
        this.body = body;
        return body;
    }

    /**
     * Adds form's fields to the request.
     *
     * @param parameters the map of field names - values
     * @return the multipart body usable to add other fields to the content.
     */
    public MultipartBody fields(Map<String, Object> parameters) {
        MultipartBody body = new MultipartBody(this);
        if (parameters != null) {
            for (Entry<String, Object> param : parameters.entrySet()) {
                if (param.getValue() instanceof File) {
                    body.field(param.getKey(), (File) param.getValue());
                } else {
                    body.field(param.getKey(), (param.getValue() == null) ? "" : param.getValue().toString());
                }
            }
        }
        this.body = body;
        return body;
    }

    /**
     * Sets the content of the HTTP request to the given JSON body. This methods set the 'Content-Type' HTTP header
     * to 'application/json.
     *
     * @param body the json object to used as body.
     * @return the current HTTP request
     */
    public RequestBodyEntity body(JsonNode body) {
        header(HeaderNames.CONTENT_TYPE, MimeTypes.JSON);
        return body(body.toString());
    }

    /**
     * Sets the content of the HTTP request to the given String body. No 'content-type' is set.
     *
     * @param body the body.
     * @return the current HTTP request
     */
    public RequestBodyEntity body(String body) {
        RequestBodyEntity b = new RequestBodyEntity(this).body(body);
        this.body = b;
        return b;
    }

}
