package org.wisdom.api.interception;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.wisdom.api.Controller;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.RouteBuilder;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check the request context.
 */
public class RequestContextTest {

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
                Collections.<Interceptor<?>, Object>emptyMap(), new Object[0]);

        assertThat(context.data()).isEmpty();
        assertThat(context.route()).isEqualTo(route);
        assertThat(context.proceed().<String>getRenderable().content()).isEqualTo("Hello");
    }

    @Test
    public void testUnboundRoute() throws Exception {
        Route route = new Route(HttpMethod.POST, "/", null, null);

        RequestContext context = new RequestContext(route, Collections.<Filter>emptyList(),
                Collections.<Interceptor<?>, Object>emptyMap(), new Object[0]);

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
                Collections.<Interceptor<?>, Object>emptyMap(), new Object[0]);

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
        RequestContext context = new RequestContext(route, ImmutableList.<Filter>of(myFilter2, myFilter),
                Collections.<Interceptor<?>, Object>emptyMap(), new Object[0]);

        assertThat(context.data()).isEmpty();
        assertThat(context.route()).isEqualTo(route);
        assertThat(context.proceed().<String>getRenderable().content()).isEqualTo("@ : Filtered : Hello");
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
