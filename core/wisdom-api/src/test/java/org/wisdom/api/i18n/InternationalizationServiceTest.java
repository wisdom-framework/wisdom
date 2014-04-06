package org.wisdom.api.i18n;

import org.junit.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check the default locale.
 */
public class InternationalizationServiceTest {

    @Test
    public void testDefaultLocale() {
        Locale defaultLocale = InternationalizationService.DEFAULT_LOCALE;
        assertThat(defaultLocale.getLanguage()).isEmpty();
        assertThat(defaultLocale.getCountry()).isEmpty();
        assertThat(defaultLocale.getVariant()).isEmpty();
    }
}
