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

public class HttpRequest extends BaseRequest {

    private HttpMethod httpMethod;
    protected String url;
    private Map<String, String> headers = new HashMap<>();
    protected Body body;
    
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

    public HttpRequest basicAuth(String username, String password) {
        String key = username + ":" + password;
        String encoded = new String(Base64.encodeBase64(key.getBytes(Charset.forName(UTF_8))), Charset.forName(UTF_8));
        header("Authorization", "Basic " + encoded);
        return this;
    }

    public HttpRequest header(String name, String value) {
        this.headers.put(name.toLowerCase(), value);
        return this;
    }

    public HttpRequest headers(Map<String, String> headers) {
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                header(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getHeaders() {
        if (headers == null) {
            return new HashMap<>();
        }
        return headers;
    }

    public Body getBody() {
        return body;
    }

}

