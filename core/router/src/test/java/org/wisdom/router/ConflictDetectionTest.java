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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wisdom.api.http.*;
import org.wisdom.api.router.RouteBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test the conflict detection.
 */
public class ConflictDetectionTest {

    RequestRouter router = new RequestRouter();

    @Before
    public void setUp() {
        Context context = mock(Context.class);
        Context.CONTEXT.set(context);
    }

    @After
    public void tearDown() {
        Context.CONTEXT.remove();
    }

    @Test
    public void sameRouteWithoutAcceptsInTwoControllers() throws Exception {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo")
        ));
        router.bindController(controller1);

        FakeController controller2 = new FakeController();
        controller2.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller2, "foo")
        ));
        router.bindController(controller2);

        Request request = mock(Request.class);
        when(request.contentMimeType()).thenReturn("application/json");
        // Retrieve route
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).getControllerObject()).isEqualTo(controller1);
    }

    @Test
    public void sameRouteInTwoControllersUsingDifferentAcceptTypes() throws Exception {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo").accepting("text/plain")
        ));
        router.bindController(controller1);

        FakeController controller2 = new FakeController();
        controller2.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller2, "foo").accepting("application/json")
        ));
        router.bindController(controller2);

        // Retrieve route
        Request request = mock(Request.class);
        when(request.contentMimeType()).thenReturn("application/json");
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).getControllerObject()).isEqualTo(controller2);

        request = mock(Request.class);
        when(request.contentMimeType()).thenReturn("text/plain");
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).getControllerObject()).isEqualTo(controller1);

        request = mock(Request.class);
        when(request.contentMimeType()).thenReturn("application/foo");
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).isUnbound()).isTrue();
    }

    @Test
    public void sameRouteInTwoControllersWithSameAcceptTypes() throws Exception {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo").accepting("text/plain",
                        "application/json")
        ));
        router.bindController(controller1);

        FakeController controller2 = new FakeController();
        controller2.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller2, "foo").accepting
                        ("application/json", "text/plain")
        ));
        router.bindController(controller2);

        // Retrieve route
        Request request = mock(Request.class);
        when(request.contentMimeType()).thenReturn("text/plain");
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).getControllerObject()).isEqualTo(controller1);

        when(request.contentMimeType()).thenReturn("application/json");
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).getControllerObject()).isEqualTo(controller1);

        request = mock(Request.class);
        when(request.contentMimeType()).thenReturn("application/foo");
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).isUnbound()).isTrue();
    }

    @Test
    public void sameRouteInTwoControllersWithConflictingAcceptTypes() throws Exception {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo").accepting("text/plain",
                        "application/json")
        ));
        router.bindController(controller1);

        FakeController controller2 = new FakeController();
        controller2.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller2, "foo").accepting
                        ("application/foo", "text/plain")
        ));
        router.bindController(controller2);

        // Retrieve route
        Request request = mock(Request.class);
        when(request.contentMimeType()).thenReturn("text/plain");
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).getControllerObject()).isEqualTo(controller1);

        when(request.contentMimeType()).thenReturn("application/json");
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).getControllerObject()).isEqualTo(controller1);

        request = mock(Request.class);
        when(request.contentMimeType()).thenReturn("application/foo");
        // True because the second one is not registered.
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).isUnbound()).isTrue();
    }

    @Test
    public void sameRouteInTwoControllersWithConflictingAcceptTypesAsTheFirstOneAcceptAll() throws Exception {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo") // Accept all
        ));
        router.bindController(controller1);

        FakeController controller2 = new FakeController();
        controller2.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller2, "foo").accepting
                        ("application/foo", "text/plain")
        ));
        router.bindController(controller2);

        // Retrieve route
        Request request = mock(Request.class);
        when(request.contentMimeType()).thenReturn("text/plain");
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).getControllerObject()).isEqualTo(controller1);

        when(request.contentMimeType()).thenReturn("application/json");
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).getControllerObject()).isEqualTo(controller1);
    }

    @Test
    public void sameRouteInTwoControllersWithConflictingAcceptTypesAsTheSecondOneAcceptAll() throws Exception {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo")
                        .accepting("application/json", "text/plain")
        ));
        router.bindController(controller1);

        FakeController controller2 = new FakeController();
        controller2.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller2, "foo") // Accept all
        ));
        router.bindController(controller2);

        // Retrieve route
        Request request = mock(Request.class);
        when(request.contentMimeType()).thenReturn("text/plain");
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).getControllerObject()).isEqualTo(controller1);

        when(request.contentMimeType()).thenReturn("application/json");
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).getControllerObject()).isEqualTo(controller1);

        request = mock(Request.class);
        when(request.contentMimeType()).thenReturn("application/foo");
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).isUnbound()).isTrue();
    }

    @Test
    public void sameRouteInSameControllers() throws Exception {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo"),
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo")
        ));
        router.bindController(controller1);

        // Retrieve route
        Request request = mock(Request.class);
        when(request.contentMimeType()).thenReturn("application/foo");
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).isUnbound()).isTrue();
    }

    @Test
    public void sameRouteInSameControllersUsingAnnotations() throws Exception {
        FakeController controller1 = new FakeController() {
            @org.wisdom.api.annotations.Route(method = HttpMethod.GET, uri = "/foo")
            public Result index() {
                return ok();
            }

            @org.wisdom.api.annotations.Route(method = HttpMethod.GET, uri = "/foo")
            public Result index2() {
                return ok();
            }
        };
        router.bindController(controller1);
        Request request = mock(Request.class);
        when(request.contentMimeType()).thenReturn("application/json");
        // Retrieve route
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).isUnbound()).isTrue();
    }

    @Test
    public void sameRouteInSameControllersUsingAnnotationsAndRoutesMethod() throws Exception {
        FakeController controller1 = new FakeController() {
            @org.wisdom.api.annotations.Route(method = HttpMethod.GET, uri = "/foo")
            public Result index() {
                return ok();
            }
        };
        controller1.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo")));
        router.bindController(controller1);
        assertThat(router.getRoutes()).isEmpty();
    }

    @Test
    public void noConflictOnDifferentMethod() throws Exception {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo"),
                new RouteBuilder().route(HttpMethod.DELETE).on("/foo").to(controller1, "foo")
        ));
        router.bindController(controller1);
        assertThat(router.getRoutes()).hasSize(2);
    }

    @Test
    public void ensureThatAllRoutesAreRemovedOnConflicts() throws Exception {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/bar").to(controller1, "foo"),
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo"),
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo")
        ));

        router.bindController(controller1);
        Request request = mock(Request.class);
        when(request.contentMimeType()).thenReturn("application/json");
        assertThat(router.getRouteFor(HttpMethod.GET, "/bar", request).isUnbound()).isTrue();
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).isUnbound()).isTrue();
    }

    @Test
    public void checkDetectionWhenNoeitherAcceptNorProduceAreSet() {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo"),
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "bar")
        ));

        router.bindController(controller1);
        assertThat(router.getRoutes()).isEmpty();
    }

    @Test
    public void checkDetectionWhenProducedAndAcceptedTypesAreTheSame() {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo").accepting("text/plain")
                        .produces("text/plain"),
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "bar").accepting("text/plain")
                        .produces("text/plain")
        ));

        router.bindController(controller1);
        assertThat(router.getRoutes()).isEmpty();
    }

    @Test
    public void checkDetectionWhenAcceptedTypesAreTheSameAndNoProducedTypes() {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo").accepting("text/plain")
                ,
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "bar").accepting("text/plain")
        ));

        router.bindController(controller1);
        assertThat(router.getRoutes()).isEmpty();
    }

    @Test
    public void checkDetectionWhenProducedTypesAreTheSameAndNoAccepted() {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo")
                        .produces("text/plain"),
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "bar")
                        .produces("text/plain")
        ));

        router.bindController(controller1);
        assertThat(router.getRoutes()).isEmpty();
    }

    @Test
    public void checkNoDetectionWhenProducedTypesAreNotTheSameAndNoAccepted() {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo")
                        .produces("text/plain"),
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "bar")
                        .produces("text/foo")
        ));

        router.bindController(controller1);
        assertThat(router.getRoutes()).hasSize(2);
    }

    @Test
    public void checkDetectionWhenOneActionDoesNotSayWhatItProduces() {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo")
                        .produces("text/plain"),
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "bar")
        ));

        router.bindController(controller1);
        assertThat(router.getRoutes()).isEmpty();
    }

    @Test
    public void checkDetectionWhenOneActionDoesNotSayWhatItProducesReverse() {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo"),
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "bar").produces("text/plain")
        ));

        router.bindController(controller1);
        assertThat(router.getRoutes()).isEmpty();
    }

    @Test
    public void checkDetectionWithProductionConflict() {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo")
                        .produces("text/plain", "application/json"),
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "bar").produces("application/json")
        ));

        router.bindController(controller1);
        assertThat(router.getRoutes()).isEmpty();
    }

    @Test
    public void checkDetectionWhenOneActionDoesNotSayWhatItProduces2() {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo")
                        .accepting("text/plain")
                        .produces("text/plain"),
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "bar").accepting("application/json")
        ));

        router.bindController(controller1);
        assertThat(router.getRoutes()).hasSize(2);
    }

    @Test
    public void checkDetectionWhenOneActionDoesNotSayWhatItProduces3() {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo")
                        .accepting("text/plain"),
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "bar")
                        .accepting("application/json").produces("application/json")
        ));

        router.bindController(controller1);
        assertThat(router.getRoutes()).hasSize(2);
    }

    @Test
    public void checkDetectionWhenOneActionDoesNotSayWhatItConsumes() throws Exception {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo")
                        .accepting("text/plain")
                        .produces("text/plain"),
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "bar")
        ));

        router.bindController(controller1);
        assertThat(router.getRoutes()).hasSize(0);
    }

    @Test
    public void checkDetectionWhenOneActionDoesNotSayWhatItConsumesReverse() {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo"),
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "bar").accepting("text/plain")
                        .produces("text/plain")
        ));

        router.bindController(controller1);
        assertThat(router.getRoutes()).isEmpty();
    }

    @Test
    public void checkNoDetection() {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo").produces("text/plain"),
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "bar").produces("text/foo")
        ));
        router.bindController(controller1);
        assertThat(router.getRoutes()).hasSize(2);
    }

    @Test
    public void checkNoDetectionBecauseOfProduced() {
        FakeController controller1 = new FakeController();
        controller1.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "foo").produces("text/plain")
                        .accepting("application/json"),
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller1, "bar").produces("text/foo")
        ));
        router.bindController(controller1);
        assertThat(router.getRoutes()).hasSize(2);
    }



}
