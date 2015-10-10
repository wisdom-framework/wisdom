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
import org.apache.http.HttpResponse;
import org.junit.After;
import org.wisdom.api.concurrent.ExecutionContextService;
import org.wisdom.api.concurrent.ManagedExecutorService;
import org.wisdom.api.content.ContentEngine;
import org.wisdom.api.content.ContentSerializer;
import org.wisdom.api.http.Renderable;
import org.wisdom.executors.ManagedExecutorServiceImpl;
import org.wisdom.executors.context.HttpExecutionContextService;
import org.wisdom.framework.vertx.ssl.SSLServerContext;
import org.wisdom.test.parents.FakeConfiguration;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Utility methods used in test.
 */
public class VertxBaseTest {

    public static final int NUMBER_OF_CLIENTS;

    public static final Random RANDOM;

    static {
        NUMBER_OF_CLIENTS = Integer.getInteger("vertx.test.clients", 10);
        RANDOM = new Random();
    }

    protected ManagedExecutorService executor = new ManagedExecutorServiceImpl("test",
            new FakeConfiguration(Collections.<String, Object>emptyMap()),
            ImmutableList.<ExecutionContextService>of(new HttpExecutionContextService()));

    Vertx vertx = Vertx.vertx();

    ExecutorService clients = Executors.newFixedThreadPool(NUMBER_OF_CLIENTS);

    List<Integer> success = new ArrayList<>();
    List<Integer> failure = new ArrayList<>();

    public static void waitForStart(WisdomVertxServer server) throws InterruptedException, IOException {
        int attempt = 0;
        while (server.httpPort() == 0 && attempt < 10) {
            Thread.sleep(1000);
            attempt++;
        }
        if (server.httpPort() == 0) {
            throw new IllegalStateException("Server not started after " + attempt + " attempts");
        }

        // No one is publishing /ping, so we are expected to get a 404.
        // Before we was trying on / but test are expecting parameters.
        URL url = new URL("http://localhost:" + server.httpPort() + "/ping");
        attempt = 0;
        int code = 0;
        while (code == 0  && attempt < 10) {
            try {
                Thread.sleep(1000);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                code = connection.getResponseCode();
                if (code != 0) {
                    System.out.println("Server started (code: " + code + ")");
                }
            } catch (IOException e) {
                // Next try...
            }
            attempt++;
        }

        if (code == 0) {
            throw new IllegalStateException("Server not ready after " + attempt + " attempts");
        }
    }

    public static void waitForHttpsStart(WisdomVertxServer server) throws InterruptedException {
        int attempt = 0;
        while (server.httpsPort() == 0 && attempt < 10) {
            Thread.sleep(1000);
            attempt++;
        }
    }

    public static boolean isOk(HttpResponse response) {
        return response != null  && response.getStatusLine().getStatusCode() == 200;
    }

    public static boolean isOk(int status) {
        return status == 200;
    }

    public static boolean containsExactly(byte[] content, byte[] expected) {
        if (content.length != expected.length) {
            return false;
        }
        for (int i = 0; i < content.length; i++) {
            if (content[i] != expected[i]) {
                return false;
            }
        }
        return true;
    }

    @After
    public void stopping() {
        if (vertx != null) {
            vertx.close();
        }

        failure.clear();
        success.clear();

        executor.shutdownNow();

        // Reset SSL Context
        SSLServerContext.reset();
    }

    public synchronized void success(int id) {
        success.add(id);
    }

    public synchronized void fail(int id) {
        failure.add(id);
    }



    static ContentEngine getMockContentEngine() {
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
        ContentEngine contentEngine = mock(ContentEngine.class);
        when(contentEngine.getContentSerializerForContentType(anyString())).thenReturn(serializer);
        return contentEngine;
    }
}
