package org.wisdom.pools;

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

    public ScheduledTask(ListeningExecutorService executor, Callable<V> callable, long hungTime, boolean periodic) {
        super(executor, callable, null, hungTime);
        this.periodic = periodic;
    }

    protected ScheduledTask(ListeningExecutorService executor, Runnable runnable, V result, long hungTime, boolean periodic) {
        super(executor, runnable, result, null, hungTime);
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
}
