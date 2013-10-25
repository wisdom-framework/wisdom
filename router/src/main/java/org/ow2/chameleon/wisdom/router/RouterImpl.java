package org.ow2.chameleon.wisdom.router;

import com.google.common.collect.Maps;
import org.apache.felix.ipojo.annotations.*;
import org.ow2.chameleon.wisdom.api.Controller;
import org.ow2.chameleon.wisdom.api.http.HttpMethod;
import org.ow2.chameleon.wisdom.api.route.Route;
import org.ow2.chameleon.wisdom.api.route.Router;
import org.ow2.chameleon.wisdom.api.route.RoutingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * The route.
 */
@Component
@Provides
@Instantiate(name = "router")
public class RouterImpl implements Router {

    private static Logger logger = LoggerFactory.getLogger(RouterImpl.class);
    private Set<Route> routes = new LinkedHashSet<Route>();

    @Bind
    public synchronized void bindController(Controller controller) {
        logger.info("Adding routes from " + controller);
        routes.addAll(controller.routes());
    }

    @Unbind
    public synchronized void unbindController(Controller controller) {
        logger.info("Removing routes from " + controller);
        routes.removeAll(controller.routes());
    }

    @Validate
    public void start() {
        logger.info("Router starting");
    }

    @Invalidate
    public void stop() {
        logger.info("Router stopping");
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
    public Route getRouteFor(String method, String uri) {
        return getRouteFor(HttpMethod.from(method), uri);
    }

    @Override
    public String getReverseRouteFor(Class<? extends Controller> clazz, String method, Map<String,
            Object> params) throws RoutingException {
        for (Route route : copy()) {

            if (route.getControllerClass().equals(clazz)
                    && route.getControllerMethod().getName().equals(method)) {

                return computeUrlForRoute(route, params);
            }
        }
        return null;
    }

    @Override
    public String getReverseRouteFor(Controller controller, String method, Map<String, Object> params) {
        return getReverseRouteFor(controller.getClass(), method, params);
    }

    @Override
    public String getReverseRouteFor(Class<? extends Controller> clazz, String method) {
        return getReverseRouteFor(clazz, method, null);
    }

    @Override
    public String getReverseRouteFor(Controller controller, String method) {
        return getReverseRouteFor(controller.getClass(), method, null);
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
        if (queryParameterMap.entrySet().size() > 0) {

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
}

