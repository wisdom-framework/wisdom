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

    private URL parseUrl(String s) throws Exception {
        return new URI(s.replaceAll("\\s+", "%20")).toURL();
    }

    public HttpRequest(HttpMethod method, String url) {
        this.httpMethod = method;
        try {
            this.url = parseUrl(url).toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        super.httpRequest = this;
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

