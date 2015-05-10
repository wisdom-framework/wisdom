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
    String SYSTEM = "wisdom-system-executor";

    /**
     * The type of thread to use
     */
    enum ThreadType {
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
    <T> ManagedFutureTask<T> submit(Callable<T> task);

    /**
     * Submits a task.
     *
     * @param task the task
     * @return a {@code ManagedFutureTask} representing pending completion of
     * the task
     * @throws RejectedExecutionException when the pool cannot accept the task.
     */
    @Override
    ManagedFutureTask submit(Runnable task)
            throws RejectedExecutionException;

    /**
     * Submits a task. Submission may throws a {@link RejectedExecutionException} is the underlying executor is not
     * able to accept the task.
     *
     * @param task   the task
     * @param result the result to return on task completion
     * @return a {@code ManagedFutureTask} representing pending completion of
     * the task
     */
    @Override
    <T> ManagedFutureTask<T> submit(Runnable task, T result);

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

    /**
     * Tries to remove from the work queue all {@link java.util.concurrent.Future}
     * tasks that have been cancelled. This method can be useful as a
     * storage reclamation operation, that has no other impact on
     * functionality. Cancelled tasks are never executed, but may
     * accumulate in work queues until worker threads can actively
     * remove them. Invoking this method instead tries to remove them now.
     * However, this method may fail to remove tasks in
     * the presence of interference by other threads.
     */
    public void purge();

    /**
     * Removes this task from the executor's internal queue if it is
     * present, thus causing it not to be run if it has not already
     * started.
     * <p>
     * <p>This method may be useful as one part of a cancellation
     * scheme.  It may fail to remove tasks that have been converted
     * into other forms before being placed on the internal queue. For
     * example, a task entered using {@code submit} might be
     * converted into a form that maintains {@code Future} status.
     * However, in such cases, method {@link #purge} may be used to
     * remove those Futures that have been cancelled.
     *
     * @param task the task to remove
     * @return {@code true} if the task was removed
     */
    public boolean remove(Runnable task);

    /**
     * @return the approximate total number of tasks that have ever been scheduled for execution.
     */
    public long getTaskCount();

    /**
     * @return the execution statistics.
     */
    public ExecutionStatistics getExecutionTimeStatistics();

    /**
     * Represents execution statistics of a thread pool.
     */
    public static class ExecutionStatistics {

        private long count;
        private long sum;
        private long min = Long.MAX_VALUE;
        private long max = Long.MIN_VALUE;

        /**
         * Records a new {@code int} value into the statistics.
         *
         * @param value the input value
         */
        public void accept(int value) {
            accept((long) value);
        }

        /**
         * Records a new {@code long} value into the statistics.
         *
         * @param value the input value
         */
        public synchronized void accept(long value) {
            ++count;
            sum += value;
            min = Math.min(min, value);
            max = Math.max(max, value);
        }

        /**
         * Combines the state of another {@code ExecutionStatistics} into this
         * one.
         *
         * @param other another {@code ExecutionStatistics}
         * @throws NullPointerException if {@code other} is null
         */
        public synchronized void combine(final ExecutionStatistics other) {
            count += other.getCount();
            sum += other.getTotalExecutionTime();
            min = Math.min(min, other.getMinimumExecutionTime());
            max = Math.max(max, other.getMaximumExecutionTime());
        }

        /**
         * Returns a copy of the current statistics.
         *
         * @return the copied object
         */
        public synchronized ExecutionStatistics copy() {
            ExecutionStatistics statistics = new ExecutionStatistics();
            statistics.combine(this);
            return statistics;
        }

        /**
         * Returns the count of values recorded.
         *
         * @return the count of values
         */
        public final synchronized long getCount() {
            return count;
        }

        /**
         * Returns the number of tasks that have been recorded.
         *
         * @return the number of tasks
         */
        public final synchronized long getNumberOfTasks() {
            return getCount();
        }

        /**
         * Returns the sum of values recorded, or zero if no values have been
         * recorded.
         *
         * @return the sum of values, or zero if none
         */
        public final synchronized long getTotalExecutionTime() {
            return sum;
        }

        /**
         * Returns the minimum value recorded, or {@code Long.MAX_VALUE} if no
         * values have been recorded.
         *
         * @return the minimum value, or {@code Long.MAX_VALUE} if none
         */
        public final synchronized long getMinimumExecutionTime() {
            return min;
        }

        /**
         * Returns the maximum value recorded, or {@code Long.MIN_VALUE} if no
         * values have been recorded
         *
         * @return the maximum value, or {@code Long.MIN_VALUE} if none
         */
        public final synchronized long getMaximumExecutionTime() {
            return max;
        }

        /**
         * Returns the arithmetic mean of values recorded, or zero if no values have been
         * recorded.
         *
         * @return The arithmetic mean of values, or zero if none
         */
        public final synchronized double getAverageExecutionTime() {
            return getCount() > 0 ? (double) getTotalExecutionTime() / getNumberOfTasks() : 0.0d;
        }

        @Override
        /**
         * {@inheritDoc}
         *
         * Returns a non-empty string representation of this object suitable for
         * debugging. The exact presentation format is unspecified and may vary
         * between implementations and versions.
         */
        public synchronized String toString() {
            return String.format(
                    "%s{count=%d, sum=%d, min=%d, average=%f, max=%d}",
                    this.getClass().getSimpleName(),
                    getCount(),
                    getTotalExecutionTime(),
                    getMinimumExecutionTime(),
                    getAverageExecutionTime(),
                    getMaximumExecutionTime());
        }

    }
}
