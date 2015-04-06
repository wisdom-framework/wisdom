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
package org.wisdom.template.thymeleaf.dialect;

import com.google.common.collect.ImmutableMap;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.wisdom.api.Controller;
import org.wisdom.api.asset.Asset;
import org.wisdom.api.asset.Assets;
import org.wisdom.api.router.Router;

/**
 * A thymeleaf expression object to retrieve routes.
 * <p/>
 * <h3>Important note about all methods with plenty of arguments</h3>
 * <p>
 * Because javassist does not support varags, OGNL do not support it either. So we basically created a couple of
 * method with a different number of argument. If you have more, inject the url from your controller code.
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

    /**
     * The error message when the route cannot be found.
     */
    public static final String ERR_FIND_ROUTE = "Cannot find the reverse route for ";

    /**
     * Suffix of the error message when using paramters.
     */
    public static final String WITH_PARAM = " with params : ";

    /**
     * The router.
     */
    private final Router router;

    /**
     * The asset control point.
     */
    private final Assets assets;

    /**
     * The current controller.
     */
    private final Controller controller;

    /**
     * Creates a new routes macro.
     *
     * @param router     the router
     * @param assets     the assets
     * @param controller the controller
     */
    public Routes(Router router, Assets assets, Controller controller) {
        this.router = router;
        this.controller = controller;
        this.assets = assets;
    }

    /**
     * Retrieves the reverse route (url) of the specified action.
     *
     * @param controllerClass the controller
     * @param method          the method
     * @return the url
     * @throws org.thymeleaf.exceptions.TemplateProcessingException if the route cannot be found
     */
    public String route(String controllerClass, String method) {
        String route = router.getReverseRouteFor(controllerClass, method);
        if (route == null) {
            throw new TemplateProcessingException(ERR_FIND_ROUTE + controllerClass + "#" +
                    method);
        }
        return route;
    }

    /**
     * Retrieves the reverse route (url) of the specified action (using the current controller).
     *
     * @param method the method
     * @return the url
     * @throws org.thymeleaf.exceptions.TemplateProcessingException if the route cannot be found
     */
    public String route(String method) {
        String route = router.getReverseRouteFor(controller, method);
        if (route == null) {
            throw new TemplateProcessingException(ERR_FIND_ROUTE + controller.getClass().getName() +
                    "#" + method);
        }
        return route;
    }

    /**
     * Retrieves the reverse route (url) of the specified action.
     *
     * @param controllerClass the controller
     * @param method          the method
     * @param var1            the first parameter name
     * @param value1          the first parameter value
     * @return the url
     * @throws org.thymeleaf.exceptions.TemplateProcessingException if the route cannot be found
     */
    public String route(String controllerClass, String method, String var1, Object value1) {
        ImmutableMap<String, Object> params = ImmutableMap.of(var1, value1);
        String route = router.getReverseRouteFor(controllerClass, method, params);
        if (route == null) {
            throw new TemplateProcessingException(ERR_FIND_ROUTE + controller.getClass().getName() +
                    "#" + method + WITH_PARAM + params);
        }
        return route;
    }

    /**
     * Retrieves the reverse route (url) of the specified action.
     *
     * @param controllerClass the controller
     * @param method          the method
     * @param var1            the first parameter name
     * @param value1          the first parameter value
     * @param var2            the second parameter name
     * @param value2          the second parameter value*
     * @return the url
     * @throws org.thymeleaf.exceptions.TemplateProcessingException if the route cannot be found
     */
    public String route(String controllerClass, String method,
                        String var1, Object value1,
                        String var2, Object value2) {
        ImmutableMap<String, Object> params = ImmutableMap.of(
                var1, value1,
                var2, value2);
        String route = router.getReverseRouteFor(controllerClass, method, params);
        if (route == null) {
            throw new TemplateProcessingException(ERR_FIND_ROUTE + controller.getClass().getName() +
                    "#" + method + WITH_PARAM + params);
        }
        return route;
    }

    /**
     * Retrieves the reverse route (url) of the specified action.
     *
     * @param controllerClass the controller
     * @param method          the method
     * @param var1            the first parameter name
     * @param value1          the first parameter value
     * @param var2            the second parameter name
     * @param value2          the second parameter value
     * @param var3            the third parameter name
     * @param value3          the third parameter value
     * @return the url
     * @throws org.thymeleaf.exceptions.TemplateProcessingException if the route cannot be found
     */
    public String route(String controllerClass, String method,
                        String var1, Object value1,
                        String var2, Object value2,
                        String var3, Object value3) {
        ImmutableMap<String, Object> params = ImmutableMap.of(
                var1, value1,
                var2, value2,
                var3, value3);
        String route = router.getReverseRouteFor(controllerClass, method, params);
        if (route == null) {
            throw new TemplateProcessingException(ERR_FIND_ROUTE + controller.getClass().getName() +
                    "#" + method + " with params : " + params);
        }
        return route;
    }

    /**
     * Retrieves the reverse route (url) of the specified action.
     *
     * @param controllerClass the controller
     * @param method          the method
     * @param var1            the first parameter name
     * @param value1          the first parameter value
     * @param var2            the second parameter name
     * @param value2          the second parameter value
     * @param var3            the third parameter name
     * @param value3          the third parameter value
     * @param var4            the fourth parameter name
     * @param value4          the fourth parameter value
     * @return the url
     * @throws org.thymeleaf.exceptions.TemplateProcessingException if the route cannot be found
     */
    public String route(String controllerClass, String method,
                        String var1, Object value1,
                        String var2, Object value2,
                        String var3, Object value3,
                        String var4, Object value4) {
        ImmutableMap<String, Object> params = ImmutableMap.of(
                var1, value1,
                var2, value2,
                var3, value3,
                var4, value4);
        String route = router.getReverseRouteFor(controllerClass, method, params);
        if (route == null) {
            throw new TemplateProcessingException(ERR_FIND_ROUTE + controller.getClass().getName() +
                    "#" + method + " with params : " + params);
        }
        return route;
    }

    /**
     * Retrieves the reverse route (url) of the specified action.
     *
     * @param controllerClass the controller
     * @param method          the method
     * @param var1            the first parameter name
     * @param value1          the first parameter value
     * @param var2            the second parameter name
     * @param value2          the second parameter value
     * @param var3            the third parameter name
     * @param value3          the third parameter value
     * @param var4            the fourth parameter name
     * @param value4          the fourth parameter value
     * @param var5            the fifth parameter name
     * @param value5          the fifth parameter value
     * @return the url
     * @throws org.thymeleaf.exceptions.TemplateProcessingException if the route cannot be found
     */
    public String route(String controllerClass, String method,
                        String var1, Object value1,
                        String var2, Object value2,
                        String var3, Object value3,
                        String var4, Object value4,
                        String var5, Object value5) {
        ImmutableMap<String, Object> params = ImmutableMap.of(
                var1, value1,
                var2, value2,
                var3, value3,
                var4, value4,
                var5, value5);
        String route = router.getReverseRouteFor(controllerClass, method, params);
        if (route == null) {
            throw new TemplateProcessingException(ERR_FIND_ROUTE + controller.getClass().getName() +
                    "#" + method + " with params : " + params);
        }
        return route;
    }

    /**
     * Retrieves the reverse route (url) of the specified action on the current controller.
     *
     * @param method the method
     * @param var1   the first parameter name
     * @param value1 the first parameter value
     * @return the url
     * @throws org.thymeleaf.exceptions.TemplateProcessingException if the route cannot be found
     */
    public String route(String method, String var1, Object value1) {
        return route(controller.getClass().getName(), method, var1, value1);
    }

    /**
     * Retrieves the reverse route (url) of the specified action on the current controller.
     *
     * @param method the method
     * @param var1   the first parameter name
     * @param value1 the first parameter value
     * @param var2   the second parameter name
     * @param value2 the second parameter value
     * @return the url
     * @throws org.thymeleaf.exceptions.TemplateProcessingException if the route cannot be found
     */
    public String route(String method,
                        String var1, Object value1,
                        String var2, Object value2) {
        return route(controller.getClass().getName(), method, var1, value1, var2, value2);
    }

    /**
     * Retrieves the reverse route (url) of the specified action on the current controller.
     *
     * @param method the method
     * @param var1   the first parameter name
     * @param value1 the first parameter value
     * @param var2   the second parameter name
     * @param value2 the second parameter value
     * @param var3   the third parameter name
     * @param value3 the third parameter value
     * @return the url
     * @throws org.thymeleaf.exceptions.TemplateProcessingException if the route cannot be found
     */
    public String route(String method,
                        String var1, Object value1,
                        String var2, Object value2,
                        String var3, Object value3) {
        return route(controller.getClass().getName(), method, var1, value1, var2, value2, var3, value3);
    }

    /**
     * Retrieves the reverse route (url) of the specified action on the current controller.
     *
     * @param method the method
     * @param var1   the first parameter name
     * @param value1 the first parameter value
     * @param var2   the second parameter name
     * @param value2 the second parameter value
     * @param var3   the third parameter name
     * @param value3 the third parameter value
     * @param var4   the fourth parameter name
     * @param value4 the fourth parameter value
     * @return the url
     * @throws org.thymeleaf.exceptions.TemplateProcessingException if the route cannot be found
     */
    public String route(String method,
                        String var1, Object value1,
                        String var2, Object value2,
                        String var3, Object value3,
                        String var4, Object value4) {
        return route(controller.getClass().getName(), method,
                var1, value1, var2, value2, var3, value3, var4, value4
        );
    }

    /**
     * Retrieves the reverse route (url) of the specified action on the current controller.
     *
     * @param method the method
     * @param var1   the first parameter name
     * @param value1 the first parameter value
     * @param var2   the second parameter name
     * @param value2 the second parameter value
     * @param var3   the third parameter name
     * @param value3 the third parameter value
     * @param var4   the fourth parameter name
     * @param value4 the fourth parameter value
     * @param var5   the fifth parameter name
     * @param value5 the fifth parameter value
     * @return the url
     * @throws org.thymeleaf.exceptions.TemplateProcessingException if the route cannot be found
     */
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

    /**
     * Gets the url of the given asset. Throws an exception if the asset cannot be found.
     *
     * @param path the asset's path
     * @return the url
     */
    public String asset(String path) {
        Asset asset = assets.assetAt(path);
        if (asset == null) {
            // If the path starts with "/", try without
            if (path.startsWith("/")) {
                return asset(path.substring(1));
            }
            // Not found.
            throw new TemplateProcessingException("Cannot find the URL of the asset " + path);
        }
        return asset.getPath();
    }
}
