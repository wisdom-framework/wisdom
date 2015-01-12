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
package org.wisdom.template.thymeleaf.tracker;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.wisdom.template.thymeleaf.ThymeleafTemplateCollector;
import org.wisdom.template.thymeleaf.impl.ThymeLeafTemplateImplementation;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Check the behavior of the Template Tracker
 */
public class TemplateTrackerTest {

    @Test
    public void testStartStop() {
        TemplateTracker tracker = new TemplateTracker();
        tracker.context = mock(BundleContext.class);
        tracker.engine = mock(ThymeleafTemplateCollector.class);
        tracker.start();
        tracker.stop();
    }

    @Test
    public void testDynamism() throws MalformedURLException {
        TemplateTracker tracker = new TemplateTracker();
        tracker.context = mock(BundleContext.class);
        tracker.engine = mock(ThymeleafTemplateCollector.class);
        when(tracker.engine.extension()).thenReturn(ThymeleafTemplateCollector.THYMELEAF_TEMPLATE_EXTENSION);

        Bundle bundle = mock(Bundle.class);
        // Test on empty bundle.
        when(bundle.findEntries(anyString(), anyString(), anyBoolean())).thenReturn(null);
        assertThat(tracker.addingBundle(bundle, new BundleEvent(BundleEvent.STARTED, bundle))).isEmpty();
        // Verify no call
        verify(tracker.engine, never()).addTemplate(any(Bundle.class), any(URL.class));

        // New bundle with a template inside.
        File file = new File("src/test/resources/templates/javascript.thl.html");
        Vector<URL> v = new Vector<URL>();
        v.add(file.toURI().toURL());
        when(bundle.findEntries(anyString(), anyString(), anyBoolean())).thenReturn(v.elements());

        List<ThymeLeafTemplateImplementation> list = tracker.addingBundle(bundle, new BundleEvent(BundleEvent.STARTED, bundle));
        assertThat(list).isNotNull();
        assertThat(list).hasSize(1);
        verify(tracker.engine, times(1)).addTemplate(bundle, file.toURI().toURL());

        tracker.modifiedBundle(bundle, null, list);
        verify(tracker.engine, times(1)).updatedTemplate();

        list.clear();
        list.add(mock(ThymeLeafTemplateImplementation.class));
        tracker.removedBundle(bundle, null, list);
        verify(tracker.engine, times(1)).deleteTemplate(any(ThymeLeafTemplateImplementation.class));
    }
}
