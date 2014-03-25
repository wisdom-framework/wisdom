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

import org.wisdom.api.http.HttpMethod;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class GetRequest extends HttpRequest {

    public GetRequest(HttpMethod method, String url) {
        super(method, url);
    }

    public GetRequest(String url) {
        super(HttpMethod.GET, url);
    }

    public GetRequest field(String name, Object value) {
        StringBuilder queryString = new StringBuilder();
        if (this.url.contains("?")) {
            queryString.append("&");
        } else {
            queryString.append("?");
        }
        try {
            queryString.append(name).append("=").append(URLEncoder.encode((value == null) ? "" : value.toString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        this.url += queryString.toString();
        return this;
    }

    public GetRequest fields(Map<String, Object> parameters) {
        if (parameters != null) {
            for (Map.Entry<String, Object> param : parameters.entrySet()) {
                if (param.getValue() instanceof String || param.getValue() instanceof Number || param.getValue() instanceof Boolean) {
                    field(param.getKey(), param.getValue());
                } else {
                    throw new RuntimeException("Parameter \"" + param.getKey() + "\" can't be sent with a GET request" +
                            " because of type: " + param.getValue().getClass().getName());
                }
            }
        }
        return this;
    }

    public GetRequest basicAuth(String username, String password) {
        super.basicAuth(username, password);
        return this;
    }
}
