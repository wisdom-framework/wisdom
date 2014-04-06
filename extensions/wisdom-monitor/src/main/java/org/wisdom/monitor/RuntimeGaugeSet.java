package org.wisdom.monitor;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.google.common.collect.ImmutableMap;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.Map;

/**
 * Some information about the running system.
 */
public class RuntimeGaugeSet implements MetricSet {


    private RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();

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
