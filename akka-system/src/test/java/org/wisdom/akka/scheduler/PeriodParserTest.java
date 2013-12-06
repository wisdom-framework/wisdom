package org.wisdom.akka.scheduler;

import org.joda.time.Period;
import org.junit.Test;
import org.wisdom.akka.impl.Job;
import org.wisdom.api.annotations.scheduler.Every;
import scala.concurrent.duration.Duration;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Check the period parsing.
 */
public class PeriodParserTest {

    @Test
    public void testPeriods() {
        Duration duration = Job.getDurationFromPeriod("1d6h3s");
        assertThat(duration.toHours()).isEqualTo(30);

        duration = Job.getDurationFromPeriod("10s");
        assertThat(duration.toHours()).isEqualTo(0);
        assertThat(duration.toSeconds()).isEqualTo(10);
    }

    @Test
    public void testPredefinedPeriods() {
        Period period;

        period = Job.PERIOD_FORMATTER.parsePeriod(Every.DAY);
        assertThat(period.getDays()).isEqualTo(1);
        assertThat(period.getHours()).isEqualTo(0);
        assertThat(period.getMinutes()).isEqualTo(0);

        period = Job.PERIOD_FORMATTER.parsePeriod(Every.HOUR);
        assertThat(period.getDays()).isEqualTo(0);
        assertThat(period.getHours()).isEqualTo(1);
        assertThat(period.getMinutes()).isEqualTo(0);

        period = Job.PERIOD_FORMATTER.parsePeriod(Every.MINUTE);
        assertThat(period.getDays()).isEqualTo(0);
        assertThat(period.getHours()).isEqualTo(0);
        assertThat(period.getMinutes()).isEqualTo(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testErroneousPeriod() {
        Job.getDurationFromPeriod("1hour");
    }
}
