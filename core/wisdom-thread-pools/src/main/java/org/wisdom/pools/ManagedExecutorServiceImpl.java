package org.wisdom.pools;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.concurrent.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * Implementation of the {@link org.wisdom.api.concurrent.ManagedExecutorService}.
 * Instances must be created explicitly.
 */
@Component
@Provides
public class ManagedExecutorServiceImpl implements ManagedExecutorService {

    private final String name;
    private final long hungTime;
    private final RejectionPolicy rejectionPolicy;
    private final ListeningExecutorService executor;

    private final Set<Task<?>> tasks = new LinkedHashSet<>();

    private final Logger logger;

    @Requires
    List<ExecutionContextService> ecs;

    public ManagedExecutorServiceImpl(
            String name,
            ThreadFactory factory,
            ThreadType tu,
            long hungTime,
            int coreSize,
            int maxSize,
            long keepAlive,
            int workQueueCapacity,
            RejectionPolicy policy) {

        Preconditions.checkNotNull(name);
        this.name = name;
        this.logger = LoggerFactory.getLogger("executor-" + name);
        ThreadFactoryBuilder builder = new ThreadFactoryBuilder()
                .setDaemon(tu == ThreadType.DAEMON)
                .setNameFormat(name + "-%s")
                        //TODO Customize priority
                .setPriority(Thread.NORM_PRIORITY)
                .setUncaughtExceptionHandler(
                        new Thread.UncaughtExceptionHandler() {
                            @Override
                            public void uncaughtException(Thread t, Throwable e) {
                                logger.error("Uncaught exception in thread '{}'",
                                        t.getName(), e);
                            }
                        });

        if (factory != null) {
            builder.setThreadFactory(factory);
        }


        this.hungTime = hungTime;
        this.rejectionPolicy = policy;

        BlockingQueue<Runnable> queue = createWorkQueue(workQueueCapacity);
        this.executor = MoreExecutors.listeningDecorator(
                new ThreadPoolExecutor(coreSize, maxSize, keepAlive,
                        TimeUnit.MILLISECONDS, queue, builder.build()));
    }

    protected BlockingQueue<Runnable> createWorkQueue(int workQueueCapacity) {
        if (workQueueCapacity < 0) {
            throw new IllegalArgumentException();
        }
        BlockingQueue<Runnable> queue;
        if (workQueueCapacity == Integer.MAX_VALUE) {
            queue = new LinkedBlockingQueue<>();
        } else if (workQueueCapacity == 0) {
            queue = new SynchronousQueue<>();
        } else {
            queue = new ArrayBlockingQueue<>(workQueueCapacity);
        }
        return queue;
    }

    /**
     * @return the name of the thread pool.
     */
    @Override
    public String name() {
        return name;
    }

    @Override
    public Collection<ManagedFutureTask> getHungTasks() {
        List<ManagedFutureTask> hung = new ArrayList<>();
        for (ManagedFutureTask task : tasks) {
            if (task.isTaskHang()) {
                hung.add(task);
            }
        }
        return hung;
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }


    @Override
    public List<Runnable> shutdownNow() {
        for (Task task : tasks) {
            task.cancel(true);
        }
        return executor.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return executor.isShutdown();
    }

    /**
     * Returns {@code true} if all tasks have completed following shut down.
     * Note that {@code isTerminated} is never {@code true} unless
     * either {@code shutdown} or {@code shutdownNow} was called first.
     *
     * @return {@code true} if all tasks have completed following shut down
     */
    @Override
    public boolean isTerminated() {
        return executor.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit)
            throws InterruptedException {
        return executor.awaitTermination(timeout, unit);
    }

    @Override
    public <T> ManagedFutureTask<T> submit(Callable<T> task) {
        if (task == null) {
            throw new NullPointerException();
        }
        return getNewTaskFor(task).execute();
    }

    private <V> Task<V> getNewTaskFor(Runnable task, V result) {
        return new Task<>(executor, task, result, createExecutionContext(),
                hungTime);
    }

    private ExecutionContext createExecutionContext() {
        List<ExecutionContext> ec = new ArrayList<>();
        for (ExecutionContextService ecs : this.ecs) {
            ec.add(ecs.prepare());
        }
        return CompositeExecutionContext.create(ec);
    }

    protected <V> Task<V> getNewTaskFor(Callable<V> callable) {
        return new Task(executor, callable, null, hungTime);
    }

    @Override
    public <T> ManagedFutureTask<T> submit(Runnable task, T result) {
        if (task == null) {
            throw new NullPointerException();
        }
        final Task t = getNewTaskFor(task, result).execute();
        t.addListener(new Runnable() {
            @Override
            public void run() {
                tasks.remove(t);
            }
        });
        tasks.add(t);
        return t;
    }

    @Override
    public ManagedFutureTask<?> submit(Runnable task) {
        if (task == null) {
            throw new NullPointerException();
        }
        final Task t = getNewTaskFor(task, null).execute();
        t.addListener(new Runnable() {
            @Override
            public void run() {
                tasks.remove(t);
            }
        }, executor);
        tasks.add(t);
        return t;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        List<Future<T>> futures = executor.invokeAll(tasks);
        List<Future<T>> manageable = new ArrayList<>(futures.size());
        int i = 0;
        for (Callable<T> callable : tasks) {
            Task task = getNewTaskFor(callable);
            task.submitted(futures.get(i));
            manageable.add(task);
            i++;
        }
        return manageable;
    }


    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        List<Future<T>> futures = executor.invokeAll(tasks, timeout, unit);
        List<Future<T>> manageable = new ArrayList<>(futures.size());
        int i = 0;
        for (Callable<T> callable : tasks) {
            Task task = getNewTaskFor(callable);
            task.submitted(futures.get(i));
            manageable.add(task);
            i++;
        }
        return manageable;
    }


    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
        return executor.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
                           long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return executor.invokeAny(tasks, timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        Task<Void> task = getNewTaskFor(command, null);
        task.execute();
    }
}
