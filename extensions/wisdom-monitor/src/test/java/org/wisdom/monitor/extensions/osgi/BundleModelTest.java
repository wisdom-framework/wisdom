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

import org.assertj.core.api.Condition;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

import java.util.Dictionary;
import java.util.Hashtable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test the bundle models.
 */
public class BundleModelTest {
    @Test
    public void testBundles() throws Exception {
        final Bundle bundle1 = mock(Bundle.class);
        when(bundle1.getBundleId()).thenReturn(1l);
        final Bundle bundle2 = mock(Bundle.class);
        when(bundle2.getBundleId()).thenReturn(2l);
        BundleContext bc = mock(BundleContext.class);
        when(bc.getBundles()).thenReturn(new Bundle[]
                {
                        bundle1,
                        bundle2
                });

        assertThat(BundleModel.bundles(bc))
                .have(new Condition<BundleModel>() {
                    @Override
                    public boolean matches(BundleModel model) {
                        return model.getId() == bundle1.getBundleId() || model.getId() == bundle2.getBundleId();
                    }
                })
                .hasSize(2);
    }

    @Test
    public void state() throws Exception {
        Bundle bundle = mock(Bundle.class);
        BundleModel model = new BundleModel(bundle);
        when(bundle.getState()).thenReturn(Bundle.ACTIVE);
        assertThat(model.getState()).isEqualTo("ACTIVE");

        when(bundle.getState()).thenReturn(Bundle.INSTALLED);
        assertThat(model.getState()).isEqualTo("INSTALLED");

        when(bundle.getState()).thenReturn(Bundle.RESOLVED);
        assertThat(model.getState()).isEqualTo("RESOLVED");
    }

    @Test
    public void id() throws Exception {
        Bundle bundle = mock(Bundle.class);
        BundleModel model = new BundleModel(bundle);
        when(bundle.getBundleId()).thenReturn(1l);
        assertThat(model.getId()).isEqualTo(1l);
    }

    @Test
    public void name() throws Exception {
        Bundle bundle = mock(Bundle.class);
        BundleModel model = new BundleModel(bundle);
        when(bundle.getBundleId()).thenReturn(1l);
        when(bundle.getSymbolicName()).thenReturn("acme");
        when(bundle.getVersion()).thenReturn(new Version(1, 0, 0));
        assertThat(model.getName()).contains("acme").contains("1.0.0");

        when(bundle.getVersion()).thenReturn(new Version(1, 0, 0, "snapshot"));
        assertThat(model.getName()).contains("acme").contains("1.0.0.snapshot");
    }

    @Test
    public void headers() throws Exception {
        Bundle bundle = mock(Bundle.class);
        BundleModel model = new BundleModel(bundle);

        Dictionary<String, String> headers = new Hashtable<>();
        headers.put("k1", "v1");
        headers.put(Constants.BUNDLE_MANIFESTVERSION, "2");
        headers.put(Constants.IMPORT_PACKAGE, "org.osgi.framework");

        when(bundle.getHeaders()).thenReturn(headers);
        assertThat(model.getHeaders())
                .containsEntry(Constants.BUNDLE_MANIFESTVERSION, "2")
                .containsEntry(Constants.IMPORT_PACKAGE, "org.osgi.framework")
                .containsEntry("k1", "v1");
    }
}
