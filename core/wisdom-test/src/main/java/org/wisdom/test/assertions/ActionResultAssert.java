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

import org.assertj.core.api.AbstractAssert;
import org.wisdom.test.parents.Action;

import java.nio.charset.Charset;

/**
 * Specific AssertJ Assertions for {@link org.wisdom.test.parents.Action.ActionResult}.
 */
public class ActionResultAssert extends AbstractAssert<ActionResultAssert, Action.ActionResult> {

    protected ActionResultAssert(Action.ActionResult actual) {
        super(actual, ActionResultAssert.class);
    }

    /**
     * Creates a {@link ActionResultAssert}.
     *
     * @param actual the result
     * @return the assertion
     */
    public static ActionResultAssert assertThat(Action.ActionResult actual) {
        return new ActionResultAssert(actual).isNotNull();
    }

    /**
     * Creates a {@link StatusAssert}.
     */
    public StatusAssert status() {
        isNotNull();
        return StatusAssert.assertThat(actual.getResult().getStatusCode());
    }

    /**
     * Asserts that the {@link org.wisdom.test.parents.Action.ActionResult} has the given HTTP status.
     *
     * @param statusCode the status code
     * @return the current {@link ActionResultAssert}
     */
    public ActionResultAssert hasStatus(Integer statusCode) {
        isNotNull();

        if (actual.getResult().getStatusCode() != statusCode) {
            failWithMessage("Expected status to be <%s> but was <%s>", statusCode, actual.getResult().getStatusCode());
        }

        return this;
    }

    /**
     * Asserts that the {@link org.wisdom.test.parents.Action.ActionResult} has the given content type.
     *
     * @param contentType the content type
     * @return the current {@link ActionResultAssert}
     */
    public ActionResultAssert hasContentType(String contentType) {
        isNotNull();

        if (!actual.getResult().getContentType().equals(contentType)) {
            failWithMessage("Expected content type to be <%s> but was <%s>", contentType,
                    actual.getResult().getContentType());
        }

        return this;
    }

    /**
     * Asserts that the {@link org.wisdom.test.parents.Action.ActionResult} has the given content type and charset.
     *
     * @param fullContentType the full content type (content type and charset)
     * @return the current {@link ActionResultAssert}
     */
    public ActionResultAssert hasFullContentType(String fullContentType) {
        isNotNull();

        if (!actual.getResult().getFullContentType().equals(fullContentType)) {
            failWithMessage("Expected content type to be <%s> but was <%s>", fullContentType,
                    actual.getResult().getFullContentType());
        }

        return this;
    }

    /**
     * Asserts that the {@link org.wisdom.test.parents.Action.ActionResult} has the given charset.
     *
     * @param charset the charset
     * @return the current {@link ActionResultAssert}
     */
    public ActionResultAssert hasCharset(Charset charset) {
        isNotNull();

        if (!actual.getResult().getCharset().equals(charset)) {
            failWithMessage("Expected charset to be <%s> but was <%s>", charset.displayName(),
                    actual.getResult().getCharset().displayName());
        }

        return this;
    }

    /**
     * Asserts that the {@link org.wisdom.test.parents.Action.ActionResult} has the given key-value in its session.
     *
     * @param key   the key
     * @param value the value
     * @return the current {@link ActionResultAssert}
     */
    public ActionResultAssert hasInSession(String key, String value) {
        isNotNull();
        SessionAssert.assertThat(actual.getContext().session()).containsEntry(key, value);

        return this;
    }

    /**
     * Asserts that the {@link org.wisdom.test.parents.Action.ActionResult} doest not have the given key in its session.
     *
     * @param key the key
     * @return the current {@link ActionResultAssert}
     */
    public ActionResultAssert doesNotHaveInSession(String key) {
        isNotNull();
        SessionAssert.assertThat(actual.getContext().session()).doesNotContain(key);
        return this;
    }

    /**
     * Asserts that the {@link org.wisdom.test.parents.Action.ActionResult} has an empty session.
     *
     * @return the current {@link ActionResultAssert}
     */
    public ActionResultAssert sessionIsEmpty() {
        isNotNull();
        SessionAssert.assertThat(actual.getContext().session()).isEmpty();

        return this;
    }

    /**
     * Asserts that the {@link org.wisdom.test.parents.Action.ActionResult}'s session is not empty.
     *
     * @return the current {@link ActionResultAssert}
     */
    public ActionResultAssert sessionIsNotEmpty() {
        isNotNull();
        SessionAssert.assertThat(actual.getContext().session()).isNotEmpty();

        return this;
    }

    /**
     * Asserts that the {@link org.wisdom.test.parents.Action.ActionResult} has the given content.
     *
     * @param klass the body's class
     * @param body  the body
     * @return the current {@link ActionResultAssert}
     */
    public <T> ActionResultAssert hasContent(Class<T> klass, T body) {
        isNotNull();
        Object content = actual.getResult().getRenderable().content();

        if (content == null) {
            failWithMessage("Expected content to not be null");
            return this;
        }

        if (! klass.isInstance(content)) {
            failWithMessage("Expected content to be a <%s> but was a <%s>", klass.getName(),
                    content.getClass().getName());
        }

        if (! content.equals(body)) {
            failWithMessage("Expected content to be <%s> but was <%s>", body,
                    content);
        }

        return this;
    }

    /**
     * Asserts that the {@link org.wisdom.test.parents.Action.ActionResult} has the given content. If the actual
     * content is not a String, {@code toString} is called before doing the comparison.
     *
     * @param content the content
     * @return the current {@link ActionResultAssert}
     */
    public ActionResultAssert hasContent(String content) {
        isNotNull();
        Object body = actual.getResult().getRenderable().content();

        if (body == null) {
            failWithMessage("Expected content to not be null");
            return this;
        }

        if (! content.equals(body.toString())) {
            failWithMessage("Expected content to be <%s> but was <%s>", content,
                    content);
        }

        return this;
    }

    /**
     * Asserts that the {@link org.wisdom.test.parents.Action.ActionResult} has the given String in its body. If the
     * actual body is not a String, {@code toString} is called before doing the comparison.
     *
     * @param inBody the text that should be found in the body
     * @return the current {@link ActionResultAssert}
     */
    public ActionResultAssert hasInContent(String inBody) {
        isNotNull();
        Object content = actual.getResult().getRenderable().content();

        if (content == null) {
            failWithMessage("Expected content to not be null");
            return this;
        }

        if (! content.toString().contains(inBody)) {
            failWithMessage("Expected content to contain <%s> but was <%s>", inBody,
                    content.toString());
        }

        return this;
    }

    /**
     * Asserts that the {@link org.wisdom.test.parents.Action.ActionResult} has a content matching the given regex.
     *
     * @param regex the regex
     * @return the current {@link ActionResultAssert}
     */
    public ActionResultAssert hasContentMatch(String regex) {
        isNotNull();
        Object content = actual.getResult().getRenderable().content();

        if (content == null) {
            failWithMessage("Expected content to not be null");
            return this;
        }

        if (! content.toString().matches(regex)) {
            failWithMessage("Expected content to match <%s> but was <%s>", regex,
                    content.toString());
        }

        return this;
    }

    /**
     * Asserts that the result is not null.
     *
     * @return the current {@link ActionResultAssert}
     */
    @Override
    public ActionResultAssert isNotNull() {
        super.isNotNull();

        if (actual.getResult() == null) {
            failWithMessage("Result should not be null");
        }

        return this;
    }

    /**
     * Asserts that the result contains the given header. The value is not checked.
     *
     * @param headerName the header name
     * @return the current {@link ActionResultAssert}
     */
    public ActionResultAssert hasHeader(String headerName) {
        super.isNotNull();

        if (! actual.getResult().getHeaders().containsKey(headerName)) {
            failWithMessage("Header <%s> expected in <%s>", headerName, actual.getResult().getHeaders());
        }

        return this;
    }
}
