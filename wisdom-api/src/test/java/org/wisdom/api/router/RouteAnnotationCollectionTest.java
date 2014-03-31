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
        if ("method1".equals(routes.get(0).getControllerMethod().getName())) {
            assertThat(routes.get(0).getHttpMethod()).isEqualTo(HttpMethod.GET);
            assertThat(routes.get(0).getUrl()).isEqualTo("/");
            assertThat(routes.get(0).getControllerObject()).isEqualTo(instance);
            assertThat(routes.get(0).getControllerMethod().getName()).isEqualTo("method1");
            assertThat(routes.get(1).getHttpMethod()).isEqualTo(HttpMethod.POST);
            assertThat(routes.get(1).getUrl()).isEqualTo("/");
            assertThat(routes.get(1).getControllerObject()).isEqualTo(instance);
            assertThat(routes.get(1).getControllerMethod().getName()).isEqualTo("method2");
        } else {
            assertThat(routes.get(1).getHttpMethod()).isEqualTo(HttpMethod.GET);
            assertThat(routes.get(1).getUrl()).isEqualTo("/");
            assertThat(routes.get(1).getControllerObject()).isEqualTo(instance);
            assertThat(routes.get(1).getControllerMethod().getName()).isEqualTo("method1");
            assertThat(routes.get(0).getHttpMethod()).isEqualTo(HttpMethod.POST);
            assertThat(routes.get(0).getUrl()).isEqualTo("/");
            assertThat(routes.get(0).getControllerObject()).isEqualTo(instance);
            assertThat(routes.get(0).getControllerMethod().getName()).isEqualTo("method2");
        }
    }


    @Test
    public void testMatches() {
        Controller instance = new Controller() {

            @org.wisdom.api.annotations.Route(method = HttpMethod.GET, uri = "/*")
            public Result method1() {
                return null;
            }

            @Override
            public List<Route> routes() {
                return null;
            }
        };
        List<Route> routes = RouteUtils.collectRouteFromControllerAnnotations(instance);
        Route route = routes.get(0);
        assertThat(route.matches(HttpMethod.GET, "/")).isTrue();
        assertThat(route.matches(HttpMethod.GET, "/foo")).isTrue();
        assertThat(route.matches(HttpMethod.POST, "/foo")).isFalse();
    }
}
