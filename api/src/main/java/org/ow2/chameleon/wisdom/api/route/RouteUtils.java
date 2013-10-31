package org.ow2.chameleon.wisdom.api.route;

import org.ow2.chameleon.wisdom.api.Controller;
import org.ow2.chameleon.wisdom.api.annotations.Attribute;
import org.ow2.chameleon.wisdom.api.annotations.Body;
import org.ow2.chameleon.wisdom.api.annotations.Parameter;
import org.ow2.chameleon.wisdom.api.http.Context;

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
        List<String> list = new ArrayList<String>();

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
                .replaceAll("\\{.*?\\*\\}", "(.*?)")

                // Replace {id+} by (.+?)
                .replaceAll("\\{.*?\\+\\}", "(.+?)")

                // Replace {name} by ([^/]*?)
                .replaceAll("\\{.*?\\}", "([^/]+?)");

        // Replace ending * by (.*?)
        if (s.endsWith("*")) {
            s = s.substring(0, s.length() -1) + "(.*?)";
        }
        return s;
    }

    /**
     * Collects the @Route annotation on <em>action</em> method.
     * This set will be added at the end of the list retrieved from the {@link org.ow2.chameleon.wisdom.api
     * .Controller#routes()}
     * @param controller the controller
     * @return the list of route, empty if none are available
     */
    public static List<Route> collectRouteFromControllerAnnotations(Controller controller) {
        List<Route> routes = new ArrayList<>();
        Method[] methods = controller.getClass().getMethods();
        for (Method method : methods) {
            org.ow2.chameleon.wisdom.api.annotations.Route annotation
                    = method.getAnnotation(org.ow2.chameleon.wisdom.api.annotations.Route.class);
            if (annotation != null) {
                routes.add(new RouteBuilder().route(annotation.method()).on(annotation.uri()).to(controller, method));
            }
        }
        return routes;
    }

    public static Object getParameter(Argument argument, Context context) {
        String value = context.parameterFromPath(argument.name);
        if (value == null) {
            value = context.parameter(argument.name);
        }
        if (value == null) {
            return null;
        }
        if (argument.type.equals(Integer.class)  || argument.type.equals(Integer.TYPE)) {
            return Integer.parseInt(value);
        } else if (argument.type.equals(Boolean.class) || argument.type.equals(Boolean.TYPE)) {
            return Boolean.parseBoolean(value);
        }
        return value;
    }

    public static Object getAttribute(Argument argument, Context context) {
        String value = context.attributes().get(argument.name);
        if (value == null) {
            return null;
        }
        if (argument.type.equals(Integer.class)  || argument.type.equals(Integer.TYPE)) {
            return Integer.parseInt(value);
        } else if (argument.type.equals(Boolean.class) || argument.type.equals(Boolean.TYPE)) {
            return Boolean.parseBoolean(value);
        }
        return value;
    }

    public static List<Argument> buildArguments(Method method) {
        List<Argument> arguments = new ArrayList<>();
        Annotation[][] annotations = method.getParameterAnnotations();
        Class[] typesOfParameters = method.getParameterTypes();
        for (int i = 0; i < annotations.length; i++) {
            if (annotations[i].length == 0) {
                // All parameters must have been annotated.
                throw new RuntimeException("The method " + method + " has a parameter with no annotation indicating " +
                        "the injected data");
            }
            Annotation annotation = annotations[i][0];
            if (annotation instanceof Parameter) {
                Parameter parameter = (Parameter) annotation;
                arguments.add(new Argument(parameter.value(),
                        Source.PARAMETER, typesOfParameters[i]));
            } else if (annotation instanceof Attribute) {
                Attribute parameter = (Attribute) annotation;
                arguments.add(new Argument(parameter.value(),
                        Source.ATTRIBUTE, typesOfParameters[i]));
            } else if (annotation instanceof Body) {
                Body parameter = (Body) annotation;
                arguments.add(new Argument(null,
                        Source.BODY, typesOfParameters[i]));
            }
        }
        return arguments;
    }

    public static class Argument {
        public final String name;
        public final Source source;
        public final Class type;

        public Argument(String name, Source source, Class type) {
            this.name = name;
            this.source = source;
            this.type = type;
        }
    }

    public enum Source {
        PARAMETER,
        ATTRIBUTE,
        BODY
    }
}
