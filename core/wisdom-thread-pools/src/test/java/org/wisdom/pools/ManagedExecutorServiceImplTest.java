package org.wisdom.pools;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.junit.Before;
import org.junit.Test;
import org.wisdom.api.concurrent.ExecutionContext;
import org.wisdom.api.concurrent.ExecutionContextService;
import org.wisdom.api.concurrent.ManagedExecutorService;
import org.wisdom.api.concurrent.ManagedFutureTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class ManagedExecutorServiceImplTest {

    AtomicInteger counter = new AtomicInteger();

    ManagedExecutorServiceImpl executor = new ManagedExecutorServiceImpl(
            "test",
            null,
            ManagedExecutorService.ThreadType.POOLED,
            10,
            10,
            25,
            1000,
            20,
            ManagedExecutorService.RejectionPolicy.ABORT);

    @Before
    public void setUp() {
        counter.set(0);
        executor.ecs = new ArrayList<>();
    }

    @Test
    public void testCreation() throws ExecutionException, InterruptedException {
        Future<String> future = executor.submit(new MyCallable());
        assertThat(future.get()).isEqualTo("hello");
    }

    @Test
    public void testThatCreatedFuturesAreListenable() throws ExecutionException, InterruptedException {
        final StringBuilder builder = new StringBuilder();
        Future<String> future = executor.submit(new MyCallable());
        assertThat(future).isInstanceOf(ListenableFuture.class);
        assertThat(future).isInstanceOf(Task.class);
        ((Task<String>) future).onSuccess(new ManagedFutureTask.SuccessCallback<String>() {
            @Override
            public void onSuccess(ManagedFutureTask<String> future, String result) {
                builder.append(result).append(" wisdom");
            }
        }, MoreExecutors.directExecutor()).onSuccess(new ManagedFutureTask.SuccessCallback<String>() {
            @Override
            public void onSuccess(ManagedFutureTask<String> future, String result) {
                builder.append(" !");
            }
        }, MoreExecutors.directExecutor());

        assertThat(future.get()).isEqualTo("hello");
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
            public void onFailure(ManagedFutureTask<?> future, Throwable throwable) {
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
        assertThat(future.getTaskRunTime()).isGreaterThanOrEqualTo(100);
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
    }

    private class MyCallable implements Callable<String> {

        @Override
        public String call() throws Exception {
            counter.incrementAndGet();
            return "hello";
        }
    }

}