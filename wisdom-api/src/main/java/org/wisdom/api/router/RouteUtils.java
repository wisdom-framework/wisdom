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
import org.wisdom.api.annotations.Attribute;
import org.wisdom.api.annotations.Body;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.FileItem;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
     *
     * Extracts the name of the parameters from a route
     *
     * /{my_id}/{my_name}
     *
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
     * Gets a raw uri like /{name}/id/* and returns /(.*)/id/*
     *
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
            s = s.substring(0, s.length() -1) + ANY_CHARS;
        }
        return s;
    }

    /**
     * Collects the @Route annotation on <em>action</em> method.
     * This set will be added at the end of the list retrieved from the {@link org.wisdom.api
     * .Controller#routes()}
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

    public static String getPrefixedUri(String prefix, String uri) {
        String localURI = uri;
        if (! localURI.startsWith("/")  && ! prefix.endsWith("/")) {
            localURI = prefix + "/" + localURI;
        } else {
            localURI = prefix + localURI;
        }
        return localURI;
    }

    public static Object getParameter(Argument argument, Context context) {
        String value = context.parameterFromPath(argument.name);
        if (value == null) {
            value = context.parameter(argument.name);
        }
        if (isInteger(argument)) {
            if (value == null) {
                return 0;
            }
            return Integer.parseInt(value);
        } else if (isBoolean(argument)) {
            return value != null && Boolean.parseBoolean(value);
        }
        return value;
    }

    private static boolean isBoolean(Argument argument) {
        return argument.type.equals(Boolean.class) || argument.type.equals(Boolean.TYPE);
    }

    private static boolean isInteger(Argument argument) {
        return argument.type.equals(Integer.class)  || argument.type.equals(Integer.TYPE);
    }

    public static Object getParameter(Argument argument, Map<String, String> values) {
        String value = values.get(argument.name);
        if (isInteger(argument)) {
            if (value == null) {
                return 0;
            }
            return Integer.parseInt(value);
        } else if (isBoolean(argument)) {
            return value != null && Boolean.parseBoolean(value);
        }
        return value;
    }

    public static Object getAttribute(Argument argument, Context context) {
        // File item case.
        if (argument.type.equals(FileItem.class)) {
            return context.getFile(argument.name);
        }

        // Regular attributes.
        List<String> values = context.attributes().get(argument.name);
        if (isInteger(argument)) {
            if (! containsAtLeastAValue(values)) {
                return 0;
            }
            return Integer.parseInt(values.get(0));
        } else if (isBoolean(argument)) {
            return containsAtLeastAValue(values) && Boolean.parseBoolean(values.get(0));
        } else if (argument.type.equals(String.class) && containsAtLeastAValue(values)) {
            return values.get(0);
        }
        return values;
    }

    private static boolean containsAtLeastAValue(List<String> values) {
        return values != null && !values.isEmpty();
    }

    public static List<Argument> buildArguments(Method method) {
        List<Argument> arguments = new ArrayList<>();
        Annotation[][] annotations = method.getParameterAnnotations();
        Class<?>[] typesOfParameters = method.getParameterTypes();
        for (int i = 0; i < annotations.length; i++) {
            boolean sourceDetected = false;
            for (int j = 0; ! sourceDetected  && j < annotations[i].length; j++) {
                Annotation annotation = annotations[i][j];
                if (annotation instanceof Parameter) {
                    Parameter parameter = (Parameter) annotation;
                    arguments.add(new Argument(parameter.value(),
                            Source.PARAMETER, typesOfParameters[i]));
                    sourceDetected = true;
                } else if (annotation instanceof Attribute) {
                    Attribute parameter = (Attribute) annotation;
                    arguments.add(new Argument(parameter.value(),
                            Source.ATTRIBUTE, typesOfParameters[i]));
                    sourceDetected = true;
                } else if (annotation instanceof Body) {
                    arguments.add(new Argument(null,
                            Source.BODY, typesOfParameters[i]));
                    sourceDetected = true;
                }
            }
            if (! sourceDetected) {
                // All parameters must have been annotated.
                throw new RuntimeException("The method " + method + " has a parameter without annotations indicating " +
                        " the injected data");
            }


        }
        return arguments;
    }

    public static String getPath(Controller controller) {
        Path path = controller.getClass().getAnnotation(Path.class);
        if (path == null) {
            return EMPTY_PREFIX;
        } else {
            return path.value();
        }
    }

    public static class Argument {
        private final String name;
        private final Source source;
        private final Class<?> type;

        public Argument(String name, Source source, Class<?> type) {
            this.name = name;
            this.source = source;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public Source getSource() {
            return source;
        }

        public Class<?> getType() {
            return type;
        }
    }

    public enum Source {
        PARAMETER,
        ATTRIBUTE,
        BODY
    }
}
