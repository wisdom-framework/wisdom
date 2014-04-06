package org.wisdom.monitor;

import akka.actor.Cancellable;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.*;
import com.google.common.collect.ImmutableMap;
import org.apache.felix.ipojo.annotations.Context;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.akka.AkkaSystemService;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.websockets.Publisher;
import org.wisdom.api.templates.Template;
import scala.concurrent.duration.FiniteDuration;

import java.io.ByteArrayOutputStream;
import java.lang.management.ManagementFactory;
import java.util.HashSet;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * The controller providing the monitoring capabilities.
 */
@Controller
@Path("/monitor")
public class MonitorController extends DefaultController {

    private final ThreadDump threadDump;
    @Requires
    Publisher publisher;

    @Requires
    ApplicationConfiguration configuration;

    @Requires
    Json json;

    @Requires
    AkkaSystemService akka;

    @View("monitor")
    Template monitor;

    @View("services")
    Template services;

    @View("bundles")
    Template bundles;

    @Context
    BundleContext context;

    final MetricRegistry metrics;

    private final static Logger LOGGER = LoggerFactory.getLogger(MonitorController.class);
    private Cancellable task;
    private OSGiMetrics osgi;

    public MonitorController() {
        this.metrics = new MetricRegistry();
        this.osgi = new OSGiMetrics(context);
        this.threadDump = new ThreadDump(ManagementFactory.getThreadMXBean());
    }

    @Validate
    public void start() {
        LOGGER.debug("Start counting");
        osgi.reset();
        LOGGER.info("Registering Metrics JVM metrics");
        metrics.register("jvm.memory", new MemoryUsageGaugeSet());
        metrics.register("jvm.garbage", new GarbageCollectorMetricSet());
        metrics.register("jvm.threads", new ThreadStatesGaugeSet());
        metrics.register("jvm.files", new FileDescriptorRatioGauge());
        metrics.register("jvm.buffers", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
        metrics.register("jvm.cpu", new CpuGaugeSet());
        metrics.register("jvm.runtime", new RuntimeGaugeSet());
        metrics.register("osgi", osgi);

        //metrics.register("threadLocks", new ThreadDeadlockDetector(ManagementFactory.getThreadMXBean()));
        task = akka.system().scheduler().schedule(new FiniteDuration(0, TimeUnit.SECONDS),
                new FiniteDuration(5, TimeUnit.SECONDS), new Runnable() {
                    public void run() {
                        publisher.publish("/monitor/update", json.toJson(getData()));
                    }
                }, akka.system().dispatcher()
        );
    }

    @Invalidate
    public void stop() {
        osgi.stop();
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

    @Route(method = HttpMethod.GET, uri = "/")
    public Result index() {
        return ok(render(monitor));
    }

    @Route(method = HttpMethod.GET, uri = "/services")
    public Result svc() {
        return ok(render(services));
    }

    @Route(method = HttpMethod.GET, uri = "/bundles")
    public Result bundle() {
        return ok(render(bundles));
    }

    private int getProviderBundleCount(List<ServiceModel> svc) {
        HashSet<String> set = new HashSet<String>();
        for (ServiceModel service : svc) {
            set.add(service.getProvidingBundle());
        }
        return set.size();
    }

    private int getProviderCount(List<ServiceModel> svc) {
        HashSet<String> set = new HashSet<String>();
        for (ServiceModel service : svc) {
            String name = service.getProperties().get("instance.name");
            if (name != null) {
                set.add(name);
            }
        }
        return set.size();
    }

    @Route(method = HttpMethod.GET, uri = "/metrics")
    public Result metrics() {
        return ok(getData()).json();
    }

    @Route(method = HttpMethod.GET, uri = "/services.json")
    public Result services() {
        final List<ServiceModel> svc = ServiceModel.services(context);
        return ok(ImmutableMap.of(
                "services", svc,
                "events", osgi.serviceEvents,
                "providers", Integer.toString(getProviderCount(svc)),
                "bundles", Integer.toString(getProviderBundleCount(svc)))).json();
    }

    @Route(method = HttpMethod.GET, uri = "/bundles.json")
    public Result bundles() {
        final List<BundleModel> list = BundleModel.bundles(context);
        return ok(ImmutableMap.of(
                "bundles", list,
                "events", osgi.bundleEvents,
                "active", Integer.toString(getActiveBundleCount(list)),
                "installed", Integer.toString(getInstalledBundleCount(list)))).json();
    }

    private int getInstalledBundleCount(List<BundleModel> bundles) {
        int count = 0;
        for (BundleModel bundle : bundles) {
            if ("INSTALLED".equals(bundle.getState())) {
                count++;
            }
        }
        return count;
    }

    private int getActiveBundleCount(List<BundleModel> bundles) {
        int count = 0;
        for (BundleModel bundle : bundles) {
            if ("ACTIVE".equals(bundle.getState())) {
                count++;
            }
        }
        return count;
    }

    @Route(method = HttpMethod.GET, uri = "/bundle/{id}")
    public Result toggleBundle(@Parameter("id") long id) {
        Bundle bundle = context.getBundle(id);
        if (bundle == null) {
            return notFound("Bundle " + id + " not found");
        } else {
            if (bundle.getState() == Bundle.ACTIVE) {
                try {
                    bundle.stop();
                } catch (BundleException e) {
                    return badRequest(e);
                }
            } else if (bundle.getState() == Bundle.INSTALLED || bundle.getState() == Bundle.RESOLVED) {
                try {
                    bundle.start();
                } catch (BundleException e) {
                    return badRequest(e);
                }
            }
        }
        return ok();
    }

    @Route(method = HttpMethod.POST, uri = "/bundle/{id}")
    public Result updateBundle(@Parameter("id") long id) {
        Bundle bundle = context.getBundle(id);
        if (bundle == null) {
            return notFound("Bundle " + id + " not found");
        } else {
            try {
                bundle.update();
            } catch (BundleException e) {
                return badRequest(e);
            }
        }
        return ok();
    }

    @Route(method = HttpMethod.DELETE, uri = "/bundle/{id}")
    public Result uninstallBundle(@Parameter("id") long id) {
        Bundle bundle = context.getBundle(id);
        if (bundle == null) {
            return notFound("Bundle " + id + " not found");
        } else {
            try {
                bundle.uninstall();
            } catch (BundleException e) {
                return badRequest(e);
            }
        }
        return ok();
    }


    private ImmutableMap<String, SortedMap<String, ? extends Metric>> getData() {
        return ImmutableMap.of(
                "gauges", metrics.getGauges(),
                "timers", metrics.getTimers(),
                "meters", metrics.getMeters(),
                "counters", metrics.getCounters(),
                "histograms", metrics.getHistograms()
        );
    }

    @Route(method = HttpMethod.GET, uri = "/thread-dump")
    public Result threadDump() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        threadDump.dump(out);
        return ok(out.toString()).as(MimeTypes.TEXT);
    }
}
