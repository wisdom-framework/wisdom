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

import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListeningExecutorService;
import org.wisdom.api.concurrent.ManagedScheduledFutureTask;

import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of scheduled task.
 * Be aware that scheduled tasks do not support execution context.
 */
public class ScheduledTask<V> extends Task<V> implements ManagedScheduledFutureTask<V>, ScheduledFuture<V> {

    private final boolean periodic;
    private ScheduledFuture scheduledFuture;

    public ScheduledTask(ListeningExecutorService executor, Callable<V> callable, long hungTime, boolean periodic,
                         AbstractManagedExecutorService parent) {
        super(executor, callable, null, hungTime, parent);
        this.periodic = periodic;
    }

    protected ScheduledTask(ListeningExecutorService executor, Runnable runnable, V result, long hungTime, boolean
            periodic,  AbstractManagedExecutorService parent) {
        super(executor, runnable, result, null, hungTime, parent);
        this.periodic = periodic;

    }

    protected ScheduledTask<V> submittedScheduledTask(ScheduledFuture delegate) {
        this.submissionDate = System.currentTimeMillis();
        this.scheduledFuture = delegate;
        this.future = JdkFutureAdapters.listenInPoolThread(delegate);
        return this;
    }


    /**
     * Returns {@code true} if this task is periodic. A periodic task may
     * re-run according to some schedule. A non-periodic task can be
     * run only once.
     *
     * @return {@code true} if this task is periodic
     */
    @Override
    public boolean isPeriodic() {
        return periodic;
    }

    /**
     * Returns the remaining delay associated with this object, in the
     * given time unit.
     *
     * @param unit the time unit
     * @return the remaining delay; zero or negative values indicate
     * that the delay has already elapsed
     */
    @Override
    public long getDelay(TimeUnit unit) {
        return scheduledFuture.getDelay(unit);
    }


    @Override
    public int compareTo(Delayed o) {
        return scheduledFuture.compareTo(o);
    }

    /**
     * Wraps the enhanced callable as a runnable.
     * @return the wrapped runnable.
     */
    public Runnable asRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    callable.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
