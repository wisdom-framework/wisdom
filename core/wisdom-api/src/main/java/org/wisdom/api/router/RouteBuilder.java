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

import com.google.common.base.Preconditions;
import org.wisdom.api.Controller;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

import java.lang.reflect.Method;

/**
 * Builder object to create routes.
 * <p>Example:</p>
 * <code>
 * <pre>
 *         Route route = new RouteBuilder().route(HttpMethod.GET).on("/foo/{id}").to(MyController, "index");
 *         Route route = new RouteBuilder().route(HttpMethod.GET).on("/foo/{id}").to(MyController, "index").accepting
 *         ("plain/text").providing("application/json");
 * </pre>
 * </code>
 * Notice that route builder cannot be reused, and so can generate only one route object.
 */
public class RouteBuilder {

    private static final String ERROR_CTRL = "Cannot find the controller method `";
    private static final String ERROR_IN = "` in `";

    private Controller controller;
    private Method controllerMethod;
    private HttpMethod httpMethod;
    private String uri;

    /**
     * Sets the HTTP Method of the resulting route.
     *
     * @param method the HTTP method, must not be {@literal null}.
     * @return the current route builder
     */
    public RouteBuilder route(HttpMethod method) {
        httpMethod = method;
        return this;
    }

    /**
     * Sets the URI (pattern) of the resulting route.
     *
     * @param uri the uri that can use the route uri pattern, must not be {@literal null}.
     * @return the current route builder
     */
    public RouteBuilder on(String uri) {
        if (!uri.startsWith("/")) {
            this.uri = "/" + uri;
        } else {
            this.uri = uri;
        }
        return this;
    }

    /**
     * Sets the targeted action method of the resulting route.
     *
     * @param controller the controller object, must not be {@literal null}.
     * @param method     the method name, must not be {@literal null}.
     * @return the current route builder
     */
    public Route to(Controller controller, String method) {
        Preconditions.checkNotNull(controller);
        Preconditions.checkNotNull(method);
        this.controller = controller;
        try {
            this.controllerMethod = verifyThatControllerAndMethodExists(controller.getClass(),
                    method);
        } catch (Exception e) {
            throw new IllegalArgumentException(ERROR_CTRL + method + ERROR_IN + controller
                    .getClass() + "`, or the method is invalid", e);
        }
        return _build();
    }

    /**
     * Sets the targeted action method of the resulting route.
     *
     * @param controller the controller object, must not be {@literal null}.
     * @param method     the method name, must not be {@literal null}.
     * @return the current route builder
     */
    public Route to(Controller controller, Method method) {
        Preconditions.checkNotNull(controller);
        Preconditions.checkNotNull(method);
        this.controller = controller;
        this.controllerMethod = method;
        if (!method.getReturnType().isAssignableFrom(Result.class)) {
            throw new IllegalArgumentException(ERROR_CTRL + method + ERROR_IN + controller
                    .getClass() + "`, or the method does not return a " + Result.class.getName() + " object");
        }
        return _build();
    }

    /**
     * Internal method building the route.
     *
     * @return the route.
     */
    private Route _build() {
        Preconditions.checkNotNull(controller);
        Preconditions.checkNotNull(httpMethod);
        Preconditions.checkNotNull(uri);
        Preconditions.checkNotNull(httpMethod);

        return new Route(httpMethod, uri, controller, controllerMethod);
    }

    /**
     * Checks that the action method really exists.
     *
     * @param controller       the controller object
     * @param controllerMethod the method name
     * @return the Method object.
     * @throws NoSuchMethodException if the action method does not exist in the given controller object.
     */
    private Method verifyThatControllerAndMethodExists(Class<?> controller,
                                                       String controllerMethod) throws NoSuchMethodException {

        Method methodFromQueryingClass = null;

        // 1. Make sure method is in class
        // 2. Make sure only one method is there. Otherwise we cannot really
        // know what to do with the parameters.
        for (Method method : controller.getMethods()) {

            if (method.getName().equals(controllerMethod)) {
                if (methodFromQueryingClass == null) {
                    methodFromQueryingClass = method;
                } else {
                    throw new NoSuchMethodException();
                }
            }

        }

        if (methodFromQueryingClass == null) {
            throw new NoSuchMethodException("The method " + controllerMethod + " does not exist in " + controller.getName());
        }

        // make sure that the return type of that controller method
        // is of type Result.
        if (methodFromQueryingClass.getReturnType().isAssignableFrom(Result.class)) {
            return methodFromQueryingClass;
        } else {
            throw new NoSuchMethodException("The method " + controllerMethod + " is declared in " + controller
                    .getName() + " but does not return a " + Result.class.getName() + " object.");
        }
    }

}
