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
import com.codahale.metrics.MetricSet;
import com.google.common.collect.ImmutableMap;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Map;

/**
 * Some information about the running system.
 */
public class RuntimeGaugeSet implements MetricSet {


    private RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();

    /**
     * @return a map containing the runtime information, mainly the uptime.
     */
    public Map<String, Metric> getMetrics() {
        return ImmutableMap.<String, Metric>of(
                "uptime", new Gauge<Long>() {
                    public Long getValue() {
                        return bean.getUptime();
                    }
                }
        );
    }
}
