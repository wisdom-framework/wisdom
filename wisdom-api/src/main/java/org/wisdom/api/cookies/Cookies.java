package org.wisdom.api.cookies;

/**
 * HTTP Cookies set
 */
public interface Cookies {

    /**
     * @param name Name of the cookie to retrieve
     * @return the cookie that is associated with the given name, or null if there is no such cookie
     */
    public Cookie get(String name);

}
