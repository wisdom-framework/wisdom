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
package org.wisdom.api.concurrent;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the test execution statistics
 */
public class ExecutionStatisticsTest {


    @Test
    public void testStatisticsAndCopy() {
        ManagedExecutorService.ExecutionStatistics statistics = new ManagedExecutorService.ExecutionStatistics();
        statistics.accept(9);
        statistics.accept(11l);
        assertThat(statistics.getAverageExecutionTime()).isEqualTo(10);
        assertThat(statistics.getCount()).isEqualTo(2);
        assertThat(statistics.getNumberOfTasks()).isEqualTo(2);
        assertThat(statistics.getMinimumExecutionTime()).isEqualTo(9);
        assertThat(statistics.getMaximumExecutionTime()).isEqualTo(11);
        assertThat(statistics.getTotalExecutionTime()).isEqualTo(20);

        statistics = statistics.copy();
        assertThat(statistics.getAverageExecutionTime()).isEqualTo(10);
        assertThat(statistics.getCount()).isEqualTo(2);
        assertThat(statistics.getNumberOfTasks()).isEqualTo(2);
        assertThat(statistics.getMinimumExecutionTime()).isEqualTo(9);
        assertThat(statistics.getMaximumExecutionTime()).isEqualTo(11);
        assertThat(statistics.getTotalExecutionTime()).isEqualTo(20);
    }

    @Test
    public void testCombine() {
        ManagedExecutorService.ExecutionStatistics statistics = new ManagedExecutorService.ExecutionStatistics();
        statistics.accept(9);

        ManagedExecutorService.ExecutionStatistics statistics2 = new ManagedExecutorService.ExecutionStatistics();
        statistics.accept(11l);

        statistics.combine(statistics2);

        assertThat(statistics.getAverageExecutionTime()).isEqualTo(10);
        assertThat(statistics.getCount()).isEqualTo(2);
        assertThat(statistics.getNumberOfTasks()).isEqualTo(2);
        assertThat(statistics.getMinimumExecutionTime()).isEqualTo(9);
        assertThat(statistics.getMaximumExecutionTime()).isEqualTo(11);
        assertThat(statistics.getTotalExecutionTime()).isEqualTo(20);
    }


}