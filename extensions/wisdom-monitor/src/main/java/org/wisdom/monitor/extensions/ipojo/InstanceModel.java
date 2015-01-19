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
package org.wisdom.monitor.extensions.ipojo;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.architecture.Architecture;
import org.apache.felix.ipojo.handlers.dependency.DependencyDescription;
import org.apache.felix.ipojo.handlers.dependency.DependencyHandlerDescription;
import org.apache.felix.ipojo.handlers.providedservice.ProvidedService;
import org.apache.felix.ipojo.handlers.providedservice.ProvidedServiceDescription;
import org.apache.felix.ipojo.handlers.providedservice.ProvidedServiceHandlerDescription;
import org.apache.felix.ipojo.util.DependencyModel;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.util.*;

/**
 * The instance model consumed by the template.
 */
public class InstanceModel {

    private final Architecture architecture;

    /**
     * Creates the instance model from the instance's architecture.
     *
     * @param architecture the architecture
     */
    public InstanceModel(Architecture architecture) {
        this.architecture = architecture;
    }

    /**
     * Creates the list of instance models from all {@link org.apache.felix.ipojo.architecture.Architecture} service
     * published in the service registry.
     *
     * @param context the bundle context
     * @return the list of models, empty if none.
     */
    public static List<InstanceModel> instances(BundleContext context) {
        List<InstanceModel> instances = new ArrayList<>();
        try {
            for (ServiceReference ref : context.getServiceReferences(Architecture.class, null)) {
                instances.add(new InstanceModel((Architecture) context.getService(ref)));
            }
        } catch (InvalidSyntaxException e) { //NOSONAR
            // Ignore it.
        }
        return instances;
    }


    /**
     * @return the instance name.
     */
    public String getName() {
        return architecture.getInstanceDescription().getName();
    }

    /**
     * @return the raw architecture.
     */
    public String getArchitecture() {
        return architecture.getInstanceDescription().getDescription()
                .toString().replace("\t", " ").replace("  ", " ");
    }

    /**
     * @return the factory's name.
     */
    public String getFactory() {
        return architecture.getInstanceDescription().getComponentDescription().getName();
    }

    /**
     * @return the instance's state.
     */
    public String getState() {
        switch (architecture.getInstanceDescription().getState()) {
            case ComponentInstance.DISPOSED:
                return "DISPOSED";
            case ComponentInstance.INVALID:
                return "INVALID";
            case ComponentInstance.VALID:
                return "VALID";
            case ComponentInstance.STOPPED:
                return "STOPPED";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * @return the list of provided services
     */
    public List<ProvidedServiceModel> getServices() {
        ProvidedServiceHandlerDescription pshd = (ProvidedServiceHandlerDescription) architecture.getInstanceDescription().getHandlerDescription("org" +
                ".apache.felix.ipojo:provides");
        if (pshd == null) {
            return Collections.emptyList();
        } else {
            List<ProvidedServiceModel> list = new ArrayList<>(pshd.getProvidedServices().length);
            for (ProvidedServiceDescription description : pshd.getProvidedServices()) {
                list.add(new ProvidedServiceModel(description));
            }
            return list;
        }
    }

    /**
     * @return the list of required services
     */
    public List<ServiceDependencyModel> getDependencies() {
        DependencyHandlerDescription handler = (DependencyHandlerDescription) architecture.getInstanceDescription()
                .getHandlerDescription("org.apache.felix.ipojo:requires");
        if (handler == null) {
            return Collections.emptyList();
        } else {
            List<ServiceDependencyModel> list = new ArrayList<>(handler.getDependencies().length);
            for (DependencyDescription dependency : handler.getDependencies()) {
                list.add(new ServiceDependencyModel(dependency));
            }
            return list;
        }
    }

    /**
     * Simplified model of provided service.
     */
    private class ProvidedServiceModel {

        private final ProvidedServiceDescription description;

        private ProvidedServiceModel(ProvidedServiceDescription desc) {
            this.description = desc;
        }

        /**
         * @return the set of published interfaces, cannot be empty.
         */
        public String[] getInterfaces() {
            return description.getServiceSpecifications();
        }

        /**
         * @return whether or not the service is published.
         */
        public boolean isPublished() {
            return description.getState() == ProvidedService.REGISTERED;
        }

        /**
         * @return the published properties (only if the service is published).
         */
        public Map<String, String> getProperties() {
            TreeMap<String, String> map = new TreeMap<>();
            if (isPublished()) {
                String[] keys = description.getServiceReference().getPropertyKeys();
                for (String name : keys) {
                    Object value = description.getServiceReference().getProperty(name);
                    if (value != null) {
                        if (value.getClass().isArray()) {
                            map.put(name, Arrays.toString((Object[]) value));
                        } else {
                            map.put(name, value.toString());
                        }
                    } else {
                        map.put(name, "null");
                    }
                }
            }
            return map;
        }
    }

    /**
     * Simplified model of service dependency.
     */
    private class ServiceDependencyModel {
        private final DependencyDescription dependency;

        public ServiceDependencyModel(DependencyDescription dependency) {
            this.dependency = dependency;
        }

        /**
         * @return whether or not the dependency is resolved.
         */
        public boolean isResolved() {
            return dependency.getState() == DependencyModel.RESOLVED;
        }

        /**
         * @return the required service specification.
         */
        public String getInterface() {
            return dependency.getSpecification();
        }

        /**
         * @return whether or not the dependency is optional.
         */
        public boolean isOptional() {
            return dependency.isOptional();
        }

        /**
         * @return whether or not the dependency is aggregate.
         */
        public boolean isAggregate() {
            return dependency.isMultiple();
        }

        /**
         * @return the dependency filter if any.
         */
        public String getFilter() {
            return dependency.getFilter();
        }
    }
}
