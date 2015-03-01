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
import org.wisdom.api.http.Context;

/**
 * Specific AssertJ Assertion for {@link org.wisdom.api.http.Context}.
 */
public class ContextAssert extends AbstractAssert<ContextAssert, Context> {

    protected ContextAssert(Context actual) {
        super(actual, ContextAssert.class);
    }

    /**
     * Creates a {@link ContextAssert}.
     *
     * @param actual the context
     * @return the created {@link ContextAssert}
     */
    public static ContextAssert assertThat(Context actual) {
        return new ContextAssert(actual);
    }

    /**
     * Checks that the actual context has a given parameter.
     *
     * @param key   the name
     * @param value the value
     * @return the current {@link ContextAssert}
     */
    public ContextAssert hasParameter(String key, String value) {
        isNotNull();
        if (actual.parameter(key) == null) {
            failWithMessage("Expected to have parameter <%s>", key);
        }

        if (!actual.parameter(key).equals(value)) {
            failWithMessage("Expected to have parameter <%s> set to <%s> but was <%s>", key, value,
                    actual.parameter(key));
        }

        return this;
    }

    /**
     * Checks that the actual context has a given entry in its session.
     *
     * @param key   the name
     * @param value the value
     * @return the current {@link ContextAssert}
     */
    public ContextAssert hasInSession(String key, String value) {
        isNotNull();
        SessionAssert.assertThat(actual.session()).containsEntry(key, value);

        return this;
    }

    /**
     * Checks that the actual context is multipart.
     *
     * @return the current {@link ContextAssert}
     */
    public ContextAssert isMultipart() {
        isNotNull();

        if (!actual.isMultipart()) {
            failWithMessage("Expected to be multipart");
        }

        return this;
    }

    /**
     * Checks that the actual context is not multipart.
     *
     * @return the current {@link ContextAssert}
     */
    public ContextAssert isNotMultipart() {
        isNotNull();

        if (actual.isMultipart()) {
            failWithMessage("Expected NOT to be multipart");
        }

        return this;
    }

    /**
     * Checks that the actual context has the given String in its body (i.e. content).
     *
     * @param inBody the body snippet
     * @return the current {@link ContextAssert}
     */
    public ContextAssert hasInBody(String inBody) {
        isNotNull();

        if (!actual.body().contains(inBody)) {
            failWithMessage("Expected body to contain <%s> but body is <%s>", inBody, actual.body());
        }

        return this;
    }

    /**
     * Checks that the actual context has a body matching the given regex.
     *
     * @param regex the regex
     * @return the current {@link ContextAssert}
     */
    public ContextAssert hasBodyMatch(String regex) {
        isNotNull();

        if (!actual.body().matches(regex)) {
            failWithMessage("Expected body to match <%s> but body is <%s>", regex, actual.body());
        }

        return this;
    }

    /**
     * Checks that the actual context has the given body.
     *
     * @param body the expected body
     * @return the current {@link ContextAssert}
     */
    public ContextAssert hasBody(String body) {
        isNotNull();

        if (!actual.body().equals(body)) {
            failWithMessage("Expected body to be <%s> but was <%s>", body, actual.body());
        }

        return this;
    }

    /**
     * Checks that the actual context has the given body.
     *
     * @param klass the body class
     * @param body  the expected body
     * @return the current {@link ContextAssert}
     */
    public <T> ContextAssert hasBody(Class<T> klass, T body) {
        isNotNull();

        if (!actual.body(klass).equals(body)) {
            failWithMessage("Expected body to be <%s> but was <%s>", body.toString(), actual.body(klass).toString());
        }

        return this;
    }
}
