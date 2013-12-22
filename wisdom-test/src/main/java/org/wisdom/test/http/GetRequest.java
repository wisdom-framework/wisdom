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
