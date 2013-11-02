package org.ow2.chameleon.wisdom.error;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.ow2.chameleon.wisdom.api.DefaultController;
import org.ow2.chameleon.wisdom.api.error.ErrorHandler;
import org.ow2.chameleon.wisdom.api.http.Context;
import org.ow2.chameleon.wisdom.api.http.HttpMethod;
import org.ow2.chameleon.wisdom.api.http.Result;
import org.ow2.chameleon.wisdom.api.router.Route;
import org.ow2.chameleon.wisdom.api.router.Router;
import org.ow2.chameleon.wisdom.api.templates.Template;

import java.util.ArrayList;
import java.util.List;

/**
 * Wisdom default error handler.
 */
@Component
@Provides(specifications = ErrorHandler.class)
@Instantiate
public class DefaultPageErrorHandler extends DefaultController implements ErrorHandler {

    @Requires(filter = "(name=error/404)", proxy = false, optional = true)
    private Template noroute;
    @Requires(filter = "(name=error/500)", proxy = false, optional = true)
    private Template internalerror;
    @Requires
    private Router router;

    @Override
    public Result onNoRoute(HttpMethod method, String uri) {
        if (noroute == null) {
            return null;
        } else {
            return notFound(render(noroute,
                    "method", method,
                    "uri", uri,
                    "routes", router.getRoutes()
            ));
        }
    }

    @Override
    public Result onError(Context context, Route route, Throwable e) {
        if (internalerror == null) {
            return null;
        }

        String cause = "";
        if (e.getCause() != null) {
            cause = e.getCause().getMessage();
        }
        String fileName = null;
        int line = -1;
        StackTraceElement[] stack = e.getStackTrace();
        if (stack != null  && stack.length != 0) {
            fileName = stack[0].getFileName();
            line = stack[0].getLineNumber();
        }

        List<StackTraceElement> cleaned = cleanup(stack);

        return internalServerError(render(internalerror,
                "route", route,
                "context", context,
                "exception", e,
                "message", e.getMessage(),
                "cause", cause,
                "file", fileName,
                "line", line,
                "stack", cleaned));
    }

    private List<StackTraceElement> cleanup(StackTraceElement[] stack) {
        List<StackTraceElement> elements = new ArrayList<>();
        if (stack == null) {
            return elements;
        } else {
            for (StackTraceElement element : stack) {
                // Remove all iPOJO calls.
                if (element.getMethodName().startsWith("__M_")) {
                    String newMethodName = element.getMethodName().substring("__M_".length());
                    elements.add(new StackTraceElement(element.getClassName(), newMethodName, element.getFileName(),
                            element.getLineNumber()));
                } else if (element.getLineNumber() >= 0) {
                    elements.add(element);
                }
            }
        }
        return elements;
    }
}
