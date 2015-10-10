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
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import org.junit.Test;
import org.wisdom.api.concurrent.ManagedExecutorService;
import org.wisdom.executors.ManagedExecutorServiceImpl;
import org.wisdom.executors.context.HttpExecutionContextService;
import org.wisdom.executors.context.TCCLExecutionContextService;
import org.wisdom.test.parents.FakeConfiguration;

import java.io.*;
import java.net.URL;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class AsyncInputStreamTest {

    CountDownLatch latch;

    Vertx vertx = Vertx.vertx();

    ManagedExecutorService executor = new ManagedExecutorServiceImpl("test",
            new FakeConfiguration(Collections.<String, Object>emptyMap()),
            ImmutableList.of(new HttpExecutionContextService(), new TCCLExecutionContextService()));

    @Test
    public void testReadSmallFile() throws FileNotFoundException, InterruptedException {
        latch = new CountDownLatch(1);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        File file = new File("src/test/resources/a_file.txt");
        FileInputStream fis = new FileInputStream(file);
        Context context = vertx.getOrCreateContext();
        final AsyncInputStream async = new AsyncInputStream(vertx, executor, fis)
                .endHandler(event -> {
                    assertThat(bos.toString()).startsWith("This is a file.");
                    latch.countDown();
                }).setContext(context);
        context.runOnContext(event -> async.handler(event1 -> {
            try {
                bos.write(event1.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        latch.await(30, TimeUnit.SECONDS);
    }

    @Test
    public void testReadSmallFileFromUrl() throws IOException, InterruptedException {
        latch = new CountDownLatch(1);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        File file = new File("src/test/resources/a_file.txt");
        URL url = file.toURI().toURL();
        Context context = vertx.getOrCreateContext();
        final AsyncInputStream async = new AsyncInputStream(vertx, executor,
                url.openStream())
                .endHandler(event -> {
                    assertThat(bos.toString()).startsWith("This is a file.");
                    latch.countDown();
                }).setContext(context);
        context.runOnContext(event -> async.handler(buffer -> {
            try {
                bos.write(buffer.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        latch.await(30, TimeUnit.SECONDS);
    }

    @Test
    public void testReadMediumFileFromUrl() throws IOException, InterruptedException {
        latch = new CountDownLatch(1);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        File file = new File("src/test/resources/On_The_Road_Again.jpg");
        URL url = file.toURI().toURL();
        final AsyncInputStream async =
                new AsyncInputStream(vertx, executor, url.openStream());
        async.endHandler(event -> {
            assertThat(async.transferredBytes()).isEqualTo(12073403L);
            try {
                assertThat(async.isClosed()).isTrue();
            } catch (Exception e) {
                fail(e.getMessage());
            }
            latch.countDown();
        });
        vertx.runOnContext(event -> async.handler(event1 -> {
            try {
                bos.write(event1.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        latch.await(30, TimeUnit.SECONDS);
    }
}