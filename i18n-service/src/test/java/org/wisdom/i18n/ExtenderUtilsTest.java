package org.wisdom.i18n;

import com.google.common.collect.Iterators;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.wisdom.api.i18n.InternationalizationService;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Check utilities functions
 */
public class ExtenderUtilsTest {
    @Test
    public void testGetLocaleFromResourceName() throws Exception {
        assertThat(ExtenderUtils.getLocaleFromResourceName("messages.properties")).isEqualTo
                (InternationalizationService.DEFAULT_LOCALE);
        assertThat(ExtenderUtils.getLocaleFromResourceName("messages_fr.properties")).isEqualTo
                (Locale.FRENCH);
        assertThat(ExtenderUtils.getLocaleFromResourceName("messages_fr-fr.properties")).isEqualTo
                (Locale.FRANCE);
        assertThat(ExtenderUtils.getLocaleFromResourceName("messages_en.properties")).isEqualTo
                (Locale.ENGLISH);
        assertThat(ExtenderUtils.getLocaleFromResourceName("messages_en-ca.properties")).isEqualTo
                (Locale.CANADA);
    }

    @Test
    public void testTraverseOnMockBundle() {
        List<String> structure = Arrays.asList(
                "i18n/messages.properties",
                "i18n/messages_fr.properties"
        );
        Enumeration<String> paths = Iterators.asEnumeration(structure.iterator());
        Bundle bundle = mock(Bundle.class);
        when(bundle.getEntryPaths(anyString())).thenReturn(paths);
        when(bundle.getEntry("i18n/messages.properties"))
                .thenReturn(this.getClass().getClassLoader().getResource("i18n/messages.properties"));
        when(bundle.getEntry("i18n/messages_fr.properties"))
                .thenReturn(this.getClass().getClassLoader().getResource("i18n/messages_fr.properties"));

        List<I18nExtension> list;
        list = ExtenderUtils.analyze("/i18n/", bundle);

        assertThat(list.size()).isEqualTo(2);
        assertThat(list.get(0).locale()).isEqualTo(InternationalizationService.DEFAULT_LOCALE);
        assertThat(list.get(0).get("welcome")).isEqualTo("hello");
        assertThat(list.get(0).get("lang")).isEqualTo("english");
        assertThat(list.get(1).locale()).isEqualTo(Locale.FRENCH);
        assertThat(list.get(1).get("welcome")).isEqualTo("bonjour");
        assertThat(list.get(1).get("lang")).isEqualTo("français");

    }

    @Test
    public void testTraverseOnEmptyBundle() {
        List<String> structure = Arrays.asList();
        Enumeration<String> paths = Iterators.asEnumeration(structure.iterator());
        Bundle bundle = mock(Bundle.class);
        when(bundle.getEntryPaths(anyString())).thenReturn(paths);
        List<I18nExtension> list;
        list = ExtenderUtils.analyze("/i18n/", bundle);

        assertThat(list.size()).isEqualTo(0);
    }

    @Test
    public void testTraverseOnEmptyBundleWhenReturningNull() {
        Bundle bundle = mock(Bundle.class);
        when(bundle.getEntryPaths(anyString())).thenReturn(null);
        List<I18nExtension> list;
        list = ExtenderUtils.analyze("/i18n/", bundle);

        assertThat(list.size()).isEqualTo(0);
    }
}
