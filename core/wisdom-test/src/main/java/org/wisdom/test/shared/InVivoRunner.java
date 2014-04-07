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

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.testing.helpers.OSGiHelper;
import org.ow2.chameleon.testing.helpers.Stability;
import org.ow2.chameleon.testing.helpers.TimeUtils;
import org.wisdom.test.parents.DependencyInjector;

/**
 * A Junit Runner executed within the OSGi Framework (Wisdom).
 */
public class InVivoRunner extends BlockJUnit4ClassRunner {


    private final BundleContext context;
    private final OSGiHelper helper;

    public InVivoRunner(BundleContext context, Class<?> klass) throws InitializationError {
        super(klass);
        // Set time factor.
        TimeUtils.TIME_FACTOR = Integer.getInteger("TIME_FACTOR", 1); //NOSONAR
        this.context = context;
        this.helper = new OSGiHelper(context);
    }

    public Object createTest() throws Exception {
        Stability.waitForStability(context);
        Object object = super.createTest();
        DependencyInjector.inject(object, helper);
        return object;
    }

    @Override
    public void run(RunNotifier notifier) {
        super.run(notifier);
        helper.dispose();
    }
}
