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
package org.wisdom.api.router;

import com.google.common.collect.ImmutableMap;
import org.wisdom.api.Controller;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Request;

import java.util.Map;

/**
 * A default implementation of the router interface.
 */
public abstract class AbstractRouter implements Router {

    /**
     * Gets the route for the given method and uri. This implementation delegates to
     * {@link #getRouteFor(org.wisdom.api.http.HttpMethod, String)}.
     *
     * @param method  the method (must be a valid HTTP method)
     * @param uri     the uri
     * @param request the incoming request
     * @return the route, {@literal unbound} if no controller handles the request
     */
    @Override
    public Route getRouteFor(String method, String uri, Request request) {
        return getRouteFor(HttpMethod.from(method), uri, request);
    }

    /**
     * Gets the route for the given method and uri. This implementation delegates to
     * {@link #getRouteFor(org.wisdom.api.http.HttpMethod, String)}.
     *
     * @param method  the method (must be a valid HTTP method)
     * @param uri     the uri
     * @return the route, {@literal unbound} if no controller handles the request
     * @deprecated use {@link #getRouteFor(String, String, Request)}
     */
    @Override
    @Deprecated
    public Route getRouteFor(String method, String uri) {
        return getRouteFor(HttpMethod.from(method), uri, null);
    }

    /**
     * Gets the route for the given method and uri. This implementation delegates to
     * {@link #getRouteFor(org.wisdom.api.http.HttpMethod, String)}.
     *
     * @param method  the method (must be a valid HTTP method)
     * @param uri     the uri
     * @return the route, {@literal unbound} if no controller handles the request
     * @deprecated use {@link #getRouteFor(HttpMethod, String, Request)}
     */
    @Override
    @Deprecated
    public Route getRouteFor(HttpMethod method, String uri) {
        return getRouteFor(method, uri, null);
    }

    /**
     * Gets the url of the route handled by the specified action method. This implementation delegates to
     * {@link #getReverseRouteFor(String, String, java.util.Map)}.
     *
     * @param clazz  the controller class
     * @param method the controller method
     * @param params map of parameter name - value
     * @return the url, {@literal null} if the action method is not found
     */
    @Override
    public String getReverseRouteFor(Class<? extends Controller> clazz, String method, Map<String,
            Object> params) {
        return getReverseRouteFor(clazz.getName(), method, params);
    }

    /**
     * Gets the url of the route handled by the specified action method. The action does not takes parameters. This
     * implementation delegates to {@link #getReverseRouteFor(String, String, java.util.Map)}.
     *
     * @param className the controller class
     * @param method    the controller method
     * @return the url, {@literal null} if the action method is not found
     */
    @Override
    public String getReverseRouteFor(String className, String method) {
        return getReverseRouteFor(className, method, null);
    }

    /**
     * Gets the url of the route handled by the specified action method. This
     * implementation delegates to {@link #getReverseRouteFor(java.lang.Class, String, java.util.Map)}.
     *
     * @param controller the controller object
     * @param method     the controller method
     * @param params     map of parameter name - value
     * @return the url, {@literal null} if the action method is not found
     */
    @Override
    public String getReverseRouteFor(Controller controller, String method, Map<String, Object> params) {
        return getReverseRouteFor(controller.getClass(), method, params);
    }

    /**
     * Gets the url of the route handled by the specified action method. The action does not takes parameters. This
     * implementation delegates to {@link #getReverseRouteFor(java.lang.Class, String, java.util.Map)}.
     *
     * @param clazz  the controller class
     * @param method the controller method
     * @return the url, {@literal null} if the action method is not found
     */
    @Override
    public String getReverseRouteFor(Class<? extends Controller> clazz, String method) {
        return getReverseRouteFor(clazz, method, null);
    }

    /**
     * Gets the url of the route handled by the specified action method. The action does not takes parameters.
     *
     * @param controller the controller object
     * @param method     the controller method
     * @return the url, {@literal null} if the action method is not found
     */
    @Override
    public String getReverseRouteFor(Controller controller, String method) {
        return getReverseRouteFor(controller.getClass(), method, null);
    }

    /**
     * Gets the url of the route handled by the specified action method.
     *
     * @param controller the controller object
     * @param method     the controller method
     * @param var1       the first parameter name
     * @param val1       the first parameter value
     * @return the url, {@literal null} if the action method is not found.
     */
    @Override
    public String getReverseRouteFor(Controller controller, String method, String var1, Object val1) {
        return getReverseRouteFor(controller, method, ImmutableMap.of(var1, val1));
    }

    /**
     * Gets the url of the route handled by the specified action method.
     *
     * @param controller the controller object
     * @param method     the controller method
     * @param var1       the first parameter name
     * @param val1       the first parameter value
     * @param var2       the second parameter name
     * @param val2       the second parameter value
     * @return the url, {@literal null} if the action method is not found.
     */
    @Override
    public String getReverseRouteFor(Controller controller, String method, String var1, Object val1, String var2, Object val2) {
        return getReverseRouteFor(controller, method, ImmutableMap.of(var1, val1, var2, val2));
    }

    /**
     * Gets the url of the route handled by the specified action method.
     *
     * @param controller the controller object
     * @param method     the controller method
     * @param var1       the first parameter name
     * @param val1       the first parameter value
     * @param var2       the second parameter name
     * @param val2       the second parameter value
     * @param var3       the third parameter name
     * @param val3       the third parameter value
     * @return the url, {@literal null} if the action method is not found.
     */
    @Override
    public String getReverseRouteFor(Controller controller, String method, String var1, Object val1, String var2,
                                     Object val2, String var3, Object val3) {
        return getReverseRouteFor(controller, method, ImmutableMap.of(var1, val1, var2, val2, var3,
                val3));
    }

    /**
     * Gets the url of the route handled by the specified action method.
     *
     * @param controller the controller object
     * @param method     the controller method
     * @param var1       the first parameter name
     * @param val1       the first parameter value
     * @param var2       the second parameter name
     * @param val2       the second parameter value
     * @param var3       the third parameter name
     * @param val3       the third parameter value
     * @param var4       the fourth parameter name
     * @param val4       the fourth parameter value
     * @return the url, {@literal null} if the action method is not found.
     */
    @Override
    public String getReverseRouteFor(Controller controller, String method, String var1, Object val1, String var2, Object val2, String var3, Object val3, String var4, Object val4) {
        return getReverseRouteFor(controller, method, ImmutableMap.of(var1, val1, var2, val2, var3,
                val3, var4, val4));
    }

    /**
     * Gets the url of the route handled by the specified action method.
     *
     * @param controller the controller object
     * @param method     the controller method
     * @param var1       the first parameter name
     * @param val1       the first parameter value
     * @param var2       the second parameter name
     * @param val2       the second parameter value
     * @param var3       the third parameter name
     * @param val3       the third parameter value
     * @param var4       the fourth parameter name
     * @param val4       the fourth parameter value
     * @param var5       the fifth parameter name
     * @param val5       the fifth parameter value
     * @return the url, {@literal null} if the action method is not found.
     */
    @Override
    public String getReverseRouteFor(Controller controller, String method, String var1, Object val1, String var2, Object val2, String var3, Object val3, String var4, Object val4, String var5, Object val5) {
        return getReverseRouteFor(controller, method, ImmutableMap.of(var1, val1, var2, val2, var3,
                val3, var4, val4, var5, val5));
    }

    /**
     * Gets the url of the route handled by the specified action method.
     *
     * @param clazz  the controller class
     * @param method the controller method
     * @param var1   the first parameter name
     * @param val1   the first parameter value
     * @return the url, {@literal null} if the action method is not found.
     */
    @Override
    public String getReverseRouteFor(Class<? extends Controller> clazz, String method, String var1, Object val1) {
        return getReverseRouteFor(clazz, method, ImmutableMap.of(var1, val1));
    }

    /**
     * Gets the url of the route handled by the specified action method.
     *
     * @param clazz  the controller class
     * @param method the controller method
     * @param var1   the first parameter name
     * @param val1   the first parameter value
     * @param var2   the second parameter name
     * @param val2   the second parameter value
     * @return the url, {@literal null} if the action method is not found.
     */
    @Override
    public String getReverseRouteFor(Class<? extends Controller> clazz, String method, String var1, Object val1, String var2, Object val2) {
        return getReverseRouteFor(clazz, method, ImmutableMap.of(var1, val1, var2, val2));
    }

    /**
     * Gets the url of the route handled by the specified action method.
     *
     * @param clazz  the controller class
     * @param method the controller method
     * @param var1   the first parameter name
     * @param val1   the first parameter value
     * @param var2   the second parameter name
     * @param val2   the second parameter value
     * @param var3   the third parameter name
     * @param val3   the third parameter value
     * @return the url, {@literal null} if the action method is not found.
     */
    @Override
    public String getReverseRouteFor(Class<? extends Controller> clazz, String method, String var1, Object val1, String var2,
                                     Object val2, String var3, Object val3) {
        return getReverseRouteFor(clazz, method, ImmutableMap.of(var1, val1, var2, val2, var3,
                val3));
    }

    /**
     * Gets the url of the route handled by the specified action method.
     *
     * @param clazz  the controller class
     * @param method the controller method
     * @param var1   the first parameter name
     * @param val1   the first parameter value
     * @param var2   the second parameter name
     * @param val2   the second parameter value
     * @param var3   the third parameter name
     * @param val3   the third parameter value
     * @param var4   the fourth parameter name
     * @param val4   the fourth parameter value
     * @return the url, {@literal null} if the action method is not found.
     */
    @Override
    public String getReverseRouteFor(Class<? extends Controller> clazz, String method, String var1, Object val1, String var2, Object val2, String var3, Object val3, String var4, Object val4) {
        return getReverseRouteFor(clazz, method, ImmutableMap.of(var1, val1, var2, val2, var3,
                val3, var4, val4));
    }

    /**
     * Gets the url of the route handled by the specified action method.
     *
     * @param clazz  the controller class
     * @param method the controller method
     * @param var1   the first parameter name
     * @param val1   the first parameter value
     * @param var2   the second parameter name
     * @param val2   the second parameter value
     * @param var3   the third parameter name
     * @param val3   the third parameter value
     * @param var4   the fourth parameter name
     * @param val4   the fourth parameter value
     * @param var5   the fifth parameter name
     * @param val5   the fifth parameter value
     * @return the url, {@literal null} if the action method is not found.
     */
    @Override
    public String getReverseRouteFor(Class<? extends Controller> clazz, String method, String var1, Object val1, String var2, Object val2, String var3, Object val3, String var4, Object val4, String var5, Object val5) {
        return getReverseRouteFor(clazz, method, ImmutableMap.of(var1, val1, var2, val2, var3,
                val3, var4, val4, var5, val5));
    }
}
