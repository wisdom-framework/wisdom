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
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
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
import org.wisdom.api.http.HeaderNames;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Request;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.RouteBuilder;
import org.wisdom.api.router.Router;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Check the wisdom server behavior about response encoding.
 * This class is listening for http requests on random port.
 */
public class ResponseEncodingTest extends VertxBaseTest {

    private WisdomVertxServer server;
    protected static final String LOREM = "Lorem ipsum dolor sit amet, consectetur adipisicing elit," +
            " sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim" +
            " veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo" +
            " consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolor" +
            "e eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in" +
            " culpa qui officia deserunt mollit anim id est laborum.";


    @After
    public void tearDown() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    private String generate(int length) {
        StringBuilder builder = new StringBuilder();
        while (builder.length() < length) {
            builder.append(LOREM);
        }
        return builder.toString();
    }

    @Test
    public void testEncodingOfResponse() throws InterruptedException, IOException {
        Router router = prepareServer();

        byte[] content = generate(1000).getBytes();

        // Prepare the router with a controller
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() {
                return ok(content, false);
            }
        };
        final Route route1 = new RouteBuilder().route(HttpMethod.GET)
                .on("/")
                .to(controller, "index");
        configureRouter(router, route1);

        server.start();
        waitForStart(server);

        // Now start bunch of clients
        int num = NUMBER_OF_CLIENTS;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(num);

        int port = server.httpPort();

        for (int i = 0; i < num; ++i) {// create and start threads
            executor.submit(new Client(startSignal, doneSignal, port, i, content, "gzip"));
        }

        startSignal.countDown();      // let all threads proceed
        assertThat(doneSignal.await(60, TimeUnit.SECONDS)).isTrue(); // wait for all to finish

        assertThat(failure).isEmpty();
        assertThat(success).hasSize(num);
    }

    @Test
    public void testThatSmallResponsesAreNotEncoded() throws InterruptedException, IOException {
        Router router = prepareServer();

        byte[] content = generate(100).getBytes();

        // Prepare the router with a controller
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() {
                return ok(content, false);
            }
        };
        final Route route1 = new RouteBuilder().route(HttpMethod.GET)
                .on("/")
                .to(controller, "index");
        configureRouter(router, route1);

        server.start();
        waitForStart(server);

        // Now start bunch of clients
        int num = NUMBER_OF_CLIENTS;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(num);

        int port = server.httpPort();

        for (int i = 0; i < num; ++i) {// create and start threads
            executor.submit(new Client(startSignal, doneSignal, port, i, content, null));
        }

        startSignal.countDown();      // let all threads proceed
        assertThat(doneSignal.await(60, TimeUnit.SECONDS)).isTrue(); // wait for all to finish

        assertThat(failure).isEmpty();
        assertThat(success).hasSize(num);
    }

    @Test
    public void testThatLargeResponsesAreNotEncoded() throws InterruptedException, IOException {
        Router router = prepareServer();

        byte[] content = generate(2000).getBytes();

        // Prepare the router with a controller
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() {
                return ok(content, false);
            }
        };
        final Route route1 = new RouteBuilder().route(HttpMethod.GET)
                .on("/")
                .to(controller, "index");
        configureRouter(router, route1);

        server.start();
        waitForStart(server);

        // Now start bunch of clients
        int num = NUMBER_OF_CLIENTS;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(num);

        int port = server.httpPort();

        for (int i = 0; i < num; ++i) {// create and start threads
            executor.submit(new Client(startSignal, doneSignal, port, i, content, null));
        }

        startSignal.countDown();      // let all threads proceed
        assertThat(doneSignal.await(60, TimeUnit.SECONDS)).isTrue(); // wait for all to finish

        assertThat(failure).isEmpty();
        assertThat(success).hasSize(num);
    }

    private void configureRouter(Router router, Route root) {
        doAnswer(invocationOnMock -> {
            String path = (String) invocationOnMock.getArguments()[1];
            if (path.equals("/")) {
                return root;
            }
            return null;
        }).when(router).getRouteFor(anyString(), anyString(), any(Request.class));
    }

    private Router prepareServer() {
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getBooleanWithDefault("vertx.compression", true)).thenReturn(true);
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
        when(configuration.getBytes(ApplicationConfiguration.ENCODING_MIN_SIZE,
                ApplicationConfiguration.DEFAULT_ENCODING_MIN_SIZE)).thenReturn(800L);
        when(configuration.getBytes(ApplicationConfiguration.ENCODING_MAX_SIZE,
                ApplicationConfiguration.DEFAULT_ENCODING_MAX_SIZE)).thenReturn(1500L);

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

    private class Client implements Runnable {
        private final CountDownLatch startSignal;
        private final CountDownLatch doneSignal;
        private final int port;
        private final int id;
        private byte[] content;
        private String encoding;

        Client(CountDownLatch startSignal, CountDownLatch doneSignal, int port, int id,
               byte[] content, String expectedEncoding) {
            this.startSignal = startSignal;
            this.doneSignal = doneSignal;
            this.port = port;
            this.id = id;
            this.content = content;
            this.encoding = expectedEncoding;
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
                httpclient = HttpClientBuilder.create()
                        .disableContentCompression()
                        .build();
                HttpClientContext context = HttpClientContext.create();

                final String uri = "http://localhost:" + port + "/?id=" + id;
                HttpGet req1 = new HttpGet(uri);
                req1.setHeader(HeaderNames.ACCEPT_ENCODING, "gzip, deflate, sdch");

                response = httpclient.execute(req1, context);
                byte[] content = EntityUtils.toByteArray(response.getEntity());

                assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
                System.out.println(Arrays.toString(response.getAllHeaders()));
                if (encoding == null) {
                    assertThat(response.getFirstHeader(HeaderNames.CONTENT_ENCODING)).isNull();
                    assertThat(content.length).isEqualTo(this.content.length);
                } else {
                    assertThat(response.getFirstHeader(HeaderNames.CONTENT_ENCODING).getValue())
                            .isEqualTo(encoding);
                    assertThat(content.length).isLessThan(this.content.length);
                }

                success(id);

            } finally {
                IOUtils.closeQuietly(httpclient);
                IOUtils.closeQuietly(response);
            }
        }
    }
}
