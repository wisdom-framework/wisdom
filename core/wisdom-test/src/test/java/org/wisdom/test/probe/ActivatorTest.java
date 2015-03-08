/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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

import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.wisdom.test.shared.InVivoRunnerFactory;

import java.util.Dictionary;

import static org.mockito.Mockito.*;

public class ActivatorTest {


    @Test
    public void testActivator() throws Exception {
        Activator activator = new Activator();
        BundleContext context = mock(BundleContext.class);
        ServiceRegistration registration = mock(ServiceRegistration.class);

        when(context.registerService(any(Class.class),
                any(InVivoRunnerFactory.class), any(Dictionary.class))).thenReturn(registration);

        activator.start(context);
        activator.stop(context);

        verify(context).registerService(any(Class.class),
                any(InVivoRunnerFactory.class), any(Dictionary.class));
        verify(registration).unregister();
    }

}