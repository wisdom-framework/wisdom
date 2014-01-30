package org.wisdom.api.router;

import org.wisdom.api.Controller;
import org.wisdom.api.http.HttpMethod;

import java.util.Collection;
import java.util.Map;

/**
 * The router service interface.
 */
public interface Router {

    Route getRouteFor(HttpMethod method, String uri);

    Route getRouteFor(String method, String uri);

    String getReverseRouteFor(Class<? extends Controller> clazz, String method, Map<String,
            Object> params);

    String getReverseRouteFor(String className, String method, Map<String, Object> params);

    String getReverseRouteFor(String className, String method);

    String getReverseRouteFor(Controller controller, String method, Map<String,
            Object> params);

    String getReverseRouteFor(Class<? extends Controller> clazz, String method);

    String getReverseRouteFor(Controller controller, String method);

    Collection<Route> getRoutes();

    // Method avoiding using maps in controllers.

    String getReverseRouteFor(Controller controller, String method, String var1, Object val1);

    String getReverseRouteFor(Controller controller, String method, String var1, Object val1, String var2,
                                     Object val2);

    String getReverseRouteFor(Controller controller, String method, String var1, Object val1, String var2,
                                     Object val2, String var3, Object val3);

    String getReverseRouteFor(Controller controller, String method, String var1, Object val1, String var2,
                                     Object val2, String var3, Object val3, String var4, Object val4);

    String getReverseRouteFor(Controller controller, String method, String var1, Object val1, String var2,
                                     Object val2, String var3, Object val3, String var4, Object val4, String var5,
                                     Object val5);

    String getReverseRouteFor(Class<? extends Controller> clazz, String method, String var1, Object val1);

    String getReverseRouteFor(Class<? extends Controller> clazz, String method, String var1, Object val1, String var2,
                                     Object val2);

    String getReverseRouteFor(Class<? extends Controller> clazz, String method, String var1, Object val1, String var2,
                                     Object val2, String var3, Object val3);

    String getReverseRouteFor(Class<? extends Controller> clazz, String method, String var1, Object val1, String var2,
                                     Object val2, String var3, Object val3, String var4, Object val4);

    String getReverseRouteFor(Class<? extends Controller> clazz, String method, String var1, Object val1, String var2,
                                     Object val2, String var3, Object val3, String var4, Object val4, String var5,
                                     Object val5);
}
