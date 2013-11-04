package org.ow2.chameleon.wisdom.test.impl;

import org.junit.runners.model.InitializationError;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.core.Chameleon;
import org.ow2.chameleon.core.ChameleonConfiguration;
import org.ow2.chameleon.testing.helpers.Stability;
import org.ow2.chameleon.wisdom.test.InVivoRunner;
import org.ow2.chameleon.wisdom.test.InVivoRunnerFactory;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: clement
 * Date: 03/11/2013
 * Time: 16:05
 * To change this template use File | Settings | File Templates.
 */
public class ChameleonExecutor {

    private Chameleon chameleon;

    public void start(File root) throws Exception {
        ChameleonConfiguration configuration = new ChameleonConfiguration(root);
        StringBuilder packages = new StringBuilder();
        Packages.junit(packages);
        Packages.wisdomtest(packages);
        Packages.javaxinject(packages);
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
        Bundle bundle = chameleon.context().installBundle("local", ProbeBundleMaker.probe());
        bundle.start();
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
