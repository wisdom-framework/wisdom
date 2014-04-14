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
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.*;
import com.google.common.collect.ImmutableMap;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.akka.AkkaSystemService;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.View;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.websockets.Publisher;
import org.wisdom.api.templates.Template;
import org.wisdom.monitor.service.HealthCheck;
import org.wisdom.monitor.service.MonitorExtension;
import scala.concurrent.duration.FiniteDuration;

import java.io.ByteArrayOutputStream;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * Monitor extension handling the main dashboard.
 */
@Controller
@Path("/monitor/dashboard")
public class DashboardExtension extends DefaultController implements MonitorExtension {

    private final ThreadDump threadDump;

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

    final MetricRegistry metrics;

    private final static Logger LOGGER = LoggerFactory.getLogger(DashboardExtension.class);

    private Cancellable task;

    public DashboardExtension() {
        this.metrics = new MetricRegistry();
        this.threadDump = new ThreadDump(ManagementFactory.getThreadMXBean());
    }

    @Validate
    public void start() {
        LOGGER.info("Registering Metrics JVM metrics");
        metrics.register("jvm.memory", new MemoryUsageGaugeSet());
        metrics.register("jvm.garbage", new GarbageCollectorMetricSet());
        metrics.register("jvm.threads", new ThreadStatesGaugeSet());
        metrics.register("jvm.files", new FileDescriptorRatioGauge());
        metrics.register("jvm.buffers", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
        metrics.register("jvm.cpu", new CpuGaugeSet());
        metrics.register("jvm.runtime", new RuntimeGaugeSet());

        //metrics.register("threadLocks", new ThreadDeadlockDetector(ManagementFactory.getThreadMXBean()));
        task = akka.system().scheduler().schedule(new FiniteDuration(0, TimeUnit.SECONDS),
                new FiniteDuration(10, TimeUnit.SECONDS), new Runnable() {
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

    private ImmutableMap<String, SortedMap<String, ?>> getData() {
        return ImmutableMap.of(
                "gauges", metrics.getGauges(),
                "health", getHealth()
        );
    }

    private SortedMap<String, Boolean> getHealth() {
        SortedMap<String, Boolean> map = new TreeMap<>();
        for (HealthCheck hc : healthChecks) {
            map.put(hc.name(), hc.check());
        }
        return map;
    }

    @Route(method = HttpMethod.GET, uri = "/thread-dump")
    public Result threadDump() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        threadDump.dump(out);
        return ok(out.toString()).as(MimeTypes.TEXT);
    }

    @Invalidate
    public void stop() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
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
