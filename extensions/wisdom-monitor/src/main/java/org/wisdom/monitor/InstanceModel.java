package org.wisdom.monitor;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.architecture.Architecture;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by clement on 02/04/2014.
 */
public class InstanceModel {

    public static List<InstanceModel> instances(BundleContext context) {
        List<InstanceModel> instances = new ArrayList<InstanceModel>();
        try {
            for (ServiceReference ref : context.getServiceReferences(Architecture.class, null)) {
                instances.add(new InstanceModel((Architecture) context.getService(ref)));
            }
        } catch (InvalidSyntaxException e) {
            // Ignore it.
        }
        return instances;
    }

    private final Architecture architecture;

    public InstanceModel(Architecture architecture) {
        this.architecture = architecture;
    }

    public String getName() {
        return architecture.getInstanceDescription().getName();
    }

    public String getArchitecture() {
        return architecture.getInstanceDescription().getDescription().toString();
    }

    public String getFactory() {
        return architecture.getInstanceDescription().getComponentDescription().getName();
    }

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
        }
        return null;
    }

}
