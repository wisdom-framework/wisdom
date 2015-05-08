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
package org.wisdom.framework.instances;

import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.ConfigurationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.framework.instances.api.InstantiatedBy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages factories annotated with {@link InstantiatedByManager}.
 * It tracks these factories and listens configuration admin instance, and binds them.
 */
@Component
@Provides
@Instantiate
public class InstantiatedByManager implements ConfigurationListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstantiatedByManager.class);

    @Context
    BundleContext context;

    @Requires
    ConfigurationAdmin admin;

    private List<InstanceDeclaration> declarations = new ArrayList<>();

    private final Lock lock = new ReentrantLock(true);

    /**
     * Bind a factory.
     *
     * @param factory the factory
     */
    @Bind(aggregate = true)
    public void bindFactory(Factory factory) {

        // Only support primitive component
        if (!(factory instanceof ComponentFactory)) {
            return;
        }

        String cn = factory.getClassName();
        if (cn == null) {
            return;
        }

        // Has the factory the annotation
        try {
            Class clazz = ((ComponentFactory) factory).loadClass(cn);
            InstantiatedBy annotation = (InstantiatedBy) clazz.getAnnotation(InstantiatedBy.class);

            if (annotation != null) {
                // Match !
                LOGGER.info("Factory annotated with `@InstantiatedBy` found : {}, configuration : {}",
                        factory.getName(),
                        annotation.value());

                addInstanceDeclaration(factory, annotation.value());
            }
        } catch (ClassNotFoundException e) {
            LOGGER.error("Cannot load the component class {}", cn, e);
        }
    }

    /**
     * Unbinds a factory.
     *
     * @param factory the factory
     */
    @Unbind
    public void unbindFactory(Factory factory) {
        try {
            lock.lock();
            InstanceDeclaration declaration = getDeclarationByFactory(factory);
            if (declaration != null) {
                LOGGER.info("Disposing instance created by");
                declaration.dispose();
                declarations.remove(declaration);
            }
        } finally {
            lock.unlock();
        }
    }

    private void addInstanceDeclaration(Factory factory, String value) {
        try {
            lock.lock();
            // Do we know this factory already ?
            InstanceDeclaration declaration = getDeclarationByFactory(factory);
            if (declaration != null) {
                declaration.dispose();
                declarations.remove(declaration);
            }
            // Add it
            declaration = new InstanceDeclaration(factory, value);
            declarations.add(declaration);

            // Do we have a configuration
            Configuration[] configurations = getConfigurationList();

            if (configurations != null) {
                for (Configuration configuration : configurations) {
                    if (declaration.matches(configuration)) {
                        LOGGER.debug("Found a matching configuration for " + factory.getName() + " => " + configuration
                                .getPid());
                        declaration.attachOrUpdate(configuration);
                    }
                }
            }
        } finally {
            lock.unlock();
        }

    }

    private Configuration[] getConfigurationList() {
        Configuration[] configurations;
        try {
            configurations = admin.listConfigurations(null);
        } catch (InvalidSyntaxException | IOException e) {
            // Cannot happen as the filter is null.
            throw new RuntimeException("Invalid Syntax Exception or IOException", e);
        }
        return configurations;
    }

    private InstanceDeclaration getDeclarationByFactory(Factory factory) {
        for (InstanceDeclaration declaration : declarations) {
            if (factory.equals(declaration.factory)) {
                return declaration;
            }
        }
        return null;
    }


    private List<InstanceDeclaration> getDeclarationsByConfiguration(String pid, String factoryPid) {
        List<InstanceDeclaration> result = new ArrayList<>();
        for (InstanceDeclaration declaration : declarations) {
            if (declaration.matches(pid, factoryPid)) {
                result.add(declaration);
            }
        }
        return result;
    }

    /**
     * Receives a configuration event.
     *
     * @param event the event.
     */
    @Override
    public void configurationEvent(ConfigurationEvent event) {
        LOGGER.debug("event received : " + event.getPid() + " - " + event.getType());
        try {
            lock.lock();
            final List<InstanceDeclaration> impacted
                    = getDeclarationsByConfiguration(event.getPid(), event.getFactoryPid());

            if (impacted.isEmpty()) {
                return;
            }

            switch (event.getType()) {
                case ConfigurationEvent.CM_DELETED:
                    for (InstanceDeclaration declaration : impacted) {
                        LOGGER.info("Configuration " + event.getPid() + " deleted");
                        declaration.dispose(event.getPid());
                    }
                    break;
                case ConfigurationEvent.CM_UPDATED:
                    for (InstanceDeclaration declaration : impacted) {
                        final Configuration configuration = find(event.getPid());
                        if (configuration == null) {
                            LOGGER.error("Weird case, a matching declaration was found, but cannot be found a second " +
                                    "times, may be because of rapid changes in the config admin");
                        } else {
                            declaration.attachOrUpdate(configuration);
                        }
                    }

                    break;
            }
        } finally {
            lock.unlock();
        }

    }

    private Configuration find(String pid) {
        final Configuration[] configurations = getConfigurationList();
        if (configurations != null) {
            for (Configuration conf : configurations) {
                if (conf.getPid().equals(pid)) {
                    return conf;
                }
            }
        }
        return null;
    }

    /**
     * Represent an instantiation request.
     */
    private class InstanceDeclaration {

        /**
         * The factory.
         */
        private final Factory factory;
        /**
         * The PID of the configuration, can be a configuration PID and factory PID.
         */
        private final String target;

        /**
         * The component instance, created and disposed dynamically.
         * The key if the configuration PID.
         */
        private Map<String, ComponentInstance> instances = new LinkedHashMap<>();

        /**
         * Creates a new instance of {@link org.wisdom.framework.instances.InstantiatedByManager.InstanceDeclaration}.
         *
         * @param factory the factory
         * @param target  the target
         */
        private InstanceDeclaration(Factory factory, String target) {
            this.factory = factory;
            this.target = target;
        }

        /**
         * Disposes the all created instances.
         */
        public void dispose() {
            for (ComponentInstance instance : instances.values()) {
                LOGGER.info("Disposing " + instance.getInstanceName());
                instance.dispose();
            }
            instances.clear();
        }

        /**
         * Disposes the instance matching the given configuration pid.
         *
         * @param pid the pid
         */
        public void dispose(String pid) {
            ComponentInstance instance = instances.remove(pid);
            if (instance != null) {
                LOGGER.info("Disposing " + instance.getInstanceName());
                instance.dispose();
            }
        }

        /**
         * Checks whether or not the given configuration match the configuration name. It matches against the
         * configuration PID and factory PID.
         *
         * @param configuration the configuration
         * @return {@code true} if it matches, {@code false} otherwise
         */
        public boolean matches(Configuration configuration) {
            return matches(configuration.getPid(), configuration.getFactoryPid());
        }

        /**
         * Checks whether of not the given pid **or** factory pid matches the current required configuration.
         *
         * @param pid        the pid
         * @param factoryPid the factory pid
         * @return {@code true} if it matches, {@code false} otherwise
         */
        public boolean matches(String pid, String factoryPid) {
            return target.equals(pid)
                    || target.equals(factoryPid);
        }

        private ComponentInstance create(Configuration configuration) {
            try {
                return factory.createComponentInstance(configuration.getProperties());
            } catch (UnacceptableConfiguration | MissingHandlerException | ConfigurationException
                    e) {
                LOGGER.error("Component creation failed from configuration {} ({})", configuration.getPid(),
                        configuration.getProperties(), e);
            }
            return null;
        }

        /**
         * Attaches this request to the given configuration. It creates the iPOJO instance if it does not exist. It
         * updates it if the instance was already created.
         *
         * @param configuration the configuration.
         */
        public void attachOrUpdate(Configuration configuration) {
            // Do we have a configuration with the same pid
            final String pid = configuration.getPid();
            ComponentInstance instance = instances.get(pid);

            if (instance == null) {
                LOGGER.info("Attaching {} to factory {}", pid, factory.getName());
                instance = create(configuration);
                if (instance != null) {
                    LOGGER.info("Instance {} created from {}", instance.getInstanceName(), pid);
                    instances.put(pid, instance);
                }
            } else {
                instance.reconfigure(configuration.getProperties());
                LOGGER.info("Instance {} reconfigured from {}", instance.getInstanceName(), pid);
            }
        }
    }

}
