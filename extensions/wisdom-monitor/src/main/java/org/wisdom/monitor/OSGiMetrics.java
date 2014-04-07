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
package org.wisdom.monitor;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.google.common.collect.ImmutableMap;
import org.osgi.framework.*;

import java.util.Map;

/**
 * Some metrics about the OSGi environment.
 */
public class OSGiMetrics implements BundleListener, ServiceListener, MetricSet {

    private BundleContext context;
    protected int bundleEvents;
    protected int serviceEvents;

    public OSGiMetrics(BundleContext bc) {
        this.context = bc;
    }

    public void reset() {
        bundleEvents = 0;
        serviceEvents = 0;
        context.removeBundleListener(this);
        context.addServiceListener(this);
    }

    public void stop() {
        context.removeBundleListener(this);
        context.removeServiceListener(this);
    }

    /**
     * Receives notification that a bundle has had a lifecycle change.
     *
     * @param event The <code>BundleEvent</code>.
     */
    public void bundleChanged(BundleEvent event) {
        bundleEvents++;
    }

    /**
     * Receives notification that a service has had a lifecycle change.
     *
     * @param event The <code>ServiceEvent</code> object.
     */
    public void serviceChanged(ServiceEvent event) {
        serviceEvents++;
    }

    public Map<String, Metric> getMetrics() {
        return ImmutableMap.<String, Metric>builder()
                .put(
                        "bundle.installed", new Gauge<Integer>() {
                            public Integer getValue() {
                                return getInstalledCount();
                            }
                        }
                ).put(
                        "bundle.resolved", new Gauge<Integer>() {
                            public Integer getValue() {
                                return getResolvedCount();
                            }
                        }
                ).put(
                        "bundle.active", new Gauge<Integer>() {
                            public Integer getValue() {
                                return getActiveCount();
                            }
                        }
                ).put(
                        "bundle.events", new Gauge<Integer>() {
                            public Integer getValue() {
                                return bundleEvents;
                            }
                        }
                ).put(
                        "service.published", new Gauge<Integer>() {
                            public Integer getValue() {
                                return getServiceCount();
                            }
                        }
                ).put(
                        "service.events", new Gauge<Integer>() {
                            public Integer getValue() {
                                return serviceEvents;
                            }
                        }
                ).build();
    }

    private Integer getInstalledCount() {
        int count = 0;
        for (Bundle bundle : context.getBundles()) {
            if (bundle.getState() == Bundle.INSTALLED) {
                count++;
            }
        }
        return count;
    }

    private Integer getResolvedCount() {
        int count = 0;
        for (Bundle bundle : context.getBundles()) {
            if (bundle.getState() == Bundle.RESOLVED) {
                count++;
            }
        }
        return count;
    }

    private Integer getActiveCount() {
        int count = 0;
        for (Bundle bundle : context.getBundles()) {
            if (bundle.getState() == Bundle.ACTIVE) {
                count++;
            }
        }
        return count;
    }

    private Integer getServiceCount() {
        try {
            return context.getAllServiceReferences(null, null).length;
        } catch (InvalidSyntaxException e) {
            return -1;
        }

    }
}
