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

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Status;
import org.wisdom.api.interception.Filter;
import org.wisdom.api.interception.RequestContext;
import org.wisdom.api.router.Route;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * A class exposing a HTTP Request filter to compute HTTP metrics.
 */
public class HttpMetricFilter implements Filter, Status {

    private final BundleContext context;
    private final Pattern interceptionPattern;
    private final Integer interceptionPriority;
    private ServiceRegistration<Filter> reg;

    // initialized after call of init method
    private ConcurrentMap<Integer, Meter> metersByStatusCode;
    private Meter otherMeter;
    private Counter activeRequests;
    private Timer requestTimer;


    /**
     * Creates a new instance of the filter.
     *
     * @param context       the bundle context
     * @param configuration the application configuration
     * @param registry      the metric registry
     */
    public HttpMetricFilter(BundleContext context, ApplicationConfiguration configuration,
                            MetricRegistry registry) {
        this.context = context;
        Map<Integer, String> meterNamesByStatusCode = createMeterNamesByStatusCode();
        this.interceptionPattern = Pattern.compile(configuration.getWithDefault("monitor.http.interception", ".*"));
        this.interceptionPriority = configuration.getIntegerWithDefault("monitor.http.priority", 10000);
        this.metersByStatusCode = new ConcurrentHashMap<>(meterNamesByStatusCode
                .size());
        for (Map.Entry<Integer, String> entry : meterNamesByStatusCode.entrySet()) {
            metersByStatusCode.put(entry.getKey(),
                    registry.meter("http.responseCodes." + entry.getValue()));
        }
        this.otherMeter = registry.meter("http.responseCodes.others");
        this.activeRequests = registry.counter("http.activeRequests");
        this.requestTimer = registry.timer("http.requests");
    }

    private static Map<Integer, String> createMeterNamesByStatusCode() {
        final Map<Integer, String> meterNamesByStatusCode = new HashMap<>(6);
        meterNamesByStatusCode.put(OK, "ok");
        meterNamesByStatusCode.put(NOT_MODIFIED, "notModified");
        meterNamesByStatusCode.put(BAD_REQUEST, "badRequest");
        meterNamesByStatusCode.put(NOT_FOUND, "notFound");
        meterNamesByStatusCode.put(INTERNAL_SERVER_ERROR, "serverError");
        return meterNamesByStatusCode;
    }

    /**
     * Starts the filter.
     */
    public void start() {
        reg = context.registerService(Filter.class, this, null);
    }


    /**
     * Stops the filter.
     */
    public void stop() {
        if (reg != null) {
            reg.unregister();
            reg = null;
        }
    }


    /**
     * The interception method. The method should call {@link org.wisdom.api.interception.RequestContext#proceed()}
     * to call the next interceptor. Without this call it cuts the chain.
     *
     * @param route   the route
     * @param context the filter context
     * @return the result
     * @throws Exception if anything bad happen
     */
    @Override
    public Result call(Route route, RequestContext context) throws Exception {
        activeRequests.inc();
        final Timer.Context ctxt = requestTimer.time();
        Result result = null;
        try {
            result = context.proceed();
            return result;
        } finally {
            ctxt.stop();
            activeRequests.dec();
            markMeterForStatusCode(result);
        }
    }

    private void markMeterForStatusCode(Result result) {
        if (result == null) {
            otherMeter.mark();
            return;
        }
        final Meter metric = metersByStatusCode.get(result.getStatusCode());
        if (metric != null) {
            metric.mark();
        } else {
            otherMeter.mark();
        }
    }

    /**
     * Gets the Regex Pattern used to determine whether the route is handled by the filter or not.
     * Notice that the router are caching these patterns and so cannot changed.
     */
    @Override
    public Pattern uri() {
        return interceptionPattern;
    }

    /**
     * Gets the filter priority, determining the position of the filter in the filter chain. Filter with a high
     * priority are called first. Notice that the router are caching these priorities and so cannot changed.
     * <p/>
     * It is heavily recommended to allow configuring the priority from the Application Configuration.
     *
     * @return the priority
     */
    @Override
    public int priority() {
        return interceptionPriority;
    }
}
