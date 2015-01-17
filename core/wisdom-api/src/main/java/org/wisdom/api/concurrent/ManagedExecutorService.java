package org.wisdom.api.concurrent;

import com.google.common.util.concurrent.ListeningExecutorService;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;

/**
 * Executor Service exposed as a service to execute tasks. This interface
 * extends {@link com.google.common.util.concurrent.ListeningExecutorService}
 * to make the returned {@link java.util.concurrent.Future} listenable.
 * <p>
 * Implementations are configured using:
 * <ul>
 * <li>the type of thread to use: {@link org.wisdom.api.concurrent.ManagedExecutorService.ThreadType}</li>
 * <li>the rejection policy: {@link org.wisdom.api.concurrent.ManagedExecutorService.RejectionPolicy}</li>
 * </ul>
 * <p>
 * These two parameter configure the {@link java.util.concurrent.ThreadFactory}
 * used by the executor.
 * <p>
 * Unlike regular {@link java.util.concurrent.ExecutorService}, this executor
 * returns {@link org.wisdom.api.concurrent.ManagedFutureTask} instead of raw
 * {@link java.util.concurrent.Future}.
 */
public interface ManagedExecutorService extends ListeningExecutorService {

    /**
     * A special name used by the core Wisdom Thread Pool.
     */
    public static final String SYSTEM = "wisdom-system-pool";

    /**
     * The type of thread to use
     */
    public static enum ThreadType {
        POOLED,
        DAEMON
    }

    public static enum RejectionPolicy {
        ABORT,
        rejectionPolicy, RETRY_AND_ABORT
    }

    /**
     * @return the name of the thread pool.
     */
    public String name();

    /**
     * Gets the set of task considered as stuck.
     *
     * @return the set of task that have hung, empty is none.
     */
    public Collection<ManagedFutureTask> getHungTasks();

    /**
     * Submits a task.
     *
     * @param task the task
     * @return a {@code ManagedFutureTask} representing pending completion of
     * the task
     * @throws RejectedExecutionException when the pool cannot accept the task.
     */
    @Override
    <T> ManagedFutureTask<T> submit(Callable<T> task)
            throws RejectedExecutionException;

    /**
     * Submits a task.
     *
     * @param task the task
     * @return a {@code ManagedFutureTask} representing pending completion of
     * the task
     * @throws RejectedExecutionException when the pool cannot accept the task.
     */
    @Override
    ManagedFutureTask<?> submit(Runnable task)
            throws RejectedExecutionException;

    /**
     * Submits a task.
     *
     * @param task   the task
     * @param result the result to return on task completion
     * @return a {@code ManagedFutureTask} representing pending completion of
     * the task
     * @throws RejectedExecutionException when the pool cannot accept the task.
     */
    @Override
    <T> ManagedFutureTask<T> submit(Runnable task, T result)
            throws RejectedExecutionException;
}
