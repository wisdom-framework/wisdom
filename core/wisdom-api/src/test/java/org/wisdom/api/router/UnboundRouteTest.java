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
package org.wisdom.api.router;

import org.junit.Test;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Status;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unbound Route behavior assertion.
 */
public class UnboundRouteTest {

    @Test
    public void testUnboundRoute() throws Throwable {
        Route route = new Route(HttpMethod.GET, "/", null, null);
        Route route2 = new Route(HttpMethod.GET, "/", null, null);
        assertThat(route.isUnbound()).isTrue();
        assertThat(route.getArguments().isEmpty());

        assertThat(route.hashCode()).isEqualTo(route2.hashCode());
        assertThat(route).isEqualTo(route2);
        assertThat(route).isNotEqualTo("aaa");

        assertThat(route.invoke().getStatusCode()).isEqualTo(Status.NOT_FOUND);
    }

    @Test
    public void testUnboundRouteBecauseOfNotAcceptable() throws Throwable {
        Route route = new Route(HttpMethod.GET, "/", Status.UNSUPPORTED_MEDIA_TYPE);
        assertThat(route.invoke().getStatusCode()).isEqualTo(Status.UNSUPPORTED_MEDIA_TYPE);
        assertThat(route.getUnboundStatus()).isEqualTo(Status.UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    public void testUnboundRouteBecauseOfNotFound() throws Throwable {
        Route route = new Route(HttpMethod.GET, "/", Status.NOT_FOUND);
        assertThat(route.invoke().getStatusCode()).isEqualTo(Status.NOT_FOUND);
    }
}
