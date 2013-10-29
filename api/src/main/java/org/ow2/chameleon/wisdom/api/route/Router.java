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

    // Method avoiding using maps in controllers.

    public String getReverseRouteFor(Controller controller, String method, String var1, Object val1);

    public String getReverseRouteFor(Controller controller, String method, String var1, Object val1, String var2,
                                     Object val2);

    public String getReverseRouteFor(Controller controller, String method, String var1, Object val1, String var2,
                                     Object val2, String var3, Object val3);

    public String getReverseRouteFor(Controller controller, String method, String var1, Object val1, String var2,
                                     Object val2, String var3, Object val3, String var4, Object val4);

    public String getReverseRouteFor(Controller controller, String method, String var1, Object val1, String var2,
                                     Object val2, String var3, Object val3, String var4, Object val4, String var5,
                                     Object val5);
}
