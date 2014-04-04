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
