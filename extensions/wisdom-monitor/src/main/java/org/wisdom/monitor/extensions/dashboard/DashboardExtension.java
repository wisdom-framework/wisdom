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

import akka.actor.Cancellable;
import com.codahale.metrics.*;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.jvm.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import org.apache.felix.ipojo.annotations.Context;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.wisdom.akka.AkkaSystemService;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.View;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.websockets.Publisher;
import org.wisdom.api.templates.Template;
import org.wisdom.monitor.service.HealthCheck;
import org.wisdom.monitor.service.MonitorExtension;
import scala.concurrent.duration.FiniteDuration;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static org.wisdom.monitor.extensions.dashboard.HealthState.ko;

/**
 * Monitor extension handling the main dashboard.
 */
@Controller
@Path("/monitor/dashboard")
public class DashboardExtension extends DefaultController implements MonitorExtension {

    @Requires
    Publisher publisher;

    @Requires
    ApplicationConfiguration configuration;

    @Requires
    Json json;

    @Requires
    AkkaSystemService akka;

    @Requires(specification = HealthCheck.class, optional = true)
    List<HealthCheck> healthChecks;

    @View("monitor")
    Template monitor;

    @Context
    BundleContext bc;

    final MetricRegistry metrics;

    private Cancellable task;
    private HttpMetricFilter httpMetricFilter;

    public DashboardExtension() {
        this.metrics = new MetricRegistry();
    }

    @Validate
    public void start() {
        logger().info("Registering JVM metrics");
        metrics.register("jvm.memory", new MemoryUsageGaugeSet());
        metrics.register("jvm.garbage", new GarbageCollectorMetricSet());
        metrics.register("jvm.threads", new ThreadStatesGaugeSet());
        metrics.register("jvm.files", new FileDescriptorRatioGauge());
        metrics.register("jvm.buffers", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
        metrics.register("jvm.cpu", new CpuGaugeSet());
        metrics.register("jvm.runtime", new RuntimeGaugeSet());

        if (configuration.getBooleanWithDefault("monitor.http.enabled", true)) {
            logger().info("Registering HTTP metrics");
            this.httpMetricFilter = new HttpMetricFilter(bc, configuration, metrics);
            httpMetricFilter.start();
        }

        if (configuration.getBooleanWithDefault("monitoring.jmx.enabled", true)) {
            logger().info("Initializing Metrics JMX reporting");
            final JmxReporter jmxReporter = JmxReporter.forRegistry(metrics).build();
            jmxReporter.start();
        }

        if (configuration.getBooleanWithDefault("monitoring.graphite.enabled", false)) {
            logger().info("Initializing Metrics Graphite reporting");
            String graphiteHost = configuration.getOrDie("monitoring.graphite.host");
            int graphitePort = configuration.getIntegerOrDie("monitoring.graphite.port");
            Graphite graphite = new Graphite(new InetSocketAddress(graphiteHost, graphitePort));
            GraphiteReporter graphiteReporter = GraphiteReporter.forRegistry(metrics)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .build(graphite);
            graphiteReporter.start(1, TimeUnit.MINUTES);
        }

        task = akka.system().scheduler().schedule(new FiniteDuration(0, TimeUnit.SECONDS),
                new FiniteDuration(configuration.getIntegerWithDefault("monitoring.period", 10), TimeUnit.SECONDS), new Runnable() {
                    public void run() {
                        publisher.publish("/monitor/update", json.toJson(getData()));
                    }
                }, akka.system().dispatcher()
        );


    }

    @Route(method = HttpMethod.GET, uri = "/metrics")
    public Result metrics() {
        return ok(getData()).json();
    }

    private ImmutableMap<String, ?> getData() {
        long active = 0;
        Counter counter = metrics.counter("http.activeRequests");
        if (counter != null) {
            active = counter.getCount();
        }

        return ImmutableMap.<String, Object>builder()
                .put("gauges", metrics.getGauges())
                .put("activeRequests", active)
                .put("timers", metrics.getTimers())
                .put("counter", metrics.getCounters())
                .put("meter", metrics.getMeters())
                .put("health", getHealth())
                .build();
    }

    private SortedMap<String, HealthState> getHealth() {
        SortedMap<String, HealthState> map = new TreeMap<>();

        for (HealthCheck hc : healthChecks) {
            try {
                if (hc.check()) {
                    map.put(hc.name(), HealthState.ok());
                } else {
                    map.put(hc.name(), ko());
                }
            } catch (Exception e) {
                map.put(hc.name(), ko(e));
            }
        }
        return map;
    }

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
        String stack = "";
        for (StackTraceElement element : stackTrace) {
            if (!stack.isEmpty()) {
                stack += "\n";
            }
            stack += element.getClassName() + "." + element.getMethodName() + "(" + element.getFileName() + ":" +
                    element.getLineNumber() + ")";
        }
        return stack;
    }

    @Invalidate
    public void stop() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }

        if (httpMetricFilter != null) {
            httpMetricFilter.stop();
        }

        // Remove all.
        metrics.removeMatching(new MetricFilter() {
            public boolean matches(String s, Metric metric) {
                return true;
            }
        });
    }

    @Route(method = HttpMethod.GET, uri = "")
    public Result index() {
        return ok(render(monitor));
    }

    @Override
    public String label() {
        return "Dashboard";
    }

    @Override
    public String url() {
        return "/monitor/dashboard";
    }

    @Override
    public String category() {
        return "root";
    }
}
