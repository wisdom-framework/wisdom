package org.wisdom.api.router;

import org.junit.Test;
import org.wisdom.api.http.HttpMethod;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unbound Route behavior assertion.
 */
public class UnboundRouteTest {

    @Test
    public void testCreation() throws Throwable {
        Route route = new Route(HttpMethod.GET, "/", null, null);
        Route route2 = new Route(HttpMethod.GET, "/", null, null);
        assertThat(route.isUnbound()).isTrue();
        assertThat(route.getArguments().isEmpty());

        assertThat(route.hashCode()).isEqualTo(route2.hashCode());
        assertThat(route).isEqualTo(route2);
        assertThat(route).isNotEqualTo("aaa");

        assertThat(route.invoke().getStatusCode()).isEqualTo(404);
    }
}
