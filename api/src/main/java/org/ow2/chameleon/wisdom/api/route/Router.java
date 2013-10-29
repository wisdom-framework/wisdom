package org.ow2.chameleon.wisdom.api.route;

import org.ow2.chameleon.wisdom.api.Controller;
import org.ow2.chameleon.wisdom.api.http.HttpMethod;

import java.util.Collection;
import java.util.Map;

/**
 * The router service interface.
 */
public interface Router {

    public Route getRouteFor(HttpMethod method, String uri);

    public Route getRouteFor(String method, String uri);

    public String getReverseRouteFor(Class<? extends Controller> clazz, String method, Map<String,
            Object> params);

    public String getReverseRouteFor(String className, String method, Map<String, Object> params);

    public String getReverseRouteFor(String className, String method);

    public String getReverseRouteFor(Controller controller, String method, Map<String,
            Object> params);

    public String getReverseRouteFor(Class<? extends Controller> clazz, String method);

    public String getReverseRouteFor(Controller controller, String method);

    public Collection<Route> getRoutes();


}
