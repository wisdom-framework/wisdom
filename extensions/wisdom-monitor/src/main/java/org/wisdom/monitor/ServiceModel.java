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
