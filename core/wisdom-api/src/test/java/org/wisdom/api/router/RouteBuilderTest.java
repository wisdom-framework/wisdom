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

import com.google.common.net.MediaType;
import org.junit.Test;
import org.wisdom.api.Controller;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.api.utils.KnownMimeTypes;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the syntax of the route builder.
 */
public class RouteBuilderTest {

    private class MyController implements Controller {

        public Result index() {
            return Results.ok();
        }

        public void invalid() {
            // Does not return result.
        }

        @Override
        public List<Route> routes() {
            return null;
        }
    }

    @Test
    public void testBuildingWithString() throws NoSuchMethodException {
        MyController controller = new MyController();
        Method method = controller.getClass().getMethod("index");
        Route route = new RouteBuilder().route(HttpMethod.GET).on("/").to(controller, "index");
        assertThat(route).isNotNull();
        assertThat(route.getControllerClass()).isEqualTo(MyController.class);
        assertThat(route.getControllerObject()).isEqualTo(controller);
        assertThat(route.getControllerMethod()).isEqualTo(method);
        assertThat(route.getHttpMethod()).isEqualTo(HttpMethod.GET);
        assertThat(route.getUrl()).isEqualTo("/");
    }

    @Test
    public void testBuildingWithStringWithoutFirstSlash() throws NoSuchMethodException {
        MyController controller = new MyController();
        Method method = controller.getClass().getMethod("index");
        Route route = new RouteBuilder().route(HttpMethod.GET).on("foo").to(controller, "index"); // => /foo
        assertThat(route).isNotNull();
        assertThat(route.getControllerClass()).isEqualTo(MyController.class);
        assertThat(route.getControllerObject()).isEqualTo(controller);
        assertThat(route.getControllerMethod()).isEqualTo(method);
        assertThat(route.getHttpMethod()).isEqualTo(HttpMethod.GET);
        assertThat(route.getUrl()).isEqualTo("/foo");
    }

    @Test
    public void testBuildingWithMethod() throws NoSuchMethodException {
        MyController controller = new MyController();
        Method method = controller.getClass().getMethod("index");
        Route route = new RouteBuilder().route(HttpMethod.GET).on("/").to(controller, method);
        assertThat(route).isNotNull();
        assertThat(route.getControllerClass()).isEqualTo(MyController.class);
        assertThat(route.getControllerObject()).isEqualTo(controller);
        assertThat(route.getControllerMethod()).isEqualTo(method);
        assertThat(route.getHttpMethod()).isEqualTo(HttpMethod.GET);
        assertThat(route.getUrl()).isEqualTo("/");
    }

    @Test
    public void testBuildingWithOneConsume() throws NoSuchMethodException {
        MyController controller = new MyController();
        Method method = controller.getClass().getMethod("index");
        Route route = new RouteBuilder().route(HttpMethod.GET).on("/").to(controller, method)
                .accepting(KnownMimeTypes.getMimeTypeByExtension("json"));
        assertThat(route).isNotNull();
        assertThat(route.getControllerClass()).isEqualTo(MyController.class);
        assertThat(route.getControllerObject()).isEqualTo(controller);
        assertThat(route.getControllerMethod()).isEqualTo(method);
        assertThat(route.getHttpMethod()).isEqualTo(HttpMethod.GET);
        assertThat(route.getUrl()).isEqualTo("/");
        assertThat(route.getAcceptedMediaTypes()).hasSize(1).contains(KnownMimeTypes.getMediaTypeByExtension("json"));
        assertThat(route.getProducedMediaTypes()).isEmpty();
    }

    @Test
    public void testBuildingWithOneProduce() throws NoSuchMethodException {
        MyController controller = new MyController();
        Method method = controller.getClass().getMethod("index");
        Route route = new RouteBuilder().route(HttpMethod.GET).on("/").to(controller, method)
                .producing(KnownMimeTypes.getMimeTypeByExtension("json"));
        assertThat(route).isNotNull();
        assertThat(route.getControllerClass()).isEqualTo(MyController.class);
        assertThat(route.getControllerObject()).isEqualTo(controller);
        assertThat(route.getControllerMethod()).isEqualTo(method);
        assertThat(route.getHttpMethod()).isEqualTo(HttpMethod.GET);
        assertThat(route.getUrl()).isEqualTo("/");
        assertThat(route.getProducedMediaTypes()).hasSize(1).contains(KnownMimeTypes.getMediaTypeByExtension("json"));
        assertThat(route.getAcceptedMediaTypes()).isEmpty();
    }

    @Test
    public void testBuildingWithOneConsumeUsingWildcard() throws NoSuchMethodException {
        MediaType text = MediaType.ANY_TEXT_TYPE;
        MyController controller = new MyController();
        Method method = controller.getClass().getMethod("index");
        Route route = new RouteBuilder().route(HttpMethod.GET).on("/").to(controller, method)
                .accepting("text/*");
        assertThat(route).isNotNull();
        assertThat(route.getControllerClass()).isEqualTo(MyController.class);
        assertThat(route.getControllerObject()).isEqualTo(controller);
        assertThat(route.getControllerMethod()).isEqualTo(method);
        assertThat(route.getHttpMethod()).isEqualTo(HttpMethod.GET);
        assertThat(route.getUrl()).isEqualTo("/");
        assertThat(route.getAcceptedMediaTypes()).hasSize(1).contains(text);
    }

    @Test
    public void testBuildingWithTwoConsumeInOneCall() throws NoSuchMethodException {
        MyController controller = new MyController();
        Method method = controller.getClass().getMethod("index");
        Route route = new RouteBuilder().route(HttpMethod.GET).on("/").to(controller, method)
                .accepting(KnownMimeTypes.getMimeTypeByExtension("json"),
                        KnownMimeTypes.getMimeTypeByExtension("html"));
        assertThat(route).isNotNull();
        assertThat(route.getControllerClass()).isEqualTo(MyController.class);
        assertThat(route.getControllerObject()).isEqualTo(controller);
        assertThat(route.getControllerMethod()).isEqualTo(method);
        assertThat(route.getHttpMethod()).isEqualTo(HttpMethod.GET);
        assertThat(route.getUrl()).isEqualTo("/");
        assertThat(route.getAcceptedMediaTypes()).hasSize(2)
                .contains(KnownMimeTypes.getMediaTypeByExtension("json"))
                .contains(KnownMimeTypes.getMediaTypeByExtension("html"));
    }

    @Test
    public void testBuildingWithTwoConsumeInTwoCalls() throws NoSuchMethodException {
        MyController controller = new MyController();
        Method method = controller.getClass().getMethod("index");
        Route route = new RouteBuilder().route(HttpMethod.GET).on("/").to(controller, method)
                .accepting(KnownMimeTypes.getMimeTypeByExtension("json"))
                .accepting(KnownMimeTypes.getMimeTypeByExtension("html"));
        assertThat(route).isNotNull();
        assertThat(route.getControllerClass()).isEqualTo(MyController.class);
        assertThat(route.getControllerObject()).isEqualTo(controller);
        assertThat(route.getControllerMethod()).isEqualTo(method);
        assertThat(route.getHttpMethod()).isEqualTo(HttpMethod.GET);
        assertThat(route.getUrl()).isEqualTo("/");
        assertThat(route.getAcceptedMediaTypes()).hasSize(2)
                .contains(KnownMimeTypes.getMediaTypeByExtension("json"))
                .contains(KnownMimeTypes.getMediaTypeByExtension("html"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidMethodWithString() throws NoSuchMethodException {
        MyController controller = new MyController();
        new RouteBuilder().route(HttpMethod.GET).on("/").to(controller, "invalid");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingMethod() throws NoSuchMethodException {
        MyController controller = new MyController();
        new RouteBuilder().route(HttpMethod.GET).on("/").to(controller, "doesNotExist");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidMethodWithMethod() throws NoSuchMethodException {
        MyController controller = new MyController();
        Method method = controller.getClass().getMethod("invalid");
        new RouteBuilder().route(HttpMethod.GET).on("/").to(controller, method);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMethodFromAnotherController() throws NoSuchMethodException {
        MyController controller = new MyController();
        MyController controller2 = new MyController();
        Method method = controller.getClass().getMethod("invalid");
        new RouteBuilder().route(HttpMethod.GET).on("/").to(controller2, method);
    }

}
