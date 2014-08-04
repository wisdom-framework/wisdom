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
package org.wisdom.router;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.RouteBuilder;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Test the reverse route generation.
 */
public class ReverseRouterTest {

    RequestRouter router = new RequestRouter();

    @Test
    public void simpleRoute() throws Exception {
        FakeController controller = new FakeController();
        controller.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller, "foo")
        ));
        router.bindController(controller);

        assertThat(router.getReverseRouteFor(controller, "foo")).isEqualTo("/foo");
        assertThat(router.getReverseRouteFor(controller, "foo", "q", "v")).isEqualTo("/foo?q=v");
    }

    @Test
    public void missingRoute() throws Exception {
        FakeController controller = new FakeController();
        controller.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller, "foo")
        ));
        router.bindController(controller);

        assertThat(router.getReverseRouteFor(controller, "bar")).isNull();
    }

    @Test
    public void routeWithPathParameter() throws Exception {
        FakeController controller = new FakeController();
        controller.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo/{id}").to(controller, "foo")
        ));
        router.bindController(controller);

        // Weird result but it's what we want, the placeholder is not replaced.
        assertThat(router.getReverseRouteFor(controller, "foo")).isEqualTo("/foo/{id}");
        assertThat(router.getReverseRouteFor(controller, "foo", "id", "w")).isEqualTo("/foo/w");
        // Add query parameter.
        assertThat(router.getReverseRouteFor(controller, "foo", "id", "w", "q", "v")).isEqualTo("/foo/w?q=v");
    }

    @Test
    public void routeWithTwoPathParameters() throws Exception {
        FakeController controller = new FakeController();
        controller.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo/{id}/{email}").to(controller, "foo")
        ));
        router.bindController(controller);

        assertThat(router.getReverseRouteFor(controller, "foo", "id", "w", "email",
                "foo@aol.com")).isEqualTo("/foo/w/foo@aol.com");
    }

    @Test
    public void unbindTest() {
        FakeController controller = new FakeController();
        controller.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo/{path+}").to(controller, "foo")
        ));
        router.bindController(controller);

        assertThat(router.getReverseRouteFor(controller, "foo", "path", "p/a/t/h")).isEqualTo("/foo/p/a/t/h");

        router.unbindController(controller);
        assertThat(router.getReverseRouteFor(controller, "foo", "path", "p/a/t/h")).isNull();

    }

    @Test
    public void testURLEncoding() throws Exception {
        router.bindController(new UrlCodingController());
        checkEncoding("123", "456", "789", "123", "456", "789");
        checkEncoding("+", "+", "+", "+",   "+",   "%2B");
        checkEncoding(" ", " ", " ", "%20", "%20", "+");
        checkEncoding("&", "&", "&", "&",   "&",   "%26");
        checkEncoding("=", "=", "=", "=",   "=",   "%3D");
        checkEncoding("/", "/", "/", "%2F", "%2F",   "%2F");
        checkEncoding("~", "~", "~", "~",   "~",   "%7E");

    }

    public void checkEncoding(String decoded1, String decoded2, String decoded3,
                              String encoded1, String encoded2, String encoded3) throws Exception {
        final String expected = "/urlcoding/" + encoded1 + "/" + encoded2 + "?q=" + encoded3;
        final String computed = router.getReverseRouteFor(UrlCodingController.class, "coding", "p1", decoded1, "p2",
                decoded2, "q", decoded3);
        assertThat(computed).isEqualTo(expected);

    }

    private class UrlCodingController extends DefaultController {

        @org.wisdom.api.annotations.Route(method = HttpMethod.GET, uri = "/urlcoding/{p1}/{p2}")
        public Result coding(@Parameter("p1") String p1, @Parameter("q") String q) {
            return ok(p1 + "," + context().parameterFromPathEncoded("p2") + "," + q);
        }
    }


}
