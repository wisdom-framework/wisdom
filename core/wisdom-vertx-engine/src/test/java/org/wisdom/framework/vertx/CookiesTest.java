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

import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Test;
import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.cookies.Cookie;
import org.wisdom.api.cookies.SessionCookie;
import org.wisdom.api.crypto.Crypto;
import org.wisdom.api.exceptions.ExceptionMapper;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Request;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.RouteBuilder;
import org.wisdom.api.router.Router;
import org.wisdom.api.utils.CookieDataCodec;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Check the wisdom server behavior.
 * This class is listening for http requests on random port.
 */
public class CookiesTest extends VertxBaseTest {

    private WisdomVertxServer server;

    @After
    public void tearDown() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    @Test
    public void testCookie() throws InterruptedException, IOException {
        Router router = prepareServer();

        // Prepare the router with a controller
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() {
                if (context().parameter("id") == null) {
                    System.err.println("No param: " + context().parameters());
                }
                return ok("Alright").with(Cookie.builder("my-cookie", context().parameter("id")).setMaxAge(1000)
                        .build());
            }

            @SuppressWarnings("unused")
            public Result logged() {
                String id = context().cookieValue("my-cookie");
                if (id == null) {
                    return badRequest("no cookie");
                } else {
                    return ok(id);
                }
            }
        };
        final Route route1 = new RouteBuilder().route(HttpMethod.GET)
                .on("/")
                .to(controller, "index");
        final Route route2 = new RouteBuilder().route(HttpMethod.GET)
                .on("/logged")
                .to(controller, "logged");
        configureRouter(router, route1, route2);

        server.start();
        waitForStart(server);

        // Now start bunch of clients
        int num = NUMBER_OF_CLIENTS;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(num);

        int port = server.httpPort();

        for (int i = 0; i < num; ++i) {// create and start threads
            executor.submit(new LoggedClient(startSignal, doneSignal, port, i, false));
        }

        startSignal.countDown();      // let all threads proceed
        assertThat(doneSignal.await(60, TimeUnit.SECONDS)).isTrue(); // wait for all to finish

        assertThat(failure).isEmpty();
        assertThat(success).hasSize(num);

    }

    private void configureRouter(Router router, Route root, Route logged) {
        doAnswer(invocationOnMock -> {
            String path = (String) invocationOnMock.getArguments()[1];
            if (path.equals("/")) {
                return root;
            }
            if (path.equals("/logged")) {
                return logged;
            }
            return null;
        }).when(router).getRouteFor(anyString(), anyString(), any(Request.class));
    }

    @Test
    public void testCookieContainingComplexValues() throws InterruptedException, IOException {
        Router router = prepareServer();

        // Prepare the router with a controller
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() throws UnsupportedEncodingException {
                if (context().parameter("id") == null) {
                    System.err.println("No param: " + context().parameters());
                }
                return ok("Alright").with(Cookie.builder("my-cookie",
                        CookieDataCodec.encode(ImmutableMap.of("key1", "val1", "key2", "val2", "id", context().parameter("id")))).setMaxAge(1000).build());
            }

            @SuppressWarnings("unused")
            public Result logged() throws UnsupportedEncodingException {
                String encoded = context().cookieValue("my-cookie");
                Map<String, String> map = new LinkedHashMap<>();
                CookieDataCodec.decode(map, encoded);
                // Check contained values
                if (map.get("id") == null) {
                    return badRequest("cannot find id in " + map);
                }

                if (!"val1".equalsIgnoreCase(map.get("key1"))) {
                    return badRequest("cannot find key1 in " + map);
                }

                if (!"val2".equalsIgnoreCase(map.get("key2"))) {
                    return badRequest("cannot find key2 in " + map);
                }

                return ok(map.get("id"));
            }
        };
        final Route route1 = new RouteBuilder().route(HttpMethod.GET)
                .on("/")
                .to(controller, "index");
        final Route route2 = new RouteBuilder().route(HttpMethod.GET)
                .on("/logged")
                .to(controller, "logged");
        configureRouter(router, route1, route2);

        server.start();
        waitForStart(server);

        // Now start bunch of clients
        int num = NUMBER_OF_CLIENTS;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(num);

        int port = server.httpPort();

        for (int i = 0; i < num; ++i) {// create and start threads
            executor.submit(new LoggedClient(startSignal, doneSignal, port, i, true));
        }

        startSignal.countDown();      // let all threads proceed
        assertThat(doneSignal.await(60, TimeUnit.SECONDS)).isTrue(); // wait for all to finish

        assertThat(failure).isEmpty();
        assertThat(success).hasSize(num);

    }

    @Test
    public void testTwoCookies() throws InterruptedException, IOException {
        Router router = prepareServer();

        // Prepare the router with a controller
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() {
                return ok("Alright")
                        .with(Cookie.builder("my-cookie", context().parameter("id")).setMaxAge(1000).build())
                        .with(Cookie.builder("my-cookie-2", context().parameter("id")).setMaxAge(1000).build());
            }

            @SuppressWarnings("unused")
            public Result logged() {
                String id = context().cookieValue("my-cookie");
                String id2 = context().cookie("my-cookie-2").value();
                if (id == null || id2 == null) {
                    return badRequest("no cookie");
                } else {
                    return ok(id);
                }
            }
        };
        final Route route1 = new RouteBuilder().route(HttpMethod.GET)
                .on("/")
                .to(controller, "index");
        final Route route2 = new RouteBuilder().route(HttpMethod.GET)
                .on("/logged")
                .to(controller, "logged");
        configureRouter(router, route1, route2);

        server.start();
        waitForStart(server);

        // Now start bunch of clients
        int num = NUMBER_OF_CLIENTS;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(num);

        int port = server.httpPort();

        for (int i = 0; i < num; ++i) // create and start threads
            executor.submit(new LoggedClient(startSignal, doneSignal, port, i, false));

        startSignal.countDown();      // let all threads proceed
        doneSignal.await(30, TimeUnit.SECONDS);           // wait for all to finish

        assertThat(failure).isEmpty();
        assertThat(success).hasSize(num);

    }

    @Test
    public void testSession() throws InterruptedException, IOException {
        Router router = prepareServer();

        // Prepare the router with a controller
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() {
                context().session().put("id", context().parameter("id"));
                return ok("Alright");
            }

            @SuppressWarnings("unused")
            public Result logged() {
                String id = context().session().get("id");
                if (id == null) {
                    return badRequest("no session");
                } else {
                    return ok(id);
                }
            }
        };
        final Route route1 = new RouteBuilder().route(HttpMethod.GET)
                .on("/")
                .to(controller, "index");
        final Route route2 = new RouteBuilder().route(HttpMethod.GET)
                .on("/logged")
                .to(controller, "logged");
        configureRouter(router, route1, route2);

        server.start();
        waitForStart(server);

        // Now start bunch of clients
        int num = 1;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(num);

        int port = server.httpPort();

        for (int i = 0; i < num; ++i) {
            executor.submit(new LoggedClient(startSignal, doneSignal, port, i, true));
        }

        startSignal.countDown();      // let all threads proceed
        doneSignal.await(60, TimeUnit.SECONDS);           // wait for all to finish

        assertThat(failure).isEmpty();
        assertThat(success).hasSize(num);

    }

    @Test
    public void testFlash() throws InterruptedException, IOException {
        Router router = prepareServer();

        // Prepare the router with a controller
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() {
                context().flash().put("id", context().parameter("id"));
                return ok("Alright");
            }

            @SuppressWarnings("unused")
            public Result logged() {
                String id = context().flash().get("id");
                if (id == null) {
                    return badRequest("no flash");
                } else {
                    return ok(id);
                }
            }
        };
        final Route route1 = new RouteBuilder().route(HttpMethod.GET)
                .on("/")
                .to(controller, "index");
        final Route route2 = new RouteBuilder().route(HttpMethod.GET)
                .on("/logged")
                .to(controller, "logged");
        configureRouter(router, route1, route2);

        server.start();
        waitForStart(server);

        // Now start bunch of clients
        int num = NUMBER_OF_CLIENTS;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(num);

        int port = server.httpPort();

        for (int i = 0; i < num; ++i) { // create and start threads
            executor.submit(new LoggedClient(startSignal, doneSignal, port, i, true));
        }

        startSignal.countDown();      // let all threads proceed
        doneSignal.await(30, TimeUnit.SECONDS);           // wait for all to finish

        assertThat(failure).isEmpty();
        assertThat(success).hasSize(num);

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
        when(configuration.getWithDefault(Cookie.APPLICATION_COOKIE_PREFIX, "wisdom")).thenReturn("wisdom");
        when(configuration.getIntegerWithDefault(SessionCookie.SESSION_EXPIRE_TIME_SECOND,
                3600) * 1000).thenReturn(1000);
        when(configuration.getBooleanWithDefault(SessionCookie.SESSION_SEND_ONLY_IF_CHANGED, true)).thenReturn(true);
        when(configuration.getBooleanWithDefault(SessionCookie.SESSION_OVER_HTTPS_ONLY, false)).thenReturn(false);
        when(configuration.getBooleanWithDefault(SessionCookie.SESSION_HTTP_ONLY, true)).thenReturn(true);

        when(configuration.getStringArray("wisdom.websocket.subprotocols")).thenReturn(new String[0]);
        when(configuration.getStringArray("vertx.websocket-subprotocols")).thenReturn(new String[0]);

        Crypto crypto = mock(Crypto.class);
        when(crypto.sign(anyString())).thenReturn("aaaaaa");

        Router router = mock(Router.class);


        // Configure the server.
        server = new WisdomVertxServer();
        server.configuration = configuration;
        server.accessor = new ServiceAccessor(
                crypto,
                configuration,
                router,
                getMockContentEngine(),
                executor,
                null,
                Collections.<ExceptionMapper>emptyList()
        );
        server.vertx = vertx;
        return router;
    }

    @Test
    public void testThatCookiesAreWithdrawnCorrectly() throws InterruptedException, IOException {
        Router router = prepareServer();

        // Prepare the router with a controller
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() {
                if (context().parameter("id") == null) {
                    return badRequest("'id' parameter required");
                }
                return ok("Alright").with(Cookie.builder("my-cookie", context().parameter("id")).setMaxAge(3600)
                        .setSecure(false).build());
            }

            @SuppressWarnings("unused")
            public Result logged() {
                String id = context().cookieValue("my-cookie");
                if (id == null) {
                    return badRequest("no cookie");
                } else {
                    return ok(id).without("my-cookie");
                }
            }
        };
        final Route route1 = new RouteBuilder().route(HttpMethod.GET)
                .on("/")
                .to(controller, "index");
        final Route route2 = new RouteBuilder().route(HttpMethod.GET)
                .on("/logged")
                .to(controller, "logged");
        configureRouter(router, route1, route2);

        server.start();
        waitForStart(server);

        // Now start bunch of clients
        int num = NUMBER_OF_CLIENTS;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(num);

        int port = server.httpPort();

        for (int i = 0; i < num; ++i) {// create and start threads
            final LoggedClient task = new LoggedClient(startSignal, doneSignal, port, i, false)
                    .additionalChecks((context, response, content) -> context.getCookieStore().getCookies().isEmpty());
            executor.submit(task);
        }

        startSignal.countDown();      // let all threads proceed
        assertThat(doneSignal.await(60, TimeUnit.SECONDS)).isTrue(); // wait for all to finish

        assertThat(failure).isEmpty();
        assertThat(success).hasSize(num);

    }

    @Test
    public void testThatCookiesCanBeReplaced() throws InterruptedException, IOException {
        Router router = prepareServer();

        // Prepare the router with a controller
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() {
                if (context().parameter("id") == null) {
                    return badRequest("'id' parameter required");
                }
                return ok("Alright").with(Cookie.builder("my-cookie", context().parameter("id")).setMaxAge(3600)
                        .setSecure(false).build());
            }

            @SuppressWarnings("unused")
            public Result logged() {
                String id = context().cookieValue("my-cookie");
                if (id == null) {
                    return badRequest("no cookie");
                } else {
                    return ok(id).with(Cookie.cookie("my-cookie", id + "_").build());
                }
            }
        };
        final Route route1 = new RouteBuilder().route(HttpMethod.GET)
                .on("/")
                .to(controller, "index");
        final Route route2 = new RouteBuilder().route(HttpMethod.GET)
                .on("/logged")
                .to(controller, "logged");
        configureRouter(router, route1, route2);

        server.start();
        waitForStart(server);

        // Now start bunch of clients
        int num = NUMBER_OF_CLIENTS;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(num);

        int port = server.httpPort();

        createAndSubmitClients(num, startSignal, doneSignal, port);

        startSignal.countDown();      // let all threads proceed
        assertThat(doneSignal.await(60, TimeUnit.SECONDS)).isTrue(); // wait for all to finish

        assertThat(failure).isEmpty();
        assertThat(success).hasSize(num);

    }

    private void createAndSubmitClients(int num, CountDownLatch startSignal, CountDownLatch doneSignal, int port) {
        for (int i = 0; i < num; ++i) {// create and start threads
            final int id = i;
            final LoggedClient task = new LoggedClient(startSignal, doneSignal, port, i, false)
                    .additionalChecks((context, response, content) -> {
                        final org.apache.http.cookie.Cookie cookie = context.getCookieStore().getCookies().get(0);
                        return cookie.getValue().equals(id + "_");
            });
            executor.submit(task);
        }
    }

    @Test
    public void testThatCookiesCanBeWithdrawnAndReplaced() throws InterruptedException, IOException {
        Router router = prepareServer();

        // Prepare the router with a controller
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() {
                if (context().parameter("id") == null) {
                    return badRequest("'id' parameter required");
                }
                return ok("Alright").with(Cookie.builder("my-cookie", context().parameter("id")).setMaxAge(3600)
                        .setSecure(false).build());
            }

            @SuppressWarnings("unused")
            public Result logged() {
                String id = context().cookieValue("my-cookie");
                if (id == null) {
                    return badRequest("no cookie");
                } else {
                    return ok(id).without("my-cookie").with(Cookie.cookie("my-cookie", id + "_").build());
                }
            }
        };
        final Route route1 = new RouteBuilder().route(HttpMethod.GET)
                .on("/")
                .to(controller, "index");
        final Route route2 = new RouteBuilder().route(HttpMethod.GET)
                .on("/logged")
                .to(controller, "logged");
        configureRouter(router, route1, route2);

        server.start();
        waitForStart(server);

        // Now start bunch of clients
        int num = NUMBER_OF_CLIENTS;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(num);

        int port = server.httpPort();

        createAndSubmitClients(num, startSignal, doneSignal, port);

        startSignal.countDown();      // let all threads proceed
        assertThat(doneSignal.await(60, TimeUnit.SECONDS)).isTrue(); // wait for all to finish

        assertThat(failure).isEmpty();
        assertThat(success).hasSize(num);

    }

    private class LoggedClient implements Runnable {
        private final CountDownLatch startSignal;
        private final CountDownLatch doneSignal;
        private final int port;
        private final int id;
        private final boolean noCheck;
        private Checker additionalChecks;

        LoggedClient(CountDownLatch startSignal, CountDownLatch doneSignal, int port, int id, boolean noCheck) {
            this.startSignal = startSignal;
            this.doneSignal = doneSignal;
            this.port = port;
            this.id = id;
            this.noCheck = noCheck;
        }

        public LoggedClient additionalChecks(Checker checks) {
            this.additionalChecks = checks;
            return this;
        }

        public void run() {
            try {
                startSignal.await();
                doWork();
            } catch (Throwable ex) {
                ex.printStackTrace();
                fail(id);
            }
            doneSignal.countDown();
        }

        void doWork() throws IOException {
            CloseableHttpClient httpclient = null;
            CloseableHttpResponse response = null;
            try {
                RequestConfig globalConfig = RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD)
                        .build();
                httpclient = HttpClients.custom()
                        .setDefaultRequestConfig(globalConfig)
                        .build();
                CookieStore cookieStore = new BasicCookieStore();
                HttpClientContext context = HttpClientContext.create();
                context.setCookieStore(cookieStore);

                final String uri = "http://localhost:" + port + "/?id=" + id;
                HttpGet req1 = new HttpGet(uri);

                response = httpclient.execute(req1, context);
                String content = EntityUtils.toString(response.getEntity());

                assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
                assertThat(content).isEqualTo("Alright");

                if (!noCheck) {
                    org.apache.http.cookie.Cookie c = context.getCookieStore().getCookies().get(0);
                    if (!c.getName().equalsIgnoreCase("my-cookie")) {
                        System.err.println("Wrong cookie name for " + id);
                        fail(id);
                        return;
                    }
                    if (!c.getValue().equalsIgnoreCase(String.valueOf(id))) {
                        System.err.println("Wrong cookie content for " + id);
                        fail(id);
                        return;
                    }
                }

                // Proceed to the second request.
                HttpGet req2 = new HttpGet("http://localhost:" + port + "/logged");
                response = httpclient.execute(req2, context);
                content = EntityUtils.toString(response.getEntity());
                if (!isOk(response)) {
                    System.err.println("Bad status code for " + id + " : " + content);
                    fail(id);
                    return;
                }

                if (!content.equalsIgnoreCase(String.valueOf(id))) {
                    System.err.println("Wrong response content for " + id);
                    fail(id);
                    return;
                }

                if (additionalChecks != null) {
                    try {
                        if (additionalChecks.check(context, response, content)) {
                            success(id);
                        } else {
                            fail(id);
                        }
                    } catch (Exception e) {
                        System.err.println("Additional checks have thrown an exception for " + id);
                        e.printStackTrace();
                        fail(id);
                    }
                } else {
                    success(id);
                }
            } finally {
                IOUtils.closeQuietly(httpclient);
                IOUtils.closeQuietly(response);
            }
        }
    }

    public interface Checker {
        boolean check(HttpClientContext context, CloseableHttpResponse response, String content) throws Exception;
    }
}
