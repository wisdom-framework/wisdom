package org.ow2.chameleon.wisdom.test.shared;

import org.junit.runners.model.InitializationError;
import org.osgi.framework.BundleContext;

/**
 *
 */
public class InVivoRunnerFactory {

    private final BundleContext context;

    public InVivoRunnerFactory(BundleContext context) {
        this.context = context;
    }

    public InVivoRunner create(String clazz) throws ClassNotFoundException, InitializationError {
        Class c = findClass(clazz);
        return new InVivoRunner(context, c);
    }

    private Class findClass(String clazz) throws ClassNotFoundException {
        return context.getBundle().loadClass(clazz);
    }
}
