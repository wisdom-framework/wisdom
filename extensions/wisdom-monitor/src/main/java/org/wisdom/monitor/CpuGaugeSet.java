package org.wisdom.monitor;

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
