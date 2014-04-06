package org.wisdom.monitor;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.util.*;

/**
 * Bundle Model.
 */
public class BundleModel {

    public BundleModel(Bundle bundle) {
        this.bundle = bundle;
    }

    public static List<BundleModel> bundles(BundleContext context) {
        List<BundleModel> bundles = new ArrayList<BundleModel>();
        for (Bundle bundle : context.getBundles()) {
            bundles.add(new BundleModel(bundle));
        }
        return bundles;
    }

    private final Bundle bundle;

    public String getState() {
        switch(bundle.getState()) {
            case Bundle.ACTIVE: return "ACTIVE";
            case Bundle.INSTALLED: return "INSTALLED";
            case Bundle.RESOLVED: return "RESOLVED";
            case Bundle.STARTING: return "STARTING";
            case Bundle.STOPPING: return "STOPPING";
            case Bundle.UNINSTALLED: return "UNINSTALLED";
            default: return Integer.toString(bundle.getState());
        }
    }

    public long getId() {
        return bundle.getBundleId();
    }

    public String getName() {
        return bundle.getSymbolicName() + " - " + bundle.getVersion();
    }

    public Map<String, String> getHeaders() {
        Map<String, String> map = new TreeMap<String, String>();
        Enumeration<String> enumeration = bundle.getHeaders().keys();
        while (enumeration.hasMoreElements()) {
            String key = enumeration.nextElement();
            map.put(key, bundle.getHeaders().get(key));
        }
        return map;
    }
}
