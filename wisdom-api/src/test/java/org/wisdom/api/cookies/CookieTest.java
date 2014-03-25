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
package org.wisdom.api.cookies;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check cookies.
 */
public class CookieTest {

    @Test
    public void testSimpleCookies() {
        Cookie cookie = Cookie.cookie("cookie", "hello").build();
        assertThat(cookie.name()).isEqualTo("cookie");
        assertThat(cookie.value()).isEqualTo("hello");
    }

    @Test
    public void testFullCookies() {
        Cookie cookie = Cookie.cookie("cookie", "hello")
                .setComment("comment")
                .setDomain("domain")
                .setHttpOnly(false)
                .setMaxAge(1)
                .setPath("/path")
                .setSecure(true)
                .build();
        assertThat(cookie.name()).isEqualTo("cookie");
        assertThat(cookie.value()).isEqualTo("hello");
        assertThat(cookie.comment()).isEqualTo("comment");
        assertThat(cookie.domain()).isEqualTo("domain");
        assertThat(cookie.isHttpOnly()).isFalse();
        assertThat(cookie.maxAge()).isEqualTo(1);
        assertThat(cookie.path()).isEqualTo("/path");
        assertThat(cookie.isSecure()).isEqualTo(true);
    }

    @Test
    public void testCookieCopy() {
        Cookie original = Cookie.cookie("cookie", "hello")
                .setComment("comment")
                .setDomain("domain")
                .setHttpOnly(false)
                .setMaxAge(1)
                .setPath("/path")
                .setSecure(true)
                .build();
        Cookie cookie = Cookie.builder(original).setValue("bye").build();
        assertThat(cookie.name()).isEqualTo("cookie");
        assertThat(cookie.value()).isEqualTo("bye");
        assertThat(cookie.comment()).isEqualTo("comment");
        assertThat(cookie.domain()).isEqualTo("domain");
        assertThat(cookie.isHttpOnly()).isFalse();
        assertThat(cookie.maxAge()).isEqualTo(1);
        assertThat(cookie.path()).isEqualTo("/path");
        assertThat(cookie.isSecure()).isEqualTo(true);
    }

    @Test
    public void testEqualsAndHashCode() {
        Cookie original = Cookie.cookie("cookie", "hello")
                .setComment("comment")
                .setDomain("domain")
                .setHttpOnly(false)
                .setMaxAge(1)
                .setPath("/path")
                .setSecure(true)
                .build();
        Cookie cookie = Cookie.builder(original).build();
        assertThat(original).isEqualTo(cookie);
        assertThat(original).isNotSameAs(cookie);
        assertThat(original.hashCode()).isEqualTo(cookie.hashCode());
    }

}
