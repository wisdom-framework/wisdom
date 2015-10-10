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

import com.google.common.collect.ImmutableList;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.streams.Pump;
import org.junit.After;
import org.junit.Test;
import org.wisdom.api.concurrent.ManagedExecutorService;
import org.wisdom.executors.ManagedExecutorServiceImpl;
import org.wisdom.executors.context.HttpExecutionContextService;
import org.wisdom.executors.context.TCCLExecutionContextService;
import org.wisdom.test.parents.FakeConfiguration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class AsyncISTest {

    ManagedExecutorService executor = new ManagedExecutorServiceImpl("test",
            new FakeConfiguration(Collections.<String, Object>emptyMap()),
            // Generic argument required in Maven, don't know really why.
            ImmutableList.of(new HttpExecutionContextService(), new TCCLExecutionContextService()));

    // 1 MB random bytes
    int size = 1024 * 1024;
    byte[] content = new byte[size];
    private HttpServer server;
    private Vertx vertx;

    {
        new Random().nextBytes(content);
    }

    @After
    public void stop() {
        executor.shutdownNow();
        if (server != null) {
            server.close();
        }
        if (vertx != null) {
            vertx.close();
        }

    }

    @Test
    public void testWithHttpServer() throws Exception {
        vertx = Vertx.vertx();
        final CountDownLatch latch = new CountDownLatch(1);
        vertx.runOnContext(v -> {
            server = vertx.createHttpServer().requestHandler(event -> {
                AsyncInputStream in = new AsyncInputStream(vertx, executor, new ByteArrayInputStream(content));
                final HttpServerResponse response = event.response();
                response.setStatusCode(200);
                response.setChunked(true);
                response.putHeader("Content-Type", "application/octet-stream");
                in.endHandler(event1 -> response.end());
                Pump.pump(in, response).start();
            }).listen(10002, "localhost", event -> {
                if (event.succeeded()) {
                    latch.countDown();
                }
                //Else, Let latch elapse and make test fail
            });
        });
        assertTrue(latch.await(30, TimeUnit.SECONDS));

        URL url = new URL("http://localhost:10002/");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = url.openConnection().getInputStream();
        byte[] buffer = new byte[512];
        while (true) {
            int amount = in.read(buffer);
            if (amount == -1) {
                break;
            } else {
                out.write(buffer, 0, amount);
            }
        }
        byte[] received = out.toByteArray();
        assertArrayEquals(content, received);
    }

    @Test
    public void testPumpWithBoundedWriteStream() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        vertx = Vertx.vertx();
        final AsyncInputStream in = new AsyncInputStream(
                vertx,
                executor,
                new ByteArrayInputStream(content),
                512);
        final BoundedWriteStream buffer = new BoundedWriteStream(1024);

        vertx.runOnContext(event -> {
            Pump.pump(in, buffer).start();
        });

        while (AsyncInputStream.STATUS_PAUSED != in.getState()) {
            sleep(1);
        }
        byte[] data = buffer.drain();
        assertData(data, 0);

        while (AsyncInputStream.STATUS_PAUSED != in.getState()) {
            sleep(1);
        }
        data = buffer.drain();
        assertData(data, 1024);
        assertEquals(1024, data.length);
        latch.countDown();
        latch.await(30, TimeUnit.SECONDS);
    }

    private void assertData(byte[] data, int offset) {
        byte[] expected = new byte[data.length];
        System.arraycopy(content, offset, expected, 0, expected.length);
        assertArrayEquals(expected, data);
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
