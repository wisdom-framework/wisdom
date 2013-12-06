package org.wisdom.api.router;

import com.google.common.collect.ImmutableMap;
import org.wisdom.api.Controller;
import org.wisdom.api.http.HttpMethod;

import java.util.Map;

/**
 * A default implementation of the router interface.
 */
public abstract class AbstractRouter implements Router {


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
}
