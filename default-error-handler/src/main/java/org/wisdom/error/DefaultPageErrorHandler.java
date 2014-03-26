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
 * This component exposes a {@link org.wisdom.api.interception.Filter} handling unbound routes and internal errors.
 */
@Component
@Provides(specifications = Filter.class)
@Instantiate
public class DefaultPageErrorHandler extends DefaultController implements Filter {

    /**
     * The logger used by the filter.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("wisdom-error");

    /**
     * The pattern to interceptor all requests.
     */

    public static final Pattern ALL_REQUESTS = Pattern.compile("/.*");

    /**
     * The 404 template.
     */
    @Requires(filter = "(name=error/404)", proxy = false, optional = true, id = "404")
    private Template noroute;

    /**
     * The 500 template.
     */
    @Requires(filter = "(name=error/500)", proxy = false, optional = true, id = "500")
    private Template internalerror;

    /**
     * The router.
     */
    @Requires
    private Router router;

    /**
     * Generates the error page.
     *
     * @param context the context.
     * @param route   the route
     * @param e       the thrown error
     * @return the HTTP result serving the error page
     */
    private Result onError(Context context, Route route, Throwable e) {
        Throwable localException = e;

        // If the template is not there, just wrap the exception within a JSON Object.
        if (internalerror == null) {
            return internalServerError(e);
        }


        // Manage ITE
        if (localException instanceof InvocationTargetException) {
            localException = ((InvocationTargetException) localException).getTargetException();
        }

        // Retrieve the cause if any.
        String cause;
        StackTraceElement[] stack;
        if (localException.getCause() != null) {
            cause = localException.getCause().getMessage();
            stack = localException.getCause().getStackTrace();
        } else {
            cause = localException.getMessage();
            stack = localException.getStackTrace();
        }

        // Retrieve the file name.
        String fileName = null;
        int line = -1;
        if (stack != null && stack.length != 0) {
            fileName = stack[0].getFileName();
            line = stack[0].getLineNumber();
        }

        // Remove iPOJO trace from the stack trace.
        List<StackTraceElement> cleaned = cleanup(stack);

        // We are good to go !
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

    /**
     * Removes the '__M_' iPOJO trace from the stack trace.
     * @param stack the original stack trace
     * @return the cleaned stack trace
     */
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
     * The interception method. When the request is unbound, generate a 404 page. When the controller throws an
     * exception generates a 500 page.
     *
     * @param route the route
     * @param context the filter context
     * @return the generated result.
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
