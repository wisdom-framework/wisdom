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

import org.wisdom.api.cookies.SessionCookie;
import org.wisdom.api.http.Context;
import org.wisdom.test.http.HttpResponse;
import org.wisdom.test.parents.Action;

/**
 * Wisdom specific AssertJ assertions.
 */
public class WisdomAssertions {

    /**
     * Give access to {@link org.wisdom.test.parents.Action.ActionResult} assertion.
     *
     * @param actual The tested ActionResult.
     * @return an ActionResultAssert assert.
     */
    public static ActionResultAssert assertThat(Action.ActionResult actual) {
        return ActionResultAssert.assertThat(actual);
    }

    /**
     * Give access to {@link org.wisdom.api.http.Context} assertion.
     *
     * @param actual The tested Context.
     * @return a ContextAssert assert.
     */
    public static ContextAssert assertThat(Context actual) {
        return ContextAssert.assertThat(actual);
    }

    /**
     * Give access to {@link org.wisdom.api.cookies.SessionCookie} assertion.
     *
     * @param actual The tested SessionCookie
     * @return a SessionCookieAssert assert.
     */
    public static SessionAssert assertThat(SessionCookie actual) {
        return SessionAssert.assertThat(actual);
    }

    /**
     * Give access to {@link org.wisdom.test.http.HttpResponse} assertion.
     *
     * @param actual The tested HttpResponse.
     * @param <T>    The HttpResponse type.
     * @return a HttpResponseAssert assert.
     */
    public static <T> HttpResponseAssert<T> assertThat(HttpResponse<T> actual) {
        return HttpResponseAssert.assertThat(actual);
    }

    /**
     * Give access to {@link org.wisdom.api.http.Status} assertion.
     *
     * @param actual The tested Status.
     * @return a StatusAssert assert.
     */
    public static StatusAssert assertStatus(Integer actual) {
        return StatusAssert.assertThat(actual);
    }
}
