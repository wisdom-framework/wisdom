package org.wisdom.pools;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
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
 * Component responsible fo pool creation.
 */
@Component
@Instantiate
public class Creator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Creator.class);

    private static final String[] EXPOSED_CLASSES_FOR_EXECUTORS = new String[]{
            ExecutorService.class.getName(),
            ManagedExecutorService.class.getName(),
            ListeningExecutorService.class.getName()
    };

    private static final String[] EXPOSED_CLASSES_FOR_SCHEDULERS = new String[]{
            ScheduledExecutorService.class.getName(),
            ManagedScheduledExecutorService.class.getName(),
            ListeningScheduledExecutorService.class.getName()
    };

    @Requires
    ApplicationConfiguration configuration;

    @Context
    BundleContext context;

    @Requires(specification = ExecutionContextService.class)
    List<ExecutionContextService> ecs;

    private final Map<ServiceRegistration, ExecutorService> instances = new HashMap<>();

    @Validate
    public void start() {
        Configuration conf = configuration.getConfiguration("pools");

        createExecutor(ManagedExecutorService.SYSTEM,
                conf.getConfiguration("executors." + ManagedExecutorService.SYSTEM));
        createScheduler(ManagedScheduledExecutorService.SYSTEM,
                conf.getConfiguration("schedulers." + ManagedScheduledExecutorService.SYSTEM));

        createOtherExecutors(conf.getConfiguration("executors"));
        createOtherSchedulers(conf.getConfiguration("schedulers"));
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
            executor = new ManagedExecutorServiceImpl(ManagedExecutorService.SYSTEM, conf, ecs);

        } else {
            executor = new ManagedExecutorServiceImpl(
                    ManagedExecutorService.SYSTEM,
                    ManagedExecutorService.ThreadType.POOLED,
                    60000,
                    5,
                    25,
                    5000,
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

    private void createSystemExecutor(Configuration conf) {
        createExecutor(ManagedExecutorService.SYSTEM, conf);
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

    private Dictionary<String, ?> getPublishedProperties(AbstractManagedExecutorService executor) {
        Hashtable<String, String> properties = new Hashtable<>();
        properties.put("name", executor.name());
        return properties;
    }


    @Invalidate
    public void stop() {
        for (Map.Entry<ServiceRegistration, ExecutorService> entry : instances.entrySet()) {
            entry.getKey().unregister();
            entry.getValue().shutdownNow();
        }
        instances.clear();
    }
}
