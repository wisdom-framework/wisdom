/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
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
package org.wisdom.executors.scheduler;

import com.google.common.base.Strings;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.wisdom.api.annotations.scheduler.Every;
import org.wisdom.api.concurrent.ManagedScheduledFutureTask;
import org.wisdom.api.scheduler.Scheduled;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Structure holding a job.
 */
public class Job {

    /**
     * The period formatter to parse the perdiod given as String.
     */
    public static final PeriodFormatter PERIOD_FORMATTER = new PeriodFormatterBuilder()
            .printZeroRarelyFirst()
            .appendDays()
            .appendSuffix("d", "d")
            .printZeroRarelyLast()
            .appendHours()
            .appendSuffix("h", "h")
            .printZeroRarelyLast()
            .appendMinutes()
            .appendSuffix("m", "m")
            .printZeroRarelyLast()
            .appendSeconds()
            .appendSuffix("s", "s")
            .toFormatter();
    private final Method method;
    private final Scheduled scheduled;
    private final TimeUnit unit;
    private ManagedScheduledFutureTask task;
    private long period;

    /**
     * Creates a new instance of Job.
     *
     * @param scheduled the scheduled object, must not be {@literal null}
     * @param method    the method to call on this scheduled object, must not be {@literal null}
     * @param every     the every annotation
     */
    public Job(Scheduled scheduled, Method method, Every every) {
        this.method = method;
        this.scheduled = scheduled;
        if (every.period() > 0) {
            this.period = every.period();
            this.unit = every.unit();
        } else {
            this.period = getDurationFromPeriod(every);
            this.unit = TimeUnit.SECONDS;
            if (this.period == -1) {
                throw new IllegalArgumentException("Cannot retrieve the period of the @Every annotation of " + method
                        .getName() + ", neither the period as String nor as long was given");
            }
        }
    }

    /**
     * Translates the given (Joda) Period to duration in seconds.
     *
     * @param period the period
     * @return the duration representing the same amount of time in seconds
     */
    public static long toDuration(Period period) {
        return period.toStandardSeconds().getSeconds();
    }

    /**
     * Parses the given String as a period and returns the number of seconds.
     * The given String must follows this syntax: XdYhZmTs.
     *
     * @param every the period to parse
     * @return the parsed period in seconds, -1 if there are no period
     */
    public static long getDurationFromPeriod(Every every) {
        if (!Strings.isNullOrEmpty(every.value())) {
            return toDuration(PERIOD_FORMATTER.parsePeriod(every.value()));
        }
        return -1;
    }

    public Method method() {
        return method;
    }

    /**
     * Gets the runnable invoking the scheduled method.
     *
     * @return the runnable.
     */
    public Runnable function() {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    method.invoke(scheduled);
                } catch (IllegalAccessException e) {
                    WisdomTaskScheduler.getLogger().error("Error while accessing to the scheduled method {}.{}",
                            scheduled.getClass().getName(), method.getName(), e);
                } catch (InvocationTargetException e) {
                    WisdomTaskScheduler.getLogger().error("Error in scheduled method {}.{}",
                            scheduled.getClass().getName(), method.getName(), e);
                }
            }
        };
    }

    /**
     * Method called when the job is submitted. It provides a reference to the task object.
     *
     * @param task the object used to cancel the task.
     */
    public void submitted(ManagedScheduledFutureTask task) {
        this.task = task;
    }

    /**
     * @return the Cancellable object, {@literal null} if the job is not yet submitted.
     */
    public ManagedScheduledFutureTask task() {
        return task;
    }

    /**
     * @return the scheduled object.
     */
    public Scheduled scheduled() {
        return scheduled;
    }

    /**
     * @return the period.
     */
    public long period() {
        return period;
    }

    /**
     * @return the time unit.
     */
    public TimeUnit unit() {
        return unit;
    }
}
