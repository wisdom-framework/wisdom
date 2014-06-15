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
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.util.ArrayList;
import java.util.List;

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
        List<InstanceModel> instances = new ArrayList<InstanceModel>();
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
        return architecture.getInstanceDescription().getDescription().toString();
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

}
