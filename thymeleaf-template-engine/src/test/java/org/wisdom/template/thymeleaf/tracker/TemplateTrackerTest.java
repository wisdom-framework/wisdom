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
import static org.mockito.Matchers.anyString;
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

        Bundle bundle = mock(Bundle.class);
        // Test on empty bundle.
        when(bundle.findEntries(anyString(), anyString(), anyBoolean())).thenReturn(null);
        assertThat(tracker.addingBundle(bundle, new BundleEvent(BundleEvent.STARTED, bundle))).isEmpty();
        // Verify no call
        verify(tracker.engine, never()).addTemplate(any(URL.class));

        // New bundle with a template inside.
        File file = new File("src/test/resources/templates/javascript.html");
        Vector<URL> v = new Vector<URL>();
        v.add(file.toURI().toURL());
        when(bundle.findEntries(anyString(), anyString(), anyBoolean())).thenReturn(v.elements());

        List<ThymeLeafTemplateImplementation> list = tracker.addingBundle(bundle, new BundleEvent(BundleEvent.STARTED, bundle));
        assertThat(list).isNotNull();
        assertThat(list).hasSize(1);
        verify(tracker.engine, times(1)).addTemplate(file.toURI().toURL());

        tracker.modifiedBundle(bundle, null, list);
        verify(tracker.engine, times(1)).updatedTemplate(any(ThymeLeafTemplateImplementation.class));

        list.clear();
        list.add(mock(ThymeLeafTemplateImplementation.class));
        tracker.removedBundle(bundle, null, list);
        verify(tracker.engine, times(1)).deleteTemplate(any(ThymeLeafTemplateImplementation.class));
    }
}
