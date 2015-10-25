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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.wisdom.api.concurrent.ExecutionContextService;
import org.wisdom.api.concurrent.ManagedScheduledExecutorService;
import org.wisdom.api.concurrent.ManagedScheduledFutureTask;
import org.wisdom.api.configuration.Configuration;

import java.util.List;
import java.util.concurrent.*;

/**
 * Implementation of the {@link org.wisdom.api.concurrent.ManagedScheduledExecutorService}.
 * Instances must be created explicitly.
 */
public class ManagedScheduledExecutorServiceImpl
        extends AbstractManagedExecutorService implements ManagedScheduledExecutorService {

    public ManagedScheduledExecutorServiceImpl(String name, Configuration configuration,
                                               List<ExecutionContextService> ecs) {
        this(
                name,
                configuration.get("threadType", ThreadType.class, ThreadType.POOLED),
                configuration.getDuration("hungTime", TimeUnit.MILLISECONDS, 60000),
                configuration.getIntegerWithDefault("coreSize", 5),
                configuration.getIntegerWithDefault("priority", Thread.NORM_PRIORITY),
                ecs
        );
    }

    public ManagedScheduledExecutorServiceImpl(
            String name,
            ThreadType tu,
            long hungTime,
            int coreSize,
            int priority,
            List<ExecutionContextService> ecs) {

        super(name, hungTime, ecs);
        ThreadFactoryBuilder builder = new ThreadFactoryBuilder()
                .setDaemon(tu == ThreadType.DAEMON)
                .setNameFormat(name + "-%s")
                .setPriority(priority)
                .setUncaughtExceptionHandler(
                        new Thread.UncaughtExceptionHandler() {
                            @Override
                            public void uncaughtException(Thread t, Throwable e) {
                                logger.error("Uncaught exception in thread '{}'",
                                        t.getName(), e);
                            }
                        });

        setInternalPool(new ScheduledThreadPoolExecutor(coreSize, builder.build()));
    }

    protected <V> Task<V> getNewTaskFor(Runnable task, V result) {
        return new Task<>(executor, task, result, createExecutionContext(),
                hungTime, this);
    }

    protected synchronized <V> Task<V> getNewTaskFor(Callable<V> callable) {
        return new Task(executor, callable, createExecutionContext(), hungTime, this);
    }

    /**
     * Creates and executes a ScheduledFuture that becomes enabled after the
     * given delay.
     *
     * @param callable the function to execute
     * @param delay    the time from now to delay execution
     * @param unit     the time unit of the delay parameter
     * @return a ScheduledFuture that can be used to extract result or cancel
     * @throws java.util.concurrent.RejectedExecutionException if the task cannot be
     *                                                         scheduled for execution
     * @throws NullPointerException                            if callable is null
     */
    @Override
    public synchronized <V> ManagedScheduledFutureTask<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        ScheduledTask<V> task = getNewScheduledTaskFor(callable, false);
        ScheduledFuture<V> future =
                ((ScheduledExecutorService) executor).schedule(task.callable, delay, unit);
        task.submittedScheduledTask(future);
        return task;
    }

    private <V> ScheduledTask<V> getNewScheduledTaskFor(Callable<V> callable, boolean periodic) {
        return new ScheduledTask<>(executor, callable,
                hungTime, periodic, this);
    }

    private ScheduledTask<Void> getNewScheduledTaskFor(Runnable command, boolean periodic) {
        return new ScheduledTask<>(executor, command, null,
                hungTime, periodic, this);
    }

    /**
     * Creates and executes a one-shot action that becomes enabled
     * after the given delay.
     *
     * @param command the task to execute
     * @param delay   the time from now to delay execution
     * @param unit    the time unit of the delay parameter
     * @return a ScheduledFuture representing pending completion of
     * the task and whose {@code get()} method will return
     * {@code null} upon completion
     * @throws java.util.concurrent.RejectedExecutionException if the task cannot be
     *                                                         scheduled for execution
     * @throws NullPointerException                            if command is null
     */
    @Override
    public synchronized ManagedScheduledFutureTask<?> schedule(Runnable command, long delay, TimeUnit unit) {
        ScheduledTask<?> task = getNewScheduledTaskFor(command, false);
        ScheduledFuture<?> future =
                ((ScheduledExecutorService) executor).schedule(task.callable, delay, unit);
        task.submittedScheduledTask(future);
        return task;
    }


    @Override
    public synchronized ManagedScheduledFutureTask<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        ScheduledTask<?> task = getNewScheduledTaskFor(command, true);
        ScheduledFuture<?> future =
                ((ScheduledExecutorService) executor).scheduleAtFixedRate(task.asRunnable(),
                        initialDelay, period, unit);
        task.submittedScheduledTask(future);
        return task;
    }


    @Override
    public synchronized ManagedScheduledFutureTask<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        ScheduledTask<?> task = getNewScheduledTaskFor(command, true);
        ScheduledFuture<?> future =
                ((ScheduledExecutorService) executor).scheduleWithFixedDelay(task.asRunnable(),
                        initialDelay, delay, unit);
        task.submittedScheduledTask(future);
        return task;
    }


}
