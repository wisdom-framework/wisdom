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

import org.junit.runners.model.InitializationError;
import org.osgi.framework.BundleContext;

/**
 * The in-vivo runner factory. This class is responsible from the creation of the `in-vivo junit runner`.
 */
public class InVivoRunnerFactory {

    private final BundleContext context;

    /**
     * Creates a new instance of {@link InVivoRunnerFactory}.
     *
     * @param context the bundle context.
     */
    public InVivoRunnerFactory(BundleContext context) {
        this.context = context;
    }

    /**
     * Creates a runner for the given class. The class is loaded from the bundle classloader (using a specific
     * classloader).
     *
     * @param clazz the class name to load (ending with `IT`)
     * @return the runner to use
     * @throws ClassNotFoundException if the class cannot be found
     * @throws InitializationError    if the class cannot be initialized, because for instance services cannot be
     *                                injected
     */
    public InVivoRunner create(String clazz) throws ClassNotFoundException, InitializationError {
        Class c = findClass(clazz);
        return new InVivoRunner(context, c);
    }

    private Class findClass(String clazz) throws ClassNotFoundException {
        // Here things start to be a bit more complicated.
        // Loading the class directly is not an issue but we should define our own classloader to access the
        // application classes.
        ClassLoader loader = new InVivoClassLoader(clazz, context);
        return loader.loadClass(clazz);
    }
}
