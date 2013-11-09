package org.ow2.chameleon.wisdom.router;

import org.fest.util.Collections;
import org.junit.Test;
import org.ow2.chameleon.wisdom.api.http.HttpMethod;
import org.ow2.chameleon.wisdom.api.http.Result;
import org.ow2.chameleon.wisdom.api.router.Route;
import org.ow2.chameleon.wisdom.api.router.RouteBuilder;
import org.ow2.chameleon.wisdom.api.router.RoutingException;

import static org.assertj.core.api.Fail.fail;
import static org.fest.assertions.Assertions.assertThat;

/**
 * Test the conflict detection.
 */
public class ConflictDetectionTest {

    RouterImpl router = new RouterImpl();

    @Test(expected = RoutingException.class)
    public void sameRouteInTwoControllers() throws Exception {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(Collections.list(
           new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo")
        ));
        router.bindController(controller1);

        FakeController controller2 = new FakeController();
        controller2.setRoutes(Collections.list(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller2, "foo")
        ));
        router.bindController(controller2);
    }

    @Test(expected = RoutingException.class)
    public void sameRouteInSameControllers() throws Exception {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(Collections.list(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo"),
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo")
        ));
        router.bindController(controller1);
    }

    @Test(expected = RoutingException.class)
    public void sameRouteInSameControllersUsingAnnotations() throws Exception {
        FakeController controller1 = new FakeController() {
            @org.ow2.chameleon.wisdom.api.annotations.Route(method = HttpMethod.GET, uri = "/foo")
            public Result index() {
                return ok();
            }

            @org.ow2.chameleon.wisdom.api.annotations.Route(method = HttpMethod.GET, uri = "/foo")
            public Result index2() {
                return ok();
            }
        };
        router.bindController(controller1);
    }

    @Test(expected = RoutingException.class)
    public void sameRouteInSameControllersUsingAnnotationsAndRoutesMethod() throws Exception {
        FakeController controller1 = new FakeController() {
            @org.ow2.chameleon.wisdom.api.annotations.Route(method = HttpMethod.GET, uri = "/foo")
            public Result index() {
                return ok();
            }
        };
        controller1.setRoutes(Collections.list(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo")));
        router.bindController(controller1);
    }

    @Test
    public void noConflictOnDifferentMethod() throws Exception {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(Collections.list(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo"),
                new RouteBuilder().route(HttpMethod.DELETE).on("/foo").to(controller1, "foo")
        ));
        router.bindController(controller1);
    }

    @Test
    public void ensureThatAllRoutesAreRemovedOnConflicts() throws Exception {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(Collections.list(
                new RouteBuilder().route(HttpMethod.GET).on("/bar").to(controller1, "foo"),
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo"),
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo")
        ));
        try {
            router.bindController(controller1);
            fail("Routing exception expected");
        } catch (RoutingException e) {
             // Ok.
        }

        assertThat(router.getRouteFor(HttpMethod.GET, "/bar")).isNull();
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo")).isNull();
    }

}
