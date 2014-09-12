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
package org.wisdom.api.interception;

import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;
import org.wisdom.api.http.*;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.RouteBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Check the request context.
 */
public class RequestContextTest {

    @Before
    public void setUp() {
        Map<String, Object> data = new HashMap<>();
        // Create a fake context.
        Context context = mock(Context.class);
        Request request = mock(Request.class);
        when(context.request()).thenReturn(request);
        when(request.data()).thenReturn(data);
        Context.CONTEXT.set(context);
    }

    @After
    public void tearDown() {
        Context.CONTEXT.remove();
    }

    private class MyController implements Controller {

        public Result ok() {
            return Results.ok("Hello");
        }

        @Override
        public List<Route> routes() {
            return null;
        }
    }

    @Test
    public void testEmptyChain() throws Exception {
        MyController controller = new MyController();
        Route route = new RouteBuilder().route(HttpMethod.GET).on("/").to(controller, "ok");

        RequestContext context = new RequestContext(route, Collections.<Filter>emptyList(),
                Collections.<Interceptor<?>, Object>emptyMap(), new Object[0], null);

        assertThat(context.data()).isEmpty();
        assertThat(context.route()).isEqualTo(route);
        assertThat(context.proceed().<String>getRenderable().content()).isEqualTo("Hello");
    }

    @Test
    public void testUnboundRoute() throws Exception {
        Route route = new Route(HttpMethod.POST, "/", null, null);

        RequestContext context = new RequestContext(route, Collections.<Filter>emptyList(),
                Collections.<Interceptor<?>, Object>emptyMap(), new Object[0], null);

        assertThat(context.data()).isEmpty();
        assertThat(context.route()).isEqualTo(route);
        assertThat(context.proceed().getStatusCode()).isEqualTo(404);
    }

    @Test
    public void testFilterChain() throws Exception {
        MyController controller = new MyController();
        MyFilter myFilter = new MyFilter();
        Route route = new RouteBuilder().route(HttpMethod.GET).on("/").to(controller, "ok");

        RequestContext context = new RequestContext(route, ImmutableList.<Filter>of(myFilter),
                Collections.<Interceptor<?>, Object>emptyMap(), new Object[0], null);

        assertThat(context.data()).isEmpty();
        assertThat(context.route()).isEqualTo(route);
        assertThat(context.proceed().<String>getRenderable().content()).isEqualTo("Filtered : Hello");
    }

    @Test
    public void testWithTwoFilters() throws Exception {
        MyController controller = new MyController();
        MyFilter myFilter = new MyFilter();
        MySecondFilter myFilter2 = new MySecondFilter();
        Route route = new RouteBuilder().route(HttpMethod.GET).on("/").to(controller, "ok");

        // The order matters here as the ordering is checked by the route implementation.
        RequestContext context = new RequestContext(route, ImmutableList.of(myFilter2, myFilter),
                Collections.<Interceptor<?>, Object>emptyMap(), new Object[0], null);

        assertThat(context.data()).isEmpty();
        assertThat(context.route()).isEqualTo(route);
        assertThat(context.proceed().<String>getRenderable().content()).isEqualTo("@ : Filtered : Hello");
    }

    @Test
    public void testWithTwoFiltersSharingData() throws Exception {
        Filter myFilter1 = new Filter() {
            @Override
            public Result call(Route route, RequestContext context) throws Exception {
                context.data().put("echo", 3);
                return context.proceed();
            }

            @Override
            public Pattern uri() {
                return Pattern.compile("/");
            }

            @Override
            public int priority() {
                return 10;
            }
        };
        Filter myFilter2 = new Filter() {
            @Override
            public Result call(Route route, RequestContext context) throws Exception {
                int echo = (int) context.data().get("echo");
                context.data().put("echo", echo + 1);
                return context.proceed();
            }

            @Override
            public Pattern uri() {
                return Pattern.compile("/");
            }

            @Override
            public int priority() {
                return 11;
            }
        };
        Controller controller = new DefaultController() {

            public Result test() {
                return Results.ok(request().data().get("echo")).as(MimeTypes.TEXT);
            }
        };

        Route route = new RouteBuilder().route(HttpMethod.GET).on("/").to(controller, "test");

        // The order matters here as the ordering is checked by the route implementation.
        RequestContext context = new RequestContext(route, ImmutableList.of(myFilter1, myFilter2),
                Collections.<Interceptor<?>, Object>emptyMap(), new Object[0], null);

        assertThat(context.data()).isEmpty();
        assertThat(context.route()).isEqualTo(route);
        assertThat(context.proceed().<String>getRenderable().content()).isEqualTo(4);
    }

    private class MyFilter implements Filter {
        @Override
        public Result call(Route route, RequestContext context) throws Exception {
            Result result = context.proceed();
            return Results.ok("Filtered : " + result.getRenderable().content());
        }

        @Override
        public Pattern uri() {
            return Pattern.compile("/");
        }

        @Override
        public int priority() {
            return 10;
        }
    }

    private class MySecondFilter implements Filter {
        @Override
        public Result call(Route route, RequestContext context) throws Exception {
            Result result = context.proceed();
            return Results.ok("@ : " + result.getRenderable().content());
        }

        @Override
        public Pattern uri() {
            return Pattern.compile("/");
        }

        @Override
        public int priority() {
            return 11;
        }
    }
}
