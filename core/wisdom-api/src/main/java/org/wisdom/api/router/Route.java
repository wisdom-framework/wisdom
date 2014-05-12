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
import com.google.common.collect.Maps;
import org.wisdom.api.Controller;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.api.router.parameters.ActionParameter;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a route.
 * Routes can be bound if an action method can handle the request, or unbound if not.
 * <p>
 * <strong>IMPORTANT:</strong> Router implementation must extends this class to provide a valid implementation of the
 * {@link org.wisdom.api.router.Route#invoke()} method.
 */
public class Route {

    protected final HttpMethod httpMethod;
    protected final String uri;
    protected final Controller controller;
    protected final Method controllerMethod;
    protected final List<String> parameterNames;
    protected final Pattern regex;

    protected final List<ActionParameter> arguments;

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
     *
     * @param httpMethod       the method
     * @param uri              the uri
     * @param controller       the controller object
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
        // Unbound route case.
        if (controllerMethod != null) {
            if (!controllerMethod.isAccessible()) {
                controllerMethod.setAccessible(true);
            }
            this.arguments = RouteUtils.buildActionParameterList(this.controllerMethod);
            parameterNames = ImmutableList.copyOf(RouteUtils.extractParameters(uri));
            regex = Pattern.compile(RouteUtils.convertRawUriToRegex(uri));
        } else {
            parameterNames = Collections.emptyList();
            regex = null;
            arguments = Collections.emptyList();
        }
    }

    /**
     * Gets the route uri.
     *
     * @return the uri
     */
    public String getUrl() {
        return uri;
    }

    /**
     * Gets the HTTP method.
     *
     * @return the method
     */
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    /**
     * Gets the controller class handling the route.
     *
     * @return the controller class, {@literal null} for unbound routes
     */
    public Class<? extends Controller> getControllerClass() {
        return controller.getClass();
    }

    /**
     * Gets the controller method handling the route.
     *
     * @return the controller method, {@literal null} for unbound routes
     */
    public Method getControllerMethod() {
        return controllerMethod;
    }

    /**
     * Matches /index to /index or /me/1 to /person/{id}.
     *
     * @param method the method
     * @param uri    the uri
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
     * Matches /index to /index or /me/1 to /person/{id}.
     *
     * @param method the method
     * @param uri    the uri
     * @return True if the actual route matches a raw rout. False if not.
     */
    public boolean matches(String method, String uri) {
        return matches(HttpMethod.from(method), uri);
    }

    /**
     * This method does not do any decoding / encoding.
     * <p>
     * If you want to decode you have to do it yourself.
     * <p>
     * Most likely with:
     * http://docs.oracle.com/javase/6/docs/api/java/net/URI.html
     *
     * @param uri The whole encoded uri.
     * @return A map with all parameters of that uri. Encoded in => encoded out.
     */
    public Map<String, String> getPathParametersEncoded(String uri) {
        Map<String, String> map = Maps.newHashMap();
        if (regex == null) {
            // Unbound case
            return map;
        }
        Matcher m = regex.matcher(uri);
        if (m.matches()) {
            for (int i = 1; i < m.groupCount() + 1; i++) {
                map.put(parameterNames.get(i - 1), m.group(i));
            }
        }
        return map;
    }

    /**
     * Gets the controller object.
     *
     * @return the controller handling the request, {@literal null} for unbound routes.
     */
    public Controller getControllerObject() {
        return controller;
    }


    /**
     * Invokes the action method. This method must be overridden by router implementation.
     * On unbound route, a {@literal 404 - NOT FOUND} result is returned. Otherwise,
     * it invokes the route without any parameter support / injection support.
     * <p>
     *
     * @return the result returned by the action method
     * @throws Throwable if anything goes wrong
     */
    public Result invoke() throws Throwable {
        if (isUnbound()) {
            return Results.notFound();
        } else {
            return (Result) controllerMethod.invoke(controller);
        }
    }

    /**
     * The list of arguments.
     *
     * @return the list, empty if none.
     */
    public List<ActionParameter> getArguments() {
        return arguments;
    }

    /**
     * A simple implementation of the toString method for routes.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        if (isUnbound()) {
            return "{"
                    + getHttpMethod() + " " + getUrl() + " => "
                    + "UNBOUND"
                    + "}";
        }
        return "{"
                + getHttpMethod() + " " + uri + " => "
                + controller.getClass().toString() + "#" + controllerMethod.getName()
                + "}";
    }

    /**
     * For unbound routes, only the uri and method are checked. For bound routes, the controller and method are also
     * checks.
     *
     * @param o the compared object
     * @return {@literal true} if the the given route is equal to the current route, {@literal false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Route)) {
            return false;
        }

        Route route = (Route) o;

        if (this.isUnbound()) {
            return route.isUnbound()
                    && httpMethod == route.httpMethod
                    && uri.equals(route.uri);
        }
        // Bound route.
        return controller.equals(route.controller)
                && controllerMethod.equals(route.controllerMethod)
                && httpMethod == route.httpMethod
                && uri.equals(route.uri);

    }

    /**
     * A simple hash code method.
     *
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        int result;
        if (isUnbound()) {
            result = httpMethod.hashCode();
            result = 31 * result + uri.hashCode();
        } else {
            result = httpMethod.hashCode();
            result = 31 * result + uri.hashCode();
            result = 31 * result + controller.hashCode();
            result = 31 * result + controllerMethod.hashCode();
        }
        return result;
    }

    /**
     * Is the route unbound?
     *
     * @return {@literal true} if the route is unbound, {@literal false} otherwise.
     */
    public boolean isUnbound() {
        return controllerMethod == null;
    }
}
