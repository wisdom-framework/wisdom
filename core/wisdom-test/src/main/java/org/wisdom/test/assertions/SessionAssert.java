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
import org.wisdom.api.cookies.SessionCookie;

/**
 * Specific AssertJ assertion for {@link org.wisdom.api.cookies.SessionCookie}.
 */
public class SessionAssert extends AbstractAssert<SessionAssert, SessionCookie> {

    /**
     * Creates a {@link SessionAssert}.
     *
     * @param actual the session cookie
     */
    protected SessionAssert(SessionCookie actual) {
        super(actual, SessionAssert.class);
    }

    /**
     * Creates a {@link SessionAssert}. Tests should use this method.
     *
     * @param actual the session cookie
     * @return the session assert
     */
    public static SessionAssert assertThat(SessionCookie actual) {
        return new SessionAssert(actual);
    }

    /**
     * Enforces that the session is empty.
     *
     * @return the current session assert.
     */
    public SessionAssert isEmpty() {
        isNotNull();

        if (!actual.isEmpty()) {
            failWithMessage("Expected session to be empty");
        }

        return this;
    }

    /**
     * Enforces that the session is not empty.
     *
     * @return the current session assert.
     */
    public SessionAssert isNotEmpty() {
        isNotNull();

        if (actual.isEmpty()) {
            failWithMessage("Expected session not to be empty");
        }

        return this;
    }

    /**
     * Enforces that the session contains the given entry.
     *
     * @param key   the key
     * @param value the value
     * @return the current session assert.
     */
    public SessionAssert containsEntry(String key, String value) {
        isNotNull();
        isNotEmpty();

        if (!value.equals(actual.get(key))) {
            failWithMessage("Expected session to contain entry <%s, %s> but value was <%s>", key, value,
                    String.valueOf(actual.get(key)));
        }

        return this;
    }

    /**
     * Enforces that the session does not contain the given entry.
     *
     * @param key the key
     * @return the current session assert.
     */
    public SessionAssert doesNotContain(String key) {
        isNotNull();
        isNotEmpty();

        if (actual.getData().containsKey(key)) {
            failWithMessage("Expected session to not contain key '%s'", key);
        }

        return this;
    }
}
