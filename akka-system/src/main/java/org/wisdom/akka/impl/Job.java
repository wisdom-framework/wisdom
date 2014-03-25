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
                    AkkaScheduler.getLogger().error("Error while accessing to the scheduled method {}.{}",
                            scheduled.getClass().getName(), method.getName(), e);
                } catch (InvocationTargetException e) {
                    AkkaScheduler.getLogger().error("Error in scheduled method {}.{}",
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
