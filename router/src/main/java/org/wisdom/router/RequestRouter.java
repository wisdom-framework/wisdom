package org.wisdom.router;

import com.google.common.collect.Maps;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.Controller;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.interceptor.Interceptor;
import org.wisdom.api.router.AbstractRouter;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.RouteUtils;
import org.wisdom.api.router.RoutingException;

import javax.validation.Validator;
import java.util.*;

/**
 * The route.
 */
@Component
@Provides
@Instantiate(name = "router")
public class RequestRouter extends AbstractRouter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestRouter.class);

    @Requires(optional = true, specification = Interceptor.class)
    private List<Interceptor<?>> interceptors;

    @Requires(optional = true, proxy = false)
    private Validator validator;

    private Set<RouteDelegate> routes = new LinkedHashSet<RouteDelegate>();

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
                routes.add(new RouteDelegate(this, newRoute));
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

        // now prepare the query string for this url if we got some query params
        if (queryParameterMap.entrySet().isEmpty()) {

            StringBuilder queryParameterStringBuffer = new StringBuilder();

            // The uri is now replaced => we now have to add potential query parameters
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

    protected List<Interceptor<?>> getInterceptors() {
        return interceptors;
    }
}

