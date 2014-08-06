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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * An implementation of HTTP Request used to send the results from a form. It's useful when testing controller expecting
 * input from forms.
 * <p/>
 * To use this class, creates a regular request that you give this class (in the constructor).
 */
public class
        MultipartBody extends BaseRequest implements Body {

    private Map<String, Object> parameters = new HashMap<>();

    private boolean hasFile;
    private HttpRequest httpRequestObj;

    /**
     * Creates the Multipart body from the given request.
     *
     * @param httpRequest the base request, must not be {@literal null}.
     */
    public MultipartBody(HttpRequest httpRequest) {
        super(httpRequest);
        this.httpRequestObj = httpRequest;
    }

    /**
     * Adds a field.
     *
     * @param name  the field's name
     * @param value the value
     * @return the current multipart body
     */
    public MultipartBody field(String name, String value) {
        parameters.put(name, value);
        return this;
    }

    /**
     * Adds an upload file.
     *
     * @param name the field's name
     * @param file the file to upload
     * @return the current multipart body
     */
    public MultipartBody field(String name, File file) {
        this.parameters.put(name, file);
        hasFile = true;
        return this;
    }

    /**
     * Sets the basic authentication.
     *
     * @param username the username
     * @param password the password
     * @return the current multipart body
     */
    public MultipartBody basicAuth(String username, String password) {
        httpRequestObj.basicAuth(username, password);
        return this;
    }

    /**
     * Computes the request payload.
     *
     * @return the payload containing the declared fields and files.
     */
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

    /**
     * A helper method to build a list a {@link org.apache.http.NameValuePair} from a map of parameters.
     *
     * @param parameters the parameters
     * @return the list
     */
    public static List<NameValuePair> getList(Map<String, Object> parameters) {
        List<NameValuePair> result = new ArrayList<>();
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
