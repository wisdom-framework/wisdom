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

import org.assertj.core.api.Assertions;
import org.joda.time.Period;
import org.junit.Test;
import org.wisdom.api.annotations.scheduler.Every;

/**
 * Check the period parsing.
 */
public class PeriodParserTest {

    @Test
    public void testPeriods() {
        long duration = Job.getDurationFromPeriod(JobTest.create("1d6h3s"));
        Assertions.assertThat(duration).isEqualTo((30 * 60  * 60) + 3);

        duration = Job.getDurationFromPeriod(JobTest.create("10s"));
        Assertions.assertThat(duration).isEqualTo(10);
    }

    @Test
    public void testPredefinedPeriods() {
        Period period;

        period = Job.PERIOD_FORMATTER.parsePeriod(Every.DAY);
        Assertions.assertThat(period.getDays()).isEqualTo(1);
        Assertions.assertThat(period.getHours()).isEqualTo(0);
        Assertions.assertThat(period.getMinutes()).isEqualTo(0);

        period = Job.PERIOD_FORMATTER.parsePeriod(Every.HOUR);
        Assertions.assertThat(period.getDays()).isEqualTo(0);
        Assertions.assertThat(period.getHours()).isEqualTo(1);
        Assertions.assertThat(period.getMinutes()).isEqualTo(0);

        period = Job.PERIOD_FORMATTER.parsePeriod(Every.MINUTE);
        Assertions.assertThat(period.getDays()).isEqualTo(0);
        Assertions.assertThat(period.getHours()).isEqualTo(0);
        Assertions.assertThat(period.getMinutes()).isEqualTo(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testErroneousPeriod() {
        Job.getDurationFromPeriod(JobTest.create("1hour"));
    }
}
