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
package org.wisdom.api.concurrent;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * An interface representing the 'submitted' task. This class extends {@link com.google.common.util.concurrent.ListenableFuture} to observe the completion of the task and also provides management methods such as {@link #isTaskHang()}.
 * <p>
 * To ease the usage of callbacks, it offers:
 * {@link #onSuccess(org.wisdom.api.concurrent.ManagedFutureTask.SuccessCallback)} and {@link #onFailure(org.wisdom.api.concurrent.ManagedFutureTask.FailureCallback)}.
 * <p>
 * Depending on which executor the task is submitted, an
 * {@link org.wisdom.api.concurrent.ExecutionContext} can be applied and removed
 * before and after the task execution.
 *
 * @param <V> the type of result returned by the future. {@link java.lang.Void} for {@link java.lang.Runnable}.
 * @since 0.7.1
 */
public interface ManagedFutureTask<V> extends ListenableFuture<V>, Future<V> {

    /**
     * Success callback invoked when the task completes successfully.
     *
     * @param <V> the type of result returned by the future. {@link java.lang.Void} for {@link java.lang.Runnable}.
     */
    interface SuccessCallback<V> {
        /**
         * Callback invoked when the future succeeds.
         *
         * @param future the future
         * @param result the result
         */
        void onSuccess(ManagedFutureTask<V> future, V result);
    }

    /**
     * Failure callback invoked when the task fails (throws an exception).
     *
     * @param <V> the type of result returned by the future. {@link java.lang.Void} for {@link java.lang.Runnable}.
     */
    interface FailureCallback<V> {
        /**
         * Callback invoked when the future fails.
         *
         * @param future    the future
         * @param throwable the thrown error
         */
        void onFailure(ManagedFutureTask<V> future, Throwable throwable);
    }


    /**
     * Registers an onSuccess callback.
     *
     * @param callback the callback to register
     * @param executor the executor running the callback
     * @return the current task
     * @see SuccessCallback
     */
    ManagedFutureTask<V> onSuccess(SuccessCallback<V> callback, Executor executor);

    /**
     * Registers an onSuccess callback. The callback is executed in the same
     * executor as the future.
     *
     * @param callback the callback to register
     * @return the current task
     * @see SuccessCallback
     */
    ManagedFutureTask<V> onSuccess(SuccessCallback<V> callback);

    /**
     * Registers an onFailure callback.
     *
     * @param callback the callback to register
     * @param executor the executor running the callback
     * @return the current task
     * @see FailureCallback
     */
    ManagedFutureTask<V> onFailure(FailureCallback callback, Executor executor);

    /**
     * Registers an onFailure callback. The callback is executed in the same
     * executor as the future.
     *
     * @param callback the callback to register
     * @return the current task
     * @see FailureCallback
     */
    ManagedFutureTask<V> onFailure(FailureCallback callback);

    /**
     * Checks whether the current task is hanging.
     *
     * @return {@code true} if the current task is hanging
     */
    boolean isTaskHang();

    /**
     * Checks whether the executor responsible for the task execution is down.
     *
     * @return {@code true} if the executor executing the task has been shutdown.
     */
    boolean isShutdown();

    /**
     * Computes the run time of the task. If the task is submitted but not
     * started, 0 is returned. It the task is still under execution (not
     * completed), the current run time is returned. If the task has been
     * completed, the 'exact' run time is returned.
     *
     * @return the run time
     */
    long getTaskRunTime();

    /**
     * Gets the task start time. This is not the submission time, but the time
     * when the task execution has started.
     *
     * @return the start time, 0 if not started
     */
    long getTaskStartTime();

    /**
     * Gets the completion time.
     *
     * @return the completion time, 0 is the task has not been completed yet.
     */
    long getTaskCompletionTime();

    /**
     * Gets the hung threshold.
     *
     * @return the hung threshold in milliseconds
     */
    long getHungTaskThreshold();

    /**
     * Gets the execution context that need to be applied before the task
     * execution.
     *
     * @return the execution context, {@code null} if none
     * @see org.wisdom.api.concurrent.CompositeExecutionContext
     */
    ExecutionContext getExecutionContext();

}
