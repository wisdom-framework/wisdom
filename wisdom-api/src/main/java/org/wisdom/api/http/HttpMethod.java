package org.wisdom.api.http;

/**
 * The HTTP Method.
 */
public enum HttpMethod {

    HEAD,

    GET,
    POST,
    PUT,
    DELETE,

    OPTIONS,
    PATCH;

    public static HttpMethod from(String method) {
        return HttpMethod.valueOf(method.toUpperCase());
    }

}
