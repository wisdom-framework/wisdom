package org.ow2.chameleon.wisdom.test.internals;

import org.junit.runners.model.InitializationError;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.core.Chameleon;
import org.ow2.chameleon.core.ChameleonConfiguration;
import org.ow2.chameleon.testing.helpers.Stability;
import org.ow2.chameleon.wisdom.test.shared.InVivoRunner;
import org.ow2.chameleon.wisdom.test.shared.InVivoRunnerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Handles a Chameleon and manage the singleton instance.
 */
public class ChameleonExecutor {

    private static ChameleonExecutor INSTANCE;
    private Chameleon chameleon;

    private ChameleonExecutor() {
        // Avoid direct instantiation.
    }

    public static ChameleonExecutor instance(File root) throws Exception {
        if (INSTANCE == null) {
            INSTANCE = new ChameleonExecutor();
            INSTANCE.start(root);
        }
        return INSTANCE;
    }

    public static void stopRunningInstance() throws Exception {
        if (INSTANCE != null) {
            INSTANCE.stop();
            INSTANCE = null;
        }
    }

    public void start(File root) throws Exception {
        ChameleonConfiguration configuration = new ChameleonConfiguration(root);

        StringBuilder packages = new StringBuilder();
        Packages.junit(packages);
        Packages.wisdomtest(packages);
        Packages.javaxinject(packages);
        Packages.assertj(packages);
        Packages.osgihelpers(packages);
        configuration.put("org.osgi.framework.system.packages.extra", packages.toString());

        chameleon = new Chameleon(configuration);
        chameleon.start();
        Stability.waitForStability(chameleon.context());


    }

    public BundleContext context() {
        return chameleon.context();
    }

    public void stop() throws Exception {
        chameleon.stop();
    }

    public void deployProbe() throws BundleException {
        for (Bundle bundle : chameleon.context().getBundles()) {
            if (bundle.getSymbolicName().equals(ProbeBundleMaker.BUNDLE_NAME)) {
                return;
            }
        }
        try {
            Bundle bundle = chameleon.context().installBundle("local", ProbeBundleMaker.probe());
            bundle.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retireve the InVivoRunner Factory and create an instance.
     */
    public InVivoRunner getInVivoRunnerInstance(Class clazz) throws InitializationError, ClassNotFoundException {
        ServiceReference<InVivoRunnerFactory> reference = context().getServiceReference(InVivoRunnerFactory.class);
        if (reference == null) {
            throw new IllegalStateException("Cannot retrieve the test probe from Wisdom");
        } else {
            InVivoRunnerFactory factory = context().getService(reference);
            return factory.create(clazz.getName());
        }
    }
}
