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

    /**
     * The key to retrieve the number of processor.
     */
    public static final String PROCESSORS = "processors";

    /**
     * The key to retrieve the system load.
     */
    public static final String SYSTEM_LOAD_AVERAGE = "systemLoadAverage";

    /**
     * The key to retrieve the CPU system load.
     */
    public static final String CPU_SYSTEM_LOAD = "cpuSystemLoad";

    /**
     * The key to retrieve the CPU process load.
     */
    public static final String CPU_PROCESS_LOAD = "cpuProcessLoad";

    /**
     * The key to retrieve the CPU process time.
     */
    public static final String CPU_PROCESS_TIME = "cpuProcessTime";

    private OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();

    /**
     * @return the CPU metrics. The returned map contains the following metrics: number of processors,
     * average system load, CPU system load, CPU process load, and Process CPU time. These values are retrieved using
     * JMX and in particular the Sun's implementation of the OperatingSystemMXBean MBean. This bean is not necessarily
     * available. In this case {@literal -1} is returned.
     */
    public Map<String, Metric> getMetrics() {

        return ImmutableMap.<String, Metric>of(
                PROCESSORS, new Gauge<Integer>() {
                    /**
                     * @return the number of processor.
                     */
                    public Integer getValue() {
                        return bean.getAvailableProcessors();
                    }
                },
                SYSTEM_LOAD_AVERAGE, new Gauge<Double>() {
                    /**
                     * @return the average system load.
                     */
                    public Double getValue() {
                        return bean.getSystemLoadAverage();
                    }
                },
                CPU_SYSTEM_LOAD, new Gauge<Double>() {
                    /**
                     * @return the system CPU load.
                     */
                    public Double getValue() {
                        return getSystemCpuLoad();
                    }
                },
                CPU_PROCESS_LOAD, new Gauge<Double>() {
                    /**
                     * @return the process CPU load.
                     */
                    public Double getValue() {
                        return getCpuProcessLoad();
                    }
                },
                CPU_PROCESS_TIME, new Gauge<Long>() {
                    /**
                     * @return the process CPU load.
                     */
                    public Long getValue() {
                        return getCpuTime();
                    }
                }
        );
    }

    /**
     * This method uses a Sun specific class (implementation of the Operating System MX Bean.
     * @return the CPU system load, or {@literal -1.0} if not available.
     */
    private Double getSystemCpuLoad() {
        if (bean instanceof com.sun.management.OperatingSystemMXBean) { //NOSONAR
            return ((com.sun.management.OperatingSystemMXBean) bean).getSystemCpuLoad() * 100.0;  //NOSONAR
        } else {
            return -1.0;
        }
    }

    /**
     * This method uses a Sun specific class (implementation of the Operating System MX Bean.
     * @return the CPU process load, or {@literal -1.0} if not available.
     */
    private Double getCpuProcessLoad() {
        if (bean instanceof com.sun.management.OperatingSystemMXBean) { //NOSONAR
            return ((com.sun.management.OperatingSystemMXBean) bean).getProcessCpuLoad() * 100.0; //NOSONAR
        } else {
            return -1.0;
        }
    }

    /**
     * This method uses a Sun specific class (implementation of the Operating System MX Bean.
     * @return the CPU process time, or {@literal -1L} if not available.
     */
    private Long getCpuTime() {
        if (bean instanceof com.sun.management.OperatingSystemMXBean) { //NOSONAR
            return ((com.sun.management.OperatingSystemMXBean) bean).getProcessCpuTime();  //NOSONAR
        } else {
            return -1L;
        }
    }
}
