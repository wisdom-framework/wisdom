package org.wisdom.api.concurrent;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledFuture;

/**
 * An interface representing a periodic 'submitted' task. This class extends
 * {@link com.google.common.util.concurrent.ListenableFuture} to observe the completion
 * of the task and also provides management methods such as {@link #isTaskHang()}.
 * <p>
 * To ease the usage of callbacks, it offers:
 * {@link #onSuccess(org.wisdom.api.concurrent.ManagedScheduledFutureTask.SuccessCallback)} and {@link #onFailure(org.wisdom.api.concurrent.ManagedScheduledFutureTask.FailureCallback)}.
 * <p>
 * Depending on which executor the task is submitted, an
 * {@link ExecutionContext} can be applied and removed
 * before and after the task execution.
 *
 * @param <V> the type of result returned by the future. {@link Void} for {@link Runnable}.
 * @since 0.7.1
 */
public interface ManagedScheduledFutureTask<V>
        extends ManagedFutureTask<V>, RunnableScheduledFuture<V> {

}
