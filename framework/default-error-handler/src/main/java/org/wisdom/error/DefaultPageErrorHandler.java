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

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FileUtils;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.DefaultController;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.*;
import org.wisdom.api.interception.Filter;
import org.wisdom.api.interception.RequestContext;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.Router;
import org.wisdom.api.templates.Template;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
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
    public static final String NO_CONTENT = "";

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
     * The 500 template.
     */
    @Requires(filter = "(name=error/pipeline)", proxy = false, optional = true, id = "pipeline")
    protected Template pipeline;

    /**
     * The router.
     */
    @Requires
    protected Router router;

    @Requires
    protected ApplicationConfiguration configuration;

    @Requires
    protected Json json;

    /**
     * The directory where error report (created by watchers) are created.
     */
    private File pipelineErrorDirectory;


    /**
     * Methods called when this component is starting. It builds the pipeline error directory from the
     * configuration's base directory.
     */
    @Validate
    public void start() {
        pipelineErrorDirectory = new File(configuration.getBaseDir().getParentFile(), "pipeline");
    }

    /**
     * @return the first error file contained in the pipeline error's directory, {@literal null} if none. Notice that
     * the returned file may depend on the operating system.
     */
    public File getFirstErrorFile() {
        if (!pipelineErrorDirectory.isDirectory()) {
            return null;
        }
        // We make the assumption that the directory only store error report and nothing else.
        File[] files = pipelineErrorDirectory.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        // Return the first error report.
        return files[0];
    }

    /**
     * Generates the error page.
     *
     * @param context the context.
     * @param route   the route
     * @param e       the thrown error
     * @return the HTTP result serving the error page
     */
    private Result renderInternalError(Context context, Route route, Throwable e) {
        Throwable localException;

        // If the template is not there, just wrap the exception within a JSON Object.
        if (internalerror == null) {
            return internalServerError(e);
        }


        // Manage ITE
        if (e instanceof InvocationTargetException) {
            localException = ((InvocationTargetException) e).getTargetException();
        } else {
            localException = e;
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
     *
     * @param stack the original stack trace
     * @return the cleaned stack trace
     */
    public static List<StackTraceElement> cleanup(StackTraceElement[] stack) {
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
     * @param route   the route
     * @param context the filter context
     * @return the generated result.
     * @throws Exception if anything bad happen
     */
    @Override
    public Result call(Route route, RequestContext context) throws Exception {

        // Manage the error file.
        // In dev mode, if the watching pipeline throws an error, this error is stored in the error.json file
        // If this file exist, we should display a page telling the user that something terrible happened in his last
        // change.
        if (configuration.isDev() && context.request().accepts(MimeTypes.HTML) && pipeline != null) {
            // Check whether the error file is there
            File error = getFirstErrorFile();
            if (error != null) {
                logger().debug("Error file detected, preparing rendering");
                try {
                    return renderPipelineError(error);
                } catch (IOException e) {
                    LOGGER.error("An exception occurred while generating the error page for {} {}",
                            route.getHttpMethod(),
                            route.getUrl(), e);
                    return renderInternalError(context.context(), route, e);
                }
            }
        }

        try {
            Result result = context.proceed();
            if (result.getStatusCode() == NOT_FOUND) {
                // HEAD Implementation.
                if (route.getHttpMethod() == HttpMethod.HEAD) {
                    return switchToGet(route, context);
                }
                return renderNotFound(route, result);
            }
            return result;
        } catch (Exception e) {
            LOGGER.error("An exception occurred while processing request {} {}", route.getHttpMethod(),
                    route.getUrl(), e);
            return renderInternalError(context.context(), route, e);
        }
    }

    private Result renderPipelineError(File error) throws IOException {
        String content = FileUtils.readFileToString(error);
        ObjectNode node = (ObjectNode) json.parse(content);

        String message = node.get("message").asText();
        String file = null;
        if (node.get("file") != null) {
            file = node.get("file").asText();
        }
        String watcher = node.get("watcher").asText();
        int line = -1;
        int character = -1;

        if (node.get("line") != null) {
            line = node.get("line").asInt();
        }

        String title = null;
        if (node.get("title") != null) {
            title = node.get("title").asText();
        }

        if (node.get("character") != null) {
            character = node.get("character").asInt();
        }

        String fileContent = "";
        InterestingLines lines = null;

        File source = null;
        if (file != null) {
            source = new File(file);
            if (source.isFile()) {
                fileContent = FileUtils.readFileToString(source);
                if (line != -1 && line != 0) {
                    lines = extractInterestedLines(fileContent, line, 4);
                }
            }
        }

        return internalServerError(render(pipeline,
                "title", title,
                "message", message,
                "source", source,
                "line", line,
                "character", character,
                "lines", lines,
                "watcher", watcher));
    }

    private Result renderNotFound(Route route, Result result) {
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

    private Result switchToGet(Route route, RequestContext context) {
        // A HEAD request was emitted, and unfortunately, no action handled it. Switch to GET.
        Route getRoute = router.getRouteFor(HttpMethod.GET, route.getUrl());
        if (getRoute == null || getRoute.isUnbound()) {
            return renderNotFound(route, Results.notFound());
        } else {
            try {
                Result result = getRoute.invoke();
                // Replace the content with NO_CONTENT but we need to preserve the headers (CONTENT-TYPE and
                // CONTENT-LENGTH). These headers may not have been set, so we searches values in the renderable
                // objects too.
                final Renderable renderable = result.getRenderable();
                final String type = result.getHeaders().get(HeaderNames.CONTENT_TYPE);
                final String length = result.getHeaders().get(HeaderNames.CONTENT_LENGTH);

                Result newResult = result.render(NO_CONTENT);

                if (type != null) {
                    newResult.with(HeaderNames.CONTENT_TYPE, type);
                } else if (renderable != null) {
                    newResult.with(HeaderNames.CONTENT_TYPE, renderable.mimetype());
                }

                if (length != null) {
                    newResult.with(HeaderNames.CONTENT_LENGTH, length);
                } else if (renderable != null) {
                    logger().info("Length from renderable : " + renderable.length());
                    newResult.with(HeaderNames.CONTENT_LENGTH, String.valueOf(renderable.length()));
                }
                return newResult;
            } catch (Exception exception) {
                LOGGER.error("An exception occurred while processing request {} {}", route.getHttpMethod(),
                        route.getUrl(), exception);
                return renderInternalError(context.context(), route, exception);
            }
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
     * <p>
     * It is heavily recommended to allow configuring the priority from the Application Configuration.
     *
     * @return the priority
     */
    @Override
    public int priority() {
        return 1000;
    }

    /**
     * A structure storing the line of an error.
     */
    public static class InterestingLines {

        /**
         * The first line to display (line number).
         */
        public final int firstLine;

        /**
         * The line where the error occurs (line number).
         */
        public final int errorLine;

        /**
         * The set of lines.
         */
        public final String[] focus;

        /**
         * Creates the interested line instance.
         *
         * @param firstLine the first line
         * @param focus     the set of lines
         * @param errorLine the error line.
         */
        private InterestingLines(int firstLine, String[] focus, int errorLine) {
            this.firstLine = firstLine;
            this.errorLine = errorLine;
            // We keep a copy of the array.
            this.focus = focus; //NOSONAR
        }

    }

    /**
     * Extracts interesting lines to be displayed to the user.
     *
     * @param source the source
     * @param line   the line responsible of the error
     * @param border number of lines to use as a border
     * @return the interested line structure
     */
    public InterestingLines extractInterestedLines(String source, int line, int border) {
        try {
            if (source == null) {
                return null;
            }
            String[] lines = source.split("\n");
            int firstLine = Math.max(0, line - border);
            int lastLine = Math.min(lines.length - 1, line + border);
            List<String> focusOn = new ArrayList<>();
            focusOn.addAll(Arrays.asList(lines).subList(firstLine, lastLine + 1));
            return new InterestingLines(firstLine + 1, focusOn.toArray(new String[focusOn.size()]), line - firstLine - 1);
        } catch (Exception e) {
            logger().error("Cannot extract the interesting lines", e);
            return null;
        }
    }

}
