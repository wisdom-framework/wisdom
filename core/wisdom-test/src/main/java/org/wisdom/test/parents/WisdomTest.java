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
package org.wisdom.test.parents;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.core.services.Stability;
import org.ow2.chameleon.testing.helpers.TimeUtils;
import org.wisdom.api.http.HeaderNames;
import org.wisdom.api.http.Status;
import org.wisdom.test.WisdomRunner;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A class easing the implementation of tests.
 * It provides a couple of useful methods to retrieve the content of action's results.
 */
@RunWith(WisdomRunner.class)
public class WisdomTest extends WisdomUnitTest implements Status, HeaderNames {

    /**
     * The bundle context.
     * The Wisdom Test Framework injects the bundle context of the bundle containing the test classes. Other bundle
     * contexts can be retrieve using {@code context.getBundle(id).getBundleContext()}.
     */
    @Inject
    public BundleContext context;

    /**
     * A method executed before the execution of each test method checking that the bundle context is correctly
     * injected and that the framework is in a stable state. Stability means that all bundles are resolved (except
     * fragments), and there are no flipping states.
     */
    @Before
    public void ensureBundleContextInjection() {
        assertThat(context).isNotNull();
        int factor = Integer.getInteger("time.factor", 1);
        if (factor != 1) {
            // Set the time factor, it should use an API, but we don't have such an API yet.
            TimeUtils.TIME_FACTOR = factor; //NOSONAR
        }

        ServiceReference<Stability> reference
                = context.getServiceReference(Stability.class);
        Stability stability = context.getService(reference);
        if (!stability.waitForStability()) {
            throw new IllegalStateException("Cannot reach stability");
        }
    }
}
