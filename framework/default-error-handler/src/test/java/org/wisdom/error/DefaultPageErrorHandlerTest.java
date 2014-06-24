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
package org.wisdom.error;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.*;
import org.wisdom.api.interception.Filter;
import org.wisdom.api.interception.Interceptor;
import org.wisdom.api.interception.RequestContext;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.Router;
import org.wisdom.api.templates.Template;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Check the behavior of the default page error handler.
 */
public class DefaultPageErrorHandlerTest {
    @Test
    public void testCleanup() throws Exception {
        StackTraceElement[] stack = ImmutableList.of(
                new StackTraceElement("org.wisdom.samples.error.ErroneousController", "__M_doSomethingWrong",
                        "ErroneousController.java", 36),
                new StackTraceElement("org.wisdom.samples.error.ErroneousController", "doSomethingWrong",
                        "ErroneousController.java", -1)
        ).toArray(new StackTraceElement[2]);

        List<StackTraceElement> cleanup = DefaultPageErrorHandler.cleanup(stack);
        assertThat(cleanup).hasSize(1);
        assertThat(cleanup.get(0).getMethodName()).isEqualTo(stack[1].getMethodName());
    }

    @Test
    public void testUri() throws Exception {
        DefaultPageErrorHandler handler = new DefaultPageErrorHandler();
        assertThat(handler.uri().matcher("/").matches()).isTrue();
        assertThat(handler.uri().matcher("/foo").matches()).isTrue();
    }

    @Test
    public void testPriority() throws Exception {
        DefaultPageErrorHandler handler = new DefaultPageErrorHandler();
        assertThat(handler.priority()).isGreaterThan(0);
    }

    @Test
    public void switchToHeadWhenGetRouteExist() throws Exception {
        DefaultPageErrorHandler handler = new DefaultPageErrorHandler();
        handler.router = mock(Router.class);

        handler.configuration = mock(ApplicationConfiguration.class);
        when(handler.configuration.isDev()).thenReturn(false);

        Controller controller = new MyController();
        Route route = new Route(HttpMethod.GET, "/", controller, controller.getClass().getMethod("action"));
        Route reqRoute = new Route(HttpMethod.HEAD, "/", null, null);


        when(handler.router.getRouteFor(HttpMethod.HEAD, "/")).thenReturn(new Route(HttpMethod.HEAD, "/", null, null));
        when(handler.router.getRouteFor(HttpMethod.GET, "/")).thenReturn(route);

        RequestContext rc = new RequestContext(reqRoute, Collections.<Filter>emptyList(),
                Collections.<Interceptor<?>, Object>emptyMap(), new Object[0]);

        Result result = handler.call(reqRoute, rc);
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(Status.OK);
        assertThat(result.getRenderable().length()).isEqualTo(0);
        assertThat(result.getContentType()).isEqualTo(MimeTypes.JSON);
    }

    @Test
    public void switchToHeadWhenGetRouteDoesNotExist() throws Exception {
        DefaultPageErrorHandler handler = new DefaultPageErrorHandler();
        handler.configuration = mock(ApplicationConfiguration.class);
        when(handler.configuration.isDev()).thenReturn(false);
        handler.router = mock(Router.class);

        Route reqRoute = new Route(HttpMethod.HEAD, "/", null, null);

        when(handler.router.getRouteFor(HttpMethod.HEAD, "/")).thenReturn(new Route(HttpMethod.HEAD, "/", null, null));
        when(handler.router.getRouteFor(HttpMethod.GET, "/")).thenReturn(new Route(HttpMethod.GET, "/", null, null));

        RequestContext rc = new RequestContext(reqRoute, Collections.<Filter>emptyList(),
                Collections.<Interceptor<?>, Object>emptyMap(), new Object[0]);

        Result result = handler.call(reqRoute, rc);
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(Status.NOT_FOUND);
        assertThat(result.getRenderable().length()).isEqualTo(0);
    }

    @Test
    public void pipelineErrorWithoutError() throws Exception {
        DefaultPageErrorHandler handler = new DefaultPageErrorHandler();
        handler.configuration = mock(ApplicationConfiguration.class);
        when(handler.configuration.isDev()).thenReturn(true);
        when(handler.configuration.getBaseDir()).thenReturn(new File("src/test/resources/wisdom/missing_on_purpose"));
        handler.router = mock(Router.class);
        handler.pipeline = mock(Template.class);

        Request request = mock(Request.class);
        when(request.accepts(MimeTypes.HTML)).thenReturn(true);

        Context context = mock(Context.class);
        when(context.request()).thenReturn(request);

        Context.CONTEXT.set(context);
        MyController controller = new MyController();
        Route route = new Route(HttpMethod.GET, "/", controller, controller.getClass().getMethod("action"));


        RequestContext rc = new RequestContext(route, Collections.<Filter>emptyList(),
                Collections.<Interceptor<?>, Object>emptyMap(), new Object[0]);

        handler.start();
        Result result = handler.call(route, rc);
        assertThat(result.getStatusCode()).isEqualTo(200);
    }

    @Test
    public void pipelineErrorWithError() throws Exception {
        DefaultPageErrorHandler handler = new DefaultPageErrorHandler();
        handler.configuration = mock(ApplicationConfiguration.class);
        when(handler.configuration.isDev()).thenReturn(true);
        when(handler.configuration.getBaseDir()).thenReturn(new File("src/test/resources/wisdom"));

        handler.router = mock(Router.class);
        handler.pipeline = mock(Template.class);
        handler.json = mock(Json.class);
        when(handler.json.parse(anyString())).thenAnswer(new Answer<JsonNode>() {
            @Override
            public JsonNode answer(InvocationOnMock invocation) throws Throwable {
                return new ObjectMapper().readValue((String) invocation.getArguments()[0], JsonNode.class);
            }
        });

        handler.start();

        Request request = mock(Request.class);
        when(request.accepts(MimeTypes.HTML)).thenReturn(true);

        Context context = mock(Context.class);
        when(context.request()).thenReturn(request);

        Context.CONTEXT.set(context);
        MyController controller = new MyController();
        Route route = new Route(HttpMethod.GET, "/", controller, controller.getClass().getMethod("action"));


        RequestContext rc = new RequestContext(route, Collections.<Filter>emptyList(),
                Collections.<Interceptor<?>, Object>emptyMap(), new Object[0]);

        Result result = handler.call(route, rc);
        assertThat(result.getStatusCode()).isEqualTo(500);
    }

    @After
    public void cleanupContext() {
        Context.CONTEXT.remove();
    }


    private class MyController extends DefaultController {

        public Result action() {
            return ok("OK").json();
        }

    }
}
