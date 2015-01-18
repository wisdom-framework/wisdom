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
package org.wisdom.executors.scheduler;

import org.joda.time.Period;
import org.junit.Test;
import org.wisdom.api.annotations.scheduler.Every;
import org.wisdom.api.scheduler.Scheduled;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Checks Job parsing and handling.
 */
public class JobTest {

    public static Every create(String period) {
        Every every = mock(Every.class);
        when(every.value()).thenReturn(period);
        // Default values
        when(every.unit()).thenReturn(TimeUnit.SECONDS);
        when(every.period()).thenReturn(-1L);
        return every;
    }

    public static Every create(long time, TimeUnit unit) {
        Every every = mock(Every.class);
        when(every.value()).thenReturn("");
        when(every.unit()).thenReturn(unit);
        when(every.period()).thenReturn(time);
        return every;
    }

    @Test
    public void testToDuration() throws Exception {
        long duration = Job.toDuration(Period.hours(1));
        assertThat(duration).isEqualTo(3600);

        duration = Job.toDuration(Period.days(1));
        assertThat(duration).isEqualTo(24 * 3600);

        duration = Job.toDuration(Period.hours(2).plusMinutes(30).plusSeconds(5));
        assertThat(duration).isEqualTo(9005);
    }

    @Test
    public void testGetDurationFromPeriod() throws Exception {
        assertThat(Job.getDurationFromPeriod(create("1d"))).isEqualTo(24 * 3600);
        assertThat(Job.getDurationFromPeriod(create("0d2h"))).isEqualTo(2 * 3600);
        assertThat(Job.getDurationFromPeriod(create("1h30m"))).isEqualTo(3600 + 30 * 60);
        assertThat(Job.getDurationFromPeriod(create("1m"))).isEqualTo(60);
        assertThat(Job.getDurationFromPeriod(create("0s"))).isEqualTo(0);
        assertThat(Job.getDurationFromPeriod(create("60s"))).isEqualTo(60);
    }

    @Test
    public void testGetDurationFromAmountAndUnit() throws Exception {
        Job job = new Job(null, null, create(1, TimeUnit.DAYS));
        assertThat(job.period()).isEqualTo(1);
        assertThat(job.unit()).isEqualTo(TimeUnit.DAYS);

        job = new Job(null, null, create(2, TimeUnit.HOURS));
        assertThat(job.period()).isEqualTo(2);
        assertThat(job.unit()).isEqualTo(TimeUnit.HOURS);
    }

    @Test
    public void testGetFunction() throws NoSuchMethodException {
        MyScheduled scheduled = new MyScheduled();
        Job job = new Job(scheduled, MyScheduled.class.getMethod("operation"), create("60s"));
        Runnable runnable = job.function();
        assertThat(runnable).isNotNull();
        assertThat(scheduled.called).isFalse();
        runnable.run();
        assertThat(scheduled.called).isTrue();
        assertThat(job.period()).isEqualTo(60);
    }

    @Test
    public void testInvokeErroneousScheduled() throws NoSuchMethodException {
        MyScheduled scheduled = new MyScheduled();
        Job job = new Job(scheduled, MyScheduled.class.getMethod("error"), create("60s"));
        Runnable runnable = job.function();
        assertThat(runnable).isNotNull();
        assertThat(scheduled.called).isFalse();
        runnable.run();
        assertThat(scheduled.called).isFalse();
        assertThat(job.period()).isEqualTo(60);
    }

    private class MyScheduled implements Scheduled {

        private boolean called;

        public void operation() {
            called = true;
        }

        public void error() {
            called = false;
            throw new NullPointerException("My Bad");
        }
    }
}