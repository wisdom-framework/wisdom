package org.wisdom.akka.impl;

import akka.actor.Cancellable;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.wisdom.api.scheduler.Scheduled;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Structure holding a job.
 */
public class Job {

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
    private final Duration duration;
    private final Scheduled scheduled;
    private Cancellable cancellable;

    public Job(Scheduled scheduled, Method method, String value) {
        this.method = method;
        this.scheduled = scheduled;
        this.duration = getDurationFromPeriod(value);
    }

    public static Duration toDuration(Period period) {
        return Duration.create(period.getDays(), TimeUnit.DAYS)
                .plus(Duration.create(period.getHours(), TimeUnit.HOURS)
                        .plus(Duration.create(period.getMinutes(), TimeUnit.MINUTES)
                                .plus(Duration.create(period.getSeconds(), TimeUnit.SECONDS))));
    }

    public static Duration getDurationFromPeriod(String value) {
        return toDuration(PERIOD_FORMATTER.parsePeriod(value));
    }

    public Method method() {
        return method;
    }

    public FiniteDuration duration() {
        return (FiniteDuration) duration;
    }

    public Runnable getFunction() {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    method.invoke(scheduled);
                } catch (IllegalAccessException e) {
                    AkkaScheduler.LOGGER.error("Error while accessing to the scheduled method {}.{}",
                            scheduled.getClass().getName(), method.getName(), e);
                } catch (InvocationTargetException e) {
                    AkkaScheduler.LOGGER.error("Error in scheduled method {}.{}",
                            scheduled.getClass().getName(), method.getName(), e);
                }
            }
        };
    }

    public void submitted(Cancellable cancellable) {
        this.cancellable = cancellable;
    }

    public Cancellable cancellable() {
        return cancellable;
    }

    public Scheduled scheduled() {
        return scheduled;
    }
}
