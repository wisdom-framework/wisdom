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
package org.wisdom.monitor.extensions.dashboard;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class RuntimeGaugeSetTest {

    @Test
    public void testUptimeRetrieval() {
        RuntimeGaugeSet gauge = new RuntimeGaugeSet();
        Map<String, Metric> metrics = gauge.getMetrics();

        assertThat(metrics).containsKey("uptime");
        assertThat(metrics.get("uptime")).isNotNull().isInstanceOf(Gauge.class);
        assertThat(((Gauge<Long>) metrics.get("uptime")).getValue()).isGreaterThan(0);
    }

}