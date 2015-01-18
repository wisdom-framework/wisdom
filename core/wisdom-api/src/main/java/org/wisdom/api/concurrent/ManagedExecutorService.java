package org.wisdom.api.concurrent;

import com.google.common.util.concurrent.ListeningExecutorService;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Executor Service exposed as a service to execute tasks. This interface
 * extends {@link com.google.common.util.concurrent.ListeningExecutorService}
 * to make the returned {@link java.util.concurrent.Future} listenable.
 * <p>
 * The type of thread managed by the thread pool is configured with
 * {@link org.wisdom.api.concurrent.ManagedExecutorService.ThreadType}. It configures
 * the {@link java.util.concurrent.ThreadFactory} used by the executor.
 * <p>
 * Unlike regular {@link java.util.concurrent.ExecutorService}, this executor
 * returns {@link org.wisdom.api.concurrent.ManagedFutureTask} instead of raw
 * {@link java.util.concurrent.Future}.
 */
public interface ManagedExecutorService extends ListeningExecutorService {

    /**
     * A special name used by the core Wisdom System Executor.
     */
    public static final String SYSTEM = "wisdom-system-executor";

    /**
     * The type of thread to use
     */
    public static enum ThreadType {
        POOLED,
        DAEMON
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

    /**
     * @return the approximate number of threads that are actively executing tasks.
     */
    public int getActiveCount();

    /**
     * @return the approximate total number of tasks that have completed execution.
     */
    public long getCompletedTaskCount();

    /**
     * @return the core number of threads.
     */
    public int getCorePoolSize();

    /**
     * Returns the thread keep-alive time, which is the amount of time that threads in excess of the core pool size may
     * remain idle before being terminated.
     *
     * @param unit the time unit
     * @return the thread keep alive time translated to the given time unit
     */
    public long getKeepAliveTime(TimeUnit unit);

    /**
     * @return the largest number of threads that have ever simultaneously been in the pool.
     */
    public int getLargestPoolSize();

    /**
     * @return the maximum allowed number of threads.
     */
    public int getMaximumPoolSize();

    /**
     * @return the current number of threads in the pool.
     */
    public int getPoolSize();

    /**
     * @return the task queue used by this executor.
     */
    public BlockingQueue<Runnable> getQueue();

    void purge();

    boolean remove(Runnable task);

    /**
     * @return the approximate total number of tasks that have ever been scheduled for execution.
     */
    public long getTaskCount();

}
