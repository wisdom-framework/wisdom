package org.ow2.chameleon.wisdom.api.route;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.ow2.chameleon.wisdom.api.Controller;
import org.ow2.chameleon.wisdom.api.http.HttpMethod;

import java.lang.reflect.Method;
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

        parameterNames = ImmutableList.copyOf(RouteUtils.extractParameters(uri));
        regex = Pattern.compile(RouteUtils.convertRawUriToRegex(uri));
    }

    public String getUrl() {
        return uri;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
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


    public Controller getControllerObject() {
        return controller;
    }
}
