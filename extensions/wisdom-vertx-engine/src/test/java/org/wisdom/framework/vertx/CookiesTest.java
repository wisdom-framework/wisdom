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
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultVertxFactory;
import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.content.ContentEncodingHelper;
import org.wisdom.api.content.ContentEngine;
import org.wisdom.api.content.ContentSerializer;
import org.wisdom.api.cookies.Cookie;
import org.wisdom.api.cookies.SessionCookie;
import org.wisdom.api.crypto.Crypto;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Renderable;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.RouteBuilder;
import org.wisdom.api.router.Router;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
public class CookiesTest {

    private WisdomVertxServer server;

    DefaultVertxFactory factory = new DefaultVertxFactory();
    Vertx vertx = factory.createVertx();

    @After
    public void tearDown() {
        if (server != null) {
            server.stop();
            server = null;
        }
        if (vertx != null) {
            vertx.stop();
        }

        failure.clear();
        success.clear();
    }

    public static void waitForStart(WisdomVertxServer server) throws InterruptedException {
        int attempt = 0;
        while (server.httpPort() == 0 && attempt < 10) {
            Thread.sleep(1000);
            attempt++;
        }
    }

    @Test
    public void testCookie() throws InterruptedException, IOException {
        Router router = prepareServer();

        // Prepare the router with a controller
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() {
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
        Route route1 = new RouteBuilder().route(HttpMethod.GET)
                .on("/")
                .to(controller, "index");
        Route route2 = new RouteBuilder().route(HttpMethod.GET)
                .on("/logged")
                .to(controller, "logged");
        when(router.getRouteFor("GET", "/")).thenReturn(route1);
        when(router.getRouteFor("GET", "/logged")).thenReturn(route2);

        server.start();
        waitForStart(server);

        // Now start bunch of clients
        int num = 100;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(num);

        int port = server.httpPort();

        for (int i = 0; i < num; ++i) // create and start threads
            new Thread(new LoggedClient(startSignal, doneSignal, port, i, false)).start();

        startSignal.countDown();      // let all threads proceed
        doneSignal.await(10, TimeUnit.SECONDS);           // wait for all to finish

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
        Route route1 = new RouteBuilder().route(HttpMethod.GET)
                .on("/")
                .to(controller, "index");
        Route route2 = new RouteBuilder().route(HttpMethod.GET)
                .on("/logged")
                .to(controller, "logged");
        when(router.getRouteFor("GET", "/")).thenReturn(route1);
        when(router.getRouteFor("GET", "/logged")).thenReturn(route2);

        server.start();
        waitForStart(server);

        // Now start bunch of clients
        int num = 1;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(num);

        int port = server.httpPort();

        for (int i = 0; i < num; ++i) // create and start threads
            new Thread(new LoggedClient(startSignal, doneSignal, port, i, true)).start();

        startSignal.countDown();      // let all threads proceed
        doneSignal.await(10, TimeUnit.SECONDS);           // wait for all to finish

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
        Route route1 = new RouteBuilder().route(HttpMethod.GET)
                .on("/")
                .to(controller, "index");
        Route route2 = new RouteBuilder().route(HttpMethod.GET)
                .on("/logged")
                .to(controller, "logged");
        when(router.getRouteFor("GET", "/")).thenReturn(route1);
        when(router.getRouteFor("GET", "/logged")).thenReturn(route2);

        server.start();
        waitForStart(server);

        // Now start bunch of clients
        int num = 1;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(num);

        int port = server.httpPort();

        for (int i = 0; i < num; ++i) // create and start threads
            new Thread(new LoggedClient(startSignal, doneSignal, port, i, true)).start();

        startSignal.countDown();      // let all threads proceed
        doneSignal.await(10, TimeUnit.SECONDS);           // wait for all to finish

        assertThat(failure).isEmpty();
        assertThat(success).hasSize(num);

    }


    private Router prepareServer() {
        // Prepare the configuration
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getIntegerWithDefault(eq("vertx.http.port"), anyInt())).thenReturn(0);
        when(configuration.getIntegerWithDefault(eq("vertx.https.port"), anyInt())).thenReturn(-1);
        when(configuration.getIntegerWithDefault("request.body.max.size", 100 * 1024)).thenReturn(100*1024);
        when(configuration.getIntegerWithDefault("vertx.acceptBacklog", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.receiveBufferSize", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.sendBufferSize", -1)).thenReturn(-1);
        when(configuration.getWithDefault(Cookie.APPLICATION_COOKIE_PREFIX, "wisdom")).thenReturn("wisdom");
        when(configuration.getIntegerWithDefault(SessionCookie.SESSION_EXPIRE_TIME_SECOND,
                3600) * 1000).thenReturn(1000);
        when(configuration.getBooleanWithDefault(SessionCookie.SESSION_SEND_ONLY_IF_CHANGED, true)).thenReturn(true);
        when(configuration.getBooleanWithDefault(SessionCookie.SESSION_OVER_HTTPS_ONLY, false)).thenReturn(false);
        when(configuration.getBooleanWithDefault(SessionCookie.SESSION_HTTP_ONLY, true)).thenReturn(true);

        Crypto crypto = mock(Crypto.class);
        when(crypto.sign(anyString())).thenReturn("aaaaaa");

        Router router = mock(Router.class);

        // Configure the content engine.
        ContentSerializer serializer = new ContentSerializer() {
            @Override
            public String getContentType() {
                return null;
            }

            @Override
            public void serialize(Renderable<?> renderable) {
                if (renderable.content() instanceof Exception) {
                    renderable.setSerializedForm(((Exception) renderable.content()).getMessage());
                }
            }
        };
        ContentEncodingHelper encodingHelper = new ContentEncodingHelper() {

            @Override
            public List<String> parseAcceptEncodingHeader(String headerContent) {
                return new ArrayList<String>();
            }

            @Override
            public boolean shouldEncodeWithRoute(Route route) {
                return true;
            }

            @Override
            public boolean shouldEncodeWithSize(Route route,
                                                Renderable<?> renderable) {
                return true;
            }

            @Override
            public boolean shouldEncodeWithMimeType(Renderable<?> renderable) {
                return true;
            }

            @Override
            public boolean shouldEncode(Context context, Result result,
                                        Renderable<?> renderable) {
                return false;
            }

            @Override
            public boolean shouldEncodeWithHeaders(Map<String, String> headers) {
                return false;
            }
        };
        ContentEngine contentEngine = mock(ContentEngine.class);
        when(contentEngine.getContentEncodingHelper()).thenReturn(encodingHelper);
        when(contentEngine.getContentSerializerForContentType(anyString())).thenReturn(serializer);

        // Configure the server.
        server = new WisdomVertxServer();
        server.configuration = configuration;
        server.accessor = new ServiceAccessor(
                crypto,
                configuration,
                router,
                contentEngine,
                null,
                null
        );
        server.vertx = vertx;
        return router;
    }



    private List<Integer> success = new ArrayList<>();
    private List<Integer> failure = new ArrayList<>();

    public synchronized void success(int id) {
        success.add(id);
    }

    public synchronized void fail(int id) {
        failure.add(id);
    }

    private class LoggedClient implements Runnable {
        private final CountDownLatch startSignal;
        private final CountDownLatch doneSignal;
        private final int port;
        private final int id;
        private final boolean noCheck;

        LoggedClient(CountDownLatch startSignal, CountDownLatch doneSignal, int port, int id, boolean b) {
            this.startSignal = startSignal;
            this.doneSignal = doneSignal;
            this.port = port;
            this.id = id;
            this.noCheck = b;
        }

        public void run() {
            try {
                startSignal.await();
                doWork();
            } catch (Throwable ex) {
                ex.printStackTrace();
                failure.add(id);
                doneSignal.countDown();
            }
        }

        void doWork() throws IOException {
            CloseableHttpClient httpclient = null;
            CloseableHttpResponse response = null;
            try {
                RequestConfig globalConfig = RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.BEST_MATCH)
                        .build();
                httpclient = HttpClients.custom()
                        .setDefaultRequestConfig(globalConfig)
                        .build();
                CookieStore cookieStore = new BasicCookieStore();
                HttpClientContext context = HttpClientContext.create();
                context.setCookieStore(cookieStore);

                HttpGet req1 = new HttpGet("http://localhost:" + port + "/?id=" + id);

                response = httpclient.execute(req1, context);
                String content = EntityUtils.toString(response.getEntity());

                assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
                assertThat(content).isEqualTo("Alright");

                if (! noCheck) {
                    org.apache.http.cookie.Cookie c = context.getCookieStore().getCookies().get(0);
                    assertThat(c.getName()).isEqualTo("my-cookie");
                    assertThat(c.getValue()).isEqualTo(String.valueOf(id));
                }

                // Proceed to the second request.
                HttpGet req2 = new HttpGet("http://localhost:" + port + "/logged");
                response = httpclient.execute(req2, context);
                content = EntityUtils.toString(response.getEntity());

                assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
                assertThat(content).isEqualTo(String.valueOf(id));

                success(id);
                doneSignal.countDown();

            } finally {
                IOUtils.closeQuietly(httpclient);
                IOUtils.closeQuietly(response);
            }
        }
    }
}
