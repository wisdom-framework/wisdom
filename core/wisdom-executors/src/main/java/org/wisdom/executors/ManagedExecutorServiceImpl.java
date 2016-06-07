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
import org.wisdom.api.concurrent.ManagedExecutorService;
import org.wisdom.api.configuration.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * Implementation of the {@link org.wisdom.api.concurrent.ManagedExecutorService}.
 */
public class ManagedExecutorServiceImpl extends AbstractManagedExecutorService
        implements ManagedExecutorService {

    public ManagedExecutorServiceImpl(String name, Configuration configuration, List<ExecutionContextService> ecs) {
        this(
                name,
                configuration.get("threadType", ThreadType.class, ThreadType.POOLED),
                configuration.getDuration("hungTime", TimeUnit.MILLISECONDS, 60000),
                configuration.getIntegerWithDefault("coreSize", 5),
                configuration.getIntegerWithDefault("maxSize", 25),
                configuration.getDuration("keepAlive", TimeUnit.MILLISECONDS, 5000),
                configuration.getBooleanWithDefault("allowCoreThreadTimeOut", true),
                configuration.getIntegerWithDefault("workQueueCapacity",
                        Integer.MAX_VALUE),
                configuration.getIntegerWithDefault("priority", Thread.NORM_PRIORITY),
                ecs);
    }

    public ManagedExecutorServiceImpl(
            String name,
            ThreadType tu,
            long hungTime,
            int coreSize,
            int maxSize,
            long keepAlive,
            boolean allowCoreThreadTimeOut,
            int workQueueCapacity,
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

        BlockingQueue<Runnable> queue = createWorkQueue(workQueueCapacity);
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(coreSize, maxSize, keepAlive,
                TimeUnit.MILLISECONDS, queue, builder.build(), new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                System.out.println("REJECTED EXECUTION : " + r);
            }
        });
        executor.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
        setInternalPool(executor);
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

    protected synchronized <V> Task<V> getNewTaskFor(Runnable task, V result) {
        return new Task<>(executor, task, result, createExecutionContext(),
                hungTime, this);
    }


    protected <V> Task<V> getNewTaskFor(Callable<V> callable) {
        return new Task(executor, callable, createExecutionContext(), hungTime, this);
    }


    /**
     * Set the context services. For testing purpose only.
     *
     * @param services the context services
     */
    public void setExecutionContextService(ExecutionContextService... services) {
        ecs = new ArrayList<>();
        Collections.addAll(ecs, services);
    }

}
