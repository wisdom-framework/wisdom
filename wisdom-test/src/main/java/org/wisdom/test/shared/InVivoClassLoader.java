package org.wisdom.test.shared;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A classloader responsible for loading the test classes and finding classes from the application bundles.
 */
public class InVivoClassLoader extends ClassLoader {
    private final BundleContext context;
    private final String testClass;

    public InVivoClassLoader(String clazz,BundleContext context) {
        this.context = context;
        this.testClass = clazz;
    }

    private byte[] loadBytecode(String classname) throws IOException {
        URL resource = context.getBundle().getResource(classname.replace(".", "/") + ".class");
        return IOUtils.toByteArray(resource);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (name.equals(testClass)  || name.startsWith(testClass + "$")) {
            try {
                final byte[] bytes = loadBytecode(name);
                return defineClass(name, bytes, 0, bytes.length);
            } catch (IOException e) {
                throw new RuntimeException("Cannot define class " + name + " - did not find the .class file", e);
            }
        }

        try {
            return context.getBundle().loadClass(name);
        } catch (ClassNotFoundException e) {
            // Next.
        }

        List<Bundle> bundles = Arrays.asList(context.getBundles());
        Collections.reverse(bundles);
        for (Bundle bundle : bundles) {
            try {
                return bundle.loadClass(name);
            } catch (ClassNotFoundException e) {
                // Next.
            }
        }

        return super.loadClass(name);


    }
}
