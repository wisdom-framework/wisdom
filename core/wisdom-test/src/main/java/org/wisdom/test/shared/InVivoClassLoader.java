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
package org.wisdom.test.shared;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A classloader responsible for loading the test classes and finding classes from the application bundles.
 */
public class InVivoClassLoader extends ClassLoader {
    private final BundleContext context;
    private final String testClass;

    /**
     * Creates the classloader.
     *
     * @param clazz   the test class
     * @param context the bundle context
     */
    public InVivoClassLoader(String clazz, BundleContext context) {
        this.context = context;
        this.testClass = clazz;
    }

    /**
     * Loads the byte code of the given class. The byte code is loaded form the resource contained in the bundle.
     *
     * @param classname the class name
     * @return the loaded byte array containing the byte code
     * @throws IOException if the class cannot be loaded.
     */
    private byte[] loadBytecode(String classname) throws IOException {
        URL resource = context.getBundle().getResource(classname.replace(".", "/") + ".class");
        if (resource == null) {
            throw new IOException("Cannot load the bytecode of " + classname);
        }
        return IOUtils.toByteArray(resource);
    }

    /**
     * Attempts to load the class with the given name. If this class is the test class (or one of its inner class),
     * it defines it, otherwise it loads the class using the bundle class loader.
     *
     * @param name the class name
     * @return the loaded class
     * @throws ClassNotFoundException if the class cannot be loaded (not found)
     */
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (name.equals(testClass) || name.startsWith(testClass + "$")) {
            try {
                final byte[] bytes = loadBytecode(name);
                return defineClass(name, bytes, 0, bytes.length);
            } catch (IOException e) {
                throw new RuntimeException("Cannot define class " + name + " - did not find the .class file", e);
            }
        }

        try {
            return context.getBundle().loadClass(name);
        } catch (ClassNotFoundException e) { //NOSONAR
            // Next.
        }

        List<Bundle> bundles = Arrays.asList(context.getBundles());
        Collections.reverse(bundles);
        for (Bundle bundle : bundles) {
            try {
                return bundle.loadClass(name);
            } catch (ClassNotFoundException e) { //NOSONAR
                // Next.
            }
        }

        return super.loadClass(name);


    }
}
