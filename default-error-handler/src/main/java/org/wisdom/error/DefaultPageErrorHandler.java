package org.wisdom.error;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.DefaultController;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.api.interception.Filter;
import org.wisdom.api.interception.RequestContext;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.Router;
import org.wisdom.api.templates.Template;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Wisdom default error handler.
 */
@Component
@Provides(specifications = Filter.class)
@Instantiate
public class DefaultPageErrorHandler extends DefaultController implements Filter {

    public static final Logger LOGGER = LoggerFactory.getLogger("wisdom-error");


    public static final Pattern ALL_REQUESTS = Pattern.compile("/.*");
    @Requires(filter = "(name=error/404)", proxy = false, optional = true, id = "404")
    private Template noroute;
    @Requires(filter = "(name=error/500)", proxy = false, optional = true, id = "500")
    private Template internalerror;
    @Requires
    private Router router;

    public Result onError(Context context, Route route, Throwable e) {
        Throwable localException = e;
        if (internalerror == null) {
            return null;
        }

        if (localException instanceof InvocationTargetException) {
            localException = ((InvocationTargetException) localException).getTargetException();
        }

        String cause = "";
        StackTraceElement[] stack = localException.getStackTrace();
        if (localException.getCause() != null) {
            cause = localException.getCause().getMessage();
            stack = localException.getCause().getStackTrace();
        } else {
            cause = localException.getMessage();
        }
        String fileName = null;
        int line = -1;
        if (stack != null && stack.length != 0) {
            fileName = stack[0].getFileName();
            line = stack[0].getLineNumber();
        }

        List<StackTraceElement> cleaned = cleanup(stack);

        return internalServerError(render(internalerror,
                "route", route,
                "context", context,
                "exception", localException,
                "message", localException.getMessage(),
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

    /**
     * The interception method. The method should call {@link org.wisdom.api.interception.RequestContext#proceed()}
     * to call the next interceptor. Without this call it cuts the chain.
     *
     * @param route
     * @param context the filter context
     * @return the result
     * @throws Throwable if anything bad happen
     */
    @Override
    public Result call(Route route, RequestContext context) throws Throwable {
        try {
            Result result = context.proceed();
            if (result.getStatusCode() == 404) {
                if (noroute == null) {
                    return result;
                } else {
                    return Results.notFound(render(noroute,
                            "method", route.getHttpMethod(),
                            "uri", route.getUrl(),
                            "routes", router.getRoutes()
                    ));
                }
            }
            return result;
        } catch (Exception e) {
            LOGGER.error("An exception occurred while processing request {} {}", route.getHttpMethod(),
                    route.getUrl(), e);
            return onError(context.context(), route, e);
        }
    }

    /**
     * Gets the Regex Pattern used to determine whether the route is handled by the filter or not.
     * Notice that the router are caching these patterns and so cannot changed.
     */
    @Override
    public Pattern uri() {
        return ALL_REQUESTS;
    }

    /**
     * Gets the filter priority, determining the position of the filter in the filter chain. Filter with a high
     * priority are called first. Notice that the router are caching these priorities and so cannot changed.
     * <p/>
     * It is heavily recommended to allow configuring the priority from the Application Configuration.
     *
     * @return the priority
     */
    @Override
    public int priority() {
        return 1000;
    }
}
