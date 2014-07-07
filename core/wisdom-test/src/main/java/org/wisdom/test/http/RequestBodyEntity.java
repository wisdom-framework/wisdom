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
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;

/**
 * Represents the body contained in a HTTP Request ({@link org.wisdom.test.http.HttpRequest}).
 */
public class RequestBodyEntity extends BaseRequest implements Body {

    private Object body;

    /**
     * Creates the body.
     *
     * @param httpRequest the associated request
     */
    public RequestBodyEntity(HttpRequest httpRequest) {
        super(httpRequest);
    }

    /**
     * Sets the actual body content.
     *
     * @param body the content as String
     * @return the current {@link org.wisdom.test.http.RequestBodyEntity}
     */
    public RequestBodyEntity body(String body) {
        this.body = body;
        return this;
    }

    /**
     * Sets the actual body content.
     *
     * @param body the content as JSON
     * @return the current {@link org.wisdom.test.http.RequestBodyEntity}
     */
    public RequestBodyEntity body(JsonNode body) {
        this.body = body.toString();
        return this;
    }

    /**
     * @return the body. The type depends on the content.
     */
    public Object getBody() {
        return body;
    }

    /**
     * @return the HTTP Entity wrapping the body.
     */
    public HttpEntity getEntity() {
        return new StringEntity(body.toString(), UTF_8);
    }

}
