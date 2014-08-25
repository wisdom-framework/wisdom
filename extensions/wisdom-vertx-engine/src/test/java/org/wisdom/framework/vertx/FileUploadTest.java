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

import akka.actor.ActorSystem;
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
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultVertxFactory;
import org.wisdom.akka.AkkaSystemService;
import org.wisdom.akka.impl.HttpExecutionContext;
import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.content.ContentEncodingHelper;
import org.wisdom.api.content.ContentEngine;
import org.wisdom.api.http.*;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.RouteBuilder;
import org.wisdom.api.router.Router;
import org.wisdom.framework.vertx.file.DiskFileUpload;
import scala.concurrent.Future;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Check that we file upload works.
 */
public class FileUploadTest {

    private WisdomVertxServer server;

    ActorSystem actor = ActorSystem.create();

    DefaultVertxFactory factory = new DefaultVertxFactory();
    Vertx vertx = factory.createVertx();

    private List<Integer> success = new ArrayList<>();
    private List<Integer> failure = new ArrayList<>();
    private AkkaSystemService system;

    final Random random = new Random();

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        system = mock(AkkaSystemService.class);
        when(system.system()).thenReturn(actor);
        when(system.fromThread()).thenReturn(new HttpExecutionContext(actor.dispatcher(), Context.CONTEXT.get(),
                Thread.currentThread().getContextClassLoader()));
        doAnswer(new Answer<Future<Result>>() {
            @Override
            public Future<Result> answer(InvocationOnMock invocation) throws Throwable {
                Callable<Result> callable = (Callable<Result>) invocation.getArguments()[0];
                Context context = (Context) invocation.getArguments()[1];

                return akka.dispatch.Futures.future(callable,
                        new HttpExecutionContext(actor.dispatcher(), context,
                                Thread.currentThread().getContextClassLoader()));
            }
        }).when(system).dispatchResultWithContext(any(Callable.class), any(Context.class));
    }

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

        actor.shutdown();
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
        when(configuration.getLongWithDefault("http.upload.disk.threshold", DiskFileUpload.MINSIZE)).thenReturn
                (DiskFileUpload.MINSIZE);
        when(configuration.getLongWithDefault("http.upload.max", -1l)).thenReturn(-1l);

        // Prepare the router with a controller
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() {
                FileItem item = context().file("upload");
                if (! item.isInMemory()) {
                    return badRequest("In memory expected");
                }
                if (! item.name().equals("my-file.dat")) {
                    return badRequest("broken name");
                }
                if (item.size() != 2048) {
                    return badRequest("broken file");
                }

                if (! context().form().get("comment").get(0).equals("my description")) {
                    return badRequest("broken form");
                }

                return ok(item.stream()).as(MimeTypes.BINARY);
            }
        };
        Router router = mock(Router.class);
        Route route = new RouteBuilder().route(HttpMethod.POST)
                .on("/")
                .to(controller, "index");
        when(router.getRouteFor("POST", "/")).thenReturn(route);

        ContentEncodingHelper encodingHelper = new ContentEncodingHelper() {

            @Override
            public List<String> parseAcceptEncodingHeader(String headerContent) {
                return new ArrayList<>();
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

        // Configure the server.
        server = new WisdomVertxServer();
        server.accessor = new ServiceAccessor(
                null,
                configuration,
                router,
                contentEngine,
                system,
                null
        );
        server.configuration = configuration;
        server.vertx = vertx;
        server.start();

        VertxHttpServerTest.waitForStart(server);

        // Now start bunch of clients
        int num = 100;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(num);

        int port = server.httpPort();

        for (int i = 1; i < num + 1; ++i) {
            // create and start threads
            new Thread(new Client(startSignal, doneSignal, port, i, 2048)).start();
        }

        startSignal.countDown();      // let all threads proceed
        doneSignal.await(60, TimeUnit.SECONDS);           // wait for all to finish

        assertThat(failure).isEmpty();
        assertThat(success).hasSize(num);
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
        // Reduce it to force disk storage
        when(configuration.getLongWithDefault("http.upload.disk.threshold", DiskFileUpload.MINSIZE)).thenReturn
                (1024l);
        when(configuration.getLongWithDefault("http.upload.max", -1l)).thenReturn(-1l);

        // Prepare the router with a controller
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() {
                FileItem item = context().file("upload");
                if (item.isInMemory()) {
                    return badRequest("on disk expected");
                }
                if (! item.name().equals("my-file.dat")) {
                    return badRequest("broken name");
                }
                if (item.size() != 2048) {
                    return badRequest("broken file");
                }

                if (! context().form().get("comment").get(0).equals("my description")) {
                    return badRequest("broken form");
                }

                return ok(item.stream()).as(MimeTypes.BINARY);
            }
        };
        Router router = mock(Router.class);
        Route route = new RouteBuilder().route(HttpMethod.POST)
                .on("/")
                .to(controller, "index");
        when(router.getRouteFor("POST", "/")).thenReturn(route);

        ContentEncodingHelper encodingHelper = new ContentEncodingHelper() {

            @Override
            public List<String> parseAcceptEncodingHeader(String headerContent) {
                return new ArrayList<>();
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

        // Configure the server.
        server = new WisdomVertxServer();
        server.accessor = new ServiceAccessor(
                null,
                configuration,
                router,
                contentEngine,
                system,
                null
        );
        server.vertx = vertx;
        server.configuration = configuration;
        server.start();

        VertxHttpServerTest.waitForStart(server);

        // Now start bunch of clients
        int num = 100;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(num);

        int port = server.httpPort();

        for (int i = 1; i < num + 1; ++i) {
            new Thread(new Client(startSignal, doneSignal, port, i, 2048)).start();
        }

        startSignal.countDown();      // let all threads proceed
        if (!doneSignal.await(120, TimeUnit.SECONDS)) { // wait for all to finish
            Assert.fail("Did not server all requests in time");
        }

        assertThat(failure).isEmpty();
        assertThat(success).hasSize(num);
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
        when(configuration.getLongWithDefault("http.upload.disk.threshold", DiskFileUpload.MINSIZE)).thenReturn
                (DiskFileUpload.MINSIZE);
        when(configuration.getLongWithDefault("http.upload.max", -1l)).thenReturn(-1l);

        // Prepare the router with a controller
        Controller controller = new DefaultController() {
            @SuppressWarnings("unused")
            public Result index() {
                final FileItem item = context().file("upload");
                if (! item.isInMemory()) {
                    return badRequest("In memory expected");
                }
                if (! item.name().equals("my-file.dat")) {
                    return badRequest("broken name");
                }
                if (item.size() != 2048) {
                    return badRequest("broken file");
                }

                if (! context().form().get("comment").get(0).equals("my description")) {
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
        when(router.getRouteFor("POST", "/")).thenReturn(route);

        ContentEncodingHelper encodingHelper = new ContentEncodingHelper() {

            @Override
            public List<String> parseAcceptEncodingHeader(String headerContent) {
                return new ArrayList<>();
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

        // Configure the server.
        server = new WisdomVertxServer();
        server.accessor = new ServiceAccessor(
                null,
                configuration,
                router,
                contentEngine,
                system,
                null
        );
        server.configuration = configuration;
        server.vertx = vertx;
        server.start();

        VertxHttpServerTest.waitForStart(server);

        // Now start bunch of clients
        int num = 100;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(num);

        int port = server.httpPort();

        for (int i = 1; i < num + 1; ++i) // create and start threads
            new Thread(new Client(startSignal, doneSignal, port, i, 2048)).start();

        startSignal.countDown();      // let all threads proceed
        doneSignal.await(60, TimeUnit.SECONDS);           // wait for all to finish

        assertThat(failure).isEmpty();
        assertThat(success).hasSize(num);
    }

    public synchronized void success(int id) {
        success.add(id);
    }

    public synchronized void fail(int id) {
        failure.add(id);
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
                success(id);
            } catch (Throwable ex) {
                ex.printStackTrace();
                fail(id);
            } finally {
                doneSignal.countDown();
            }
        }

        void doWork() throws IOException {

            final byte[] data = new byte[size];
            random.nextBytes(data);

            CloseableHttpClient httpclient = null;
            CloseableHttpResponse response = null;
            try {
                httpclient = HttpClients.createDefault();
                HttpPost post = new HttpPost("http://localhost:" + port + "/");

                ByteArrayBody body = new ByteArrayBody(data, "my-file.dat");
                StringBody description = new StringBody("my description", ContentType.TEXT_PLAIN);
                HttpEntity entity = MultipartEntityBuilder.create()
                        .addPart("upload", body)
                        .addPart("comment", description)
                        .build();

                post.setEntity(entity);

                response = httpclient.execute(post);
                byte[] content = EntityUtils.toByteArray(response.getEntity());

                assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
                assertThat(content).containsExactly(data);

            } finally {
                IOUtils.closeQuietly(httpclient);
                IOUtils.closeQuietly(response);
            }
        }
    }

}
