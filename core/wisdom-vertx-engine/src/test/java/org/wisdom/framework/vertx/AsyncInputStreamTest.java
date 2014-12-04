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
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.impl.DefaultVertxFactory;

import java.io.*;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class AsyncInputStreamTest {

    CountDownLatch latch;

    Vertx vertx = new DefaultVertxFactory().createVertx();

    ActorSystem akka = ActorSystem.create();

    @Test
    public void testReadSmallFile() throws FileNotFoundException, InterruptedException {
        latch = new CountDownLatch(1);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        File file = new File("src/test/resources/a_file.txt");
        FileInputStream fis = new FileInputStream(file);
        final AsyncInputStream async = new AsyncInputStream(vertx, akka, fis)
                .endHandler(new Handler<Void>() {
                    @Override
                    public void handle(Void event) {
                        assertThat(bos.toString()).startsWith("This is a file.");
                        latch.countDown();
                    }
                }).setContext(vertx.currentContext());
        vertx.runOnContext(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                async.dataHandler(new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer event) {
                        try {
                            bos.write(event.getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        latch.await(30, TimeUnit.SECONDS);
    }

    @Test
    public void testReadSmallFileFromUrl() throws IOException, InterruptedException {
        latch = new CountDownLatch(1);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        File file = new File("src/test/resources/a_file.txt");
        URL url = file.toURI().toURL();
        final AsyncInputStream async = new AsyncInputStream(vertx, akka,
                url.openStream())
                .endHandler(new Handler<Void>() {
                    @Override
                    public void handle(Void event) {
                        assertThat(bos.toString()).startsWith("This is a file.");
                        latch.countDown();
                    }
                }).setContext(vertx.currentContext());
        vertx.runOnContext(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                async.dataHandler(new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer buffer) {
                        try {
                            bos.write(buffer.getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        latch.await(30, TimeUnit.SECONDS);
    }

    @Test
    public void testReadMediumFileFromUrl() throws IOException, InterruptedException {
        latch = new CountDownLatch(1);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        File file = new File("src/test/resources/On_The_Road_Again.jpg");
        URL url = file.toURI().toURL();
        final AsyncInputStream async =
                new AsyncInputStream(vertx, akka, url.openStream());
        async.endHandler(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                assertThat(async.transferredBytes()).isEqualTo(12073403l);
                try {
                    assertThat(async.isClosed()).isTrue();
                } catch (Exception e) {
                    fail(e.getMessage());
                }
                latch.countDown();
            }
        });
        vertx.runOnContext(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                async.dataHandler(new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer event) {
                        try {
                            bos.write(event.getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        latch.await(30, TimeUnit.SECONDS);
    }
}