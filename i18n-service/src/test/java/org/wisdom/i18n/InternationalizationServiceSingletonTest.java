package org.wisdom.i18n;

import com.google.common.collect.Iterators;
import org.assertj.core.data.MapEntry;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.wisdom.api.i18n.InternationalizationService;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Check the internationalization service implementation.
 */
public class InternationalizationServiceSingletonTest {


    private InternationalizationServiceSingleton svc;

    @Before
    public void setUp() {
        svc = new InternationalizationServiceSingleton(null);
    }

    @Test
    public void addBundle() {
        Bundle bundle = getMockBundle();

        List<I18nExtension> extensions = svc.addingBundle(bundle, null);
        assertThat(extensions.size()).isEqualTo(2);

        assertThat(svc.getAllMessages(Locale.FRENCH)).containsKeys("lang", "welcome", "autre");
        assertThat(svc.get(Locale.FRENCH, "lang")).isEqualTo("français");

        // When the locale is not supported, return default.
        assertThat(svc.get(Locale.CHINA, "lang")).isEqualTo("english");
    }

    @Test
    public void testGetAllMessages() {
        Bundle bundle = getMockBundle();

        List<I18nExtension> extensions = svc.addingBundle(bundle, null);
        assertThat(extensions).isNotEmpty();

        Map<String, String> messages = svc.getAllMessages(Locale.FRENCH);
        assertThat(messages).contains(
                MapEntry.entry("welcome", "bonjour"),
                MapEntry.entry("lang", "français"),
                MapEntry.entry("autre", "autre"),
                MapEntry.entry("extra", "extra")
        );

        messages = svc.getAllMessages(Locale.CHINA);
        assertThat(messages).contains(
                MapEntry.entry("welcome", "hello"),
                MapEntry.entry("lang", "english"),
                MapEntry.entry("extra", "extra")
        );
        assertThat(messages).doesNotContainKey("autre");
    }

    @Test
    public void addBundleAndRemoveIt() {
        Bundle bundle = getMockBundle();

        List<I18nExtension> extensions = svc.addingBundle(bundle, null);
        assertThat(extensions.size()).isEqualTo(2);
        assertThat(svc.getAllMessages(Locale.FRENCH)).hasSize(4);

        svc.removedBundle(bundle, null, extensions);
        assertThat(svc.getAllMessages(Locale.FRENCH)).hasSize(0);
    }

    @Test
    public void testGetMessagesOnAnOrderedSetOfLocales() {
        Bundle bundle = getMockBundle();

        List<I18nExtension> extensions = svc.addingBundle(bundle, null);
        assertThat(extensions).isNotEmpty();

        String value = svc.get(new Locale[] {Locale.FRENCH}, "welcome");
        assertThat(value).isEqualTo("bonjour");

        value = svc.get(new Locale[] {Locale.ENGLISH}, "welcome");
        assertThat(value).isEqualTo("hello");

        value = svc.get(new Locale[] {InternationalizationService.DEFAULT_LOCALE}, "welcome");
        assertThat(value).isEqualTo("hello");

        value = svc.get(new Locale[] {Locale.FRENCH, Locale.ENGLISH}, "extra");
        assertThat(value).isEqualTo("extra");

        value = svc.get(new Locale[] {Locale.ENGLISH, Locale.FRENCH}, "autre");
        assertThat(value).isEqualTo("autre");
    }

    private Bundle getMockBundle() {
        List<String> structure = Arrays.asList(
                "/i18n/messages.properties",
                "/i18n/messages_fr.properties"
        );
        Enumeration<String> paths = Iterators.asEnumeration(structure.iterator());
        Bundle bundle = mock(Bundle.class);
        when(bundle.getEntryPaths(anyString())).thenReturn(paths);
        when(bundle.getEntry("/i18n/messages.properties"))
                .thenReturn(this.getClass().getClassLoader().getResource("i18n/messages.properties"));
        when(bundle.getEntry("/i18n/messages_fr.properties"))
                .thenReturn(this.getClass().getClassLoader().getResource("i18n/messages_fr.properties"));
        return bundle;
    }

}
