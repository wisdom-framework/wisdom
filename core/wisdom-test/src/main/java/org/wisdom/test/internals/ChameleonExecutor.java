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
package org.wisdom.test.internals;

import org.apache.commons.io.FileUtils;
import org.junit.runners.model.InitializationError;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.core.Chameleon;
import org.ow2.chameleon.core.ChameleonConfiguration;
import org.ow2.chameleon.testing.helpers.Stability;
import org.ow2.chameleon.testing.helpers.TimeUtils;
import org.slf4j.LoggerFactory;
import org.wisdom.maven.utils.BundlePackager;
import org.wisdom.maven.utils.ChameleonInstanceHolder;
import org.wisdom.test.shared.InVivoRunner;
import org.wisdom.test.shared.InVivoRunnerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Handles a Chameleon and manage the singleton instance.
 */
public final class ChameleonExecutor {

    private static final String APPLICATION_BUNDLE = "target/osgi/application.jar";
    private static ChameleonExecutor INSTANCE;
    private File root;

    private ChameleonExecutor() {
        // Avoid direct instantiation.
    }

    /**
     * Gets the instance of Chameleon, i.e. the OSGi Framework where the test are executed.
     *
     * @param root the base directory of the Chameleon.
     * @return the Chameleon Executor instance, newly created if none, or reuses if any.
     * @throws java.io.IOException                if the chameleon configuration cannot be read.
     * @throws org.osgi.framework.BundleException if the chameleon cannot be started.
     */
    public static synchronized ChameleonExecutor instance(File root) throws BundleException, IOException {
        if (INSTANCE == null) {
            File application = new File(APPLICATION_BUNDLE);
            if (application.isFile()) {
                FileUtils.deleteQuietly(application);
            }
            INSTANCE = new ChameleonExecutor();
            INSTANCE.root = root;
            INSTANCE.start(root);
        }
        return INSTANCE;
    }

    /**
     * Stops the running Chameleon.
     *
     * @throws Exception if the Chameleon instance cannot be stopped.
     */
    public static synchronized void stopRunningInstance() throws Exception {
        if (INSTANCE != null) {
            INSTANCE.stop();
            ChameleonInstanceHolder.set(null);
            INSTANCE = null;
        }
    }

    /**
     * Starts the underlying Chameleon instance.
     *
     * @param root the base directory of the Chameleon.
     * @throws java.io.IOException                if the chameleon configuration cannot be read.
     * @throws org.osgi.framework.BundleException if the chameleon cannot be started.
     */
    private void start(File root) throws BundleException, IOException {
        ChameleonConfiguration configuration = new ChameleonConfiguration(root);
        // Use a different cache for testing.
        configuration.put("org.osgi.framework.storage", root.getAbsolutePath() + "/chameleon-test-cache");
        StringBuilder packages = new StringBuilder();
        Packages.junit(packages);
        Packages.wisdomtest(packages);
        Packages.javaxinject(packages);
        Packages.assertj(packages);
        Packages.osgihelpers(packages);
        configuration.put("org.osgi.framework.system.packages.extra", packages.toString());

        // Set the httpPort to 0 to use the random port feature.
        // Except if already set explicitly
        String port = System.getProperty("http.port");
        if (port == null) {
            System.setProperty("http.port", "0");
        }

        Chameleon chameleon = new Chameleon(configuration);
        ChameleonInstanceHolder.fixLoggingSystem(root);
        chameleon.start();

        // Set the TIME_FACTOR
        String factor = System.getProperty("TIME_FACTOR");
        if (factor != null) {
            LoggerFactory.getLogger(this.getClass()).info("Setting TIME_FACTOR to " + factor);
            TimeUtils.TIME_FACTOR = Integer.valueOf(factor);
        }

        Stability.waitForStability(chameleon.context());
        ChameleonInstanceHolder.set(chameleon);


    }

    /**
     * @return the bundle context of the underlying Chameleon, {@literal null} if not started.
     */
    public BundleContext context() {
        return ChameleonInstanceHolder.get().context();
    }

    /**
     * Stops the underlying chameleon instance.
     * If we have an application artifact, restore it in the 'application' directory. It may have been deleted from
     * the 'application' directory before running the integration tests.
     *
     * @throws Exception if it cannot be stopped.
     */
    private void stop() throws Exception {
        File original = RunnerUtils.getApplicationArtifactIfExists(root);
        ChameleonInstanceHolder.get().stop();
        // Restore the application bundle is any.
        if (original != null) {
            // We need to recompute the bundle name
            String fileName = RunnerUtils.getBundleFileName();
            File out = new File(new File(root, "application"), fileName);
            FileUtils.copyFile(original, out, true);
        }
    }

    /**
     * Deploys the `probe` bundle, i.e. the bundle containing the test classes and the Wisdom Test Utilities (such as
     * the InVivo Runner). If such a bundle is already deployed, nothing is done, else, the probe bundle is built,
     * installed and started.
     *
     * @throws BundleException if the probe bundle cannot be started.
     */
    public void deployProbe() throws BundleException {
        for (Bundle bundle : ChameleonInstanceHolder.get().context().getBundles()) {
            if (bundle.getSymbolicName().equals(ProbeBundleMaker.BUNDLE_NAME)) {
                return;
            }
        }
        try {
            Bundle probe = ChameleonInstanceHolder.get().context().installBundle("local", ProbeBundleMaker.probe());
            probe.start();
        } catch (Exception e) {
            throw new RuntimeException("Cannot install or start the probe bundle", e);
        }
    }

    /**
     * Builds and deploy the application bundle.
     * This method is called the application bundle is not in the runtime or application directories.
     */
    public void deployApplication() throws BundleException {
        File application = new File(APPLICATION_BUNDLE);
        File base = new File(".");
        if (!application.isFile()) {
            try {
                BundlePackager.bundle(base, application);
            } catch (Exception e) {
                throw new RuntimeException("Cannot build the application bundle", e);
            }
        }

        try {
            Bundle app = ChameleonInstanceHolder.get().context().installBundle(application.toURI().toURL().toExternalForm());
            app.start();
        } catch (Exception e) {
            throw new RuntimeException("Cannot install or start the application bundle", e);
        }
    }

    /**
     * Retrieves the InVivoRunner Factory and creates an instance.
     */
    public InVivoRunner getInVivoRunnerInstance(Class<?> clazz) throws InitializationError, ClassNotFoundException, IOException {
        ServiceReference<InVivoRunnerFactory> reference = context().getServiceReference(InVivoRunnerFactory.class);
        if (reference == null) {
            throw new IllegalStateException("Cannot retrieve the test probe from Wisdom");
        } else {
            InVivoRunnerFactory factory = context().getService(reference);
            return factory.create(clazz.getName());
        }
    }

}
