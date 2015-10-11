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
package org.wisdom.api.router;

import org.wisdom.api.Controller;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.router.parameters.ActionParameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A couple of method easing the manipulation of routes.
 */
public class RouteUtils {
    private static final Pattern PATH_PARAMETER_REGEX = Pattern.compile("\\{(.*?)\\}");
    private static final String ANY_CHARS = "(.*?)";
    public static final String EMPTY_PREFIX = "";


    private RouteUtils() {
        // Avoid direct instantiation, as we only have static methods in this class.
    }

    /**
     * Extracts the name of the parameters from a route.
     * <p/>
     * /{my_id}/{my_name}
     * <p/>
     * would return a List with "my_id" and "my_name"
     *
     * @param rawRoute the route's uri
     * @return a list with the names of all parameters in that route.
     */
    public static List<String> extractParameters(String rawRoute) {
        List<String> list = new ArrayList<>();

        Matcher m = PATH_PARAMETER_REGEX.matcher(rawRoute);

        while (m.find()) {
            if (m.group(1).indexOf('<') != -1) {
                // Regex case name<reg>
                list.add(m.group(1).substring(0, m.group(1).indexOf('<')));
            } else if (m.group(1).indexOf('*') != -1) {
                // Star case name*
                list.add(m.group(1).substring(0, m.group(1).indexOf('*')));
            } else if (m.group(1).indexOf('+') != -1) {
                // Plus case name+
                list.add(m.group(1).substring(0, m.group(1).indexOf('+')));
            } else {
                // Basic case (name)
                list.add(m.group(1));
            }
        }

        return list;
    }

    /**
     * Gets a raw uri like /{name}/id/* and returns /(.*)/id/*.
     *
     * @param rawUri the uri
     * @return The regex
     */
    public static String convertRawUriToRegex(String rawUri) {
        StringBuilder builder = new StringBuilder();

        boolean inRegexp = false;
        boolean inVariablePlaceholder = false;
        boolean wasRegexp = false;
        String current = "";
        for (char c : rawUri.toCharArray()) {
            // First "{"
            if (!inVariablePlaceholder && c == '{') {
                inVariablePlaceholder = true;
                wasRegexp = false;
                inRegexp = false;
                current = "";
            }

            // No in a variable placeholder
            if (!inVariablePlaceholder) {
                builder.append(c);
                continue;
            }

            // In brace.

            // Ending "}"
            if (!inRegexp && c == '}') {
                inVariablePlaceholder = false;
                if (wasRegexp) {
                    continue;
                }
                if (current.endsWith("*")) {
                    builder.append(ANY_CHARS);
                } else if (current.endsWith("+")) {
                    builder.append("(.+?)");
                } else {
                    builder.append("([^/]+?)");
                }
                continue;
            }

            // In regex
            if (inRegexp  && c != '>') {
                builder.append(c);
                continue;
            }

            // Start a regex
            if (!inRegexp && c == '<') {
                inRegexp = true;
                wasRegexp = true;
                builder.append("(");
                continue;
            }

            // End of regex
            if (inRegexp && c == '>') {
                inRegexp = false;
                builder.append(")");
                continue;
            }

            // We are in brace.
            current += c;
        }

        if (inRegexp) {
            throw new IllegalArgumentException("Invalid route uri - the regex part is not closed: " + rawUri);
        }
        if (inVariablePlaceholder) {
            throw new IllegalArgumentException("Invalid route uri - the variable placeholder is not closed: " + rawUri);
        }

        String s = builder.toString();

        // Replace ending * by (.*?)
        if (s.endsWith("*")) {
            s = s.substring(0, s.length() - 1) + ANY_CHARS;
        }
        return s;
    }

    /**
     * Collects the @Route annotation on <em>action</em> method.
     * This set will be added at the end of the list retrieved from the {@link org.wisdom.api
     * .Controller#routes()}
     *
     * @param controller the controller
     * @return the list of route, empty if none are available
     */
    public static List<Route> collectRouteFromControllerAnnotations(Controller controller) {
        String prefix = getPath(controller);
        List<Route> routes = new ArrayList<>();
        Method[] methods = controller.getClass().getMethods();
        for (Method method : methods) {
            org.wisdom.api.annotations.Route annotation = method.getAnnotation(org.wisdom.api.annotations.Route.class);
            if (annotation != null) {
                String uri = annotation.uri();
                uri = getPrefixedUri(prefix, uri);
                final Route route = new RouteBuilder().route(annotation.method())
                        .on(uri)
                        .to(controller, method)
                        .accepting(annotation.accepts())
                        .producing(annotation.produces());
                routes.add(route);
            }
        }
        return routes;
    }


    /**
     * Prepends the given prefix to the given uri.
     *
     * @param prefix the prefix
     * @param uri    the uri
     * @return the full uri
     */
    public static String getPrefixedUri(String prefix, String uri) {
        String localURI = uri;
        if (localURI.length() > 0) {
            // Put a / between the prefix and the tail only if:
            // the prefix does not ends with a /
            // the tail does not start with a /
            // the tail starts with an alphanumeric character.
            if (!localURI.startsWith("/")
                    && !prefix.endsWith("/")
                    && Character.isLetterOrDigit(localURI.indexOf(0))) {
                localURI = prefix + "/" + localURI;
            } else {
                localURI = prefix + localURI;
            }
        } else {
            // Empty tail, just return the prefix.
            return prefix;
        }
        return localURI;
    }

    /**
     * Gets the list of Argument, i.e. formal parameter metadata for the given method.
     *
     * @param method the method
     * @return the list of arguments
     */
    public static List<ActionParameter> buildActionParameterList(Method method) {
        List<ActionParameter> arguments = new ArrayList<>();
        Annotation[][] annotations = method.getParameterAnnotations();
        Class<?>[] typesOfParameters = method.getParameterTypes();
        Type[] genericTypeOfParameters = method.getGenericParameterTypes();
        for (int i = 0; i < annotations.length; i++) {
            arguments.add(ActionParameter.from(method, annotations[i], typesOfParameters[i],
                    genericTypeOfParameters[i]));
        }
        return arguments;
    }

    /**
     * Gets the 'path' value of the given controller. If the controller does not use the {@link org.wisdom.api
     * .annotations.Path} annotation, and empty prefix is returned.
     *
     * @param controller the controller
     * @return the prefix coming either empty or the value of the Path annotation
     */
    public static String getPath(Controller controller) {
        Path path = controller.getClass().getAnnotation(Path.class);
        if (path == null) {
            return EMPTY_PREFIX;
        } else {
            return path.value();
        }
    }

}
