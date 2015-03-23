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
package org.wisdom.router;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.*;
import org.wisdom.api.utils.KnownMimeTypes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Check route selection and invocation
 */
public class NegotiationTest {


    private RequestRouter router;
    private Request request;

    @Before
    public void setUp() {
        router = new RequestRouter();
        request = mock(Request.class);
        // Just to not have the ACCEPT header set to null.
        when(request.getHeader(HeaderNames.ACCEPT)).thenReturn("");
        Context context = mock(Context.class);
        when(context.request()).thenReturn(request);
        Context.CONTEXT.set(context);
    }

    @After
    public void tearDown() {
        router.stop();
        Context.CONTEXT.remove();
    }

    @Test
    public void testRouteSelectionBasedOnProducedType() throws Exception {
        Controller controller = new DefaultController() {

            @Route(method= HttpMethod.GET, uri="/", produces = "application/json")
            public Result getJson() {
                return ok("{'foo':'bar'}").json();
            }

            @Route(method= HttpMethod.GET, uri="/", produces = {"application/xml+mine", "application/xml"})
            public Result getXml() {
                return ok("<foo/>").xml();
            }
        };

        router.bindController(controller);

        // First request - ask for json
        when(request.accepts("application/json")).thenReturn(true);
        when(request.accepts("application/xml")).thenReturn(false);
        org.wisdom.api.router.Route route = router.getRouteFor(HttpMethod.GET, "/", request);
        assertThat(route.isUnbound()).isFalse();
        Result result = route.invoke();
        assertThat(result.getStatusCode()).isEqualTo(200);
        assertThat(result.getContentType()).isEqualTo("application/json");


        // Second request - ask for xml
        when(request.accepts("application/json")).thenReturn(false);
        when(request.accepts("application/xml")).thenReturn(true);
        route = router.getRouteFor(HttpMethod.GET, "/", request);
        assertThat(route.isUnbound()).isFalse();
        result = route.invoke();
        assertThat(result.getStatusCode()).isEqualTo(200);
        assertThat(result.getContentType()).isEqualTo("application/xml");

        // Third request - ask for binary
        when(request.accepts("application/json")).thenReturn(false);
        when(request.accepts("application/xml")).thenReturn(false);
        when(request.accepts(MimeTypes.BINARY)).thenReturn(true);
        route = router.getRouteFor(HttpMethod.GET, "/", request);
        assertThat(route.isUnbound()).isTrue();
        result = route.invoke();
        assertThat(result.getStatusCode()).isEqualTo(Status.NOT_ACCEPTABLE);
    }

    @Test
    public void testRouteSelectionBasedOnAcceptedType() throws Exception {
        Controller controller = new DefaultController() {

            @Route(method= HttpMethod.GET, uri="/", accepts = "application/json")
            public Result getJson() {
                return ok("{'foo':'bar'}").json();
            }

            @Route(method= HttpMethod.GET, uri="/", accepts = {"application/xml+mine", "application/xml"})
            public Result getXml() {
                return ok("<foo/>").xml();
            }
        };

        router.bindController(controller);

        // First request - ask for json
        when(request.contentMimeType()).thenReturn("application/json");
        org.wisdom.api.router.Route route = router.getRouteFor(HttpMethod.GET, "/", request);
        assertThat(route.isUnbound()).isFalse();
        Result result = route.invoke();
        assertThat(result.getStatusCode()).isEqualTo(200);
        assertThat(result.getContentType()).isEqualTo("application/json");


        // Second request - ask for xml
        when(request.contentMimeType()).thenReturn("application/xml");
        route = router.getRouteFor(HttpMethod.GET, "/", request);
        assertThat(route.isUnbound()).isFalse();
        result = route.invoke();
        assertThat(result.getStatusCode()).isEqualTo(200);
        assertThat(result.getContentType()).isEqualTo("application/xml");

        // Third request - ask for binary
        when(request.contentMimeType()).thenReturn(MimeTypes.BINARY);
        route = router.getRouteFor(HttpMethod.GET, "/", request);
        assertThat(route.isUnbound()).isTrue();
        result = route.invoke();
        assertThat(result.getStatusCode()).isEqualTo(Status.UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    public void testRouteSelectionBasedOnAcceptedAndProducedTypes() throws Exception {
        Controller controller = new DefaultController() {

            @Route(method= HttpMethod.GET, uri="/", accepts = "text/plain", produces = "application/json")
            public Result getJson() {
                return ok("{'foo':'bar'}").json();
            }

            @Route(method= HttpMethod.GET, uri="/", accepts = {"text/*"}, produces = "application/xml")
            public Result getXml() {
                return ok("<foo/>").xml();
            }
        };

        router.bindController(controller);

        when(request.contentMimeType()).thenReturn("text/plain");
        when(request.accepts("application/json")).thenReturn(true);
        org.wisdom.api.router.Route route = router.getRouteFor(HttpMethod.GET, "/", request);
        Result result = route.invoke();
        assertThat(result.getStatusCode()).isEqualTo(200);
        assertThat(result.getContentType()).isEqualTo("application/json");

        when(request.contentMimeType()).thenReturn("text/plain");
        when(request.accepts("application/json")).thenReturn(false);
        when(request.accepts("application/xml")).thenReturn(true);
        route = router.getRouteFor(HttpMethod.GET, "/", request);
        result = route.invoke();
        assertThat(result.getStatusCode()).isEqualTo(200);
        assertThat(result.getContentType()).isEqualTo("application/xml");

        when(request.contentMimeType()).thenReturn("text/foo");
        when(request.accepts("application/json")).thenReturn(true);
        when(request.accepts("application/xml")).thenReturn(false);
        route = router.getRouteFor(HttpMethod.GET, "/", request);
        result = route.invoke();
        assertThat(result.getStatusCode()).isEqualTo(Status.NOT_ACCEPTABLE);

        when(request.contentMimeType()).thenReturn(MimeTypes.BINARY);
        when(request.accepts("application/json")).thenReturn(false);
        when(request.accepts("application/xml")).thenReturn(true);
        route = router.getRouteFor(HttpMethod.GET, "/", request);
        result = route.invoke();
        assertThat(result.getStatusCode()).isEqualTo(Status.UNSUPPORTED_MEDIA_TYPE);

        when(request.contentMimeType()).thenReturn(MimeTypes.BINARY);
        when(request.accepts("application/json")).thenReturn(false);
        when(request.accepts("application/xml")).thenReturn(false);
        when(request.accepts(MimeTypes.BINARY)).thenReturn(true);
        route = router.getRouteFor(HttpMethod.GET, "/", request);
        result = route.invoke();
        assertThat(result.getStatusCode()).isEqualTo(Status.UNSUPPORTED_MEDIA_TYPE);

        when(request.contentMimeType()).thenReturn("text/foo");
        when(request.accepts("application/json")).thenReturn(true);
        when(request.accepts("application/xml")).thenReturn(false);
        route = router.getRouteFor(HttpMethod.GET, "/", request);
        result = route.invoke();
        assertThat(result.getStatusCode()).isEqualTo(Status.NOT_ACCEPTABLE);

        when(request.contentMimeType()).thenReturn("text/foo");
        when(request.accepts("application/json")).thenReturn(false);
        when(request.accepts("application/xml")).thenReturn(false);
        when(request.accepts(MimeTypes.HTML)).thenReturn(true);
        route = router.getRouteFor(HttpMethod.GET, "/", request);
        result = route.invoke();
        assertThat(result.getStatusCode()).isEqualTo(Status.NOT_ACCEPTABLE);


        when(request.contentMimeType()).thenReturn("text/plain");
        when(request.accepts("application/json")).thenReturn(true);
        when(request.accepts("application/xml")).thenReturn(true);
        route = router.getRouteFor(HttpMethod.GET, "/", request);
        result = route.invoke();
        assertThat(result.getStatusCode()).isEqualTo(200);
        assertThat(result.getContentType()).isEqualTo("application/json");

    }

}
