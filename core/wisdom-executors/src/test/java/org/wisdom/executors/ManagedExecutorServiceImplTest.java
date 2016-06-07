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
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.junit.Before;
import org.junit.Test;
import org.wisdom.api.concurrent.ExecutionContext;
import org.wisdom.api.concurrent.ExecutionContextService;
import org.wisdom.api.concurrent.ManagedExecutorService;
import org.wisdom.api.concurrent.ManagedFutureTask;
import org.wisdom.test.parents.FakeConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class ManagedExecutorServiceImplTest {

    AtomicInteger counter = new AtomicInteger();

    ManagedExecutorServiceImpl executor = new ManagedExecutorServiceImpl(
            "test",
            ManagedExecutorService.ThreadType.POOLED,
            10,
            10,
            25,
            1000,
            true,
            20,
            Thread.NORM_PRIORITY,
            null);

    @Before
    public void setUp() {
        counter.set(0);
        executor.ecs = new ArrayList<>();
    }

    @Test
    public void testCreation() throws ExecutionException, InterruptedException {
        Future<String> future = executor.submit(new MyCallable());
        assertThat(future.get()).isEqualTo("hello");

        assertThat(executor.getInternalPool()).isNotNull();
        assertThat(executor.getExecutor()).isNotNull();
    }

    @Test
    public void testThatCreatedFuturesAreListenable() throws ExecutionException, InterruptedException {
        final Semaphore semaphore = new Semaphore(0);
        final StringBuilder builder = new StringBuilder();
        Future<String> future = executor.submit(new MyCallable());
        assertThat(future).isInstanceOf(ListenableFuture.class);
        assertThat(future).isInstanceOf(Task.class);
        ((Task<String>) future).onSuccess(new ManagedFutureTask.SuccessCallback<String>() {
            @Override
            public void onSuccess(ManagedFutureTask<String> future, String result) {
                builder.append(result).append(" wisdom");
            }
        }, MoreExecutors.sameThreadExecutor()).onSuccess(new ManagedFutureTask.SuccessCallback<String>() {
            @Override
            public void onSuccess(ManagedFutureTask<String> future, String result) {
                builder.append(" !");
                semaphore.release();
            }
        }, MoreExecutors.sameThreadExecutor());

        assertThat(future.get()).isEqualTo("hello");
        semaphore.tryAcquire(10, TimeUnit.SECONDS);
        assertThat(builder.toString()).isEqualTo("hello wisdom !");
    }

    @Test
    public void testThatErrorCallbacksAreCalled() throws InterruptedException {
        final StringBuilder builder = new StringBuilder();
        final Semaphore semaphore = new Semaphore(0);
        ManagedFutureTask<?> future = executor.submit(new Runnable() {
            @Override
            public void run() {
                throw new NullPointerException("expected");
            }
        });

        ((Task) future).onFailure(new ManagedFutureTask.FailureCallback() {
            @Override
            public void onFailure(ManagedFutureTask future, Throwable throwable) {
                builder.append(throwable.getMessage());
                semaphore.release();
            }
        });

        semaphore.acquire();
        assertThat(builder.toString()).isEqualTo("expected");
        assertThat(((Task) future).cause().getMessage()).isEqualTo("expected");
    }

    @Test
    public void testManagementAPI() throws InterruptedException {
        final Semaphore semaphore = new Semaphore(0);
        long begin = System.currentTimeMillis();
        ManagedFutureTask<?> future = executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // Ignore it.
                } finally {
                    semaphore.release();
                }
            }
        });
        semaphore.acquire();
        // We should sleep here until the task are been completely done (may be in the enhanced runnable wrapper).
        Thread.sleep(20);
        assertThat(future.getTaskStartTime()).isGreaterThanOrEqualTo(begin);
        assertThat(future.getTaskCompletionTime())
                .isGreaterThanOrEqualTo(begin)
                .isGreaterThan(future.getTaskStartTime());
        assertThat(future.getTaskRunTime()).isGreaterThanOrEqualTo(10);
        assertThat(future.isDone()).isTrue();
    }

    @Test
    public void testTheExecutionOfFiftyThreads() throws InterruptedException, ExecutionException {
        List<Callable<String>> callables = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            callables.add(new MyCallable());
        }

        List<Future<String>> futures = executor.invokeAll(callables, 1, TimeUnit.MINUTES);
        for (Future<String> future : futures) {
            assertThat(future.get()).isEqualTo("hello");
        }
        assertThat(counter.get()).isEqualTo(50);

        counter.set(0);

        futures = executor.invokeAll(callables);
        for (Future<String> future : futures) {
            assertThat(future.get()).isEqualTo("hello");
        }
        assertThat(counter.get()).isEqualTo(50);

        // Management API
        // We aware that getActiveCount, getTaskCount and getCompleteTaskCount are 'approximations'.
        assertThat(executor.getActiveCount()).isBetween(0, 3);
        assertThat(executor.getCorePoolSize()).isEqualTo(10);
        assertThat(executor.getMaximumPoolSize()).isEqualTo(25);
        assertThat(executor.getLargestPoolSize()).isGreaterThanOrEqualTo(10);
        assertThat(executor.getPoolSize()).isGreaterThanOrEqualTo(10);
        assertThat(executor.getCompletedTaskCount()).isBetween(90l, 110l);
        assertThat(executor.getTaskCount()).isBetween(90l, 110l);
    }

    @Test
    public void testHungTaskDetection() throws InterruptedException {
        ManagedFutureTask<?> future = executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // Ignore it.
                }
            }
        });

        assertThat(future.getHungTaskThreshold()).isEqualTo(10);
        Thread.sleep(100);
        // The task should hung
        assertThat(future.isTaskHang()).isTrue();
        assertThat(executor.getHungTasks()).hasSize(1).contains(future);
        // Has time is not exact, reduce the limit.
        assertThat(future.getTaskRunTime()).isGreaterThanOrEqualTo(95);

        // Management API
        assertThat(executor.getQueue()).hasSize(0);
        assertThat(executor.getActiveCount()).isEqualTo(1);
    }

    @Test
    public void testExecutionContextSwitch() throws InterruptedException {
        final ThreadLocal<String> context = new ThreadLocal();
        final Semaphore semaphore = new Semaphore(0);
        final StringBuilder builder = new StringBuilder();
        executor.ecs = new ArrayList<>();


        // First call without context
        executor.submit(new Runnable() {
            @Override
            public void run() {
                builder.append(context.get());
                semaphore.release();
            }
        });

        semaphore.acquire();

        assertThat(builder.toString()).isEqualTo("null");

        // Then, set a context service, but nothing in the context.

        executor.ecs.add(new ExecutionContextService() {

            @Override
            public String name() {
                return "context";
            }

            @Override
            public ExecutionContext prepare() {
                return new ExecutionContext() {

                    private String local = context.get();

                    @Override
                    public void apply() {
                        context.set(local);
                    }

                    @Override
                    public void unapply() {
                        context.remove();
                    }
                };
            }
        });

        executor.submit(new Runnable() {
            @Override
            public void run() {
                builder.append(context.get());
                semaphore.release();
            }
        });

        semaphore.acquire();

        assertThat(builder.toString()).isEqualTo("nullnull");

        // Finally, set the context
        context.set("Stuff");
        executor.submit(new Runnable() {
            @Override
            public void run() {
                builder.append(context.get());
                semaphore.release();

            }
        });

        semaphore.acquire();

        assertThat(builder.toString()).isEqualTo("nullnullStuff");
    }

    @Test
    public void testExecutorAPI() {
        assertThat(executor.name()).isEqualToIgnoringCase("test");
        assertThat(executor.isShutdown()).isFalse();
        assertThat(executor.isTerminated()).isFalse();
        assertThat(executor.getActiveCount()).isEqualTo(0);
        assertThat(executor.getCorePoolSize()).isEqualTo(10);
        assertThat(executor.getMaximumPoolSize()).isEqualTo(25);
        // Nothing started.
        assertThat(executor.getLargestPoolSize()).isEqualTo(0);
        assertThat(executor.getPoolSize()).isEqualTo(0);

        assertThat(executor.getCompletedTaskCount()).isEqualTo(0);
        assertThat(executor.getTaskCount()).isEqualTo(0);

        assertThat(executor.getKeepAliveTime(TimeUnit.SECONDS)).isEqualTo(1);
    }

    @Test(expected = NullPointerException.class)
    public void testThatWeCannotSubmitNullRunnable() {
        executor.submit((Runnable) null);
    }

    @Test(expected = NullPointerException.class)
    public void testThatWeCannotSubmitNullCallable() {
        executor.submit((Callable) null);
    }

    @Test
    public void testThatWeCanSubmitARunnableWithAnExpectedResult() throws ExecutionException, InterruptedException {
        final Semaphore semaphore = new Semaphore(0);
        ManagedFutureTask<String> future = executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5);
                    semaphore.release();
                } catch (InterruptedException e) {
                    // Ignore it.
                }

            }
        }, "hello");

        assertThat(future.get()).isEqualToIgnoringCase("hello");
    }

    @Test
    public void testCreationWithUnboundQueue() throws ExecutionException, InterruptedException {
        ManagedExecutorServiceImpl service = new ManagedExecutorServiceImpl("unbound",
                ManagedExecutorService.ThreadType.POOLED, 60000, 10, 25, 1000, true,
                Integer.MAX_VALUE, Thread.NORM_PRIORITY, null);
        assertThat(service.getQueue()).isInstanceOf(LinkedBlockingQueue.class);
    }

    @Test
    public void testCreationWithHandOffQueue() throws ExecutionException, InterruptedException {
        ManagedExecutorServiceImpl service = new ManagedExecutorServiceImpl("unbound",
                ManagedExecutorService.ThreadType.POOLED, 60000, 10, 25, 1000, true,
                0, Thread.NORM_PRIORITY, null);
        assertThat(service.getQueue()).isInstanceOf(SynchronousQueue.class);
    }

    @Test
    public void testCreationWithDefaultConfiguration() {
        FakeConfiguration configuration = new FakeConfiguration(ImmutableMap.<String, Object>of("name", "default"));
        ManagedExecutorServiceImpl service = new ManagedExecutorServiceImpl("default", configuration, null);
        assertThat(service).isNotNull();
        assertThat(service.getCorePoolSize()).isEqualTo(5);
        assertThat(service.getActiveCount()).isEqualTo(0);
        assertThat(service.getMaximumPoolSize()).isEqualTo(25);
        assertThat(service.getKeepAliveTime(TimeUnit.MILLISECONDS)).isEqualTo(5000);
        assertThat(service.getQueue()).isInstanceOf(LinkedBlockingQueue.class);
    }

    private class MyCallable implements Callable<String> {

        @Override
        public String call() throws Exception {
            counter.incrementAndGet();
            return "hello";
        }
    }

}