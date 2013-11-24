package org.ow2.chameleon.wisdom.api.http;

/**
 * The HTTP Method.
 */
public enum HttpMethod {

    HEAD,

    GET,
    POST,
    PUT,
    DELETE,

    OPTIONS;

    public static HttpMethod from(String method) {
        return HttpMethod.valueOf(method.toUpperCase());
    }

}
