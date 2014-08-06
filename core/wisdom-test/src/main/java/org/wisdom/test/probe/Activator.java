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
package org.wisdom.test.probe;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.wisdom.test.shared.InVivoRunnerFactory;

/**
 * The probe activator.
 */
public class Activator implements BundleActivator {

    private ServiceRegistration<InVivoRunnerFactory> registration;

    /**
     * Creates and registers the {@link org.wisdom.test.shared.InVivoRunnerFactory} as OSGi service.
     *
     * @param context the bundle context
     */
    @Override
    public void start(BundleContext context) {
        InVivoRunnerFactory factory = new InVivoRunnerFactory(context);
        registration = context.registerService(InVivoRunnerFactory.class, factory, null);
    }

    /**
     * Un-registers the registered service.
     *
     * @param context the bundle context
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        registration.unregister();
    }
}
