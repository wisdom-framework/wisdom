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
package org.wisdom.executors;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.wisdom.api.concurrent.ManagedExecutorService;
import org.wisdom.api.concurrent.ManagedScheduledExecutorService;
import org.wisdom.api.concurrent.ManagedScheduledFutureTask;
import org.wisdom.test.parents.FakeConfiguration;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class ManagedScheduledExecutorServiceImplTest {
    AtomicInteger counter = new AtomicInteger();

    ManagedScheduledExecutorServiceImpl executor = new ManagedScheduledExecutorServiceImpl(
            "test",
            ManagedExecutorService.ThreadType.POOLED,
            10,
            10,
            Thread.NORM_PRIORITY,
            null);

    @Before
    public void setUp() {
        counter.set(0);
        executor.ecs = new ArrayList<>();
    }

    @Test
    public void testCreationOfPeriodicTaskWithFixedRate()
            throws ExecutionException, InterruptedException {
        Semaphore semaphore = new Semaphore(0);
        ManagedScheduledFutureTask<?> future =
                executor.scheduleAtFixedRate(new MyRunnable(4, semaphore), 10, 10,
                        TimeUnit.MILLISECONDS);
        assertThat(future).isNotNull();
        semaphore.acquire();
        future.cancel(false);
        assertThat(counter.get()).isGreaterThanOrEqualTo(4);
        assertThat(future.getDelay(TimeUnit.MILLISECONDS)).isLessThanOrEqualTo(10);
        assertThat(future.isPeriodic()).isTrue();

        assertThat(executor.getCompletedTaskCount()).isBetween(2l, 5l);
    }

    @Test
    public void testCreationOfPeriodicTaskWithFixedDelay()
            throws ExecutionException, InterruptedException {
        Semaphore semaphore = new Semaphore(0);
        ManagedScheduledFutureTask<?> future =
                executor.scheduleWithFixedDelay(new MyRunnable(4, semaphore), 10, 10,
                        TimeUnit.MILLISECONDS);
        assertThat(future).isNotNull();
        semaphore.acquire();
        future.cancel(false);
        assertThat(counter.get()).isGreaterThanOrEqualTo(4);
        assertThat(future.getDelay(TimeUnit.MILLISECONDS)).isLessThanOrEqualTo(10);
        assertThat(future.isPeriodic()).isTrue();

        assertThat(executor.getCompletedTaskCount()).isBetween(2l, 5l);
    }

    @Test
    public void testTheCreationOfDelayedCallable() throws ExecutionException, InterruptedException {
        long begin = System.currentTimeMillis();
        ManagedScheduledFutureTask<String> future =
                executor.schedule(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "Hello";
            }
        }, 10, TimeUnit.MILLISECONDS);
        String result = future.get();
        long end = System.currentTimeMillis();
        assertThat(result).isEqualToIgnoringCase("Hello");
        assertThat(end - begin).isGreaterThanOrEqualTo(10);
    }

    @Test
    public void testTheCreationOfDelayedRunnable() throws ExecutionException, InterruptedException {
        final Semaphore semaphore = new Semaphore(0);
        long begin = System.currentTimeMillis();
        ManagedScheduledFutureTask future =
                executor.schedule(new Runnable() {
                    @Override
                    public void run() {
                        semaphore.release();
                    }
                }, 10, TimeUnit.MILLISECONDS);
        semaphore.acquire();
        long end = System.currentTimeMillis();
        assertThat(end - begin).isGreaterThanOrEqualTo(10);
    }

    @Test
    public void testDefaultConfiguration() {
        FakeConfiguration configuration = new FakeConfiguration(ImmutableMap.<String, Object>of("name", "default"));
        ManagedScheduledExecutorService service =
                new ManagedScheduledExecutorServiceImpl("default", configuration, null);
        assertThat(service).isNotNull();
        assertThat(service.getCorePoolSize()).isEqualTo(5);
    }

    private class MyRunnable implements Runnable {

        private final int numberOfCalls;
        private final Semaphore semaphore;
        private int calls;

        MyRunnable(int numberOfCalls, Semaphore semaphore) {
            this.numberOfCalls = numberOfCalls;
            this.semaphore = semaphore;
            this.calls = 0;
        }

        @Override
        public void run() {
            counter.incrementAndGet();
            calls++;
            if (calls >= numberOfCalls) {
                semaphore.release();
            }
        }
    }


}