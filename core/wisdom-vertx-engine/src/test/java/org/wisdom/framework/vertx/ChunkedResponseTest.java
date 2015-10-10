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

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientOptions;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Test;
import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.content.ContentEngine;
import org.wisdom.api.exceptions.ExceptionMapper;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Request;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.RouteBuilder;
import org.wisdom.api.router.Router;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Check that we generate correct chunked responses.
 */
public class ChunkedResponseTest extends VertxBaseTest {

    private WisdomVertxServer server;

    @After
    public void tearDown() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }


    @Test
    public void testChunkedResponses() throws InterruptedException, IOException {
        // Prepare the configuration
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getIntegerWithDefault(eq("vertx.http.port"), anyInt())).thenReturn(0);
        when(configuration.getIntegerWithDefault(eq("vertx.https.port"), anyInt())).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.acceptBacklog", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.receiveBufferSize", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.sendBufferSize", -1)).thenReturn(-1);
        when(configuration.getStringArray("wisdom.websocket.subprotocols")).thenReturn(new String[0]);
        when(configuration.getStringArray("vertx.websocket-subprotocols")).thenReturn(new String[0]);

        // Prepare the router with a controller
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() {
                int count = context().parameterAsInteger("id") * 1000;
                byte[] content = new byte[count];
                RANDOM.nextBytes(content);
                return ok(new ByteArrayInputStream(content));
            }
        };
        Router router = mock(Router.class);
        Route route = new RouteBuilder().route(HttpMethod.GET)
                .on("/")
                .to(controller, "index");
        when(router.getRouteFor(anyString(), anyString(), any(Request.class))).thenReturn(route);

        ContentEngine contentEngine = getMockContentEngine();

        // Configure the server.
        server = new WisdomVertxServer();
        server.configuration = configuration;
        server.accessor = new ServiceAccessor(
                null,
                configuration,
                router,
                contentEngine,
                executor,
                null,
                Collections.<ExceptionMapper>emptyList()
        );
        server.vertx = vertx;
        server.start();

        VertxHttpServerTest.waitForStart(server);

        // Now start bunch of clients
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(NUMBER_OF_CLIENTS);

        int port = server.httpPort();

        for (int i = 1; i < NUMBER_OF_CLIENTS + 1; ++i) // create and start threads
            executor.submit(new Client(startSignal, doneSignal, port, i));

        startSignal.countDown();      // let all threads proceed
        doneSignal.await(60, TimeUnit.SECONDS);           // wait for all to finish

        assertThat(failure).isEmpty();
        assertThat(success).hasSize(NUMBER_OF_CLIENTS);
    }

    @Test
    public void testAsyncChunkedResponses() throws InterruptedException, IOException {
        // Prepare the configuration
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getIntegerWithDefault(eq("vertx.http.port"), anyInt())).thenReturn(0);
        when(configuration.getIntegerWithDefault(eq("vertx.https.port"), anyInt())).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.acceptBacklog", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.receiveBufferSize", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.sendBufferSize", -1)).thenReturn(-1);
        when(configuration.getStringArray("wisdom.websocket.subprotocols")).thenReturn(new String[0]);
        when(configuration.getStringArray("vertx.websocket-subprotocols")).thenReturn(new String[0]);

        // Prepare the router with a controller
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() {
                return async(() -> {
                    int count = context().parameterAsInteger("id") * 1000;
                    byte[] content = new byte[count];
                    RANDOM.nextBytes(content);
                    return ok(new ByteArrayInputStream(content));
                });
            }
        };
        Router router = mock(Router.class);
        Route route = new RouteBuilder().route(HttpMethod.GET)
                .on("/")
                .to(controller, "index");
        when(router.getRouteFor(anyString(), anyString(), any(Request.class))).thenReturn(route);

        // Configure the server.
        server = new WisdomVertxServer();
        server.configuration = configuration;
        server.accessor = new ServiceAccessor(
                null,
                configuration,
                router,
                getMockContentEngine(),
                executor,
                null,
                Collections.<ExceptionMapper>emptyList()
        );
        server.vertx = vertx;
        server.start();

        VertxHttpServerTest.waitForStart(server);

        // Now start bunch of clients
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(NUMBER_OF_CLIENTS);

        int port = server.httpPort();

        for (int i = 1; i < NUMBER_OF_CLIENTS + 1; ++i) // create and start threads
            executor.submit(new Client(startSignal, doneSignal, port, i));

        startSignal.countDown();      // let all threads proceed
        doneSignal.await(60, TimeUnit.SECONDS);           // wait for all to finish

        assertThat(failure).isEmpty();
        assertThat(success).hasSize(NUMBER_OF_CLIENTS);
    }

    @Test
    public void testZippedFileDownload() throws InterruptedException, IOException {

        // Prepare the configuration
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getIntegerWithDefault(eq("vertx.http.port"), anyInt())).thenReturn(0);
        when(configuration.getIntegerWithDefault(eq("vertx.https.port"), anyInt())).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.acceptBacklog", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.receiveBufferSize", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.sendBufferSize", -1)).thenReturn(-1);
        when(configuration.getStringArray("wisdom.websocket.subprotocols")).thenReturn(new String[0]);
        when(configuration.getStringArray("vertx.websocket-subprotocols")).thenReturn(new String[0]);

        // Prepare the router with a controller
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() throws IOException {
                ZipFile zip = new ZipFile("src/test/resources/owl.png.zip");
                ZipEntry entry = zip.getEntry("owl.png");
                InputStream stream = zip.getInputStream(entry);
                return ok(stream);
            }
        };

        Router router = mock(Router.class);
        final Route route = new RouteBuilder().route(HttpMethod.GET)
                .on("/")
                .to(controller, "index");
        doAnswer(invocationOnMock -> route).when(router).getRouteFor(anyString(), anyString(), any(Request.class));

        // Configure the server.
        server = new WisdomVertxServer();
        server.configuration = configuration;
        server.accessor = new ServiceAccessor(
                null,
                configuration,
                router,
                getMockContentEngine(),
                executor,
                null,
                Collections.<ExceptionMapper>emptyList()
        );
        server.vertx = vertx;

        server.start();

        VertxHttpServerTest.waitForStart(server);

        // Now start bunch of clients
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(NUMBER_OF_CLIENTS);

        int port = server.httpPort();

        for (int i = 0; i < NUMBER_OF_CLIENTS; ++i) {// create and start threads
            clients.submit(new DownloadClient(startSignal, doneSignal, port, i));
        }

        startSignal.countDown();      // let all threads proceed
        assertThat(doneSignal.await(60, TimeUnit.SECONDS)).isTrue();

        assertThat(failure).isEmpty();
        assertThat(success).hasSize(NUMBER_OF_CLIENTS);
    }

    @Test
    public void testFileDownload() throws InterruptedException, IOException {

        // Prepare the configuration
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getIntegerWithDefault(eq("vertx.http.port"), anyInt())).thenReturn(0);
        when(configuration.getIntegerWithDefault(eq("vertx.https.port"), anyInt())).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.acceptBacklog", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.receiveBufferSize", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.sendBufferSize", -1)).thenReturn(-1);
        when(configuration.getStringArray("wisdom.websocket.subprotocols")).thenReturn(new String[0]);
        when(configuration.getStringArray("vertx.websocket-subprotocols")).thenReturn(new String[0]);

        // Prepare the router with a controller
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() throws IOException {
                File file = new File("src/test/resources/owl.png");
                return ok(file);
            }
        };

        Router router = mock(Router.class);
        final Route route = new RouteBuilder().route(HttpMethod.GET)
                .on("/")
                .to(controller, "index");
        doAnswer(invocationOnMock -> route).when(router).getRouteFor(anyString(), anyString(), any(Request.class));


        // Configure the server.
        server = new WisdomVertxServer();
        server.configuration = configuration;
        server.accessor = new ServiceAccessor(
                null,
                configuration,
                router,
                getMockContentEngine(),
                executor,
                null,
                Collections.<ExceptionMapper>emptyList()
        );
        server.vertx = vertx;

        server.start();

        VertxHttpServerTest.waitForStart(server);

        // Now start bunch of clients
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(NUMBER_OF_CLIENTS);

        int port = server.httpPort();

        for (int i = 0; i < NUMBER_OF_CLIENTS; ++i) // create and start threads
            clients.submit(new DownloadClient(startSignal, doneSignal, port, i));

        startSignal.countDown();      // let all threads proceed
        doneSignal.await(60, TimeUnit.SECONDS);           // wait for all to finish

        assertThat(failure).isEmpty();
        assertThat(success).hasSize(NUMBER_OF_CLIENTS);
    }

    @Test
    public void testFileAsUrlDownload() throws InterruptedException, IOException {

        // Prepare the configuration
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getIntegerWithDefault(eq("vertx.http.port"), anyInt())).thenReturn(0);
        when(configuration.getIntegerWithDefault(eq("vertx.https.port"), anyInt())).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.acceptBacklog", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.receiveBufferSize", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.sendBufferSize", -1)).thenReturn(-1);
        when(configuration.getStringArray("wisdom.websocket.subprotocols")).thenReturn(new String[0]);
        when(configuration.getStringArray("vertx.websocket-subprotocols")).thenReturn(new String[0]);

        // Prepare the router with a controller
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() throws IOException {
                File file = new File("src/test/resources/owl.png");
                return ok(file.toURI().toURL());
            }
        };

        Router router = mock(Router.class);
        final Route route = new RouteBuilder().route(HttpMethod.GET)
                .on("/")
                .to(controller, "index");
        doAnswer(invocationOnMock -> route).when(router).getRouteFor(anyString(), anyString(), any(Request.class));


        // Configure the server.
        server = new WisdomVertxServer();
        server.configuration = configuration;
        server.accessor = new ServiceAccessor(
                null,
                configuration,
                router,
                getMockContentEngine(),
                executor,
                null,
                Collections.<ExceptionMapper>emptyList()
        );
        server.vertx = vertx;

        server.start();

        VertxHttpServerTest.waitForStart(server);

        // Now start bunch of clients
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(NUMBER_OF_CLIENTS);

        int port = server.httpPort();

        for (int i = 0; i < NUMBER_OF_CLIENTS; ++i) {
            // create and start threads
            clients.execute(new DownloadClient(startSignal, doneSignal, port, i));
        }

        startSignal.countDown();      // let all threads proceed

        assertThat(doneSignal.await(60, TimeUnit.SECONDS)).isTrue();

        assertThat(failure).isEmpty();
        assertThat(success).hasSize(NUMBER_OF_CLIENTS);
    }

    private class DownloadClient implements Runnable {
        private final CountDownLatch startSignal;
        private final CountDownLatch doneSignal;
        private final int port;
        private final int id;

        DownloadClient(CountDownLatch startSignal, CountDownLatch doneSignal, int port, int id) {
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
            }
            doneSignal.countDown();
        }

        void doWork() throws IOException {
            URL url;
            if (id % 3 == 0) {
                url = new URL("http://localhost:" + port + "/3");
            } else if (id % 2 == 0) {
                url = new URL("http://localhost:" + port + "/2");
            } else {
                url = new URL("http://localhost:" + port);
            }
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            if (!isOk(connection.getResponseCode())) {
                System.err.println("Bad error code for " + id + " got : " + connection.getResponseCode());
                fail(id);
                return;
            }
            byte[] body = IOUtils.toByteArray(connection.getInputStream());
            final File img = new File("src/test/resources/owl.png");
            byte[] expected = FileUtils.readFileToByteArray(img);

            if (!containsExactly(body, expected)) {
                System.err.println("Bad content for " + id);
                fail(id);
                return;
            }

            success(id);
        }
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
                doneSignal.countDown();
            }
        }

        void doWork() throws IOException {
            HttpClientOptions options = new HttpClientOptions()
                    .setDefaultHost("localhost")
                    .setDefaultPort(port);
            vertx.createHttpClient(options).getNow("/?id=" + id,
                    response -> response.bodyHandler((Handler<Buffer>) data -> {
                        if (!isOk(response.statusCode())) {
                            System.err.println("Bad error code for "
                                    + id + " got : " + response.statusCode());
                            fail(id);
                            return;
                        }

                        if (data.length() != id * 1000) {
                            System.err.println("Bad content for " + id + " got : " + data.length() + " " +
                                    "bytes");
                            fail(id);
                            return;
                        }
                        success(id);
                        doneSignal.countDown();
                    }));
        }
    }

}
