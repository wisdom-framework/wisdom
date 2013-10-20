package org.ow2.chameleon.wisdom.engine.servlet;

import org.ow2.chameleon.wisdom.api.http.Response;

import javax.servlet.http.HttpServletRequest;

/**
 * Implementation of responses based on Http Servlet Response
 */
public class ResponseFromServlet extends Response {

    private final HttpServletRequest httpServletResponse;
    private final ServiceAccessor services;

    public ResponseFromServlet(ServiceAccessor accessor, HttpServletRequest response) {
        httpServletResponse = response;
        services = accessor;
    }


}
