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
package org.wisdom.test.assertions;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.primitives.Doubles;
import org.assertj.core.api.AbstractAssert;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.test.http.HttpResponse;

import java.nio.charset.Charset;

/**
 * Specific AssertJ Assertion for {@link org.wisdom.test.http.HttpResponse}.
 */
public class HttpResponseAssert<T> extends AbstractAssert<HttpResponseAssert<T>, HttpResponse<T>> {

    protected HttpResponseAssert(HttpResponse<T> actual) {
        super(actual, HttpResponseAssert.class);
    }

    /**
     * Creates an {@link HttpResponseAssert} instance.
     *
     * @param actual the HTTP Response
     * @param <T>    the type of content
     * @return the created instance
     */
    public static <T> HttpResponseAssert<T> assertThat(HttpResponse<T> actual) {
        return new HttpResponseAssert<>(actual);
    }

    /**
     * Checks that the Http Response contains a header named `key` and has the value `value`.
     *
     * @param key   the expected header name
     * @param value the expected value
     * @return the current {@link HttpResponseAssert}
     */
    public HttpResponseAssert<T> hasHeader(String key, String value) {
        isNotNull().hasHeader(key);

        if (!value.equals(actual.headers().get(key))) {
            failWithMessage("Expected header to contain entry <%s, %s> but value was <%s>", key, value,
                    String.valueOf(actual.headers().get(key)));
        }

        return this;
    }

    /**
     * Checks that the Http Response contains a header named `key`.
     *
     * @param key the expected header name
     * @return the current {@link HttpResponseAssert}
     */
    public HttpResponseAssert<T> hasHeader(String key) {
        isNotNull();

        if (actual.headers().get(key) == null) {
            failWithMessage("Expected headers to contain key <%s>", key);
        }

        return this;
    }

    /**
     * Checks that the Http Response contains a cookie named `cookieName`.
     *
     * @param cookieName the expected cookie name
     * @return the current {@link HttpResponseAssert}
     */
    public HttpResponseAssert<T> hasCookie(String cookieName) {
        isNotNull();
        if (actual.cookie(cookieName) == null) {
            failWithMessage("Expected to contain a cookie with name <%s>", cookieName);
        }

        return this;
    }

    /**
     * Checks that the Http Response contains a cookie named `cookieName` and has the value `value`.
     *
     * @param cookieName the expected cookie name
     * @param value      the expected value
     * @return the current {@link HttpResponseAssert}
     */
    public HttpResponseAssert<T> hasCookie(String cookieName, String value) {
        isNotNull().hasCookie(cookieName);

        if (!actual.cookie(cookieName).getValue().equalsIgnoreCase(value)) {
            failWithMessage("Expected to contain a cookie with name <%s> and with value <%s> but the contained value " +
                    "was <%s>", cookieName, value, actual.cookie(cookieName).getValue());
        }

        return this;
    }

    /**
     * Checks that the Http Response has the given `content-type`.
     *
     * @param contentType the expected content type
     * @return the current {@link HttpResponseAssert}
     */
    public HttpResponseAssert<T> hasContentType(String contentType) {
        isNotNull();

        if (!actual.contentType().equals(contentType)) {
            failWithMessage("Expected content type to be <%s> but was <%s>", contentType, actual.contentType());
        }

        return this;
    }

    /**
     * Checks that the Http Response contains the given String in its body. This method works only for String results.
     *
     * @param inBody expected snippet
     * @return the current {@link HttpResponseAssert}
     */
    public HttpResponseAssert<T> hasInBody(String inBody) {
        isNotNull();

        if (!(actual.body() instanceof String)) {
            failWithMessage("Body is not an instance of String, body is <%s>", actual.body().toString());

        }

        if (!((String) actual.body()).contains(inBody)) {
            failWithMessage("Expected body to contain <%s>, but body is <%s>", inBody, actual.body().toString());
        }

        return this;
    }

    /**
     * Checks that the Http Response does not contains the given String in its body. This method works only for String
     * results.
     *
     * @param inBody expected snippet
     * @return the current {@link HttpResponseAssert}
     */
    public HttpResponseAssert<T> hasNotInBody(String inBody) {
        isNotNull();

        if (!(actual.body() instanceof String)) {
            failWithMessage("Body is not an instance of String, body is <%s>", actual.body().toString());

        }

        if (((String) actual.body()).contains(inBody)) {
            failWithMessage("Expected body to NOT contain <%s>, but body is <%s>", inBody, actual.body().toString());
        }

        return this;
    }

    /**
     * Checks that the Http Response body matches the given regex. This method works only for String results.
     *
     * @param regex the regex
     * @return the current {@link HttpResponseAssert}
     */
    public HttpResponseAssert<T> bodyMatches(String regex) {
        isNotNull();

        if (!(actual.body() instanceof String)) {
            failWithMessage("Body is not an instance of String, body is <%s>", actual.body().toString());

        }

        if (!((String) actual.body()).matches(regex)) {
            failWithMessage("Expected body to match <%s>, but body is <%s>", regex, actual.body().toString());
        }

        return this;
    }

    /**
     * Checks that the Http Response has the given body.
     *
     * @param body the expected body
     * @return the current {@link HttpResponseAssert}
     */
    public HttpResponseAssert<T> hasBody(T body) {
        isNotNull();

        if (!body.equals(actual.body())) {
            failWithMessage("Expected body to be <%s> but was <%s>", body.toString(), actual.body().toString());
        }

        return this;
    }

    /**
     * Checks that the Http Response body has the given length.
     *
     * @param length the expected length
     * @return the current {@link HttpResponseAssert}
     */
    public HttpResponseAssert<T> hasLength(int length) {
        isNotNull();

        if (actual.length() != length) {
            failWithMessage("Expected length to be <%s> but was <%s>", length, actual.length());
        }

        return this;
    }

    /**
     * Checks that the Http Response body has the given charset.
     *
     * @param charset the expected charset
     * @return the current {@link HttpResponseAssert}
     */
    public HttpResponseAssert<T> hasCharset(Charset charset) {
        isNotNull();

        if (!actual.charset().equals(charset.toString())) {
            failWithMessage("Expected charset to be <%s> but was <%s>", charset, actual.charset());
        }

        return this;
    }

    /**
     * Checks that the actual HTTP response has at least one headers.
     *
     * @return the current {@link HttpResponseAssert}
     */
    public HttpResponseAssert<T> hasHeaders() {
        isNotNull();
        if (actual.headers().isEmpty()) {
            failWithMessage("Expected headers to be not empty");
        }
        return this;
    }

    /**
     * Checks that the actual HTTP response has a body (content).
     *
     * @return the current {@link HttpResponseAssert}
     */
    public HttpResponseAssert<T> hasBody() {
        isNotNull();
        if (actual.body() == null) {
            failWithMessage("Expected content to be not null");
        }
        if (actual.body().toString().length() == 0) {
            failWithMessage("Expected content to be not empty");
        }
        return this;
    }

    /**
     * Checks that the actual HTTP response has the given HTTP Status.
     *
     * @param status the expected status
     * @return the current {@link HttpResponseAssert}
     */
    public HttpResponseAssert<T> hasStatus(int status) {
        isNotNull();
        if (status != actual.code()) {
            failWithMessage("Expected status code to be <%s> but was <%s>", status, actual.code());
        }
        return this;
    }

    /**
     * Checks that the Http Response contains a textual value at the given Json Pointer (JavaScript Object Notation
     * (JSON) Pointer). The Json Pointer syntax is described in the
     * <a href="https://tools.ietf.org/html/rfc6901">RFC 6901</a>.
     *
     * @param path  the Json Pointer
     * @param value the expected value
     * @return the current {@link HttpResponseAssert}
     */
    public HttpResponseAssert<T> hasJsonTextField(String path, String value) {
        isNotNull();
        isJson();

        final JsonNode node = ((JsonNode) actual.body()).at(path);
        if (node.isMissingNode()) {
            failWithMessage("Expected node pointed by <%s> to be present in <%s>", path, actual.body().toString());
        }
        if (!node.isTextual()) {
            failWithMessage("Expected node pointed by <%s> to be textual", path);
        }

        if (!node.asText().equals(value)) {
            failWithMessage("Expected node pointed by <%s> to be <%s> but was <%s>", path, value, node.asText());
        }

        return this;
    }

    /**
     * Checks that the Http Response contains a textual value at the given Json Pointer (JavaScript Object Notation
     * (JSON) Pointer) containing the given values. The Json Pointer syntax is described in the
     * <a href="https://tools.ietf.org/html/rfc6901">RFC 6901</a>.
     *
     * @param path   the Json Pointer
     * @param values the expected values
     * @return the current {@link HttpResponseAssert}
     */
    public HttpResponseAssert<T> hasJsonTextFieldContaining(String path, String... values) {
        isNotNull();
        isJson();

        final JsonNode node = ((JsonNode) actual.body()).at(path);
        if (node.isMissingNode()) {
            failWithMessage("Expected node pointed by <%s> to be present in <%s>", path, actual.body().toString());
        }
        if (!node.isTextual()) {
            failWithMessage("Expected node pointed by <%s> to be textual", path);
        }

        final String s = node.asText();
        for (String v : values) {
            if (!s.contains(v)) {
                failWithMessage("Expected node pointed by <%s> to contain <%s>", path, v);
            }
        }

        return this;
    }

    /**
     * Checks that the Http Response contains a numeric value at the given Json Pointer (JavaScript Object Notation
     * (JSON) Pointer). The Json Pointer syntax is described in the
     * <a href="https://tools.ietf.org/html/rfc6901">RFC 6901</a>.
     *
     * @param path  the Json Pointer
     * @param value the expected value
     * @return the current {@link HttpResponseAssert}
     */
    public HttpResponseAssert<T> hasJsonNumericField(String path, int value) {
        isNotNull();
        isJson();

        final JsonNode node = ((JsonNode) actual.body()).at(path);
        if (node.isMissingNode()) {
            failWithMessage("Expected node pointed by <%s> to be present in <%s>", path, actual.body().toString());
        }
        if (node.asInt() != value) {
            failWithMessage("Expected node pointed by <%s> to be <%s> but was <%s>", path, value, node.asInt());
        }

        return this;
    }

    /**
     * Checks that the Http Response contains a numeric value at the given Json Pointer (JavaScript Object Notation
     * (JSON) Pointer). The Json Pointer syntax is described in the
     * <a href="https://tools.ietf.org/html/rfc6901">RFC 6901</a>.
     *
     * @param path  the Json Pointer
     * @param value the expected value
     * @return the current {@link HttpResponseAssert}
     */
    public HttpResponseAssert<T> hasJsonNumericField(String path, long value) {
        isNotNull();
        isJson();

        final JsonNode node = ((JsonNode) actual.body()).at(path);
        if (node.isMissingNode()) {
            failWithMessage("Expected node pointed by <%s> to be present in <%s>", path, actual.body().toString());
        }
        if (node.asLong() != value) {
            failWithMessage("Expected node pointed by <%s> to be <%s> but was <%s>", path, value, node.asLong());
        }

        return this;
    }

    /**
     * Checks that the Http Response contains a numeric value at the given Json Pointer (JavaScript Object Notation
     * (JSON) Pointer). The Json Pointer syntax is described in the
     * <a href="https://tools.ietf.org/html/rfc6901">RFC 6901</a>.
     *
     * @param path  the Json Pointer
     * @param value the expected value
     * @return the current {@link HttpResponseAssert}
     */
    public HttpResponseAssert<T> hasJsonNumericField(String path, double value) {
        isNotNull();
        isJson();

        final JsonNode node = ((JsonNode) actual.body()).at(path);
        if (node.isMissingNode()) {
            failWithMessage("Expected node pointed by <%s> to be present in <%s>", path, actual.body().toString());
        }
        // We cannot compare double directly as it may lead to precision issues.
        if (Doubles.compare(node.asDouble(), value) == 0) {
            failWithMessage("Expected node pointed by <%s> to be <%s> but was <%s>", path, value, node.asDouble());
        }

        return this;
    }

    /**
     * Checks that the actual HTTP response has returned a content type that is {@code applicaiton/json}.
     *
     * @return the current {@link HttpResponseAssert}
     */
    public HttpResponseAssert<T> isJson() {
        return isNotNull().hasContentType(MimeTypes.JSON);
    }


}
