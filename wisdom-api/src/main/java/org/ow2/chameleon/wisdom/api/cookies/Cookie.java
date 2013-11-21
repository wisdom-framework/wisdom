package org.ow2.chameleon.wisdom.api.cookies;

import com.google.common.base.Preconditions;

/**
 * An HTTP cookie
 */
public class Cookie {

    /**
     * Property to configure in the application.conf file to set the prefix to all wisdom cookies.
     * {@literal wisdom} is used by default.
     */
    public static final String applicationCookiePrefix = "application.cookie.prefix";

    private final String name;
    private final String value;
    private final String comment;
    private final String domain;
    private final long maxAge;
    private final String path;
    private final boolean secure;
    private final boolean httpOnly;

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

    public String name() {
        return name;
    }

    public String value() {
        return value;
    }

    public String comment() {
        return comment;
    }

    public String domain() {
        return domain;
    }

    public long maxAge() {
        return maxAge;
    }

    public String path() {
        return path;
    }

    public boolean isSecure() {
        return secure;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Cookie cookie = (Cookie) o;

        if (httpOnly != cookie.httpOnly)
            return false;
        if (maxAge != cookie.maxAge)
            return false;
        if (secure != cookie.secure)
            return false;
        if (comment != null ? !comment.equals(cookie.comment)
                : cookie.comment != null)
            return false;
        if (domain != null ? !domain.equals(cookie.domain)
                : cookie.domain != null)
            return false;
        if (name != null ? !name.equals(cookie.name) : cookie.name != null)
            return false;
        if (path != null ? !path.equals(cookie.path) : cookie.path != null)
            return false;
        if (value != null ? !value.equals(cookie.value) : cookie.value != null)
            return false;

        return true;
    }

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

    public static Builder builder(String name, String value) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(value);
        return new Builder(name, value);
    }

    public static Builder builder(Cookie like) {
        return new Builder(like);
    }

    public static Builder cookie(String name, String value) {
        return builder(name, value);
    }

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

        public Cookie build() {
            return new Cookie(name, value, comment, domain, maxAge, path,
                    secure, httpOnly);
        }

        public Builder setValue(String value) {
            this.value = value;
            return this;
        }

        public Builder setComment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder setDomain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder setMaxAge(long maxAge) {
            this.maxAge = maxAge;
            return this;
        }

        public Builder setPath(String path) {
            this.path = path;
            return this;
        }

        public Builder setSecure(boolean secure) {
            this.secure = secure;
            return this;
        }

        public Builder setHttpOnly(boolean httpOnly) {
            this.httpOnly = httpOnly;
            return this;
        }
    }
}