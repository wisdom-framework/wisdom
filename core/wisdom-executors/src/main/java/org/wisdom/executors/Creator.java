/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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
package org.wisdom.executors;

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.concurrent.ExecutionContextService;
import org.wisdom.api.concurrent.ManagedExecutorService;
import org.wisdom.api.concurrent.ManagedScheduledExecutorService;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.configuration.Configuration;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Component responsible for pool creation.
 */
@Component
@Instantiate
public class Creator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Creator.class);

    private static final String[] EXPOSED_CLASSES_FOR_EXECUTORS = new String[]{
            ExecutorService.class.getName(),
            ManagedExecutorService.class.getName()
    };

    private static final String[] EXPOSED_CLASSES_FOR_SCHEDULERS = new String[]{
            ScheduledExecutorService.class.getName(),
            ManagedScheduledExecutorService.class.getName()
    };

    @Requires
    ApplicationConfiguration configuration;

    @Context
    BundleContext context;

    @Requires(specification = ExecutionContextService.class)
    List<ExecutionContextService> ecs;

    private final Map<ServiceRegistration, ExecutorService> instances = new HashMap<>();

    /**
     * Creates the system executors and the others specified executors.
     */
    @Validate
    public void start() {
        Configuration conf = configuration.getConfiguration("pools");

        createExecutor(ManagedExecutorService.SYSTEM,
                conf != null ? conf.getConfiguration("executors." + ManagedExecutorService.SYSTEM) : null);
        createScheduler(ManagedScheduledExecutorService.SYSTEM,
                conf != null ? conf.getConfiguration("schedulers." + ManagedScheduledExecutorService.SYSTEM) : null);

        if (conf != null) {
            createOtherExecutors(conf.getConfiguration("executors"));
            createOtherSchedulers(conf.getConfiguration("schedulers"));
        }
    }

    private void createOtherExecutors(Configuration executors) {
        if (executors == null) {
            return;
        }
        Set<String> keys = executors.asMap().keySet();
        for (String key : keys) {
            // Skip System (already created).
            if (!key.equalsIgnoreCase(ManagedExecutorService.SYSTEM)) {
                Configuration conf = executors.getConfiguration(key);
                createExecutor(key, conf);
            }
        }
    }

    private void createOtherSchedulers(Configuration schedulers) {
        if (schedulers == null) {
            return;
        }
        Set<String> keys = schedulers.asMap().keySet();
        for (String key : keys) {
            // Skip System (already created).
            if (!key.equalsIgnoreCase(ManagedScheduledExecutorService.SYSTEM)) {
                Configuration conf = schedulers.getConfiguration(key);
                createScheduler(key, conf);
            }
        }
    }

    private void createExecutor(String name, Configuration conf) {
        LOGGER.info("Creating executor {}", name);
        ManagedExecutorServiceImpl executor;
        if (conf != null) {
            executor = new ManagedExecutorServiceImpl(name, conf, ecs);

        } else {
            executor = new ManagedExecutorServiceImpl(
                    ManagedExecutorService.SYSTEM,
                    ManagedExecutorService.ThreadType.POOLED,
                    60000,
                    5,
                    25,
                    5000,
                    true,
                    Integer.MAX_VALUE,
                    Thread.NORM_PRIORITY,
                    ecs);
        }
        ServiceRegistration reg = context.registerService(
                EXPOSED_CLASSES_FOR_EXECUTORS,
                executor,
                getPublishedProperties(executor));
        instances.put(reg, executor);
    }

    private void createScheduler(String name, Configuration conf) {
        LOGGER.info("Creating scheduler {}", name);
        ManagedScheduledExecutorServiceImpl executor;
        if (conf != null) {
            executor = new ManagedScheduledExecutorServiceImpl(name, conf, ecs);
        } else {
            executor = new ManagedScheduledExecutorServiceImpl(
                    ManagedScheduledExecutorService.SYSTEM,
                    ManagedExecutorService.ThreadType.POOLED,
                    60000,
                    5,
                    Thread.NORM_PRIORITY,
                    ecs);
        }
        ServiceRegistration reg = context.registerService(
                EXPOSED_CLASSES_FOR_SCHEDULERS,
                executor,
                getPublishedProperties(executor));
        instances.put(reg, executor);
    }

    private Dictionary<String, String> getPublishedProperties(AbstractManagedExecutorService executor) {
        Hashtable<String, String> properties = new Hashtable<>();  //NOSONAR no choice here, OSGi API
        properties.put("name", executor.name());
        return properties;
    }

    /**
     * Shutdown all created executors.
     */
    @Invalidate
    public void stop() {
        for (Map.Entry<ServiceRegistration, ExecutorService> entry : instances.entrySet()) {
            entry.getKey().unregister();
            entry.getValue().shutdownNow();
        }
        instances.clear();
    }
}
