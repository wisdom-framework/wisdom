package org.ow2.chameleon.wisdom.api.route;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.ow2.chameleon.wisdom.api.Controller;
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

    private final HttpMethod httpMethod;
    private final String uri;
    private final Class<? extends Controller> controllerClass;
    private final Method controllerMethod;

    private final List<String> parameterNames;
    private final Pattern regex;

    public Route(HttpMethod httpMethod,
                 String uri,
                 Class<? extends Controller> controllerClass,
                 Method controllerMethod) {
        this.httpMethod = httpMethod;
        this.uri = uri;
        this.controllerClass = controllerClass;
        this.controllerMethod = controllerMethod;

        parameterNames = ImmutableList.copyOf(doParseParameters(uri));
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
        return controllerClass;
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
     * @param rawRoute
     * @return a list with the names of all parameters in that route.
     */
    private static List<String> doParseParameters(String rawRoute) {
        List<String> list = new ArrayList<String>();

        Pattern p = Pattern.compile("\\{(.*?)\\}");
        Matcher m = p.matcher(rawRoute);

        while (m.find()) {
            list.add(m.group(1));
        }

        return list;
    }

    /**
     * Gets a raw uri like /{name}/id/* and returns /(.*)/id/*
     *
     * @return The regex
     */
    private static String convertRawUriToRegex(String rawUri) {
        return rawUri.replaceAll("\\{.*?\\}", "([^/]*?)");
    }


}
