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
