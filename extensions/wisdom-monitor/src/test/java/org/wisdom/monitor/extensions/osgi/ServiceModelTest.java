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
package org.wisdom.monitor.extensions.osgi;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test the Service Model.
 */
public class ServiceModelTest {
    @Test
    public void testServices() throws Exception {
        ServiceReference reference1 = mock(ServiceReference.class);
        ServiceReference reference2 = mock(ServiceReference.class);
        BundleContext context = mock(BundleContext.class);

        // No services.
        when(context.getAllServiceReferences(null, null)).thenReturn(null);
        assertThat(ServiceModel.services(context)).isEmpty();

        // 1 service
        when(context.getAllServiceReferences(null, null)).thenReturn(new ServiceReference[] {reference1});
        assertThat(ServiceModel.services(context)).hasSize(1);

        // 2 services
        when(context.getAllServiceReferences(null, null)).thenReturn(new ServiceReference[] {reference1, reference2});
        assertThat(ServiceModel.services(context)).hasSize(2);
    }

    @Test
    public void interfaces() throws Exception {
        ServiceReference reference = mock(ServiceReference.class);
        ServiceModel model = new ServiceModel(reference);
        when(reference.getProperty(Constants.OBJECTCLASS)).thenReturn(new String[]{List.class.getName()});
        assertThat(model.getInterfaces()).isEqualTo(List.class.getName());

        when(reference.getProperty(Constants.OBJECTCLASS)).thenReturn(new String[]{List.class.getName(),
                String.class.getName()});
        assertThat(model.getInterfaces()).isEqualTo(List.class.getName() + ", " + String.class.getName());
    }

    @Test
    public void id() throws Exception {
        ServiceReference reference = mock(ServiceReference.class);
        ServiceModel model = new ServiceModel(reference);
        when(reference.getProperty(Constants.SERVICE_ID)).thenReturn(20l);
        assertThat(model.getId()).isEqualTo(20l);
    }

    @Test
    public void bundle() throws Exception {
        Bundle bundle = mock(Bundle.class);
        when(bundle.getSymbolicName()).thenReturn("acme");
        when(bundle.getBundleId()).thenReturn(20l);
        ServiceReference reference = mock(ServiceReference.class);
        when(reference.getBundle()).thenReturn(bundle);

        assertThat(new ServiceModel(reference).getProvidingBundle()).contains("acme").contains("[20]");
        when(bundle.getSymbolicName()).thenReturn(null);
        assertThat(new ServiceModel(reference).getProvidingBundle()).doesNotContain("acme").isEqualTo("[20]");
    }

    @Test
    public void properties() throws Exception {
        ServiceReference reference = mock(ServiceReference.class);
        ServiceModel model = new ServiceModel(reference);
        when(reference.getProperty(Constants.SERVICE_ID)).thenReturn(20l);
        when(reference.getProperty(Constants.OBJECTCLASS)).thenReturn(new String[]{List.class.getName()});
        when(reference.getProperty("foo")).thenReturn("bar");
        when(reference.getPropertyKeys()).thenReturn(new String[] { Constants.SERVICE_ID, Constants.OBJECTCLASS,
                "foo"});

        assertThat(model.getProperties())
                .containsEntry(Constants.SERVICE_ID, "20")
                .containsEntry(Constants.OBJECTCLASS, "[" + List.class.getName() + "]")
                .containsEntry("foo", "bar");

    }
}
