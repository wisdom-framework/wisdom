/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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
import org.wisdom.api.DefaultController;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Request;
import org.wisdom.api.http.Result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test route.
 */
public class RouteTest {


    @Test
    public void testAcceptedAndRefusedContentType() throws Exception {
        Controller controller = new DefaultController() {
            public Result method1() {
                return null;
            }

            public Result method2() {
                return null;
            }
        };

        Route route1 = new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller, "method1").accepts
                ("text/plain");
        Route route2 = new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller, "method2").accepts
                ("application/json", "text/*");

        assertThat(route1.matches(HttpMethod.GET, "/foo")).isTrue();
        Request request1 = mock(Request.class);
        when(request1.contentMimeType()).thenReturn("text/plain");
        assertThat(route1.isCompliantWithRequestContentType(request1)).isEqualTo(2);
        when(request1.contentMimeType()).thenReturn("text/foo");
        assertThat(route1.isCompliantWithRequestContentType(request1)).isEqualTo(0);

        assertThat(route2.matches(HttpMethod.GET, "/foo")).isTrue();
        Request request2 = mock(Request.class);
        when(request2.contentMimeType()).thenReturn("text/plain");
        assertThat(route1.isCompliantWithRequestContentType(request2)).isEqualTo(2);
        when(request2.contentMimeType()).thenReturn("text/foo");
        assertThat(route2.isCompliantWithRequestContentType(request2)).isEqualTo(1);
        when(request2.contentMimeType()).thenReturn("application/foo");
        assertThat(route2.isCompliantWithRequestContentType(request2)).isEqualTo(0);
        when(request2.contentMimeType()).thenReturn("application/json");
        assertThat(route2.isCompliantWithRequestContentType(request2)).isEqualTo(2);
    }

    @Test
    public void testAcceptWhenNoAcceptOrRequest() throws Exception {
        Controller controller = new DefaultController() {
            public Result method1() {
                return null;
            }
        };

        Route route1 = new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller, "method1").accepts
                ("text/plain");
        assertThat(route1.isCompliantWithRequestContentType(null)).isEqualTo(2);

        route1 = new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller, "method1");
        Request request1 = mock(Request.class);
        when(request1.contentMimeType()).thenReturn("text/plain");
        assertThat(route1.isCompliantWithRequestContentType(request1)).isEqualTo(2);
    }

    @Test
    public void testNotAcceptWhenRequestHasNoContent() throws Exception {
        Controller controller = new DefaultController() {
            public Result method1() {
                return null;
            }
        };

        Route route1 = new RouteBuilder().route(HttpMethod.GET).on("/foo").to(controller, "method1").accepts
                ("text/plain");
        Request request1 = mock(Request.class);
        // Will return null when the content type is retrieved
        assertThat(route1.isCompliantWithRequestContentType(request1)).isEqualTo(2);
    }




}