package org.ow2.chameleon.wisdom.samples.helpers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.ow2.chameleon.wisdom.api.Controller;
import org.ow2.chameleon.wisdom.api.http.HttpMethod;
import org.ow2.chameleon.wisdom.api.route.Route;
import org.ow2.chameleon.wisdom.api.route.RouteUtils;
import org.ow2.chameleon.wisdom.api.route.Router;
import org.ow2.chameleon.wisdom.api.route.RoutingException;

import java.util.*;

/**
 *
 */
public class FakeRouter implements Router {

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
        return getReverseRouteFor(clazz.getName(), method, params);
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
    public String getReverseRouteFor(String className, String method) {
        return getReverseRouteFor(className, method, null);
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

    @Override
    public String getReverseRouteFor(Controller controller, String method, String var1, Object val1) {
        return getReverseRouteFor(controller, method, ImmutableMap.<String, Object>of(var1, val1));
    }

    @Override
    public String getReverseRouteFor(Controller controller, String method, String var1, Object val1, String var2, Object val2) {
        return getReverseRouteFor(controller, method, ImmutableMap.<String, Object>of(var1, val1, var2, val2));
    }

    @Override
    public String getReverseRouteFor(Controller controller, String method, String var1, Object val1, String var2,
                                     Object val2, String var3, Object val3) {
        return getReverseRouteFor(controller, method, ImmutableMap.<String, Object>of(var1, val1, var2, val2, var3,
                val3));
    }

    @Override
    public String getReverseRouteFor(Controller controller, String method, String var1, Object val1, String var2, Object val2, String var3, Object val3, String var4, Object val4) {
        return getReverseRouteFor(controller, method, ImmutableMap.<String, Object>of(var1, val1, var2, val2, var3,
                val3, var4, val4));
    }

    @Override
    public String getReverseRouteFor(Controller controller, String method, String var1, Object val1, String var2, Object val2, String var3, Object val3, String var4, Object val4, String var5, Object val5) {
        return getReverseRouteFor(controller, method, ImmutableMap.<String, Object>of(var1, val1, var2, val2, var3,
                val3, var4, val4, var5, val5));
    }

    @Override
    public String getReverseRouteFor(Class<? extends Controller> clazz, String method, String var1, Object val1) {
        return getReverseRouteFor(clazz, method, ImmutableMap.<String, Object>of(var1, val1));
    }

    @Override
    public String getReverseRouteFor(Class<? extends Controller> clazz, String method, String var1, Object val1, String var2, Object val2) {
        return getReverseRouteFor(clazz, method, ImmutableMap.<String, Object>of(var1, val1, var2, val2));
    }

    @Override
    public String getReverseRouteFor(Class<? extends Controller> clazz, String method, String var1, Object val1, String var2,
                                     Object val2, String var3, Object val3) {
        return getReverseRouteFor(clazz, method, ImmutableMap.<String, Object>of(var1, val1, var2, val2, var3,
                val3));
    }

    @Override
    public String getReverseRouteFor(Class<? extends Controller> clazz, String method, String var1, Object val1, String var2, Object val2, String var3, Object val3, String var4, Object val4) {
        return getReverseRouteFor(clazz, method, ImmutableMap.<String, Object>of(var1, val1, var2, val2, var3,
                val3, var4, val4));
    }

    @Override
    public String getReverseRouteFor(Class<? extends Controller> clazz, String method, String var1, Object val1, String var2, Object val2, String var3, Object val3, String var4, Object val4, String var5, Object val5) {
        return getReverseRouteFor(clazz, method, ImmutableMap.<String, Object>of(var1, val1, var2, val2, var3,
                val3, var4, val4, var5, val5));
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
