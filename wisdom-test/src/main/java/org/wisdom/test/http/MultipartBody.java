package org.wisdom.test.http;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.Map.Entry;

public class MultipartBody extends BaseRequest implements Body {

    private Map<String, Object> parameters = new HashMap<String, Object>();

    private boolean hasFile;
    private HttpRequest httpRequestObj;

    public MultipartBody(HttpRequest httpRequest) {
        super(httpRequest);
        this.httpRequestObj = httpRequest;
    }

    public MultipartBody field(String name, String value) {
        parameters.put(name, value);
        return this;
    }

    public MultipartBody field(String name, File file) {
        this.parameters.put(name, file);
        hasFile = true;
        return this;
    }

    public MultipartBody basicAuth(String username, String password) {
        httpRequestObj.basicAuth(username, password);
        return this;
    }

    public HttpEntity getEntity() {
        if (hasFile) {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            for (Entry<String, Object> part : parameters.entrySet()) {
                if (part.getValue() instanceof File) {
                    hasFile = true;
                    builder.addPart(part.getKey(), new FileBody((File) part.getValue()));
                } else {
                    builder.addPart(part.getKey(), new StringBody(part.getValue().toString(), ContentType.APPLICATION_FORM_URLENCODED));
                }
            }
            return builder.build();
        } else {
            try {
                return new UrlEncodedFormEntity(getList(parameters), UTF_8);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static List<NameValuePair> getList(Map<String, Object> parameters) {
        List<NameValuePair> result = new ArrayList<NameValuePair>();
        if (parameters != null) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                if (entry.getValue() != null) {
                    result.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
                }
            }
        }
        return result;
    }

}
