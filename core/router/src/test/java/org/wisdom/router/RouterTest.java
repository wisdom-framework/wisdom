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

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;
import org.wisdom.api.concurrent.ExecutionContextService;
import org.wisdom.api.concurrent.ManagedExecutorService;
import org.wisdom.api.http.*;
import org.wisdom.api.interception.Filter;
import org.wisdom.api.interception.Interceptor;
import org.wisdom.api.interception.RequestContext;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.RouteBuilder;
import org.wisdom.test.parents.FakeConfiguration;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test the router implementation
 */
public class RouterTest {

    RequestRouter router = new RequestRouter();
    Request request;

    @Before
    public void setUp() {
        request = mock(Request.class);
        when(request.contentMimeType()).thenReturn("text/plain");
        Context context = mock(Context.class);
        when(context.request()).thenReturn(request);

        Context.CONTEXT.set(context);
    }

    @After
    public void tearDown() {
        Context.CONTEXT.remove();
    }

    @Test
    public void simpleRoute() throws Exception {
        FakeController controller = new FakeController();
        controller.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller, "foo")
        ));
        router.bindController(controller);

        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request)).isNotNull();
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).getControllerObject()).isEqualTo(controller);
    }

    @Test
    public void missingRoute() throws Exception {
        FakeController controller = new FakeController();
        controller.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller, "foo")
        ));
        router.bindController(controller);

        assertThat(router.getRouteFor(HttpMethod.GET, "/bar", request).isUnbound()).isTrue();
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).getControllerObject()).isEqualTo(controller);
    }

    @Test
    public void routeMissingBecauseOfBadMethod() throws Exception {
        FakeController controller = new FakeController();
        controller.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller, "foo")
        ));
        router.bindController(controller);

        assertThat(router.getRouteFor(HttpMethod.PUT, "/foo", request).isUnbound()).isTrue();
        assertThat(router.getRouteFor(HttpMethod.DELETE, "/foo", request).isUnbound()).isTrue();
        assertThat(router.getRouteFor(HttpMethod.POST, "/foo", request).isUnbound()).isTrue();
    }

    @Test
    public void routeMissingBecauseOfBrokenMethod() throws Exception {
        Controller controller = new DefaultController() {
            @org.wisdom.api.annotations.Route(method = HttpMethod.GET, uri = "/foo")
            public String hello() {
                return "hello";
            }

            @org.wisdom.api.annotations.Route(method = HttpMethod.GET, uri = "/bar")
            public Result hello2() {
                return ok("hello");
            }
        };
        // Must not trow an exception.
        router.bindController(controller);

        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).isUnbound()).isTrue();
        assertThat(router.getRouteFor(HttpMethod.GET, "/bar", request).isUnbound()).isTrue();

        controller = new DefaultController() {
            @org.wisdom.api.annotations.Route(method = HttpMethod.GET, uri = "/foo")
            public Result hello() {
                return ok("hello");
            }

            @org.wisdom.api.annotations.Route(method = HttpMethod.GET, uri = "/bar")
            public Result hello2() {
                return ok("hello");
            }
        };
        // Must not trow an exception.
        router.bindController(controller);

        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).isUnbound()).isFalse();
        assertThat(router.getRouteFor(HttpMethod.GET, "/bar", request).isUnbound()).isFalse();
    }

    @Test
    public void routeWithPathParameter() throws Exception {
        FakeController controller = new FakeController();
        controller.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo/{id}").to(controller, "foo")
        ));
        router.bindController(controller);

        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).isUnbound()).isTrue();
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo/", request).isUnbound()).isTrue();
        Route route = router.getRouteFor(HttpMethod.GET, "/foo/test", request);
        assertThat(route.isUnbound()).isFalse();
        assertThat(route.getPathParametersEncoded("/foo/test").get("id")).isEqualToIgnoringCase("test");
    }

    @Test
    public void routeWithTwoPathParameters() throws Exception {
        FakeController controller = new FakeController();
        controller.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo/{id}/{email}").to(controller, "foo")
        ));
        router.bindController(controller);

        Route route = router.getRouteFor(HttpMethod.GET, "/foo/1234/foo@aol.com", request);
        assertThat(route).isNotNull();
        assertThat(route.getPathParametersEncoded("/foo/1234/foo@aol.com").get("id")).isEqualToIgnoringCase("1234");
        assertThat(route.getPathParametersEncoded("/foo/1234/foo@aol.com").get("email")).isEqualToIgnoringCase
                ("foo@aol.com");
    }

    /**
     * Test made to reproduce #248.
     */
    @Test
    public void routeWithRegex() {
        FakeController controller = new FakeController();
        controller.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/{type<[0-9]+>}").to(controller, "foo")
        ));
        router.bindController(controller);

        Route route = router.getRouteFor(HttpMethod.GET, "/99", request);
        assertThat(route).isNotNull();
        assertThat(route.getPathParametersEncoded("/99").get("type")).isEqualToIgnoringCase("99");

        route = router.getRouteFor(HttpMethod.GET, "/xx", request);
        assertThat(route.isUnbound()).isTrue();
    }

    @Test
    public void subRoute() throws Exception {
        FakeController controller = new FakeController();
        controller.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo/*").to(controller, "foo")
        ));
        router.bindController(controller);

        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).isUnbound()).isTrue();
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo/bar", request)).isNotNull();
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo/bar", request).getControllerObject()).isEqualTo(controller);
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo/bar/baz", request)).isNotNull();
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo/bar/baz", request).getControllerObject()).isEqualTo(controller);
    }

    @Test
    public void subRouteAsParameter() throws Exception {
        FakeController controller = new FakeController();
        controller.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo/{path+}").to(controller, "foo")
        ));
        router.bindController(controller);

        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).isUnbound()).isTrue();
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo/", request).isUnbound()).isTrue();
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo/bar", request).isUnbound()).isFalse();
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo/bar", request).getControllerObject()).isEqualTo(controller);

        Route route = router.getRouteFor(HttpMethod.GET, "/foo/bar/baz", request);
        assertThat(route).isNotNull();
        assertThat(route.getControllerObject()).isEqualTo(controller);

        assertThat(route.getPathParametersEncoded("/foo/bar/baz").get("path")).isEqualToIgnoringCase("bar/baz");

    }

    @Test
    public void unbindTest() {
        FakeController controller = new FakeController();
        controller.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo/{path+}").to(controller, "foo")
        ));
        router.bindController(controller);

        assertThat(router.getRouteFor(HttpMethod.GET, "/foo/bar", request).isUnbound()).isFalse();

        router.unbindController(controller);
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo/bar", request).isUnbound()).isTrue();

    }

    @Test
    public void testBindAndUnbindFilters() {
        Filter filter = new Filter() {
            @Override
            public Result call(Route route, RequestContext context) throws Exception {
                return null;
            }

            @Override
            public Pattern uri() {
                return null;
            }

            @Override
            public int priority() {
                return 0;
            }
        };
        router.bindFilter(filter);
        assertThat(router.getFilters()).hasSize(1).contains(filter);
        router.unbindFilter(filter);
        assertThat(router.getFilters()).hasSize(0);
    }

    @Test
    public void testThatFiltersCannotBeAddedTwice() {
        Filter filter = new Filter() {
            @Override
            public Result call(Route route, RequestContext context) throws Exception {
                return null;
            }

            @Override
            public Pattern uri() {
                return null;
            }

            @Override
            public int priority() {
                return 0;
            }
        };
        router.bindFilter(filter);
        assertThat(router.getFilters()).hasSize(1).contains(filter);
        router.bindFilter(filter);
        assertThat(router.getFilters()).hasSize(1).contains(filter);
        router.unbindFilter(filter);
        assertThat(router.getFilters()).hasSize(0);
    }

    @Test
    public void testBindAndUnbindFiltersWithProxy() {
        final Filter filter = new Filter() {
            @Override
            public Result call(Route route, RequestContext context) throws Exception {
                return null;
            }

            @Override
            public Pattern uri() {
                return null;
            }

            @Override
            public int priority() {
                return 0;
            }
        };
        Filter proxy = (Filter) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{Filter.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        return method.invoke(filter, args);
                    }
                });
        router.bindFilter(proxy);
        assertThat(router.getFilters()).hasSize(1);
        assertThat(router.getDirectReferenceOnFilters().contains(proxy)).isTrue();
        assertThat(router.getDirectReferenceOnFilters().contains(filter)).isTrue();
        assertThat(router.getFilters().contains(filter)).isTrue();
        router.unbindFilter(proxy);
        assertThat(router.getFilters()).hasSize(0);
    }

    @Test
    public void testThatProxiesCannotBeAddedTwice() {
        final Filter filter = new Filter() {
            @Override
            public Result call(Route route, RequestContext context) throws Exception {
                return null;
            }

            @Override
            public Pattern uri() {
                return null;
            }

            @Override
            public int priority() {
                return 0;
            }
        };
        Filter proxy = (Filter) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{Filter.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        return method.invoke(filter, args);
                    }
                });
        Filter proxy2 = (Filter) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{Filter.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        return method.invoke(filter, args);
                    }
                });
        router.bindFilter(proxy);
        assertThat(router.getFilters()).hasSize(1);
        assertThat(router.getDirectReferenceOnFilters().contains(proxy)).isTrue();
        assertThat(router.getDirectReferenceOnFilters().contains(filter)).isTrue();
        assertThat(router.getFilters().contains(filter)).isTrue();
        router.bindFilter(proxy2);
        assertThat(router.getFilters()).hasSize(1);
        assertThat(router.getDirectReferenceOnFilters().contains(proxy)).isTrue();
        assertThat(router.getDirectReferenceOnFilters().contains(filter)).isTrue();
        assertThat(router.getFilters().contains(filter)).isTrue();
        router.unbindFilter(proxy);
        assertThat(router.getFilters()).hasSize(0);
    }

    @Test
    public void testRouteSelectionAccordingToMimeType() throws Exception {
        FakeController controller = new FakeController();
        controller.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller, "bar").accepting("application/json"),
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller, "foo").accepting("text/plain")
        ));
        router.bindController(controller);

        // The request has a text/plain content:
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request)).isNotNull();
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).getControllerMethod().getName()).isEqualTo("foo");

        when(request.contentMimeType()).thenReturn("application/json");
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request)).isNotNull();
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).getControllerMethod().getName()).isEqualTo
                ("bar");

        when(request.contentMimeType()).thenReturn("application/bin");
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request)).isNotNull();
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).isUnbound()).isEqualTo(true);
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).getUnboundStatus())
                .isEqualTo(Status.UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    public void testRouteSelectionAccordingToMimeTypeWithWildcards() throws Exception {
        FakeController controller = new FakeController();
        controller.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller, "bar").accepting("text/*"),
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller, "foo").accepting("text/plain")
        ));
        router.bindController(controller);

        // The request has a text/plain content:
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request)).isNotNull();
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).getControllerMethod().getName()).isEqualTo("foo");

        when(request.contentMimeType()).thenReturn("text/json");
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request)).isNotNull();
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).getControllerMethod().getName()).isEqualTo
                ("bar");

        when(request.contentMimeType()).thenReturn("application/bin");
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request)).isNotNull();
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).isUnbound()).isEqualTo(true);
        assertThat(router.getRouteFor(HttpMethod.GET, "/foo", request).getUnboundStatus())
                .isEqualTo(Status.UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    public void testThatTheVaryHeaderIsSet() throws Exception {
        FakeController controller = new FakeController();
        controller.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller, "bar").accepting("application/json"),
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller, "foo").accepting("text/plain")
        ));
        router.bindController(controller);

        // The request has a text/plain content:
        Route actual = router.getRouteFor(HttpMethod.GET, "/foo", request);
        Result result = actual.invoke();
        assertThat(result.getHeaders().get(HeaderNames.VARY)).isEqualTo(HeaderNames.CONTENT_TYPE);

        when(request.contentMimeType()).thenReturn("application/bin");
        actual = router.getRouteFor(HttpMethod.GET, "/foo", request);
        result = actual.invoke();
        assertThat(result.getStatusCode()).isEqualTo(Status.UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    public void testThatVaryIsNotSetWhenNotNeeded() throws Exception {
        FakeController controller = new FakeController();
        controller.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller, "bar")
        ));
        router.bindController(controller);

        // The request has a text/plain content:
        Route actual = router.getRouteFor(HttpMethod.GET, "/foo", request);
        Result result = actual.invoke();
        assertThat(result.getHeaders().get(HeaderNames.VARY)).isNull();
    }

    @Test
    public void testWeGetNotAcceptableWhenTheAcceptedTypeAreNotAccepted() throws Exception {
        FakeController controller = new FakeController();
        controller.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller, "bar").accepting("application/json")
        ));
        router.bindController(controller);

        Route actual = router.getRouteFor(HttpMethod.GET, "/foo", request);
        Result result = actual.invoke();
        assertThat(result.getStatusCode()).isEqualTo(Status.UNSUPPORTED_MEDIA_TYPE);

        when(request.contentMimeType()).thenReturn("application/json");
        actual = router.getRouteFor(HttpMethod.GET, "/foo", request);
        result = actual.invoke();
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(result.getHeaders().get(HeaderNames.VARY)).isEqualTo(HeaderNames.CONTENT_TYPE);
    }

    @Test
    public void testThatProducedMimeTypeIsHandled() throws Exception {
        FakeController controller = new FakeController();
        controller.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller, "foo").produces("text/foo"),
                new RouteBuilder().route(HttpMethod.GET).on("/bar").to(controller, "bar").produces("text/plain")
        ));
        router.bindController(controller);

        // The request has a text/plain content:
        Route actual = router.getRouteFor(HttpMethod.GET, "/foo", request);
        Result result = actual.invoke();
        assertThat(result.getHeaders().get(HeaderNames.CONTENT_TYPE)).isEqualToIgnoringCase("text/foo");
    }

    @Test
    public void testThatExactMatchAreUsed() throws Exception {
        FakeController controller = new FakeController();
        controller.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo/stuff").to(controller, "foo"),
                new RouteBuilder().route(HttpMethod.GET).on("/foo/{id}").to(controller, "bar")
        ));
        router.bindController(controller);

        Route actual = router.getRouteFor(HttpMethod.GET, "/foo/stuff", request);
        Result result = actual.invoke();
        assertThat(result.getStatusCode()).isEqualTo(Status.CREATED);

        actual = router.getRouteFor(HttpMethod.GET, "/foo/test", request);
        result = actual.invoke();
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);


        // Invert the declaration of the routes
        router.unbindController(controller);
        controller = new FakeController();
        controller.setRoutes(ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/foo/{id}").to(controller, "bar"),
                new RouteBuilder().route(HttpMethod.GET).on("/foo/stuff").to(controller, "foo")
        ));
        router.bindController(controller);

        actual = router.getRouteFor(HttpMethod.GET, "/foo/stuff", request);
        result = actual.invoke();
        assertThat(result.getStatusCode()).isEqualTo(Status.CREATED);

        actual = router.getRouteFor(HttpMethod.GET, "/foo/test", request);
        result = actual.invoke();
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
    }

    @Test
    public void testConcurrencyForFilters() throws InterruptedException {
        RequestRouter router = new RequestRouter();
        // Now start bunch of thread adding, every 10 addition, we iterate over the set.
        int num = 250;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(num);
        ExecutorService executor = Executors.newFixedThreadPool(num);

        AtomicInteger success = new AtomicInteger();
        Random random = new Random();

        for (int i = 0; i < num; ++i) {
            final int id = i;
            executor.submit(() -> {
                try {
                    Filter mock = createFakeFilter(random.nextInt());
                    startSignal.await();
                    router.bindFilter(mock);
                    if (id % 10 == 0) {
                        for (Filter filter : router.getFilters()) {
                            assertThat(filter).isNotNull();
                        }
                        router.getFilters().stream().forEach(f -> {
                            assertThat(f).isNotNull();
                        });
                    }
                    success.incrementAndGet();
                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    doneSignal.countDown();
                }
            });
        }

        startSignal.countDown();
        doneSignal.await(10, TimeUnit.SECONDS);

        assertThat(success.get()).isEqualTo(num);
        assertThat(router.getFilters().size()).isEqualTo(num);
    }

    private Filter createFakeFilter(int priority) {
        return new Filter() {
            @Override
            public Result call(Route route, RequestContext context) throws Exception {
                return null;
            }

            @Override
            public Pattern uri() {
                return null;
            }

            @Override
            public int priority() {
                return priority;
            }
        };
    }
}
