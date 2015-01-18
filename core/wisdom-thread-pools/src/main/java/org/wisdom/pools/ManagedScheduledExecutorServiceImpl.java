package org.wisdom.pools;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.concurrent.ExecutionContextService;
import org.wisdom.api.concurrent.ManagedScheduledExecutorService;
import org.wisdom.api.concurrent.ManagedScheduledFutureTask;
import org.wisdom.api.configuration.Configuration;

import java.util.List;
import java.util.concurrent.*;

/**
 * Implementation of the {@link org.wisdom.api.concurrent.ManagedExecutorService}.
 * Instances must be created explicitly.
 */
@Component
@Provides
public class ManagedScheduledExecutorServiceImpl
        extends AbstractManagedExecutorService implements ManagedScheduledExecutorService {

    @Requires
    List<ExecutionContextService> ecs;

    public ManagedScheduledExecutorServiceImpl(@Property(name = "configuration") Configuration configuration) {
        this(
                configuration.getOrDie("name"),
                configuration.get("threadType", ThreadType.class, ThreadType.POOLED),
                configuration.getDuration("hungTime", TimeUnit.MILLISECONDS, 60000),
                configuration.getIntegerWithDefault("coreSize", 5),
                configuration.getIntegerWithDefault("priority", Thread.NORM_PRIORITY)
        );
    }

    public ManagedScheduledExecutorServiceImpl(
            String name,
            ThreadType tu,
            long hungTime,
            int coreSize,
            int priority) {

        super(name, hungTime);
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
        return new Task<>(executor, task, result, createExecutionContext(ecs),
                hungTime);
    }


    protected <V> Task<V> getNewTaskFor(Callable<V> callable) {
        return new Task(executor, callable, null, hungTime);
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
    public <V> ManagedScheduledFutureTask<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        ScheduledTask<V> task = getNewScheduledTaskFor(callable, false);
        ScheduledFuture<V> future =
                ((ScheduledExecutorService) executor).schedule(callable, delay, unit);
        task.submittedScheduledTask(future);
        return task;
    }

    private <V> ScheduledTask<V> getNewScheduledTaskFor(Callable<V> callable, boolean periodic) {
        return new ScheduledTask<>(executor, callable,
                hungTime, periodic);
    }

    private ScheduledTask<Void> getNewScheduledTaskFor(Runnable command, boolean periodic) {
        return new ScheduledTask<>(executor, command, null,
                hungTime, periodic);
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
    public ManagedScheduledFutureTask<?> schedule(Runnable command, long delay, TimeUnit unit) {
        ScheduledTask<?> task = getNewScheduledTaskFor(command, false);
        ScheduledFuture<?> future =
                ((ScheduledExecutorService) executor).schedule(command, delay, unit);
        task.submittedScheduledTask(future);
        return task;
    }


    @Override
    public ManagedScheduledFutureTask<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        ScheduledTask<?> task = getNewScheduledTaskFor(command, true);
        ScheduledFuture<?> future =
                ((ScheduledExecutorService) executor).scheduleAtFixedRate(command,
                        initialDelay, period, unit);
        task.submittedScheduledTask(future);
        return task;
    }


    @Override
    public ManagedScheduledFutureTask<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        ScheduledTask<?> task = getNewScheduledTaskFor(command, true);
        ScheduledFuture<?> future =
                ((ScheduledExecutorService) executor).scheduleWithFixedDelay(command,
                        initialDelay, delay, unit);
        task.submittedScheduledTask(future);
        return task;
    }


}
