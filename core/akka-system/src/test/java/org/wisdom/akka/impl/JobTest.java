package org.wisdom.akka.impl;

import org.joda.time.Period;
import org.junit.Test;
import org.wisdom.api.scheduler.Scheduled;
import scala.concurrent.duration.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks Job parsing and handling.
 */
public class JobTest {

    @Test
    public void testToDuration() throws Exception {
        Duration duration = Job.toDuration(Period.hours(1));
        assertThat(duration.toHours()).isEqualTo(1);

        duration = Job.toDuration(Period.days(1));
        assertThat(duration.toDays()).isEqualTo(1);

        duration = Job.toDuration(Period.hours(2).plusMinutes(30).plusSeconds(5));
        assertThat(duration.toMillis()).isEqualTo(9005000);
    }

    @Test
    public void testGetDurationFromPeriod() throws Exception {
        assertThat(Job.getDurationFromPeriod("1d").toSeconds()).isEqualTo(24 * 3600);
        assertThat(Job.getDurationFromPeriod("0d2h").toSeconds()).isEqualTo(2 * 3600);
        assertThat(Job.getDurationFromPeriod("1h30m").toSeconds()).isEqualTo(3600 + 30 * 60);
        assertThat(Job.getDurationFromPeriod("1m").toSeconds()).isEqualTo(60);
        assertThat(Job.getDurationFromPeriod("0s").toSeconds()).isEqualTo(0);
        assertThat(Job.getDurationFromPeriod("60s").toSeconds()).isEqualTo(60);
    }

    @Test
    public void testGetFunction() throws NoSuchMethodException {
        MyScheduled scheduled = new MyScheduled();
        Job job = new Job(scheduled, MyScheduled.class.getMethod("operation"), "60s");
        Runnable runnable = job.function();
        assertThat(runnable).isNotNull();
        assertThat(scheduled.called).isFalse();
        runnable.run();
        assertThat(scheduled.called).isTrue();
        assertThat(job.duration().toSeconds()).isEqualTo(60);
    }

    @Test
    public void testInvokeErroneousScheduled() throws NoSuchMethodException {
        MyScheduled scheduled = new MyScheduled();
        Job job = new Job(scheduled, MyScheduled.class.getMethod("error"), "60s");
        Runnable runnable = job.function();
        assertThat(runnable).isNotNull();
        assertThat(scheduled.called).isFalse();
        runnable.run();
        assertThat(scheduled.called).isFalse();
        assertThat(job.duration().toSeconds()).isEqualTo(60);
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
