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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.net.MediaType;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.Controller;
import org.wisdom.api.content.ParameterFactories;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Request;
import org.wisdom.api.http.Status;
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
import java.util.stream.Collectors;

/**
 * The request router responsible for handling request and invoke the action methods.
 */
@Component
@Provides
@Instantiate(name = "router")
public class RequestRouter extends AbstractRouter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestRouter.class);

    private static final Map<String, String> PERCENT_ENCODING_MAP = new TreeMap<>();

    static {
        // Reserved characters.
        PERCENT_ENCODING_MAP.put("/", "%2F");

        // Common characters
        PERCENT_ENCODING_MAP.put(" ", "%20");
        PERCENT_ENCODING_MAP.put("\"", "%22");
        PERCENT_ENCODING_MAP.put("%", "%25");
        PERCENT_ENCODING_MAP.put("-", "%2D");
        PERCENT_ENCODING_MAP.put("<", "%3C");
        PERCENT_ENCODING_MAP.put(">", "%3E");
        PERCENT_ENCODING_MAP.put("\\", "%5C");
        PERCENT_ENCODING_MAP.put("ˆ", "%5E");
        PERCENT_ENCODING_MAP.put("_", "%5F");
        PERCENT_ENCODING_MAP.put("`", "%60");
        PERCENT_ENCODING_MAP.put("{", "%7B");
        PERCENT_ENCODING_MAP.put("|", "%7C");
        PERCENT_ENCODING_MAP.put("}", "%7D");

        // New line
        PERCENT_ENCODING_MAP.put("\n", "%0A");
    }

    /**
     * The comparator used to sort filters.
     */
    private Set<Filter> filters = new FilterSet();

    @Requires(optional = true, specification = Interceptor.class)
    private List<Interceptor<?>> interceptors;

    @Requires(optional = true, proxy = false)
    private Validator validator;

    @Requires(optional = true)
    private ParameterFactories engine;

    private Set<RouteDelegate> routes = new LinkedHashSet<>();

    /**
     * Binds a new controller.
     *
     * @param controller the controller
     */
    @Bind(aggregate = true, optional = true)
    public synchronized void bindController(Controller controller) {
        LOGGER.info("Adding routes from " + controller);

        List<Route> newRoutes = new ArrayList<>();
        try {

            List<Route> annotatedNewRoutes = RouteUtils.collectRouteFromControllerAnnotations(controller);
            newRoutes.addAll(annotatedNewRoutes);
            newRoutes.addAll(controller.routes());

            //check if these new routes don't pre-exist
            ensureNoConflicts(newRoutes);

        } catch (RoutingException e) {
            LOGGER.error("The controller {} declares routes conflicting with existing routes, " +
                    "the controller is ignored, reason: {}", controller, e.getMessage(), e);
            // remove all new routes as one has failed
            routes.removeAll(newRoutes);  //NOSONAR
        } catch (Exception e) {
            LOGGER.error("The controller {} declares invalid routes, " +
                    "the controller is ignored, reason: {}", controller, e.getMessage(), e);
            // remove all new routes as one has failed
            routes.removeAll(newRoutes); //NOSONAR
        }
    }

    /**
     * Unbinds a controller.
     *
     * @param controller the controller
     */
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
            if (hasSameMethodAndUrl(existing, route)) {
                // The routes are using the same HTTP Verb and URL, so we need to check the other aspect: accepted
                // and produced types
                if (hasSameOrOverlappingAcceptedTypes(existing, route) &&
                        hasSameOrOverlappingProducedTypes(existing, route)) {
                    throw new RoutingException(existing.getHttpMethod() + " " + existing.getUrl()
                            + " is already registered by controller " + existing.getControllerClass() + " - "
                            + existing.toString() + " conflicts with " + route.toString());
                }
            }
        }

        return false;
    }

    private boolean hasSameMethodAndUrl(Route actual, Route other) {
        return other.getUrl().equals(actual.getUrl())
                && other.getHttpMethod() == actual.getHttpMethod();
    }

    private boolean hasSameOrOverlappingAcceptedTypes(Route actual, Route other) {
        final Set<MediaType> actualAcceptedMediaTypes = actual.getAcceptedMediaTypes();
        final Set<MediaType> otherAcceptedMediaTypes = other.getAcceptedMediaTypes();

        // Both are empty
        if (actualAcceptedMediaTypes.isEmpty() && otherAcceptedMediaTypes.isEmpty()) {
            return true;
        }

        // One is empty
        if (actualAcceptedMediaTypes.isEmpty() || otherAcceptedMediaTypes.isEmpty()) {
            return true;
        }

        // None are empty, check intersection
        final Sets.SetView<MediaType> intersection = Sets.intersection(actualAcceptedMediaTypes, otherAcceptedMediaTypes);
        return !intersection.isEmpty();
    }

    private boolean hasSameOrOverlappingProducedTypes(Route actual, Route other) {
        final Set<MediaType> actualProducedMediaTypes = actual.getProducedMediaTypes();
        final Set<MediaType> otherProducedMediaTypes = other.getProducedMediaTypes();

        if (actualProducedMediaTypes.isEmpty() && otherProducedMediaTypes.isEmpty()) {
            return true;
        }

        // One is empty
        if (actualProducedMediaTypes.isEmpty() || otherProducedMediaTypes.isEmpty()) {
            return true;
        }

        final Sets.SetView<MediaType> intersection = Sets.intersection(actualProducedMediaTypes, otherProducedMediaTypes);
        return !intersection.isEmpty();
    }

    /**
     * Stopping the router. All routes are cleared.
     */
    @Invalidate
    public void stop() {
        routes.clear();
    }

    private synchronized Set<Route> copy() {
        return new LinkedHashSet<Route>(routes);
    }

    /**
     * Gets the {@link org.wisdom.api.router.Route} object handling the given request.
     *
     * @param method  the method the request method
     * @param uri     the URL of the request
     * @param request the incoming request
     * @return the route, {@literal unbound} if no action method can handle the request.
     */
    @Override
    public Route getRouteFor(HttpMethod method, String uri, Request request) {
        // Compute the list of matching routes - only the path is check in this first stage
        List<Route> list = new ArrayList<>(1);
        //TODO This can be faster by using an immutable list.
        list.addAll(copy().stream()
                .filter(route -> route.matches(method, uri))
                .sorted((r1, r2) -> {
                    // Exact match first.
                    if (r1.getUrl().equalsIgnoreCase(uri)) {
                        return -1;
                    } else if (r2.getUrl().equalsIgnoreCase(uri)) {
                        return 1;
                    }
                    // Not comparable
                    return 0;
                })
                .collect(Collectors.toList()));

        if (list.isEmpty()) {
            // Creates an unbound route - 404
            return new RouteDelegate(this, new Route(method, uri, Status.NOT_FOUND));
        }

        // Find the route that accept the request
        List<Route> fullMatch = new ArrayList<>();
        List<Route> partialMatch = new ArrayList<>();
        for (Route route : list) {
            final int acceptation = route.isCompliantWithRequestContentType(request);
            switch (acceptation) {
                case 2:
                    // It's a full match
                    fullMatch.add(route);
                    break;
                case 1:
                    // It's a wildcard match, we have to see if we don't have a full match later.
                    partialMatch.add(route);
                    break;
                default:
                    // Not accepted.
            }
        }

        if (fullMatch.isEmpty() && partialMatch.isEmpty()) {
            // Not Acceptable Content
            return new RouteDelegate(this, new Route(method, uri, Status.UNSUPPORTED_MEDIA_TYPE));
        }

        // Check against the produce type
        fullMatch.addAll(partialMatch);
        for (Route route : fullMatch) {
            if (route.isCompliantWithRequestAccept(request)) {
                return route;
            }
        }

        return new RouteDelegate(this, new Route(method, uri, Status.NOT_ACCEPTABLE));

    }

    /**
     * Gets the URL that would invoke the given action method.
     *
     * @param className the controller class
     * @param method    the controller method
     * @param params    map of parameter name - value
     * @return the computed URL, {@literal null} if no route matches the given action method
     */
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

    /**
     * @return a copy of the current routes.
     */
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
            String originalRegexEscaped = String.format("\\{%s(\\+)?\\}", entry.getKey());
            // If regex is in the url as placeholder we replace the placeholder
            final boolean containVar = urlWithReplacedPlaceholders.contains("{" + entry.getKey() + "}");
            final boolean containAndCanSpreadOnSeveralSegment
                    = urlWithReplacedPlaceholders.contains("{" + entry.getKey() + "+}");
            if (containVar || containAndCanSpreadOnSeveralSegment) {
                urlWithReplacedPlaceholders = urlWithReplacedPlaceholders.replaceAll(
                        originalRegexEscaped,
                        pathEncode(entry.getValue().toString(), containAndCanSpreadOnSeveralSegment));
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

    private String pathEncode(String s, boolean canSpreadOnSeveralSegments) {
        String copy = s;
        for (Map.Entry<String, String> c : PERCENT_ENCODING_MAP.entrySet()) {
            if (s.contains(c.getKey())) {
                if (c.getKey().endsWith("/") && canSpreadOnSeveralSegments) {
                    // The canSpreadOnSeveralSegments parameter is true when the uri contains + such as in {path+}. In this
                    // case, we must not convert "/" by the percent value.
                    continue;
                }
                copy = copy.replace(c.getKey(), c.getValue());
            }
        }
        return copy;
    }

    private String encode(String v) {
        try {
            return URLEncoder.encode(v, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // UTF-8 is part of the JVM specification.
            throw new IllegalArgumentException("UTF-8 not supported", e);
        }
    }

    /**
     * @return the validator object used to validate parameters.
     */
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
        return ImmutableSet.copyOf(filters);
    }

    /**
     * For testing purpose only
     * @return a direct reference on the filter set.
     */
    protected Set<Filter> getDirectReferenceOnFilters() {
        return filters;
    }

    protected List<Interceptor<?>> getInterceptors() {
        return interceptors;
    }

    protected ParameterFactories getParameterConverterEngine() {
        return engine;
    }

    /**
     * Binds a filter.
     *
     * @param filter the filter
     */
    @Bind(aggregate = true, optional = true)
    public void bindFilter(Filter filter) {
        filters.add(filter);
    }

    /**
     * Unbinds a filter.
     *
     * @param filter the filter
     */
    @Unbind
    public synchronized void unbindFilter(Filter filter) {
        filters.remove(filter);
    }

    /**
     * Sets the parameter converter engine. For testing purpose only.
     *
     * @param parameterConverterEngine the parameter converter engine
     */
    public void setParameterConverterEngine(ParameterFactories parameterConverterEngine) {
        this.engine = parameterConverterEngine;
    }

    private static final Comparator<Filter> COMPARATOR = (o1, o2) -> {

        // In case of object equality, returns 0.
        if (o1 == o2 || o1.hashCode() == o2.hashCode()) {
            return 0;
        }

        // In all the other cases, we must never return 0, that would mean equality,
        // and you can't have equal element in a set.
        int compare = Integer.valueOf(o2.priority()).compareTo(o1.priority());
        if (compare == 0) {
            return -1;
        } else {
            return compare;
        }
    };

    /**
     * An implementation of a sorted set (backed up on an array list) to manage the list of filter. This
     * ensures the 'unicity' of the filters by checking object equality and hashcode. Thus it supports proxies.
     * <p/>
     * Methods are guarded by the monitor lock.
     */
    private class FilterSet extends ArrayList<Filter> implements Set<Filter> {

        @Override
        public synchronized boolean add(Filter filter) {
            if (!contains(filter)) {
                super.add(filter);
                Collections.sort(this, COMPARATOR);
                return true;
            }
            return false;
        }

        @Override
        public synchronized boolean contains(Object o) {
            for (Object f : this) {
                if (o == f || o.hashCode() == f.hashCode()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public synchronized int indexOf(Object o) {
            if (o == null) {
                return -1;
            }

            for (int i = 0; i < size(); i++) {
                Object f = get(i);
                if (o == f || o.hashCode() == f.hashCode()) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public synchronized boolean remove(Object o) {
            int index = indexOf(o);
            if (index != -1) {
                remove(index);
                return true;
            }
            return false;
        }

        @Override
        public synchronized int size() {
            return super.size();
        }
    }
}

