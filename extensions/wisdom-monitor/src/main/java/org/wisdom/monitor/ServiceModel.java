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

import org.osgi.framework.*;

import java.util.*;

/**
 * Service structure.
 */
public class ServiceModel {

    public static List<ServiceModel> services(BundleContext context) {
        List<ServiceModel> services = new ArrayList<ServiceModel>();
        try {
            for (ServiceReference ref : context.getAllServiceReferences(null, null)) {
                services.add(new ServiceModel(ref));
            }
        } catch (InvalidSyntaxException e) {
            // Ignore it.
        }
        return services;
    }

    private final ServiceReference reference;

    public ServiceModel(ServiceReference reference) {
        this.reference = reference;
    }

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

    public long getId() {
        return (Long) this.reference.getProperty(Constants.SERVICE_ID);
    }

    public String getProvidingBundle() {
        Bundle bundle = this.reference.getBundle();
        StringBuilder builder = new StringBuilder();

        String sn = bundle.getSymbolicName();
        if (sn != null) {
            builder.append(sn);
        }
        builder.append(" [").append(bundle.getBundleId()).append("]");
        return builder.toString();
    }


    public Map<String, String> getProperties() {
        Map<String, String> map = new TreeMap<String, String>();
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
