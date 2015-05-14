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

import com.google.common.util.concurrent.*;
import org.wisdom.api.concurrent.ExecutionContext;
import org.wisdom.api.concurrent.ManagedFutureTask;

import java.util.concurrent.*;

/**
 * Implementation of {@link org.wisdom.api.concurrent.ManagedFutureTask} to be
 * used with {@link org.wisdom.executors.ManagedExecutorServiceImpl}.
 *
 * @param <V> the type of the result computed by the task. {@link Void} for task not computing a result.
 */
public class Task<V> extends FutureTask<V> implements ListenableFuture<V>, ManagedFutureTask<V> {

    private final ListeningExecutorService executor;
    private final ExecutionContext executionContext;
    protected final Callable<V> callable;
    protected ListenableFuture<V> future;
    private Throwable taskRunThrowable;
    private final AbstractManagedExecutorService parent;

    protected long submissionDate;
    private long startDate;
    private long completionDate;
    private long hungTime;

    protected Task(
            ListeningExecutorService executor,
            Runnable runnable,
            V result,
            ExecutionContext executionContext,
            long hungTime,
            AbstractManagedExecutorService parent) {
        super(runnable, result);
        this.callable = new EnhancedCallable(Executors.callable(runnable, result));
        this.executor = executor;
        this.executionContext = executionContext;
        this.hungTime = hungTime;
        this.parent = parent;
    }

    /**
     * Creates a task.
     *
     * @param executor         the executor service
     * @param callable         the callable object
     * @param executionContext the execution context used to execute the task
     * @param hungTime         the hung time for the executor service
     * @param parent           the {@link AbstractManagedExecutorService} having created this task
     */
    public Task(
            ListeningExecutorService executor,
            Callable<V> callable,
            ExecutionContext executionContext,
            long hungTime,
            AbstractManagedExecutorService parent) {
        super(callable);
        this.callable = new EnhancedCallable(callable);
        this.executor = executor;
        this.executionContext = executionContext;
        this.hungTime = hungTime;
        this.parent = parent;
    }

    protected Task<V> execute() {
        ListenableFuture<V> future = executor.submit(callable);
        submitted(future);
        return this;
    }

    protected Task<V> submitted(Future<V> future) {
        this.submissionDate = System.currentTimeMillis();
        this.future = JdkFutureAdapters.listenInPoolThread(future);
        return this;
    }

    /**
     * Registers a listener on this task. The listener is invoked when the task is completed. The listener is executed
     * the same executor as the task.
     *
     * @param listener the listener.
     */
    public void addListener(Runnable listener) {
        addListener(listener, executor);
    }

    @Override
    public void addListener(Runnable listener, Executor exec) {
        this.future.addListener(listener, exec);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return this.future != null && this.future.cancel(mayInterruptIfRunning);
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return this.future.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException, ExecutionException {
        return this.future.get(timeout, unit);
    }

    @Override
    public boolean isCancelled() {
        return this.future.isCancelled();
    }

    @Override
    public boolean isDone() {
        return this.future.isDone();
    }

    /**
     * @return the {@link Throwable} object that could have been thrown while executing this task. {@code null} if
     * the task execution succeed.
     */
    public Throwable cause() {
        return taskRunThrowable;
    }

    @Override
    protected void setException(Throwable throwable) {
        taskRunThrowable = throwable;
    }

    @Override
    public Task onSuccess(final SuccessCallback<V> callback, Executor executor) {
        Futures.addCallback(future, new FutureCallback<V>() {
            @Override
            public void onSuccess(V v) {
                callback.onSuccess(Task.this, v);
            }

            @Override
            public void onFailure(Throwable throwable) {
                // Do nothing.
            }
        }, executor);
        return this;
    }

    @Override
    public Task onSuccess(final SuccessCallback<V> callback) {
        return onSuccess(callback, executor);
    }

    @Override
    public Task onFailure(final FailureCallback callback, Executor executor) {
        Futures.addCallback(future, new FutureCallback<V>() {
            @Override
            public void onSuccess(V v) {
                // Do nothing
            }

            @Override
            public void onFailure(Throwable throwable) {
                callback.onFailure(Task.this, throwable);
            }
        }, executor);
        return this;
    }

    @Override
    public Task onFailure(final FailureCallback callback) {
        return onFailure(callback, executor);
    }

    @Override
    public boolean isTaskHang() {
        // The task was completed.
        return completionDate == 0 && System.currentTimeMillis() - submissionDate >= hungTime;
    }

    @Override
    public boolean isShutdown() {
        return executor.isShutdown();
    }

    @Override
    public long getTaskStartTime() {
        return startDate;
    }

    @Override
    public long getTaskCompletionTime() {
        return completionDate;
    }

    @Override
    public long getTaskRunTime() {
        if (startDate == 0) {
            return 0;
        }
        if (completionDate == 0) {
            return System.currentTimeMillis() - startDate;
        }
        return completionDate - startDate;
    }


    @Override
    public long getHungTaskThreshold() {
        return hungTime;
    }

    @Override
    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    class EnhancedCallable implements Callable<V> {

        private final Callable<V> delegate;

        private EnhancedCallable(Callable<V> delegate) {
            this.delegate = delegate;
        }

        @Override
        public V call() throws Exception {
            try {
                if (executionContext != null) {
                    executionContext.apply();
                }
                startDate = System.currentTimeMillis();
                return delegate.call();
            } catch (Throwable e) { //NOSONAR
                // We set the exception in the task.
                setException(e);
                throw e;
            } finally {
                completionDate = System.currentTimeMillis();
                if (executionContext != null) {
                    executionContext.unapply();
                }
                parent.addToStatistics(Task.this);
            }
        }
    }

}
