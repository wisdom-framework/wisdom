package org.wisdom.test.shared;

import org.apache.commons.io.IOUtils;
import org.junit.runners.model.InitializationError;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.net.URL;

/**
 *
 */
public class InVivoRunnerFactory {

    private final BundleContext context;

    public InVivoRunnerFactory(BundleContext context) {
        this.context = context;
    }

    public InVivoRunner create(String clazz) throws ClassNotFoundException, InitializationError, IOException {
        Class c = findClass(clazz);
        return new InVivoRunner(context, c);
    }

    private Class findClass(String clazz) throws ClassNotFoundException, IOException {
        // Here things start to be a bit more complicated.
        // Loading the class directly is not an issue but we should define our own classloader to access the
        // application classes.
        ClassLoader loader = new InVivoClassLoader(clazz, context);
        return loader.loadClass(clazz);
    }
}
