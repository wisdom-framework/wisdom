package org.wisdom.pools;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.concurrent.ExecutionContextService;
import org.wisdom.api.concurrent.ManagedExecutorService;
import org.wisdom.api.configuration.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * Implementation of the {@link org.wisdom.api.concurrent.ManagedExecutorService}.
 * Instances must be created explicitly.
 */
@Component
@Provides
public class ManagedExecutorServiceImpl extends AbstractManagedExecutorService
        implements ManagedExecutorService {

    @Requires
    List<ExecutionContextService> ecs;

    public ManagedExecutorServiceImpl(@Property(name = "configuration") Configuration configuration) {
        this(
                configuration.getOrDie("name"),
                configuration.get("threadType", ThreadType.class, ThreadType.POOLED),
                configuration.getDuration("hungTime", TimeUnit.MILLISECONDS, 60000),
                configuration.getIntegerWithDefault("coreSize", 5),
                configuration.getIntegerWithDefault("maxSize", 25),
                configuration.getDuration("keepAlive", TimeUnit.MILLISECONDS, 5000),
                configuration.getIntegerWithDefault("workQueueCapacity",
                        Integer.MAX_VALUE),
                configuration.getIntegerWithDefault("priority", Thread.NORM_PRIORITY)
        );
    }

    public ManagedExecutorServiceImpl(
            String name,
            ThreadType tu,
            long hungTime,
            int coreSize,
            int maxSize,
            long keepAlive,
            int workQueueCapacity,
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

        BlockingQueue<Runnable> queue = createWorkQueue(workQueueCapacity);
        setInternalPool(new ThreadPoolExecutor(coreSize, maxSize, keepAlive,
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

    protected <V> Task<V> getNewTaskFor(Runnable task, V result) {
        return new Task<>(executor, task, result, createExecutionContext(ecs),
                hungTime);
    }


    protected <V> Task<V> getNewTaskFor(Callable<V> callable) {
        return new Task(executor, callable, createExecutionContext(ecs), hungTime);
    }


    /**
     * For testing purpose only.
     *
     * @param services the context service
     */
    public void setExecutionContextService(ExecutionContextService... services) {
        ecs = new ArrayList<>();
        Collections.addAll(ecs, services);
    }
}
