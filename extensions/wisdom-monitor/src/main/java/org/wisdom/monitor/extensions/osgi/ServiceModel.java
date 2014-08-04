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
package org.wisdom.monitor.extensions.osgi;

import org.osgi.framework.*;

import java.util.*;

/**
 * Service structure used from the template.
 */
public class ServiceModel {

    /**
     * Creates the list of service models from the list of the currently available services.
     *
     * @param context the bundle context
     * @return the list of models
     */
    public static List<ServiceModel> services(BundleContext context) {
        List<ServiceModel> services = new ArrayList<>();
        try {
            ServiceReference[] references = context.getAllServiceReferences(null, null);
            if (references != null) {
                for (ServiceReference ref : references) {
                    services.add(new ServiceModel(ref));
                }
            }
        } catch (InvalidSyntaxException e) {  //NOSONAR
            // Ignore it.
        }
        return services;
    }

    /**
     * The represented service reference.
     */
    private final ServiceReference reference;

    /**
     * Creates a new service model from the given reference.
     *
     * @param reference the reference
     */
    public ServiceModel(ServiceReference reference) {
        this.reference = reference;
    }

    /**
     * @return the exposed interfaces.
     */
    public String getInterfaces() {
        String[] specs = (String[]) this.reference.getProperty(Constants.OBJECTCLASS);
        if (specs == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        for (String spec : specs) {
            if (builder.length() == 0) {
                builder.append(spec);
            } else {
                builder.append(", ").append(spec);
            }
        }
        return builder.toString();
    }

    /**
     * @return the service id.
     */
    public long getId() {
        return (Long) this.reference.getProperty(Constants.SERVICE_ID);
    }

    /**
     * @return the name of the bundle exposing the service.
     */
    public String getProvidingBundle() {
        Bundle bundle = this.reference.getBundle();
        StringBuilder builder = new StringBuilder();

        String sn = bundle.getSymbolicName();
        if (sn != null) {
            builder.append(sn);
            builder.append(" [").append(bundle.getBundleId()).append("]");
        } else {
            builder.append("[").append(bundle.getBundleId()).append("]");
        }
        return builder.toString();
    }


    /**
     * @return the service properties (as a map of String - String). Array values are transformed as Strings.
     */
    public Map<String, String> getProperties() {
        Map<String, String> map = new TreeMap<>();
        for (String key : this.reference.getPropertyKeys()) {
            Object value = this.reference.getProperty(key);
            if (value.getClass().isArray()) {
                map.put(key, Arrays.toString((Object[]) value));
            } else {
                map.put(key, value.toString());
            }
        }
        return map;
    }

}
