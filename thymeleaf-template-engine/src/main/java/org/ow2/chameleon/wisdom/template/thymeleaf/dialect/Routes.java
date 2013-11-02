package org.ow2.chameleon.wisdom.template.thymeleaf.dialect;

import com.google.common.collect.ImmutableMap;
import org.ow2.chameleon.wisdom.api.Controller;
import org.ow2.chameleon.wisdom.api.router.Router;
import org.thymeleaf.exceptions.TemplateProcessingException;

/**
 * A thymeleaf expression object to retrieve routes.
 *
 * <h3>Important note about all methods with plenty of arguments</h3>
 * <p>
 *     Because javassist does not support varags, OGNL do not support it either. So we basically created a couple of
 *     method with a different number of argument. If you have more, inject the url from your controller code.
 * </p>
 */
public class Routes {
    /**
     * The routes evaluation object is passed in the context using this variable.
     */
    public static final String ROUTES_VAR = "__routes__";

    /**
     * The object name used to get this object.
     */
    public static final String OBJECT_NAME = "routes";


    private final Router router;
    private final Controller controller;

    public Routes(Router router, Controller controller) {
        this.router = router;
        this.controller = controller;
    }

    public String route(String controllerClass, String method) {
        String route = router.getReverseRouteFor(controllerClass, method);
        if (route == null) {
            throw new TemplateProcessingException("Cannot find the reverse route for " + controllerClass + "#" +
                    method);
        }
        return route;
    }

    public String route(String method) {
        String route = router.getReverseRouteFor(controller, method);
        if (route == null) {
            throw new TemplateProcessingException("Cannot find the reverse route for " + controller.getClass().getName() +
                    "#" + method);
        }
        return route;
    }

    public String route(String controllerClass, String method, String var1, Object value1) {
        ImmutableMap<String, Object> params = ImmutableMap.<String, Object>of(var1, value1);
        String route = router.getReverseRouteFor(controllerClass, method, params);
        if (route == null) {
            throw new TemplateProcessingException("Cannot find the reverse route for " + controller.getClass().getName() +
                    "#" + method + " with params : " + params);
        }
        return route;
    }

    public String route(String controllerClass, String method,
                        String var1, Object value1,
                        String var2, Object value2) {
        ImmutableMap<String, Object> params = ImmutableMap.<String, Object>of(
                var1, value1,
                var2, value2);
        String route = router.getReverseRouteFor(controllerClass, method, params);
        if (route == null) {
            throw new TemplateProcessingException("Cannot find the reverse route for " + controller.getClass().getName() +
                    "#" + method + " with params : " + params);
        }
        return route;
    }

    public String route(String controllerClass, String method,
                        String var1, Object value1,
                        String var2, Object value2,
                        String var3, Object value3) {
        ImmutableMap<String, Object> params = ImmutableMap.<String, Object>of(
                var1, value1,
                var2, value2,
                var3, value3);
        String route = router.getReverseRouteFor(controllerClass, method, params);
        if (route == null) {
            throw new TemplateProcessingException("Cannot find the reverse route for " + controller.getClass().getName() +
                    "#" + method + " with params : " + params);
        }
        return route;
    }

    public String route(String controllerClass, String method,
                        String var1, Object value1,
                        String var2, Object value2,
                        String var3, Object value3,
                        String var4, Object value4) {
        ImmutableMap<String, Object> params = ImmutableMap.<String, Object>of(
                var1, value1,
                var2, value2,
                var3, value3,
                var4, value4);
        String route = router.getReverseRouteFor(controllerClass, method, params);
        if (route == null) {
            throw new TemplateProcessingException("Cannot find the reverse route for " + controller.getClass().getName() +
                    "#" + method + " with params : " + params);
        }
        return route;
    }

    public String route(String controllerClass, String method,
                        String var1, Object value1,
                        String var2, Object value2,
                        String var3, Object value3,
                        String var4, Object value4,
                        String var5, Object value5) {
        ImmutableMap<String, Object> params = ImmutableMap.<String, Object>of(
                var1, value1,
                var2, value2,
                var3, value3,
                var4, value4,
                var5, value5);
        String route = router.getReverseRouteFor(controllerClass, method, params);
        if (route == null) {
            throw new TemplateProcessingException("Cannot find the reverse route for " + controller.getClass().getName() +
                    "#" + method + " with params : " + params);
        }
        return route;
    }

    public String route(String method, String var1, Object value1) {
        return route(controller.getClass().getName(), method, var1, value1);
    }

    public String route(String method,
                        String var1, Object value1,
                        String var2, Object value2) {
        return route(controller.getClass().getName(), method, var1, value1, var2, value2);
    }

    public String route(String method,
                        String var1, Object value1,
                        String var2, Object value2,
                        String var3, Object value3) {
        return route(controller.getClass().getName(), method, var1, value1, var2, value2, var3, value3);
    }

    public String route(String method,
                        String var1, Object value1,
                        String var2, Object value2,
                        String var3, Object value3,
                        String var4, Object value4) {
        return route(controller.getClass().getName(), method,
                var1, value1, var2, value2, var3, value3, var4, value4
        );
    }

    public String route(String method,
                        String var1, Object value1,
                        String var2, Object value2,
                        String var3, Object value3,
                        String var4, Object value4,
                        String var5, Object value5) {
        return route(controller.getClass().getName(), method,
                var1, value1, var2, value2, var3, value3, var4, value4, var5, value5
        );
    }
}
