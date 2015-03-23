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
package org.wisdom.template.thymeleaf.impl;


import com.google.common.collect.Maps;
import org.wisdom.api.Controller;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Request;
import org.wisdom.api.router.AbstractRouter;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.RouteUtils;

import java.util.*;

/**
 *
 */
public class FakeRouter extends AbstractRouter {

    private Set<Route> routes = new LinkedHashSet<Route>();

    public FakeRouter() {

    }

    public FakeRouter addController(Controller controller) {
        routes.addAll(controller.routes());
        routes.addAll(RouteUtils.collectRouteFromControllerAnnotations(controller));
        return this;
    }

    public FakeRouter removeController(Controller controller) {
        routes.removeAll(controller.routes());
        routes.removeAll(RouteUtils.collectRouteFromControllerAnnotations(controller));
        return this;
    }

    private synchronized Set<Route> copy() {
        return new LinkedHashSet<Route>(routes);
    }

    @Override
    public Route getRouteFor(HttpMethod method, String uri) {
        return getRouteFor(method, uri, null);
    }

    /**
     * Gets the route for the given method and uri.
     *
     * @param method  the method (must be a valid HTTP method)
     * @param uri     the uri
     * @param request the incoming request, used to handle negotiation
     * @return the route, {@literal unbound} if no controller handles the request
     * @since 0.8.1
     */
    @Override
    public Route getRouteFor(HttpMethod method, String uri, Request request) {
        for (Route route : copy()) {
            if (route.matches(method, uri)) {
                return route;
            }
        }
        return null;
    }

    @Override
    public String getReverseRouteFor(String className, String method, Map<String, Object> params) {
        for (Route route : copy()) {

            if (route.getControllerClass().getName().equals(className)
                    && route.getControllerMethod().getName().equals(method)) {

                return computeUrlForRoute(route, params);
            }
        }
        return null;
    }

    @Override
    public Collection<Route> getRoutes() {
        return copy();
    }

    private String computeUrlForRoute(Route route, Map<String, Object> params) {
        if (params == null) {
            // No variables, return the raw url.
            return route.getUrl();
        }

        // The original url. Something like route/user/{id}/{email}/userDashboard
        String urlWithReplacedPlaceholders = route.getUrl();

        Map<String, Object> queryParameterMap = Maps.newHashMap();

        for (Map.Entry<String, Object> entry : params.entrySet()) {

            // The original regex. For the example above this results in {id}
            String originalRegex = String.format("{%s}", entry.getKey());
            String originalRegexEscaped = String.format("\\{%s\\}", entry.getKey());

            // The value that will be added into the regex => myId for instance...
            String resultingRegexReplacement = entry.getValue().toString();

            // If regex is in the url as placeholder we replace the placeholder
            if (urlWithReplacedPlaceholders.contains(originalRegex)) {

                urlWithReplacedPlaceholders = urlWithReplacedPlaceholders.replaceAll(
                        originalRegexEscaped,
                        resultingRegexReplacement);

                // If the parameter is not there as placeholder we add it as queryParameter
            } else {
                queryParameterMap.put(entry.getKey(), entry.getValue());
            }
        }

        // invoke prepare the query string for this url if we got some query params
        if (queryParameterMap.entrySet().size() > 0) {

            StringBuilder queryParameterStringBuffer = new StringBuilder();

            // The uri is invoke replaced => we invoke have to add potential query parameters
            for (Iterator<Map.Entry<String, Object>> iterator = queryParameterMap.entrySet().iterator();
                 iterator.hasNext(); ) {

                Map.Entry<String, Object> queryParameterEntry = iterator.next();
                queryParameterStringBuffer.append(queryParameterEntry.getKey());
                queryParameterStringBuffer.append("=");
                queryParameterStringBuffer.append(queryParameterEntry.getValue());

                if (iterator.hasNext()) {
                    queryParameterStringBuffer.append("&");
                }

            }

            urlWithReplacedPlaceholders = urlWithReplacedPlaceholders
                    + "?"
                    + queryParameterStringBuffer.toString();

        }


        return urlWithReplacedPlaceholders;
    }
}
