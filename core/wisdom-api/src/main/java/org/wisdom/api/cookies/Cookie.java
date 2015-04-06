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

import com.google.common.base.Preconditions;

/**
 * An HTTP cookie.
 */
public class Cookie {

    /**
     * Property to configure in the application.conf file to set the prefix to all wisdom cookies.
     * {@literal wisdom} is used by default.
     */
    public static final String APPLICATION_COOKIE_PREFIX = "application.cookie.prefix";

    /**
     * Cookie's name.
     */
    private final String name;
    /**
     * Cookie's value.
     */
    private final String value;
    /**
     * Cookie's content.
     */
    private final String comment;
    /**
     * Cookie's domain.
     */
    private final String domain;
    /**
     * Cookie's max age.
     */
    private final long maxAge;
    /**
     * Cookie's path.
     */
    private final String path;
    /**
     * Is the cookies secure.
     */
    private final boolean secure;
    /**
     * Is the cookies only for HTTP.
     */
    private final boolean httpOnly;

    /**
     * Creates a new cookie.
     *
     * @param name     the name
     * @param value    the value
     * @param comment  the comment
     * @param domain   the domain
     * @param maxAge   the max age
     * @param path     the path
     * @param secure   whether the cookie is secure
     * @param httpOnly whether the cookie is only served on HTTP
     */
    public Cookie(String name,
                  String value,
                  String comment,
                  String domain,
                  long maxAge,
                  String path,
                  boolean secure,
                  boolean httpOnly) {
        this.name = name;
        this.value = value;
        this.comment = comment;
        this.domain = domain;
        this.maxAge = maxAge;
        this.path = path;
        this.secure = secure;
        this.httpOnly = httpOnly;
    }

    /**
     * Gets the cookie's name.
     *
     * @return the name
     */
    public String name() {
        return name;
    }

    /**
     * Gets the cookie's value.
     *
     * @return the value
     */
    public String value() {
        return value;
    }

    /**
     * Gets the cookie's comment.
     *
     * @return the comment
     */
    public String comment() {
        return comment;
    }

    /**
     * Gets the cookie's domain.
     *
     * @return the domain
     */
    public String domain() {
        return domain;
    }

    /**
     * Gets the cookie's max-age.
     *
     * @return the max-age
     */
    public long maxAge() {
        return maxAge;
    }

    /**
     * Gets the cookie's path.
     *
     * @return the path
     */
    public String path() {
        return path;
    }

    /**
     * Is the cookies secure?
     *
     * @return {@literal true} is the cookie is secure, {@literal false} otherwise.
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * Is the cookies served only on HTTP?
     *
     * @return {@literal true} is the cookie is only served on HTTP, {@literal false} otherwise.
     */
    public boolean isHttpOnly() {
        return httpOnly;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Cookie cookie = (Cookie) o;

        return httpOnly == cookie.httpOnly
                && maxAge == cookie.maxAge
                && secure == cookie.secure
                && !(comment != null ? !comment.equals(cookie.comment) : cookie.comment != null)
                && !(domain != null ? !domain.equals(cookie.domain) : cookie.domain != null)
                && !(name != null ? !name.equals(cookie.name) : cookie.name != null)
                && !(path != null ? !path.equals(cookie.path) : cookie.path != null)
                && !(value != null ? !value.equals(cookie.value) : cookie.value != null);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        result = 31 * result + (domain != null ? domain.hashCode() : 0);
        result = 31 * result + (int) maxAge;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (secure ? 1 : 0);
        result = 31 * result + (httpOnly ? 1 : 0);
        return result;
    }

    /**
     * Gets a new cookie's builder.
     *
     * @param name  the cookie's name
     * @param value the cookie's value
     * @return the new builder
     */
    public static Builder builder(String name, String value) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(value);
        return new Builder(name, value);
    }

    /**
     * Gets a new cookie's builder copying the given cookie.
     *
     * @param like the cookie to copy
     * @return the new builder
     */
    public static Builder builder(Cookie like) {
        return new Builder(like);
    }

    /**
     * Equivalent to {@link #builder(String, String)}.
     *
     * @param name  the cookie's name
     * @param value the cookie's value
     * @return the new builder
     */
    public static Builder cookie(String name, String value) {
        return builder(name, value)
                // Populate default:
                .setPath("/").setHttpOnly(true).setSecure(false).setMaxAge(3600);
    }

    /**
     * A builder to create a new cookie.
     */
    public static class Builder {
        private final String name;
        private String value;
        private String comment;
        private String domain;
        private long maxAge = -1;
        private String path = "/";
        private boolean secure;
        private boolean httpOnly;

        private Builder(String name, String value) {
            this.name = name;
            this.value = value;
        }

        private Builder(Cookie like) {
            name = like.name;
            value = like.value;
            comment = like.comment;
            domain = like.domain;
            maxAge = like.maxAge;
            path = like.path;
            secure = like.secure;
            httpOnly = like.httpOnly;
        }

        /**
         * Creates the cookie.
         *
         * @return the cookie
         */
        public Cookie build() {
            return new Cookie(name, value, comment, domain, maxAge, path,
                    secure, httpOnly);
        }

        /**
         * Sets the cookie's value.
         *
         * @param value the value
         * @return the current builder
         */
        public Builder setValue(String value) {
            this.value = value;
            return this;
        }

        /**
         * Sets the cookie's comment.
         *
         * @param comment the comment
         * @return the current builder
         */
        public Builder setComment(String comment) {
            this.comment = comment;
            return this;
        }

        /**
         * Sets the cookie's domain.
         *
         * @param domain the domain
         * @return the current builder
         */
        public Builder setDomain(String domain) {
            this.domain = domain;
            return this;
        }

        /**
         * Sets the cookie's max age.
         *
         * @param maxAge the max age in seconds.
         * @return the current builder
         */
        public Builder setMaxAge(long maxAge) {
            this.maxAge = maxAge;
            return this;
        }

        /**
         * Sets the cookie's path.
         *
         * @param path the path
         * @return the current builder
         */
        public Builder setPath(String path) {
            this.path = path;
            return this;
        }

        /**
         * Sets the cookie's secure flag.
         *
         * @param secure the secure flag
         * @return the current builder
         */
        public Builder setSecure(boolean secure) {
            this.secure = secure;
            return this;
        }

        /**
         * Sets the cookie's HTTP only flag.
         *
         * @param httpOnly the http only flag
         * @return the current builder
         */
        public Builder setHttpOnly(boolean httpOnly) {
            this.httpOnly = httpOnly;
            return this;
        }
    }
}
