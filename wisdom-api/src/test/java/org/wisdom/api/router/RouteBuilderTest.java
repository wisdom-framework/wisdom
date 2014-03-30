package org.wisdom.api.router;

import org.junit.Test;
import org.wisdom.api.Controller;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the syntax of the route builder.
 */
public class RouteBuilderTest {

    private class MyController implements Controller {

        public Result index() {
            return Results.ok();
        }

        public void invalid() {
            // Does not return result.
        }

        @Override
        public List<Route> routes() {
            return null;
        }
    }

    @Test
    public void testBuildingWithString() throws NoSuchMethodException {
        MyController controller = new MyController();
        Method method = controller.getClass().getMethod("index");
        Route route = new RouteBuilder().route(HttpMethod.GET).on("/").to(controller, "index");
        assertThat(route).isNotNull();
        assertThat(route.getControllerClass()).isEqualTo(MyController.class);
        assertThat(route.getControllerObject()).isEqualTo(controller);
        assertThat(route.getControllerMethod()).isEqualTo(method);
        assertThat(route.getHttpMethod()).isEqualTo(HttpMethod.GET);
        assertThat(route.getUrl()).isEqualTo("/");
    }

    @Test
    public void testBuildingWithMethod() throws NoSuchMethodException {
        MyController controller = new MyController();
        Method method = controller.getClass().getMethod("index");
        Route route = new RouteBuilder().route(HttpMethod.GET).on("/").to(controller, method);
        assertThat(route).isNotNull();
        assertThat(route.getControllerClass()).isEqualTo(MyController.class);
        assertThat(route.getControllerObject()).isEqualTo(controller);
        assertThat(route.getControllerMethod()).isEqualTo(method);
        assertThat(route.getHttpMethod()).isEqualTo(HttpMethod.GET);
        assertThat(route.getUrl()).isEqualTo("/");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidMethodWithString() throws NoSuchMethodException {
        MyController controller = new MyController();
        new RouteBuilder().route(HttpMethod.GET).on("/").to(controller, "invalid");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingMethod() throws NoSuchMethodException {
        MyController controller = new MyController();
        new RouteBuilder().route(HttpMethod.GET).on("/").to(controller, "doesNotExist");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidMethodWithMethod() throws NoSuchMethodException {
        MyController controller = new MyController();
        Method method = controller.getClass().getMethod("invalid");
        new RouteBuilder().route(HttpMethod.GET).on("/").to(controller, method);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMethodFromAnotherController() throws NoSuchMethodException {
        MyController controller = new MyController();
        MyController controller2 = new MyController();
        Method method = controller.getClass().getMethod("invalid");
        new RouteBuilder().route(HttpMethod.GET).on("/").to(controller2, method);
    }

}
