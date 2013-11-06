package org.ow2.chameleon.wisdom.api.router;

import com.google.common.base.Preconditions;
import org.ow2.chameleon.wisdom.api.Controller;
import org.ow2.chameleon.wisdom.api.http.HttpMethod;
import org.ow2.chameleon.wisdom.api.http.Result;

import java.lang.reflect.Method;

/**
 * Builder object to create routes.
 * <p>Example:</p>
 * <code>
 * <pre>
 *         Route route = new RouteBuilder().route(HttpMethod.GET).on("/foo/{id}").to(MyController, "index");
 *     </pre>
 * </code>
 */
public class RouteBuilder {

    private Controller controller;
    private Method controllerMethod;
    private HttpMethod httpMethod;
    private String uri;

    public RouteBuilder route(HttpMethod method) {
        httpMethod = method;
        return this;
    }

    public RouteBuilder on(String uri) {
        if (!uri.startsWith("/")) {
            this.uri = "/" + uri;
        } else {
            this.uri = uri;
        }
        return this;
    }

    public Route to(Controller controller, String method) {
        Preconditions.checkNotNull(controller);
        Preconditions.checkNotNull(method);
        this.controller = controller;
        try {
            this.controllerMethod = verifyThatControllerAndMethodExists(controller.getClass(),
                    method);
        } catch (Throwable e) {
            throw new IllegalArgumentException("Cannot find the controller method `" + method + "` in `" + controller
                    .getClass() + "`, or the method is invalid", e);
        }
        return _build();
    }

    public Route to(Controller controller, Method method) {
        Preconditions.checkNotNull(controller);
        Preconditions.checkNotNull(method);
        this.controller = controller;
        try {
            this.controllerMethod = method;
            if (! method.getReturnType().isAssignableFrom(Result.class)) {
                throw new NoSuchMethodException();
            }
        } catch (Throwable e) {
            throw new IllegalArgumentException("Cannot find the controller method `" + method + "` in `" + controller
                    .getClass() + "`, or the method does not return a " + Result.class.getName() + " object", e);
        }
        return _build();
    }

    private Route _build() {
        Preconditions.checkNotNull(controller);
        Preconditions.checkNotNull(httpMethod);
        Preconditions.checkNotNull(uri);
        Preconditions.checkNotNull(httpMethod);

        return new Route(httpMethod, uri, controller, controllerMethod);
    }

    private Method verifyThatControllerAndMethodExists(Class controller,
                                                       String controllerMethod) throws Exception {

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
            throw new NoSuchMethodException();
        }

        // make sure that the return type of that controller method
        // is of type Result.
        if (methodFromQueryingClass.getReturnType().isAssignableFrom(Result.class)) {
            return methodFromQueryingClass;
        } else {
            throw new NoSuchMethodException();
        }
    }

}
