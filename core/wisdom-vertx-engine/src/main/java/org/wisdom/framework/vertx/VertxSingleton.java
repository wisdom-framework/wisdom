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
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.wisdom.api.configuration.Configuration;

import java.io.File;
import java.util.Hashtable;

/**
 * Exposes the Vert.X instance as a service.
 * It used the default vertx factory, and expose the instance as it is.
 */
@Component
@Instantiate
public class VertxSingleton {

    @Context
    protected BundleContext context;

    @Requires(optional = true, filter = "(configuration.path=vertx)")
    Configuration configuration;

    private Vertx vertx;
    private ServiceRegistration<Vertx> vertxRegistration;
    private ServiceRegistration<EventBus> busRegistration;

    //TODO Use services to retrieve the cluster manager
    // Unfortunately, vertx use SPI or a factory class name to retrieve the cluster manager factory
    // This make almost impossible to retrieve the cluster manager factory as a service
    // For the time being, we just embed hazelcast and the hazelcast-based cluster manager factory in the bundle.

    /**
     * Creates and exposed the instance of Vert.X.
     */
    @Validate
    public void start() {
        String log = System.getProperty("org.vertx.logger-delegate-factory-class-name");
        if (log == null) {
            // No logging backend configured, set one:
            System.setProperty("org.vertx.logger-delegate-factory-class-name",
                    SLF4JLogDelegateFactory.class.getName());
        }

        String coreThread = System.getProperty("vertx.pool.eventloop.size");
        if (coreThread == null) {
            final Integer threads = configuration.getInteger("pool.eventloop.size");
            if (threads != null) {
                System.setProperty("vertx.pool.eventloop.size", threads.toString());
            }
        }

//TODO Clustering....
//        // Right now we force it to Hazelcast.
//        String cf = System.getProperty("vertx.clusterManagerFactory");
//        if (cf == null) {
//            System.setProperty("vertx.clusterManagerFactory", HazelcastClusterManagerFactory.class.getName());
//        }

        // To setup the logging backend, Vert.x needs a TTCL.
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

            // Check whether we are in 'cluster' mode
            String hostname = configuration.get("cluster-host");

            Hashtable<String, Object> properties = new Hashtable<>();
            if (hostname != null) {
                // Cluster mode

                // Identify port and configuration file
                final Integer port = configuration.getIntegerWithDefault("cluster-port", 25500);
                String clusterConfig = configuration.getWithDefault("cluster-config", "conf/cluster.xml");
                System.setProperty("hazelcast.config", new File(clusterConfig).getAbsolutePath());

                vertx = Vertx.vertx(new VertxOptions().setClustered(true).setClusterHost(hostname).setClusterPort(port));
                properties.put("eventbus.port", port);
                properties.put("eventbus.host", hostname);
            } else {
                // Not a clustered environment
                vertx = Vertx.vertx(new VertxOptions().setClustered(false));
            }
            vertxRegistration = context.registerService(Vertx.class, vertx, properties);
            busRegistration = context.registerService(EventBus.class, vertx.eventBus(), properties);
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    /**
     * Unregisters and shuts down the Vert.X singleton.
     */
    @Invalidate
    public void stop() {
        unregisterQuietly(vertxRegistration);
        unregisterQuietly(busRegistration);
        vertx.close(v -> {

        });
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
