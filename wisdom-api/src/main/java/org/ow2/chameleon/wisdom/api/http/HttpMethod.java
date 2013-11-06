package org.ow2.chameleon.wisdom.api.http;

/**
 * The HTTP Method.
 */
public enum HttpMethod {

    GET,
    POST,
    PUT,
    DELETE;

    public static HttpMethod from(String method) {
        return HttpMethod.valueOf(method.toUpperCase());
    }

}
