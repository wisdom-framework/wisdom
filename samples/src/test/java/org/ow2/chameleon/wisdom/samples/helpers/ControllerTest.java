package org.ow2.chameleon.wisdom.samples.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.ow2.chameleon.wisdom.api.Controller;
import org.ow2.chameleon.wisdom.api.http.Context;
import org.ow2.chameleon.wisdom.api.http.Status;
import org.ow2.chameleon.wisdom.api.route.Route;
import org.ow2.chameleon.wisdom.api.route.RouteUtils;
import org.ow2.chameleon.wisdom.api.route.Router;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fest.reflect.core.Reflection.constructor;

/**
 *
 */
public class ControllerTest implements Status {

    private Map<Controller, List<Route>> routes = new HashMap<>();

    @After
    public void removeContextFromThreadLocal() {
        Context.context.remove();
    }

    private <T extends Controller> List<Route> collectRoutes(T controller) {
        List<Route> routes = Lists.newArrayList();
        routes.addAll(controller.routes());
        routes.addAll(RouteUtils.collectRouteFromControllerAnnotations(controller));
        return routes;
    }

    public Router router() {
        return new FakeRouter();
    }

    public <T extends Controller> ControllerBuilder<T> controller(Class<T> clazz) {
        return new ControllerBuilder<T>(clazz);
    }

    public int status(Action.ActionResult result) {
        return result.result.getStatusCode();
    }

    public String contentType(Action.ActionResult result) {
        return result.result.getContentType();
    }

    public ObjectNode json(Action.ActionResult result) {
        try {
            String s = IOUtils.toString(result.result.getRenderable().render(result.context, result.result));
            return (new ObjectMapper()).readValue(s, ObjectNode.class);
        } catch (Exception e) {
            throw new RuntimeException("Cannot retrieve the json form of result `" + result + "`", e);
        }
    }

    public ArrayNode jsonarray(Action.ActionResult result) {
        try {
            String s = IOUtils.toString(result.result.getRenderable().render(result.context, result.result));
            return (new ObjectMapper()).readValue(s, ArrayNode.class);
        } catch (Exception e) {
            throw new RuntimeException("Cannot retrieve the json form of result `" + result + "`", e);
        }
    }

    public String toString(Action.ActionResult result) {
        try {
            return IOUtils.toString(result.result.getRenderable().render(result.context, result.result));
        } catch (Exception e) {
            throw new RuntimeException("Cannot retrieve the String form of result `" + result + "`", e);
        }
    }

    public static class ControllerBuilder<T extends Controller> {

        private final Class<T> clazz;
        private final T controller;

        public ControllerBuilder(Class<T> clazz) {
            this.clazz = clazz;
            this.controller = createControllerInstance(clazz);
        }

        private <T> T createControllerInstance(Class<T> clazz) {
            // TODO Manage in constructor injection.
            return constructor().in(clazz).newInstance();
        }

        public ControllerBuilder<T> with(String field, Router router) {
            if (router instanceof FakeRouter) {
                ((FakeRouter) router).addController(controller);
            }
            return with(field, (Object) router);
        }

        public ControllerBuilder<T> with(String field, Object value) {
            try {
                Field f = controller.getClass().getDeclaredField(field);
                if (!f.isAccessible()) {
                    f.setAccessible(true);
                }
                f.set(controller, value);
            } catch (Exception e) {
                throw new RuntimeException("Cannot set the field " + field + " with " + value, e);
            }
            return this;
        }

        public T build() {
            return controller;
        }

    }
}
