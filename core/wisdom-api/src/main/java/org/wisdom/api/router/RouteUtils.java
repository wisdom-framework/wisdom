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

import com.google.common.collect.ImmutableList;
import org.wisdom.api.Controller;
import org.wisdom.api.annotations.Attribute;
import org.wisdom.api.annotations.Body;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.FileItem;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A couple of method easing the manipulation of routes.
 */
public class RouteUtils {
    private static final Pattern PATH_PARAMETER_REGEX = Pattern.compile("\\{(.*?)\\}");
    private static final String ANY_CHARS = "(.*?)";
    public static final String EMPTY_PREFIX = "";

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

        String s = rawUri
                // Replace {id<[0-9]+>} by [0-9]+
                .replaceAll("\\{.*?<", "")
                .replaceAll(">\\}", "")

                        // Replace {id*} by (.*?)
                .replaceAll("\\{.*?\\*\\}", ANY_CHARS)

                        // Replace {id+} by (.+?)
                .replaceAll("\\{.*?\\+\\}", "(.+?)")

                        // Replace {name} by ([^/]*?)
                .replaceAll("\\{.*?\\}", "([^/]+?)");

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
                routes.add(new RouteBuilder().route(annotation.method())
                        .on(uri)
                        .to(controller, method));
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
    public static List<Argument> buildArguments(Method method) {
        List<Argument> arguments = new ArrayList<>();
        Annotation[][] annotations = method.getParameterAnnotations();
        Class<?>[] typesOfParameters = method.getParameterTypes();
        Type[] genericTypeOfParameters = method.getGenericParameterTypes();
        for (int i = 0; i < annotations.length; i++) {
            boolean sourceDetected = false;
            for (int j = 0; !sourceDetected && j < annotations[i].length; j++) {
                Annotation annotation = annotations[i][j];
                if (annotation instanceof Parameter) {
                    Parameter parameter = (Parameter) annotation;
                    arguments.add(new Argument(parameter.value(),
                            Source.PARAMETER, typesOfParameters[i], genericTypeOfParameters[i]));
                    sourceDetected = true;
                } else if (annotation instanceof Attribute) {
                    Attribute parameter = (Attribute) annotation;
                    arguments.add(new Argument(parameter.value(),
                            Source.ATTRIBUTE,  typesOfParameters[i], genericTypeOfParameters[i]));
                    sourceDetected = true;
                } else if (annotation instanceof Body) {
                    arguments.add(new Argument(null,
                            Source.BODY,  typesOfParameters[i], genericTypeOfParameters[i]));
                    sourceDetected = true;
                }
            }
            if (!sourceDetected) {
                // All parameters must have been annotated.
                throw new RuntimeException("The method " + method + " has a parameter without annotations indicating " +
                        " the injected data");
            }


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

    /**
     * Formal parameter metadata.
     */
    public static class Argument {
        private final String name;
        private final Source source;
        private final Class<?> rawType;
        private final Type genericType;

        /**
         * Creates a new Argument.
         *
         * @param name   the name
         * @param source the source
         * @param rawType   the type
         */
        public Argument(String name, Source source, Class<?> rawType) {
            this(name, source, rawType, null);
        }

        /**
         * Creates a new Argument.
         *
         * @param name   the name
         * @param source the source
         * @param rawType   the type
         */
        public Argument(String name, Source source, Class<?> rawType, Type genericType) {
            this.name = name;
            this.source = source;
            this.rawType = rawType;
            this.genericType = genericType;
        }

        /**
         * @return the argument's name.
         */
        public String getName() {
            return name;
        }

        /**
         * @return the argument's source.
         */
        public Source getSource() {
            return source;
        }

        /**
         * @return the argument's type (main class).
         */
        public Class<?> getRawType() {
            return rawType;
        }

        /**
         * @return information on generics if any.
         */
        public Type getGenericType() {
            return genericType;
        }
    }

    /**
     * The parameter value source.
     */
    public enum Source {
        /**
         * A parameter from the query or from the path.
         */
        PARAMETER,
        /**
         * An attribute from a form.
         */
        ATTRIBUTE,
        /**
         * The payload.
         */
        BODY
    }
}
