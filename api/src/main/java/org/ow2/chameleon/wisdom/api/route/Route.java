package org.ow2.chameleon.wisdom.api.route;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.ow2.chameleon.wisdom.api.Controller;
import org.ow2.chameleon.wisdom.api.DefaultController;
import org.ow2.chameleon.wisdom.api.http.HttpMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a route.
 */
public class Route {

    private static final Pattern PATH_PARAMETER_REGEX = Pattern.compile("\\{(.*?)\\}");

    private final HttpMethod httpMethod;
    private final String uri;
    private final Controller controller;
    private final Method controllerMethod;

    private final List<String> parameterNames;
    private final Pattern regex;

    public Route(HttpMethod httpMethod,
                 String uri,
                 Controller controller,
                 Method controllerMethod) {
        this.httpMethod = httpMethod;
        this.uri = uri;
        this.controller = controller;
        this.controllerMethod = controllerMethod;

        parameterNames = ImmutableList.copyOf(extractParameters(uri));
        regex = Pattern.compile(convertRawUriToRegex(uri));
    }

    public String getUrl() {
        return uri;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public String getUri() {
        return uri;
    }

    public Class<? extends Controller> getControllerClass() {
        return controller.getClass();
    }

    public Method getControllerMethod() {
        return controllerMethod;
    }

    /**
     * Matches /index to /index or /me/1 to /person/{id}
     *
     * @return True if the actual route matches a raw rout. False if not.
     *
     */
    public boolean matches(HttpMethod method, String uri) {
        if (this.httpMethod == method) {
            Matcher matcher = regex.matcher(uri);
            return matcher.matches();
        } else {
            return false;
        }
    }

    /**
     * Matches /index to /index or /me/1 to /person/{id}
     *
     * @return True if the actual route matches a raw rout. False if not.
     *
     */
    public boolean matches(String httpMethod, String uri) {
        return matches(HttpMethod.from(httpMethod), uri);
    }

    /**
     * This method does not do any decoding / encoding.
     *
     * If you want to decode you have to do it yourself.
     *
     * Most likely with:
     * http://docs.oracle.com/javase/6/docs/api/java/net/URI.html
     *
     * @param uri
     *            The whole encoded uri.
     * @return A map with all parameters of that uri. Encoded in => encoded out.
     */
    public Map<String, String> getPathParametersEncoded(String uri) {

        Map<String, String> map = Maps.newHashMap();

        Matcher m = regex.matcher(uri);

        if (m.matches()) {
            for (int i = 1; i < m.groupCount() + 1; i++) {
                map.put(parameterNames.get(i - 1), m.group(i));
            }
        }

        return map;

    }

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


    public Controller getControllerObject() {
        return controller;
    }
}
