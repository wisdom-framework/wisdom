package org.wisdom.test.internals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.runners.model.InitializationError;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.core.Chameleon;
import org.ow2.chameleon.core.ChameleonConfiguration;
import org.ow2.chameleon.testing.helpers.Stability;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.maven.utils.BundlePackager;
import org.wisdom.test.shared.InVivoRunner;
import org.wisdom.test.shared.InVivoRunnerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;

/**
 * Handles a Chameleon and manage the singleton instance.
 */
public class ChameleonExecutor {

    private static final String APPLICATION_BUNDLE = "target/osgi/application.jar";
    private static ChameleonExecutor INSTANCE;
    private Chameleon chameleon;

    private ChameleonExecutor() {
        // Avoid direct instantiation.
    }

    public static synchronized ChameleonExecutor instance(File root) throws Exception {
        if (INSTANCE == null) {
            File application = new File(APPLICATION_BUNDLE);
            if (application.isFile()) {
                FileUtils.deleteQuietly(application);
            }
            INSTANCE = new ChameleonExecutor();
            INSTANCE.start(root);
        }
        return INSTANCE;
    }

    public static synchronized void stopRunningInstance() throws Exception {
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
        fixLoggingSystem(root);
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
            Bundle probe = chameleon.context().installBundle("local", ProbeBundleMaker.probe());
            probe.start();
        } catch (Exception e) {
            throw new RuntimeException("Cannot install or start the probe bundle", e);
        }
    }

    /**
     * Builds and deploy the application bundle.
     * This method is called the application bundle is not in the runtime or application directories.
     *
     */
    public void deployApplication() throws BundleException {
        File application = new File(APPLICATION_BUNDLE);
        File base = new File (".");
        if (! application.isFile()) {
            try {
                BundlePackager.bundle(base, application);
            } catch (Exception e) {
                throw new RuntimeException("Cannot build the application bundle", e);
            }
        }

        try {
            Bundle app = chameleon.context().installBundle(application.toURI().toURL().toExternalForm());
            app.start();
        } catch (Exception e) {
            throw new RuntimeException("Cannot install or start the application bundle", e);
        }
    }

    /**
     * Retrieve the InVivoRunner Factory and create an instance.
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

    private void fixLoggingSystem(File basedir) {
        ILoggerFactory factory = LoggerFactory.getILoggerFactory();
        if (factory instanceof LoggerContext) {
            // We know that we are using logback from here.
            LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
            ch.qos.logback.classic.Logger logbackLogger = lc.getLogger(Logger.ROOT_LOGGER_NAME);
            if (logbackLogger == null) {
                return;
            }
            try {
                RollingFileAppender<ILoggingEvent> fileAppender =
                        (RollingFileAppender<ILoggingEvent>) logbackLogger.getAppender("FILE");
                String file = new File(basedir, "logs/wisdom.log").getAbsolutePath();
                if (fileAppender != null) {
                    fileAppender.stop();
                    fileAppender.setFile(file);
                    fileAppender.setContext(lc);
                    fileAppender.start();
                }
                // Remove the created log directory.
                // We do that afterwards because on Windows the file cannot be deleted while we still have a logger
                // using it.
                FileUtils.deleteQuietly(new File("logs"));
            } catch (Throwable e) { //NOSONAR
                // The log system cannot be customized.
            }
        }
    }
}
