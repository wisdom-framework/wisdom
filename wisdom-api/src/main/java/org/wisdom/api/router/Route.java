package org.wisdom.api.router;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.wisdom.api.Controller;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

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

    private final List<RouteUtils.Argument> arguments;

    /**
     * Constructor used in case of delegation.
     */
    protected Route() {
        httpMethod = null;
        uri = null;
        controller = null;
        controllerMethod = null;
        parameterNames = null;
        regex = null;
        arguments = null;
    }

    /**
     * Main constructor.
     * @param httpMethod the method
     * @param uri the uri
     * @param controller the controller object
     * @param controllerMethod the controller method
     */
    public Route(HttpMethod httpMethod,
            String uri,
            Controller controller,
            Method controllerMethod) {
        this.httpMethod = httpMethod;
        this.uri = uri;
        this.controller = controller;
        this.controllerMethod = controllerMethod;
        if (! controllerMethod.isAccessible()) {
            controllerMethod.setAccessible(true);
        }
        this.arguments = RouteUtils.buildArguments(this.controllerMethod);

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
     */
    public boolean matches(String httpMethod, String uri) {
        return matches(HttpMethod.from(httpMethod), uri);
    }

    /**
     * This method does not do any decoding / encoding.
     * <p/>
     * If you want to decode you have to do it yourself.
     * <p/>
     * Most likely with:
     * http://docs.oracle.com/javase/6/docs/api/java/net/URI.html
     *
     * @param uri The whole encoded uri.
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



    public Result invoke() throws Throwable {
        Context context = Context.CONTEXT.get();
        Preconditions.checkNotNull(context);
        Object[] parameters = new Object[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            RouteUtils.Argument argument = arguments.get(i);
            switch (argument.getSource()) {
            case PARAMETER:
                parameters[i] = RouteUtils.getParameter(argument, context);
                break;
            case BODY:
                parameters[i] = context.body(argument.getType());
                break;
            case ATTRIBUTE:
                parameters[i] = RouteUtils.getAttribute(argument, context);
                break;
            default: 
                break;
            }
        }

        return (Result) controllerMethod.invoke(controller, parameters);

    }

    public List<RouteUtils.Argument> getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return "{"
                + getHttpMethod() + " " + uri + " => "
                + controller.getClass().toString() + "#" + controllerMethod.getName()
                + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (! (o instanceof Route)) {
            return false;
        }

        Route route = (Route) o;

        return controller.equals(route.controller)
                && controllerMethod.equals(route.controllerMethod)
                && httpMethod == route.httpMethod
                && uri.equals(route.uri);

    }

    @Override
    public int hashCode() {
        int result = httpMethod.hashCode();
        result = 31 * result + uri.hashCode();
        result = 31 * result + controller.hashCode();
        result = 31 * result + controllerMethod.hashCode();
        return result;
    }
}
