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
import org.wisdom.api.http.HttpMethod;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;


public class HttpRequestWithBody extends HttpRequest {

    public HttpRequestWithBody(HttpMethod method, String url) {
        super(method, url);
    }

    @Override
    public HttpRequestWithBody header(String name, String value) {
        return (HttpRequestWithBody) super.header(name, value);
    }

    public HttpRequestWithBody basicAuth(String username, String password) {
        super.basicAuth(username, password);
        return this;
    }

    public MultipartBody field(String name, Object value) {
        MultipartBody body = new MultipartBody(this).field(name, (value == null) ? "" : value.toString());
        this.body = body;
        return body;
    }

    public MultipartBody field(String name, File file) {
        MultipartBody body = new MultipartBody(this).field(name, file);
        this.body = body;
        return body;
    }

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

    public RequestBodyEntity body(JsonNode body) {
        return body(body.toString());
    }

    public RequestBodyEntity body(String body) {
        RequestBodyEntity b = new RequestBodyEntity(this).body(body);
        this.body = b;
        return b;
    }

}
