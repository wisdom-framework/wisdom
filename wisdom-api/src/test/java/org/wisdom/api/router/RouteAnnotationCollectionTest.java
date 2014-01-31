package org.wisdom.api.router;

import org.junit.Test;
import org.wisdom.api.Controller;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check the collection of the routes from @Route
 */
public class RouteAnnotationCollectionTest {


    @Test
    public void testWithClassWithoutRoutes() {
        Controller instance = new Controller() {
            @Override
            public List<Route> routes() {
                return null;
            }
        };
        List<Route> routes = RouteUtils.collectRouteFromControllerAnnotations(instance);
        assertThat(routes).isEmpty();
    }

    @Test
    public void testWithClassWithRoutes() {
        Controller instance = new Controller() {

            @org.wisdom.api.annotations.Route(method= HttpMethod.GET, uri = "/")
            public Result method1() {
                return null;
            }

            @org.wisdom.api.annotations.Route(method= HttpMethod.POST, uri = "/")
            public Result method2() {
                return null;
            }

            @Override
            public List<Route> routes() {
                return null;
            }
        };
        List<Route> routes = RouteUtils.collectRouteFromControllerAnnotations(instance);
        assertThat(routes).hasSize(2);
        assertThat(routes.get(0).getHttpMethod()).isEqualTo(HttpMethod.GET);
        assertThat(routes.get(0).getUrl()).isEqualTo("/");
        assertThat(routes.get(0).getControllerObject()).isEqualTo(instance);
        assertThat(routes.get(0).getControllerMethod().getName()).isEqualTo("method1");
        assertThat(routes.get(1).getHttpMethod()).isEqualTo(HttpMethod.POST);
        assertThat(routes.get(1).getUrl()).isEqualTo("/");
        assertThat(routes.get(1).getControllerObject()).isEqualTo(instance);
        assertThat(routes.get(1).getControllerMethod().getName()).isEqualTo("method2");
    }
}
