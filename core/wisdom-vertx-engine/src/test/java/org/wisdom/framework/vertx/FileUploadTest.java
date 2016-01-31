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
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.content.ContentEngine;
import org.wisdom.api.exceptions.ExceptionMapper;
import org.wisdom.api.http.*;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.RouteBuilder;
import org.wisdom.api.router.Router;
import org.wisdom.framework.vertx.file.DiskFileUpload;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Check that we file upload works.
 */
public class FileUploadTest extends VertxBaseTest {

    private WisdomVertxServer server;

    @After
    public void tearDown() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }


    @Test
    public void testFileUploadOfSmallFiles() throws InterruptedException, IOException {
        // Prepare the configuration
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getIntegerWithDefault(eq("vertx.http.port"), anyInt())).thenReturn(0);
        when(configuration.getIntegerWithDefault(eq("vertx.https.port"), anyInt())).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.acceptBacklog", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.receiveBufferSize", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.sendBufferSize", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("request.body.max.size", 100 * 1024)).thenReturn(100 * 1024);
        when(configuration.getLongWithDefault("http.upload.disk.threshold", DiskFileUpload.MINSIZE)).thenReturn
                (DiskFileUpload.MINSIZE);
        when(configuration.getLongWithDefault("http.upload.max", -1l)).thenReturn(-1l);
        when(configuration.getStringArray("wisdom.websocket.subprotocols")).thenReturn(new String[0]);
        when(configuration.getStringArray("vertx.websocket-subprotocols")).thenReturn(new String[0]);

        // Prepare the router with a controller
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() throws IOException {
                FileItem item = context().file("upload");
                if (!item.isInMemory()) {
                    return badRequest("In memory expected");
                }
                if (!item.name().equals("my-file.dat")) {
                    return badRequest("broken name");
                }
                if (item.size() != 2048) {
                    return badRequest("broken file");
                }

                if (!context().form().get("comment").get(0).equals("my description")) {
                    return badRequest("broken form");
                }

                final File file = item.toFile();
                if (! file.exists()  && file.length() != 2048) {
                    return badRequest("broken in memory to file handling");
                }

                return ok(item.stream()).as(MimeTypes.BINARY);
            }
        };
        Router router = mock(Router.class);
        Route route = new RouteBuilder().route(HttpMethod.POST)
                .on("/")
                .to(controller, "index");
        when(router.getRouteFor(anyString(), anyString(), any(Request.class))).thenReturn(route);

        ContentEngine contentEngine = getMockContentEngine();

        // Configure the server.
        server = new WisdomVertxServer();
        server.accessor = new ServiceAccessor(
                null,
                configuration,
                router,
                contentEngine,
                executor,
                null,
                Collections.<ExceptionMapper>emptyList()
        );
        server.configuration = configuration;
        server.vertx = vertx;
        server.start();

        VertxHttpServerTest.waitForStart(server);

        // Now start bunch of clients
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(NUMBER_OF_CLIENTS);

        int port = server.httpPort();

        for (int i = 1; i < NUMBER_OF_CLIENTS + 1; ++i) {
            // create and start threads
            clients.execute(new Client(startSignal, doneSignal, port, i, 2048));
        }

        startSignal.countDown();      // let all threads proceed
        if (!doneSignal.await(60, TimeUnit.SECONDS)) { // wait for all to finish
            Assert.fail("testFileUploadOfSmallFiles - Client not served in time");
        }

        assertThat(failure).isEmpty();
        assertThat(success).hasSize(NUMBER_OF_CLIENTS);
    }

    @Test
    public void testFileUploadOfSmallFilesOnDisk() throws InterruptedException, IOException {
        // Prepare the configuration
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getIntegerWithDefault(eq("vertx.http.port"), anyInt())).thenReturn(0);
        when(configuration.getIntegerWithDefault(eq("vertx.https.port"), anyInt())).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.acceptBacklog", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.receiveBufferSize", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.sendBufferSize", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("request.body.max.size", 100 * 1024)).thenReturn(100 * 1024);
        when(configuration.getStringArray("wisdom.websocket.subprotocols")).thenReturn(new String[0]);
        when(configuration.getStringArray("vertx.websocket-subprotocols")).thenReturn(new String[0]);
        // Reduce it to force disk storage
        when(configuration.getLongWithDefault("http.upload.disk.threshold", DiskFileUpload.MINSIZE)).thenReturn
                (1024l);
        when(configuration.getLongWithDefault("http.upload.max", -1l)).thenReturn(-1l);

        // Prepare the router with a controller
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() throws IOException {
                FileItem item = context().file("upload");
                if (item.isInMemory()) {
                    return badRequest("on disk expected");
                }
                if (!item.name().equals("my-file.dat")) {
                    return badRequest("broken name");
                }
                if (item.size() != 2048) {
                    return badRequest("broken file");
                }

                if (!context().form().get("comment").get(0).equals("my description")) {
                    return badRequest("broken form");
                }

                final File file = item.toFile();
                if (! file.exists()  && file.length() != 2048) {
                    return badRequest("broken file to file handling");
                }

                return ok(item.stream()).as(MimeTypes.BINARY);
            }
        };
        Router router = mock(Router.class);
        Route route = new RouteBuilder().route(HttpMethod.POST)
                .on("/")
                .to(controller, "index");
        when(router.getRouteFor(anyString(), anyString(), any(Request.class))).thenReturn(route);

        ContentEngine contentEngine = getMockContentEngine();

        // Configure the server.
        server = new WisdomVertxServer();
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
        server.configuration = configuration;
        server.start();

        VertxHttpServerTest.waitForStart(server);

        // Now start bunch of clients
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(NUMBER_OF_CLIENTS);

        int port = server.httpPort();

        for (int i = 1; i < NUMBER_OF_CLIENTS + 1; ++i) {
            clients.submit(new Client(startSignal, doneSignal, port, i, 2048));
        }

        startSignal.countDown();      // let all threads proceed
        if (!doneSignal.await(120, TimeUnit.SECONDS)) { // wait for all to finish
            Assert.fail("testFileUploadOfSmallFilesOnDisk - Did not server all requests in time");
        }

        assertThat(failure).isEmpty();
        assertThat(success).hasSize(NUMBER_OF_CLIENTS);
    }

    @Test
    public void testFileUploadOfSmallFilesWithAsyncDownload() throws InterruptedException, IOException {
        // Prepare the configuration
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getIntegerWithDefault(eq("vertx.http.port"), anyInt())).thenReturn(0);
        when(configuration.getIntegerWithDefault(eq("vertx.https.port"), anyInt())).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.acceptBacklog", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.receiveBufferSize", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.sendBufferSize", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("request.body.max.size", 100 * 1024)).thenReturn(100 * 1024);
        when(configuration.getLongWithDefault("http.upload.disk.threshold", DiskFileUpload.MINSIZE)).thenReturn
                (DiskFileUpload.MINSIZE);
        when(configuration.getLongWithDefault("http.upload.max", -1l)).thenReturn(-1l);
        when(configuration.getStringArray("wisdom.websocket.subprotocols")).thenReturn(new String[0]);
        when(configuration.getStringArray("vertx.websocket-subprotocols")).thenReturn(new String[0]);

        // Prepare the router with a controller
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() {
                final FileItem item = context().file("upload");
                if (!item.isInMemory()) {
                    return badRequest("In memory expected");
                }
                if (!item.name().equals("my-file.dat")) {
                    return badRequest("broken name");
                }
                if (item.size() != 2048) {
                    return badRequest("broken file");
                }

                if (!context().form().get("comment").get(0).equals("my description")) {
                    return badRequest("broken form");
                }

                return async(new Callable<Result>() {
                    @Override
                    public Result call() throws Exception {
                        return ok(item.stream()).as(MimeTypes.BINARY);
                    }
                });
            }
        };
        Router router = mock(Router.class);
        Route route = new RouteBuilder().route(HttpMethod.POST)
                .on("/")
                .to(controller, "index");
        when(router.getRouteFor(anyString(), anyString(), any(Request.class))).thenReturn(route);

        ContentEngine contentEngine = getMockContentEngine();

        // Configure the server.
        server = new WisdomVertxServer();
        server.accessor = new ServiceAccessor(
                null,
                configuration,
                router,
                contentEngine,
                executor,
                null,
                Collections.<ExceptionMapper>emptyList()
        );
        server.configuration = configuration;
        server.vertx = vertx;
        server.start();

        VertxHttpServerTest.waitForStart(server);

        // Now start bunch of clients
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(NUMBER_OF_CLIENTS);

        int port = server.httpPort();

        for (int i = 1; i < NUMBER_OF_CLIENTS + 1; ++i) // create and start threads
            clients.submit(new Client(startSignal, doneSignal, port, i, 2048));

        startSignal.countDown();      // let all threads proceed
        if (!doneSignal.await(60, TimeUnit.SECONDS)) { // wait for all to finish
            Assert.fail("testFileUploadOfSmallFilesWithAsyncDownload - Client not served in time");
        }
        assertThat(failure).isEmpty();
        assertThat(success).hasSize(NUMBER_OF_CLIENTS);
    }


    @Test
    public void testThatFileUpdateFailedWhenTheFileExceedTheConfiguredSize() throws InterruptedException, IOException {
        // Prepare the configuration
        ApplicationConfiguration configuration = mock(ApplicationConfiguration.class);
        when(configuration.getIntegerWithDefault(eq("vertx.http.port"), anyInt())).thenReturn(0);
        when(configuration.getIntegerWithDefault(eq("vertx.https.port"), anyInt())).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.acceptBacklog", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.receiveBufferSize", -1)).thenReturn(-1);
        when(configuration.getIntegerWithDefault("vertx.sendBufferSize", -1)).thenReturn(-1);
        when(configuration.getLongWithDefault("http.upload.disk.threshold", DiskFileUpload.MINSIZE)).thenReturn
                (DiskFileUpload.MINSIZE);
        when(configuration.getLongWithDefault("http.upload.max", -1l)).thenReturn(10l); // 10 bytes max
        when(configuration.getStringArray("wisdom.websocket.subprotocols")).thenReturn(new String[0]);
        when(configuration.getStringArray("vertx.websocket-subprotocols")).thenReturn(new String[0]);

        // Prepare the router with a controller
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() {
                return ok();
            }
        };
        Router router = mock(Router.class);
        Route route = new RouteBuilder().route(HttpMethod.POST)
                .on("/")
                .to(controller, "index");
        when(router.getRouteFor(anyString(), anyString(), any(Request.class))).thenReturn(route);

        ContentEngine contentEngine = getMockContentEngine();

        // Configure the server.
        server = new WisdomVertxServer();
        server.accessor = new ServiceAccessor(
                null,
                configuration,
                router,
                contentEngine,
                executor,
                null,
                Collections.<ExceptionMapper>emptyList()
        );
        server.configuration = configuration;
        server.vertx = vertx;
        server.start();

        VertxHttpServerTest.waitForStart(server);

        int port = server.httpPort();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost post = new HttpPost("http://localhost:" + port + "/?id=" + 1);

        ByteArrayBody body = new ByteArrayBody("this is too much...".getBytes(), "my-file.dat");
        StringBody description = new StringBody("my description", ContentType.TEXT_PLAIN);
        HttpEntity entity = MultipartEntityBuilder.create()
                .addPart("upload", body)
                .addPart("comment", description)
                .build();

        post.setEntity(entity);

        CloseableHttpResponse response = httpclient.execute(post);
        // We should receive a Payload too large response (413)
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(413);

    }

    private class Client implements Runnable {
        private final CountDownLatch startSignal;
        private final CountDownLatch doneSignal;
        private final int port;
        private final int id;
        private final int size;

        Client(CountDownLatch startSignal, CountDownLatch doneSignal, int port, int id, int size) {
            this.startSignal = startSignal;
            this.doneSignal = doneSignal;
            this.port = port;
            this.id = id;
            this.size = size;
        }

        public void run() {
            try {
                startSignal.await();
                doWork();
            } catch (Throwable ex) {
                fail(id);
            } finally {
                doneSignal.countDown();
            }
        }

        void doWork() throws IOException {

            final byte[] data = new byte[size];
            RANDOM.nextBytes(data);

            CloseableHttpClient httpclient = null;
            CloseableHttpResponse response = null;
            try {
                httpclient = HttpClients.createDefault();
                HttpPost post = new HttpPost("http://localhost:" + port + "/?id=" + id);

                ByteArrayBody body = new ByteArrayBody(data, "my-file.dat");
                StringBody description = new StringBody("my description", ContentType.TEXT_PLAIN);
                HttpEntity entity = MultipartEntityBuilder.create()
                        .addPart("upload", body)
                        .addPart("comment", description)
                        .build();

                post.setEntity(entity);

                response = httpclient.execute(post);
                byte[] content = EntityUtils.toByteArray(response.getEntity());

                if (!isOk(response)) {
                    System.err.println("Invalid response code for " + id + " got " + response.getStatusLine()
                            .getStatusCode() + " " + new String(content));
                    fail(id);
                    return;
                }

                if (!containsExactly(content, data)) {
                    System.err.println("Invalid content for " + id);
                    fail(id);
                    return;
                }

                success(id);

            } finally {
                IOUtils.closeQuietly(httpclient);
                IOUtils.closeQuietly(response);
            }
        }
    }

}
