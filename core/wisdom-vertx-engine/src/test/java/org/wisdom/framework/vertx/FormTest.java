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
package org.wisdom.framework.vertx;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Test;
import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.exceptions.ExceptionMapper;
import org.wisdom.api.http.HeaderNames;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.RouteBuilder;
import org.wisdom.api.router.Router;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Check that the Vertx server handle forms correctly.
 */
public class FormTest extends VertxBaseTest {

    private WisdomVertxServer server;

    @After
    public void tearDown() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    @Test
    public void testFormSubmissionAsFormURLEncoded() throws InterruptedException, IOException {
        Router router = prepareServer();

        // Prepare the router with a controller
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result submit() {
                final Map<String, List<String>> form = context().form();
                // String
                if (!form.get("key").get(0).equals("value")) {
                    return badRequest("key is not equals to value");
                }

                // Multiple values
                List<String> list = form.get("list");
                if (!(list.contains("1") && list.contains("2"))) {
                    return badRequest("list does not contains 1 and 2");
                }

                return ok(context().header(HeaderNames.CONTENT_TYPE));
            }
        };
        Route route = new RouteBuilder().route(HttpMethod.POST)
                .on("/")
                .to(controller, "submit");
        when(router.getRouteFor(anyString(), anyString(), any(org.wisdom.api.http.Request.class))).thenReturn(route);

        server.start();
        waitForStart(server);

        int port = server.httpPort();

        final HttpResponse response = Request.Post("http://localhost:" + port + "/").bodyForm(
                Form.form().add("key", "value").add("list", "1").add("list", "2").build()
        ).execute().returnResponse();

        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
        assertThat(EntityUtils.toString(response.getEntity())).contains(MimeTypes.FORM);
    }

    @Test
    public void testFormSubmissionAsMultipart() throws InterruptedException, IOException {
        Router router = prepareServer();

        // Prepare the router with a controller
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result submit() {
                final Map<String, List<String>> form = context().form();
                // String
                if (!form.get("key").get(0).equals("value")) {
                    return badRequest("key is not equals to value");
                }

                // Multiple values
                List<String> list = form.get("list");
                if (!(list.contains("1") && list.contains("2"))) {
                    return badRequest("list does not contains 1 and 2");
                }

                return ok(context().header(HeaderNames.CONTENT_TYPE));
            }
        };
        Route route = new RouteBuilder().route(HttpMethod.POST)
                .on("/")
                .to(controller, "submit");
        when(router.getRouteFor(anyString(), anyString(), any(org.wisdom.api.http.Request.class))).thenReturn(route);

        server.start();
        waitForStart(server);

        int port = server.httpPort();

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder
                .addTextBody("key", "value", ContentType.TEXT_PLAIN)
                .addTextBody("list", "1", ContentType.TEXT_PLAIN)
                .addTextBody("list", "2", ContentType.TEXT_PLAIN);

        final HttpResponse response = Request.Post("http://localhost:" + port + "/")
                .body(builder.build()).execute().returnResponse();

        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
        assertThat(EntityUtils.toString(response.getEntity())).contains(MimeTypes.MULTIPART);
    }

    private Router prepareServer() {
        // Prepare the configuration
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getIntegerWithDefault(eq("vertx.http.port"), anyInt())).thenReturn(0);
        when(configuration.getIntegerWithDefault(eq("vertx.https.port"), anyInt())).thenReturn(-1);
        when(configuration.getIntegerWithDefault("request.body.max.size", 100 * 1024)).thenReturn(100 * 1024);
        when(configuration.getIntegerWithDefault("vertx.acceptBacklog", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.receiveBufferSize", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.sendBufferSize", -1)).thenReturn(-1);
        when(configuration.getStringArray("wisdom.websocket.subprotocols")).thenReturn(new String[0]);
        when(configuration.getStringArray("vertx.websocket-subprotocols")).thenReturn(new String[0]);


        Router router = mock(Router.class);

        // Configure the server.
        server = new WisdomVertxServer();
        server.configuration = configuration;
        server.accessor = new ServiceAccessor(
                null,
                configuration,
                router,
                getMockContentEngine(),
                null,
                null,
                Collections.<ExceptionMapper>emptyList()
        );
        server.vertx = vertx;
        return router;
    }

}
