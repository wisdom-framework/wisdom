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
package org.wisdom.router;

import com.google.common.collect.Maps;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.Controller;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.interception.Filter;
import org.wisdom.api.interception.Interceptor;
import org.wisdom.api.router.AbstractRouter;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.RouteUtils;
import org.wisdom.api.router.RoutingException;

import javax.validation.Validator;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * The route.
 */
@Component
@Provides
@Instantiate(name = "router")
public class RequestRouter extends AbstractRouter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestRouter.class);

    private Set<Filter> filters = new TreeSet<>(new Comparator<Filter>() {
        @Override
        public int compare(Filter o1, Filter o2) {
            return new Integer(o2.priority()).compareTo(o1.priority());
        }
    });

    @Requires(optional = true, specification = Interceptor.class)
    private List<Interceptor<?>> interceptors;

    @Requires(optional = true, proxy = false)
    private Validator validator;

    private Set<RouteDelegate> routes = new LinkedHashSet<>();

    @Bind(aggregate = true)
    public synchronized void bindController(Controller controller) {
        LOGGER.info("Adding routes from " + controller);

        List<Route> newRoutes = new ArrayList<Route>();

        List<Route> annotatedNewRoutes = RouteUtils.collectRouteFromControllerAnnotations(controller);
        newRoutes.addAll(annotatedNewRoutes);
        newRoutes.addAll(controller.routes());

        try {
            //check if these new routes don't pre-exist
            ensureNoConflicts(newRoutes);

        } catch (RoutingException e) {
            LOGGER.error("The controller {} declares routes conflicting with existing routes, " +
                    "the controller is ignored, reason: {}", controller, e.getMessage(), e);
            // remove all new routes as one has failed
            routes.removeAll(newRoutes);
        }
    }

    @Unbind(aggregate = true)
    public synchronized void unbindController(Controller controller) {
        LOGGER.info("Removing routes from " + controller);
        Collection<RouteDelegate> copy = new LinkedHashSet<>(routes);
        for (RouteDelegate r : copy) {
            if (r.getControllerObject().equals(controller)) {
                routes.remove(r);
            }
        }
    }

    private void ensureNoConflicts(List<Route> newRoutes) {
        //check if these new routes don't pre-exist in existingRoutes
        for (Route newRoute : newRoutes) {
            if (!isRouteConflictingWithExistingRoutes(newRoute)) {
                // this routes seems to be clean, store it
                final RouteDelegate delegate = new RouteDelegate(this, newRoute);
                routes.add(delegate);
            }
        }
    }

    private boolean isRouteConflictingWithExistingRoutes(Route route) {
        for (Route existing : routes) {
            boolean sameHttpMethod = existing.getHttpMethod().equals(route.getHttpMethod());
            boolean sameUrl = existing.getUrl().equals(route.getUrl());

            // same url and method => conflict
            if (sameUrl && sameHttpMethod) {
                throw new RoutingException(existing.getHttpMethod() + " " + existing.getUrl()
                        + " is already registered in controller " + existing.getControllerClass());
            }
        }

        return false;
    }

    @Validate
    public void start() {
        LOGGER.info("Router starting");
    }

    @Invalidate
    public void stop() {
        LOGGER.info("Router stopping");
        routes.clear();
    }

    private synchronized Set<Route> copy() {
        return new LinkedHashSet<Route>(routes);
    }

    @Override
    public Route getRouteFor(HttpMethod method, String uri) {
        for (Route route : copy()) {
            if (route.matches(method, uri)) {
                return route;
            }
        }
        return new RouteDelegate(this, new Route(method, uri, null, null));
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

            String originalRegex = String.format("{%s}", entry.getKey());
            String originalRegexEscaped = String.format("\\{%s\\}", entry.getKey());

            // The value that will be added into the regex => myId for instance...
            String resultingRegexReplacement = pathEncode(entry.getValue().toString());

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

        // now prepare the query string for this url if we got some query params
        if (!queryParameterMap.entrySet().isEmpty()) {

            StringBuilder queryParameterStringBuffer = new StringBuilder();

            // The uri is now replaced => we now have to add potential query parameters
            for (Iterator<Map.Entry<String, Object>> iterator = queryParameterMap.entrySet().iterator();
                 iterator.hasNext(); ) {

                Map.Entry<String, Object> queryParameterEntry = iterator.next();
                queryParameterStringBuffer.append(queryParameterEntry.getKey());
                queryParameterStringBuffer.append("=");
                // Don't forget to encode the value.
                queryParameterStringBuffer.append(encode(queryParameterEntry.getValue().toString()));

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

    private String pathEncode(String s) {
        // TODO Implement the RFC 3986, 3.3. Path.
        return s
                .replace(" ", "%20")
                .replace("/", "%2F");
    }

    private String encode(String v) {
        try {
            return URLEncoder.encode(v, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("UTF-8 not supported", e);
        }
    }

    public Validator getValidator() {
        return validator;
    }

    /**
     * For testing purpose only.
     *
     * @param validator the validator to use
     */
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    protected Set<Filter> getFilters() {
        return filters;
    }

    protected List<Interceptor<?>> getInterceptors() {
        return interceptors;
    }

    @Bind(aggregate = true, optional = true)
    public void bindFilter(Filter filter) {
        filters.add(filter);
    }

    @Unbind
    public void unbindFilter(Filter filter) {
        filters.remove(filter);
    }
}

