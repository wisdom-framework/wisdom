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
import org.jsoup.nodes.Document;

import java.io.InputStream;
import java.util.concurrent.Future;

/**
 * The default code for all requests.
 */
public abstract class BaseRequest {

    /**
     * The UTF-8 charset.
     */
    protected static final String UTF_8 = "UTF-8";

    /**
     * The underlying HTTP Request.
     */
    protected HttpRequest httpRequest;

    /**
     * Creates a base request form the given HTTP Request.
     *
     * @param httpRequest the request
     */
    protected BaseRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    /**
     * Creates a base request.
     */
    protected BaseRequest() {
        super();
    }

    /**
     * Gets the request results as String.
     *
     * @return the result
     * @throws Exception if the request failed
     */
    public HttpResponse<String> asString() throws Exception {
        return HttpClientHelper.request(httpRequest, String.class);
    }


    /**
     * Gets the request results as String. The invocation is made asynchronously.
     *
     * @return future to retrieve the result
     */
    public Future<HttpResponse<String>> asStringAsync() {
        return HttpClientHelper.requestAsync(httpRequest, String.class, null);
    }

    /**
     * Gets the request results as String. The invocation is made asynchronously.
     *
     * @param callback the callback invoked with the resulting String.
     * @return future to retrieve the result
     */
    public Future<HttpResponse<String>> asStringAsync(Callback<String> callback) {
        return HttpClientHelper.requestAsync(httpRequest, String.class, callback);
    }

    /**
     * Gets the request results as JSON.
     *
     * @return the result
     * @throws Exception if the request failed
     */
    public HttpResponse<JsonNode> asJson() throws Exception {
        return HttpClientHelper.request(httpRequest, JsonNode.class);
    }

    /**
     * Gets the request results as HTML.
     *
     * @return the result
     * @throws Exception if the request failed
     */
    public HttpResponse<Document> asHtml() throws Exception {
        return HttpClientHelper.request(httpRequest, Document.class);
    }

    /**
     * Gets the request results as JSON. The invocation is made asynchronously.
     *
     * @return future to retrieve the result
     */
    public Future<HttpResponse<JsonNode>> asJsonAsync() {
        return HttpClientHelper.requestAsync(httpRequest, JsonNode.class, null);
    }

    /**
     * Gets the request results as String. The invocation is made asynchronously.
     *
     * @param callback the callback invoked with the resulting String.
     * @return future to retrieve the result
     */
    public Future<HttpResponse<JsonNode>> asJsonAsync(Callback<JsonNode> callback) {
        return HttpClientHelper.requestAsync(httpRequest, JsonNode.class, callback);
    }

    /**
     * Gets the request results as binary. The invocation is made asynchronously.
     *
     * @return future to retrieve the result
     */
    public HttpResponse<InputStream> asBinary() throws Exception {
        return HttpClientHelper.request(httpRequest, InputStream.class);
    }

    /**
     * Gets the request results as binary. The invocation is made asynchronously.
     *
     * @return future to retrieve the result
     */
    public Future<HttpResponse<InputStream>> asBinaryAsync() {
        return HttpClientHelper.requestAsync(httpRequest, InputStream.class, null);
    }

    /**
     * Gets the request results as binary. The invocation is made asynchronously.
     *
     * @param callback the callback invoked with the resulting String.
     * @return future to retrieve the result
     */
    public Future<HttpResponse<InputStream>> asBinaryAsync(Callback<InputStream> callback) {
        return HttpClientHelper.requestAsync(httpRequest, InputStream.class, callback);
    }
}
