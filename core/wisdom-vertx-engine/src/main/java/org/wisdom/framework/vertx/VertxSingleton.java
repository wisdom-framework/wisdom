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
package org.wisdom.framework.vertx;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.configuration.Configuration;

import java.io.File;
import java.util.Hashtable;
import java.util.concurrent.CountDownLatch;

/**
 * Exposes the Vert.X instance as a service.
 * It used the default vertx factory, and expose the instance as it is.
 */
@Component
@Instantiate
public class VertxSingleton {

    @Context
    protected BundleContext context;

    @Requires
    ApplicationConfiguration appConfiguration;

    private Vertx vertx;
    private ServiceRegistration<Vertx> vertxRegistration;
    private ServiceRegistration<EventBus> busRegistration;

    //TODO Use services to retrieve the cluster manager

    /**
     * Creates and exposed the instance of Vert.X.
     */
    @Validate
    public void start() {
        final Configuration configuration = appConfiguration.getConfiguration("vertx");


        String log = System.getProperty("org.vertx.logger-delegate-factory-class-name");
        if (log == null) {
            // No logging backend configured, set one:
            System.setProperty("org.vertx.logger-delegate-factory-class-name",
                    SLF4JLogDelegateFactory.class.getName());
        }

        VertxOptions options = new VertxOptions();
        boolean clustered = false;
        if (configuration != null) {
            clustered = configuration.getBooleanWithDefault("clustered", false);
            options = new VertxOptions(new JsonObject(configuration.asMap()));

            if (clustered) {
                options
                        .setClustered(true)
                        .setClusterHost(configuration.getOrDie("cluster-host"));
                // Identify port and configuration file
                String clusterConfig = configuration.getWithDefault("cluster-config", "conf/cluster.xml");
                System.setProperty("hazelcast.config", new File(clusterConfig).getAbsolutePath());
            }
        }

        // To setup the logging backend, Vert.x needs a TTCL.
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

            Hashtable<String, Object> properties = new Hashtable<>();
            if (clustered) {
                options.setClusterManager(new HazelcastClusterManager());
                String host = options.getClusterHost();
                properties.put("eventbus.host", host);
                Vertx.clusteredVertx(options, ar -> {
                    if (ar.failed()) {
                        throw new IllegalStateException("Cannot join cluster", ar.cause());
                    }
                    vertx = ar.result();
                    vertxRegistration = context.registerService(Vertx.class, vertx, properties);
                    busRegistration = context.registerService(EventBus.class, vertx.eventBus(), properties);
                });
            } else {
                // Not a clustered environment
                vertx = Vertx.vertx(options);
                vertxRegistration = context.registerService(Vertx.class, vertx, properties);
                busRegistration = context.registerService(EventBus.class, vertx.eventBus(), properties);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    /**
     * Unregisters and shuts down the Vert.X singleton.
     */
    @Invalidate
    public void stop() throws InterruptedException {
        unregisterQuietly(vertxRegistration);
        unregisterQuietly(busRegistration);
        CountDownLatch latch = new CountDownLatch(1);
        vertx.close(v -> latch.countDown());
        latch.await();
        vertx = null;
    }

    private static void unregisterQuietly(ServiceRegistration reg) {
        if (reg != null) {
            try {
                reg.unregister();
            } catch (IllegalStateException e) {
                // Ignore it.
            }
        }
    }
}
