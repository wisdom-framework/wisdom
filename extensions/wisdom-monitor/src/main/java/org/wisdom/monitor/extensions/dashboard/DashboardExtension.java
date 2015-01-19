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

import com.codahale.metrics.*;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import org.apache.felix.ipojo.annotations.Context;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.View;
import org.wisdom.api.concurrent.ManagedScheduledExecutorService;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.websockets.Publisher;
import org.wisdom.api.security.Authenticated;
import org.wisdom.api.templates.Template;
import org.wisdom.monitor.service.HealthCheck;
import org.wisdom.monitor.service.MonitorExtension;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.wisdom.monitor.extensions.dashboard.HealthState.ko;

/**
 * Monitor extension handling the main dashboard.
 */
@Controller
@Path("/monitor/dashboard")
@Authenticated("Monitor-Authenticator")
public class DashboardExtension extends DefaultController implements MonitorExtension {

    @Requires
    Publisher publisher;

    @Requires
    ApplicationConfiguration configuration;

    @Requires
    Json json;

    @Requires(filter = "(name=" + ManagedScheduledExecutorService.SYSTEM + ")", proxy = false)
    protected ScheduledExecutorService scheduler;

    @Requires(specification = HealthCheck.class, optional = true)
    List<HealthCheck> healthChecks;

    @View("monitor/dashboard")
    Template monitor;

    @Context
    BundleContext bc;

    final MetricRegistry registry;

    private ScheduledFuture task;
    private HttpMetricFilter httpMetricFilter;
    private ServiceRegistration<MetricRegistry> reg;

    /**
     * Creates the instance of dashboard extension.
     */
    public DashboardExtension() {
        this.registry = new MetricRegistry();
    }

    /**
     * Starts the dashboard.
     */
    @Validate
    public void start() {
        logger().info("Registering JVM metrics");
        registry.register("jvm.memory", new MemoryUsageGaugeSet());
        registry.register("jvm.garbage", new GarbageCollectorMetricSet());
        registry.register("jvm.threads", new ThreadStatesGaugeSet());
        registry.register("jvm.buffers", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
        registry.register("jvm.cpu", new CpuGaugeSet());
        registry.register("jvm.runtime", new RuntimeGaugeSet());

        if (configuration.getBooleanWithDefault("monitor.http.enabled", true)) {
            logger().info("Registering HTTP metrics");
            this.httpMetricFilter = new HttpMetricFilter(bc, configuration, registry);
            httpMetricFilter.start();
        }

        if (configuration.getBooleanWithDefault("monitor.jmx.enabled", true)) {
            logger().info("Initializing Metrics JMX reporting");
            final JmxReporter jmxReporter = JmxReporter.forRegistry(registry).build();
            jmxReporter.start();
        }

        if (configuration.getBooleanWithDefault("monitor.graphite.enabled", false)) {
            logger().info("Initializing Metrics Graphite reporting");
            String graphiteHost = configuration.getOrDie("monitor.graphite.host");
            int graphitePort = configuration.getIntegerOrDie("monitor.graphite.port");
            Graphite graphite = new Graphite(new InetSocketAddress(graphiteHost, graphitePort));
            GraphiteReporter graphiteReporter = GraphiteReporter.forRegistry(registry)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .build(graphite);
            graphiteReporter.start(1, TimeUnit.MINUTES);
        }

        logger().info("Registering the metric registry as service");
        reg = bc.registerService(MetricRegistry.class, registry, null);

        task = scheduler.scheduleAtFixedRate(new Runnable() {
            /**
             * Sends updated data to the websocket.
             */
            public void run() {
                publisher.publish("/monitor/update", json.toJson(getData()));
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    /**
     * Sends the current metrics.
     *
     * @return the metrics.
     */
    @Route(method = HttpMethod.GET, uri = "/metrics")
    public Result metrics() {
        return ok(getData()).json();
    }

    /**
     * Build an immutable map containing the current metrics.
     *
     * @return the current metrics.
     */
    private ImmutableMap<String, ?> getData() {
        long active = 0;
        Counter counter = registry.counter("http.activeRequests");
        if (counter != null) {
            active = counter.getCount();
        }

        return ImmutableMap.<String, Object>builder()
                .put("gauges", registry.getGauges())
                .put("activeRequests", active)
                .put("timers", registry.getTimers())
                .put("counters", registry.getCounters())
                .put("meters", registry.getMeters())
                .put("histograms", registry.getHistograms())
                .put("health", getHealth())
                .build();
    }

    /**
     * @return the map name - heath check of all health sensors.
     */
    private SortedMap<String, HealthState> getHealth() {
        SortedMap<String, HealthState> map = new TreeMap<>();

        for (HealthCheck hc : healthChecks) {
            try {
                if (hc.check()) {
                    map.put(hc.name(), HealthState.ok());
                } else {
                    map.put(hc.name(), ko());
                }
            } catch (Exception e) { //NOSONAR
                map.put(hc.name(), ko(e));
            }
        }
        return map;
    }

    /**
     * @return information about threads.
     */
    @Route(method = HttpMethod.GET, uri = "/threads")
    public Result threads() {
        ArrayNode array = json.newArray();
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        for (long id : bean.getAllThreadIds()) {
            ObjectNode node = json.newObject();
            ThreadInfo ti = bean.getThreadInfo(id, 10);
            node
                    .put("threadName", ti.getThreadName())
                    .put("threadId", ti.getThreadId())
                    .put("blockedTime", ti.getBlockedTime())
                    .put("blockedCount", ti.getBlockedCount())
                    .put("lockName", ti.getLockName())
                    .put("waitedTime", ti.getWaitedTime())
                    .put("waitedCount", ti.getWaitedCount())
                    .put("threadState", ti.getThreadState().toString())
                    .put("stack", stack(ti.getStackTrace()));
            array.add(node);
        }
        return ok(array);
    }

    private String stack(StackTraceElement[] stackTrace) {
        StringBuilder stack = new StringBuilder();
        for (StackTraceElement element : stackTrace) {
            if (stack.length() != 0) {
                stack.append('\n');
            }
            stack
                    .append(element.getClassName()).append(".").append(element.getMethodName())
                    .append(" (").append(element.getFileName()).append(':').append(element.getLineNumber()).append(')');
        }
        return stack.toString();
    }

    /**
     * Stops the dashboard.
     * It stops the web socket publication, and the sensors.
     */
    @Invalidate
    public void stop() {
        if (reg != null) {
            reg.unregister();
            reg = null;
        }

        if (task != null && !task.isCancelled()) {
            task.cancel(true);
        }

        if (httpMetricFilter != null) {
            httpMetricFilter.stop();
        }

        registry.removeMatching(new MetricFilter() {
            /**
             * Returns true to remove all metrics.
             * @param s the name
             * @param metric the metric
             * @return {@code true}
             */
            public boolean matches(String s, Metric metric) {
                return true;
            }
        });
    }

    /**
     * @return the dashboard page.
     */
    @Route(method = HttpMethod.GET, uri = "")
    public Result index() {
        return ok(render(monitor));
    }

    /**
     * @return "Dashboard"
     */
    @Override
    public String label() {
        return "Dashboard";
    }

    /**
     * @return the dashboard page url.
     */
    @Override
    public String url() {
        return "/monitor/dashboard";
    }

    /**
     * @return the "root" category.
     */
    @Override
    public String category() {
        return "root";
    }
}
