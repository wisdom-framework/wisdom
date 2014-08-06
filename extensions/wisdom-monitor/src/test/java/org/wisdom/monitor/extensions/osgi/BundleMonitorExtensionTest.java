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

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Status;
import org.wisdom.test.parents.Action;
import org.wisdom.test.parents.Invocation;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.wisdom.test.parents.Action.action;

/**
 * Tests the Bundle monitor extension.
 */
public class BundleMonitorExtensionTest {

    BundleMonitorExtension extension = new BundleMonitorExtension();

    BundleContext context;

    @Before
    public void setUp() {
        context = mock(BundleContext.class);
        extension.context = context;
    }

    @Test
    public void testStartStop() {
        extension.start();
        extension.stop();
    }

    @Test
    public void testBundles() {
        extension.start();
        Bundle bundle1 = mock(Bundle.class);
        when(bundle1.getBundleId()).thenReturn(0l);
        when(bundle1.getSymbolicName()).thenReturn("System");
        when(bundle1.getVersion()).thenReturn(new Version("4.4.4"));
        when(bundle1.getHeaders()).thenReturn(new Hashtable<String, String>());
        when(bundle1.getState()).thenReturn(Bundle.ACTIVE);

        Bundle bundle2 = mock(Bundle.class);
        when(bundle2.getBundleId()).thenReturn(1l);
        when(bundle2.getSymbolicName()).thenReturn("iPOJO");
        when(bundle2.getVersion()).thenReturn(new Version("1.0.0"));
        when(bundle2.getState()).thenReturn(Bundle.INSTALLED);
        when(bundle2.getHeaders()).thenReturn(new Hashtable<String, String>());

        final Bundle[] array = {bundle1, bundle2};
        when(context.getBundles()).thenReturn(array);

        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return extension.bundles();
            }
        }).invoke();

        assertThat(result.getResult().getRenderable()).isNotNull();
        Map<String, Object> map = (Map<String, Object>) result.getResult().getRenderable().content();
        assertThat(map).containsEntry("events", 0).containsEntry("active", 1).containsEntry("installed", 1);
        assertThat((List<BundleModel>) map.get("bundles")).hasSize(2);
        extension.stop();
    }

    @Test
    public void testToggle() throws BundleException {
        extension.start();

        Bundle bundle1 = mock(Bundle.class);
        when(bundle1.getBundleId()).thenReturn(0l);
        when(bundle1.getSymbolicName()).thenReturn("System");
        when(bundle1.getVersion()).thenReturn(new Version("4.4.4"));
        when(bundle1.getHeaders()).thenReturn(new Hashtable<String, String>());
        when(bundle1.getState()).thenReturn(Bundle.ACTIVE);

        Bundle bundle2 = mock(Bundle.class);
        when(bundle2.getBundleId()).thenReturn(1l);
        when(bundle2.getSymbolicName()).thenReturn("iPOJO");
        when(bundle2.getVersion()).thenReturn(new Version("1.0.0"));
        when(bundle2.getState()).thenReturn(Bundle.INSTALLED);
        when(bundle2.getHeaders()).thenReturn(new Hashtable<String, String>());

        final Bundle[] array = {bundle1, bundle2};
        when(context.getBundles()).thenReturn(array);
        when(context.getBundle(1l)).thenReturn(bundle2);

        Result r = extension.toggleBundle(1l);
        verify(bundle2, times(1)).start();
        verify(bundle2, times(0)).stop();
        assertThat(r.getStatusCode()).isEqualTo(Status.OK);

        // Fix the bundle state
        when(bundle2.getState()).thenReturn(Bundle.ACTIVE);

        r = extension.toggleBundle(1l);
        verify(bundle2, times(1)).start();
        verify(bundle2, times(1)).stop();
        assertThat(r.getStatusCode()).isEqualTo(Status.OK);

        r = extension.toggleBundle(3l);
        assertThat(r.getStatusCode()).isEqualTo(Status.NOT_FOUND);

        extension.stop();
    }



}