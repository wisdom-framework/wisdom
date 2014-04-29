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
import java.lang.management.OperatingSystemMXBean;
import java.util.Map;

/**
 * Some information about the running system.
 */
public class CpuGaugeSet implements MetricSet {


    private OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();

    /**
     * @return the CPU metrics.
     */
    public Map<String, Metric> getMetrics() {

        return ImmutableMap.<String, Metric>of(
                "processors", new Gauge<Integer>() {
                    public Integer getValue() {
                        return bean.getAvailableProcessors();
                    }
                },
                "systemLoadAgerage", new Gauge<Double>() {
                    public Double getValue() {
                        return bean.getSystemLoadAverage();
                    }
                },
                "cpuSystemLoad", new Gauge<Double>() {
                    public Double getValue() {
                        if (bean instanceof com.sun.management.OperatingSystemMXBean) {
                            return ((com.sun.management.OperatingSystemMXBean) bean).getSystemCpuLoad() * 100.0;
                        } else {
                            return -1.0;
                        }
                    }
                },
                "cpuProcessLoad", new Gauge<Double>() {
                    public Double getValue() {
                        if (bean instanceof com.sun.management.OperatingSystemMXBean) {
                            return ((com.sun.management.OperatingSystemMXBean) bean).getProcessCpuLoad() * 100.0;
                        } else {
                            return -1.0;
                        }
                    }
                },
                "cpuSystemUsage", new Gauge<Long>() {
                    public Long getValue() {
                        if (bean instanceof com.sun.management.OperatingSystemMXBean) {
                            return ((com.sun.management.OperatingSystemMXBean) bean).getProcessCpuTime();
                        } else {
                            return -1L;
                        }
                    }
                }
        );
    }
}
