/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class WisdomServiceVerticle extends AbstractVerticle {
    private final List<Server> servers;

    public WisdomServiceVerticle(ServiceAccessor accessor, List<Server> servers) {
        this.servers = servers;
    }

    /**
     * Start the verticle.<p>
     * This is called by Vert.x when the verticle instance is deployed. Don't call it yourself.<p>
     * If your verticle does things in it's startup which take some time then you can override this method
     * and call the startFuture some time later when start up is complete.
     *
     * @param startFuture a future which should be called when verticle start-up is complete.
     * @throws Exception
     */
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        CountDownLatch latch = new CountDownLatch(servers.size());
        final boolean[] inError = {false};
        for (Server server : servers) {
            server.bind(ar -> {
                if (ar.failed()) {
                    inError[0] = true;
                }
                latch.countDown();
            });
        }
        vertx.executeBlocking(f -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                // Ignore it.
            }
        }, ar -> {
            if (inError[0]) {
                startFuture.fail("One of the server was not able to start correctly");
            } else {
                startFuture.complete();
            }
        });

    }

    /**
     * Stop the verticle.<p>
     * This is called by Vert.x when the verticle instance is un-deployed. Don't call it yourself.<p>
     * If your verticle does things in it's shut-down which take some time then you can override this method
     * and call the stopFuture some time later when clean-up is complete.
     *
     * @param future a future which should be called when verticle clean-up is complete.
     * @throws Exception
     */
    @Override
    public void stop(Future<Void> future) throws Exception {
        CountDownLatch latch = new CountDownLatch(servers.size());
        final boolean[] inError = {false};
        for (Server server : servers) {
            server.close(ar -> {
                if (ar.failed()) {
                    inError[0] = true;
                }
                latch.countDown();
            });
        }

        vertx.executeBlocking(f -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                // ignore it.
            }
        }, ar -> {
            if (inError[0]) {
                future.fail("One of the server did not stopped correctly");
            } else {
                future.complete();
            }
        });
    }
}
