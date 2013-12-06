package org.wisdom.test.internals;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
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
import org.wisdom.test.shared.InVivoRunner;
import org.wisdom.test.shared.InVivoRunnerFactory;

import java.io.File;

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
            Bundle bundle = chameleon.context().installBundle("local", ProbeBundleMaker.probe());
            bundle.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieve the InVivoRunner Factory and create an instance.
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

    private void fixLoggingSystem(File basedir) {
        ILoggerFactory factory = LoggerFactory.getILoggerFactory();
        if (factory instanceof LoggerContext) {
            // Remove the created log directory.
            FileUtils.deleteQuietly(new File("logs"));
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
                if(fileAppender != null) {
                    fileAppender.stop();
                    fileAppender.setFile(file);
                    fileAppender.setContext(lc);
                    fileAppender.start();
                }
            } catch (Throwable e) {
                return;
            }
        }
    }
}
