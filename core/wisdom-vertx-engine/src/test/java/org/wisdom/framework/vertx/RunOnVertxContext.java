/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2016 Wisdom Framework
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

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class RunOnVertxContext implements TestRule {

    private volatile Vertx vertx;
    private final Supplier<Vertx> createVertx;
    private final BiConsumer<Vertx, CountDownLatch> closeVertx;

    /**
     * Create a new rule managing a Vertx instance created with default options. The Vert.x instance
     * is created and closed for each test.
     */
    public RunOnVertxContext() {
        this(new VertxOptions());
    }

    /**
     * Create a new rule managing a Vertx instance created with specified options. The Vert.x instance
     * is created and closed for each test.
     *
     * @param options the vertx options
     */
    public RunOnVertxContext(VertxOptions options) {
        this(() -> Vertx.vertx(options));
    }

    /**
     * Create a new rule with supplier/consumer for creating/closing a Vert.x instance. The lambda are invoked for each
     * test. The {@code closeVertx} lambda should invoke the consumer with null when the {@code vertx} instance is closed.
     *
     * @param createVertx the create Vert.x supplier
     * @param closeVertx  the close Vert.x consumer
     */
    public RunOnVertxContext(Supplier<Vertx> createVertx, BiConsumer<Vertx, Consumer<Void>> closeVertx) {
        this.createVertx = createVertx;
        this.closeVertx = (vertx, latch) -> closeVertx.accept(vertx, v -> latch.countDown());
    }

    /**
     * Create a new rule with supplier for creating a Vert.x instance. The lambda are invoked for each
     * test.
     *
     * @param createVertx the create Vert.x supplier
     */
    public RunOnVertxContext(Supplier<Vertx> createVertx) {
        this(createVertx, (vertx, latch) -> vertx.close(ar -> latch.accept(null)));
    }

    /**
     * Retrieves the current Vert.x instance, this value varies according to the test life cycle.
     *
     * @return the vertx object
     */
    public Vertx vertx() {
        return vertx;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                vertx = createVertx.get();
                CountDownLatch lock = new CountDownLatch(1);
                AtomicReference<Throwable> error = new AtomicReference<>();
                vertx.runOnContext((v) -> {
                            try {
                                base.evaluate();
                            } catch (Throwable throwable) {
                                error.set(throwable);
                            } finally {
                                lock.countDown();
                            }
                        }

                );

                lock.await();
                CountDownLatch latch = new CountDownLatch(1);
                closeVertx.accept(vertx, latch);
                try {
                    if (!latch.await(30 * 1000, TimeUnit.MILLISECONDS)) {
                        System.err.println("Could not close Vert.x in tme");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                if (error.get() != null) {
                    throw error.get();
                }
            }
        };
    }
}
