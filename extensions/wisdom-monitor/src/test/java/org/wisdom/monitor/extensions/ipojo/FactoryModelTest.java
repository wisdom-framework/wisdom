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
package org.wisdom.monitor.extensions.ipojo;

import com.google.common.collect.ImmutableList;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.HandlerFactory;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FactoryModelTest {

    @Test
    public void testFactories() throws Exception {
        Factory factory1 = mock(Factory.class);
        when(factory1.getName()).thenReturn("factory-1");

        Factory factory2 = mock(Factory.class);
        when(factory2.getName()).thenReturn("factory-2");

        HandlerFactory factory3 = mock(HandlerFactory.class);
        when(factory3.getName()).thenReturn("factory-3");
        when(factory3.getHandlerName()).thenReturn("h:factory-3");

        ServiceReference<Factory> ref1 = mock(ServiceReference.class);
        ServiceReference<Factory> ref2 = mock(ServiceReference.class);

        ServiceReference<HandlerFactory> ref3 = mock(ServiceReference.class);

        BundleContext context = mock(BundleContext.class);
        when(context.getServiceReferences(Factory.class, null)).thenReturn(ImmutableList.of(ref1, ref2));
        when(context.getServiceReferences(HandlerFactory.class, null)).thenReturn(ImmutableList.of(ref3));

        when(context.getService(ref1)).thenReturn(factory1);
        when(context.getService(ref2)).thenReturn(factory2);
        when(context.getService(ref3)).thenReturn(factory3);

        List<FactoryModel> models = FactoryModel.factories(context);
        assertThat(models).hasSize(3);
    }

    @Test
    public void testGetName() throws Exception {
        Factory factory = mock(Factory.class);
        when(factory.getName()).thenReturn("foo");
        FactoryModel model = new FactoryModel(factory);

        // Without version
        assertThat(model.getName()).isEqualTo(factory.getName());

        // With version
        when(factory.getVersion()).thenReturn("1.0.0");
        assertThat(model.getName()).isEqualTo(factory.getName() + " - " + factory.getVersion());
    }

    @Test
    public void testIsHandlerOnHandler() throws Exception {
        Factory factory = mock(HandlerFactory.class);
        when(factory.getName()).thenReturn("foo");
        FactoryModel model = new FactoryModel(factory);
        assertThat(model.isHandler()).isTrue();
    }

    @Test
    public void testIsHandlerOnNonHandler() throws Exception {
        Factory factory = mock(Factory.class);
        when(factory.getName()).thenReturn("foo");
        FactoryModel model = new FactoryModel(factory);
        assertThat(model.isHandler()).isFalse();
    }

    @Test
    public void testGetHandlerName() throws Exception {
        Factory factory = mock(Factory.class);
        when(factory.getName()).thenReturn("foo");
        FactoryModel model = new FactoryModel(factory);
        assertThat(model.getHandlerName()).isNull();

        HandlerFactory hf = mock(HandlerFactory.class);
        when(hf.getName()).thenReturn("foo");
        when(hf.getHandlerName()).thenReturn("foo:foo");
        model = new FactoryModel(hf);
        assertThat(model.getHandlerName()).isEqualTo("foo:foo");
    }

    @Test
    public void testGetState() throws Exception {
        Factory factory = mock(Factory.class);
        when(factory.getName()).thenReturn("foo");
        when(factory.getState()).thenReturn(Factory.INVALID);
        FactoryModel model = new FactoryModel(factory);
        assertThat(model.getState()).contains("INVALID");

        when(factory.getState()).thenReturn(Factory.VALID);
        assertThat(model.getState()).contains("VALID").doesNotContain("INVALID");
    }
}