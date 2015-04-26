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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.net.MediaType;
import org.wisdom.api.Controller;
import org.wisdom.api.http.*;
import org.wisdom.api.router.parameters.ActionParameter;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    /**
     * The HTTP method.
     */
    protected final HttpMethod httpMethod;

    /**
     * The path.
     */
    protected final String uri;

    /**
     * The invoked controller, only if the route is `bound`.
     */
    protected final Controller controller;

    /**
     * The invoked method, only if the route is `bound`.
     */
    protected final Method controllerMethod;

    /**
     * The list of parameters.
     */
    protected final List<String> parameterNames;

    /**
     * The path as regex to extract path parameters.
     */
    protected final Pattern regex;

    /**
     * The set of accepted media types.
     */
    protected Set<MediaType> acceptedMediaTypes = Collections.emptySet();

    /**
     * The set of produced media types.
     */
    protected Set<MediaType> producedMediaTypes = Collections.emptySet();

    /**
     * The list of parameters.
     */
    protected final List<ActionParameter> arguments;

    /**
     * The status to return if the route is unbound.
     */
    protected int unboundStatus;

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

        if (controller == null) {
            unboundStatus = Status.NOT_FOUND;
        }
    }

    /**
     * Constructors used for `unbound` route.
     *
     * @param httpMethod    the method
     * @param uri           the path
     * @param unboundStatus the HTTP status to return
     */
    public Route(HttpMethod httpMethod,
                 String uri,
                 int unboundStatus) {
        this(httpMethod, uri, null, null);
        this.unboundStatus = unboundStatus;
    }

    /**
     * Sets the set of media types accepted by the route.
     *
     * @param types the set of type
     * @return the current route
     */
    public Route accepts(String... types) {
        Preconditions.checkNotNull(types);
        final ImmutableSet.Builder<MediaType> builder = new ImmutableSet.Builder<>();
        builder.addAll(this.acceptedMediaTypes);
        for (String s : types) {
            builder.add(MediaType.parse(s));
        }
        this.acceptedMediaTypes = builder.build();
        return this;
    }

    /**
     * Sets the set of media types accepted by the route.
     *
     * @param types the set of type
     * @return the current route
     * @see #accepts(String...)
     */
    public Route accepting(String... types) {
        accepts(types);
        return this;
    }

    /**
     * Sets the set of media types produced by the route.
     *
     * @param types the set of type
     * @return the current route
     */
    public Route produces(String... types) {
        Preconditions.checkNotNull(types);
        final ImmutableSet.Builder<MediaType> builder = new ImmutableSet.Builder<>();
        builder.addAll(this.producedMediaTypes);
        for (String s : types) {
            final MediaType mt = MediaType.parse(s);
            if (mt.hasWildcard()) {
                throw new RoutingException("A route cannot `produce` a mime type with a wildcard: " + mt);
            }
            builder.add(mt);
        }
        this.producedMediaTypes = builder.build();
        return this;
    }

    /**
     * Sets the set of media types produced by the route.
     *
     * @param types the set of type
     * @return the current route
     * @see #produces(String...)
     */
    public Route producing(String... types) {
        produces(types);
        return this;
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
     * @throws java.lang.Exception if anything goes wrong
     */
    public Result invoke() throws Exception {
        if (isUnbound()) {
            return new Result().status(unboundStatus).noContentIfNone();
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
                    + "UNBOUND (" + unboundStatus + ")"
                    + "}";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(getHttpMethod()).append(" ").append(uri).append(" => ")
                .append(controller.getClass().toString()).append("#").append(controllerMethod.getName());

        if (!acceptedMediaTypes.isEmpty()) {
            builder.append(" - accepting: ").append(acceptedMediaTypes);
        }

        if (!producedMediaTypes.isEmpty()) {
            builder.append(" - producing: ").append(producedMediaTypes);
        }

        return builder.toString();
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
        if (!(o instanceof Route)) {   // NOSONAR we use instanceOf to support children class too.
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
            result = 31 * result + unboundStatus;
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

    /**
     * Gets the HTTP Status to return for this unbound route. This method is meaningful only if the route is unbound
     * (and so cannot be served).
     *
     * @return {@link Status#NOT_FOUND} when there are no action method to handle the route,
     * {@link Status#UNSUPPORTED_MEDIA_TYPE} when the request content cannot be accepted.
     */
    public int getUnboundStatus() {
        return unboundStatus;
    }

    /**
     * Checks whether or not the current route can accept the given request. It checks the request content type
     * against the list of accepted mime types. It does not return a boolean but an integer indicating the level of
     * acceptation: 0 - not accepted, 1 - accepted using a wildcard, 2 - full accept. This distinction comes from the
     * possibility to have wildcard in the accepted mime types. For instance, if the request contains `text/plain`,
     * and the route accepts `text/*`, it returns 1. If the route would have accepted `text/plain`, 2 would have been
     * returned.
     *
     * @param request the incoming request
     * @return the acceptation level (0, 1 or 2).
     */
    public int isCompliantWithRequestContentType(Request request) {
        if (acceptedMediaTypes == null || acceptedMediaTypes.isEmpty() || request == null) {
            return 2;
        } else {
            String content = request.contentMimeType();
            if (content == null) {
                return 2;
            } else {
                // For all consume, check whether we accept it
                MediaType contentMimeType = MediaType.parse(request.contentMimeType());
                for (MediaType type : acceptedMediaTypes) {
                    if (contentMimeType.is(type)) {
                        if (type.hasWildcard()) {
                            return 1;
                        } else {
                            return 2;
                        }
                    }
                }
                return 0;
            }
        }
    }

    /**
     * Checks whether the given request is compliant with the media type accepted by the current route.
     *
     * @param request the request
     * @return {@code true} if the request is compliant, {@code false} otherwise
     */
    public boolean isCompliantWithRequestAccept(Request request) {
        if (producedMediaTypes == null || producedMediaTypes.isEmpty() || request == null
                || request.getHeader(HeaderNames.ACCEPT) == null) {
            return true;
        } else {
            for (MediaType mt : producedMediaTypes) {
                if (request.accepts(mt.toString())) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * @return the set of produced media types.
     */
    public Set<MediaType> getProducedMediaTypes() {
        return producedMediaTypes;
    }

    /**
     * @return the set of accepted media types.
     */
    public Set<MediaType> getAcceptedMediaTypes() {
        return acceptedMediaTypes;
    }
}
