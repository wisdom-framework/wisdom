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

import io.vertx.core.http.HttpClientOptions;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Test;
import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.exceptions.ExceptionMapper;
import org.wisdom.api.http.*;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.RouteBuilder;
import org.wisdom.api.router.Router;
import org.wisdom.framework.vertx.ssl.SSLServerContext;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Check the wisdom server behavior.
 * This class is listening for http requests on random port.
 */
public class VertxHttpServerTest extends VertxBaseTest {

    private WisdomVertxServer server;

    @After
    public void tearDown() {
        if (server != null) {
            server.stop();
            server = null;
        }

        SSLServerContext.reset();
    }

    @Test
    public void testServerStartSequence() throws InterruptedException, IOException {
        prepareServer();

        server.start();

        waitForStart(server);

        int port = server.httpPort();
        URL url = new URL("http://localhost:" + port + "/test");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        assertThat(connection.getResponseCode()).isEqualTo(404);

        assertThat(server.hostname()).isEqualTo("localhost");
        assertThat(port).isGreaterThan(9000);
        assertThat(server.httpsPort()).isEqualTo(-1);
    }

    @Test
    public void testOk() throws InterruptedException, IOException {
        Router router = prepareServer();

        // Prepare the router with a controller
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() {
                return ok("Alright");
            }
        };
        Route route = new RouteBuilder().route(HttpMethod.GET)
                .on("/")
                .to(controller, "index");
        when(router.getRouteFor(anyString(), anyString(), any(Request.class))).thenReturn(route);

        server.start();
        waitForStart(server);

        int port = server.httpPort();
        URL url = new URL("http://localhost:" + port + "/");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        assertThat(connection.getResponseCode()).isEqualTo(200);
        String body = IOUtils.toString(connection.getInputStream());
        assertThat(body).isEqualTo("Alright");
    }

    @Test
    public void testInternalError() throws InterruptedException, IOException {
        Router router = prepareServer();

        // Prepare the router with a controller
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() throws IOException {
                throw new IOException("My bad");
            }
        };

        Route route = new RouteBuilder().route(HttpMethod.GET)
                .on("/")
                .to(controller, "index");
        when(router.getRouteFor(anyString(), anyString(), any(Request.class))).thenReturn(route);

        server.start();

        waitForStart(server);
        int port = server.httpPort();
        URL url = new URL("http://localhost:" + port + "/");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        assertThat(connection.getResponseCode()).isEqualTo(500);
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

    @Test
    public void testOkWithPlentyOfClients() throws InterruptedException, IOException {
        Router router = prepareServer();

        // Prepare the router with a controller
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() {
                return ok(context().parameter("id"));
            }
        };
        Route route = new RouteBuilder().route(HttpMethod.GET)
                .on("/")
                .to(controller, "index");
        when(router.getRouteFor(anyString(), anyString(), any(Request.class))).thenReturn(route);

        server.start();

        waitForStart(server);

        // Now start bunch of clients
        int num = NUMBER_OF_CLIENTS;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(num);

        int port = server.httpPort();

        for (int i = 0; i < num; ++i) {// create and start threads
            executor.submit(new Client(startSignal, doneSignal, port, i));
        }

        startSignal.countDown();      // let all threads proceed
        doneSignal.await(30, TimeUnit.SECONDS);           // wait for all to finish

        assertThat(failure).isEmpty();
        assertThat(success).hasSize(num);
    }


    @Test
    public void testOkWithPlentyOfClientsReadingJsonContent() throws InterruptedException, IOException {
        Router router = prepareServer();

        // Prepare the router with a controller
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() throws IOException {
                String content = IOUtils.toString(context().reader());
                if (!content.equals(context().body())) {
                    return badRequest("should be equal " + content + " / " + context().body());
                }
                return ok(context().body()).json();
            }
        };
        Route route = new RouteBuilder().route(HttpMethod.POST)
                .on("/")
                .to(controller, "index");
        when(router.getRouteFor(anyString(), anyString(), any(Request.class))).thenReturn(route);

        server.start();

        waitForStart(server);

        // Now start bunch of clients
        int num = NUMBER_OF_CLIENTS;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(num);

        for (int i = 0; i < num; ++i) {
            executor.submit(new PostClient(startSignal, doneSignal, i));
        }

        startSignal.countDown();      // let all threads proceed
        doneSignal.await(30, TimeUnit.SECONDS);           // wait for all to finish

        assertThat(failure).isEmpty();
        assertThat(success).hasSize(num);
    }

    private class Client implements Runnable {
        private final CountDownLatch startSignal;
        private final CountDownLatch doneSignal;
        private final int port;
        private final int id;

        Client(CountDownLatch startSignal, CountDownLatch doneSignal, int port, int id) {
            this.startSignal = startSignal;
            this.doneSignal = doneSignal;
            this.port = port;
            this.id = id;
        }

        public void run() {
            try {
                startSignal.await();
                doWork();
            } catch (Throwable ex) {
                ex.printStackTrace();
                fail(id);
            } finally {
                doneSignal.countDown();
            }
        }

        void doWork() throws IOException {
            URL url = new URL("http://localhost:" + port + "/?id=" + id);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            assertThat(connection.getResponseCode()).isEqualTo(200);
            String body = IOUtils.toString(connection.getInputStream());
            if (!body.equals(String.valueOf(id))) {
                System.err.println("Wrong content for " + id);
                fail(id);
                return;
            }
            success(id);
        }
    }

    private class PostClient implements Runnable {
        private final CountDownLatch startSignal;
        private final CountDownLatch doneSignal;
        private final int id;

        PostClient(CountDownLatch startSignal, CountDownLatch doneSignal, int id) {
            this.startSignal = startSignal;
            this.doneSignal = doneSignal;
            this.id = id;
        }

        public void run() {
            try {
                startSignal.await();
                doWork();
            } catch (Throwable ex) {
                ex.printStackTrace();
                fail(id);
                doneSignal.countDown();
            }
        }

        void doWork() throws IOException {
            final String message = "{'id':" + id + "}";
            HttpClientOptions options = new HttpClientOptions()
                    .setDefaultHost("localhost")
                    .setDefaultPort(server.httpPort());
            vertx.createHttpClient(options)
                    .post("/",
                            response -> {
                                response.bodyHandler(
                                        data -> {
                                            try {
                                                if (!isOk(response.statusCode())) {
                                                    System.err.println("Bad response code for " + id + ", " +
                                                            "got " + response.statusCode());
                                                    fail(id);
                                                    return;
                                                }

                                                if (!message.equals(data.toString())) {
                                                    System.err.println("Bad content for " + id + " - " + data);
                                                    fail(id);
                                                    return;
                                                }
                                                success(id);
                                            } catch (Exception e) {
                                                System.err.println(e.getMessage());
                                                fail(id);
                                            } finally {
                                                doneSignal.countDown();
                                            }
                                        }

                                );
                            }

                    )
                    .putHeader(HeaderNames.CONTENT_LENGTH, String.valueOf(message.length()))
                    .putHeader(HeaderNames.CONTENT_TYPE, MimeTypes.JSON)
                    .write(message)
                    .end();
        }
    }
}
