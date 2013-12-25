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
