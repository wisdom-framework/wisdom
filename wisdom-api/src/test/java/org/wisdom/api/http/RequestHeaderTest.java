package org.wisdom.api.http;

import org.junit.Test;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check the implemented methods from RequestHeader.
 */
public class RequestHeaderTest {
    @Test
    public void testGetLocaleList() throws Exception {

        List<Locale> locales = RequestHeader.getLocaleList("da, en-gb;q=0.8, en;q=0.7");
        assertThat(locales.get(0)).isEqualTo(Locale.forLanguageTag("da"));
        assertThat(locales.get(1)).isEqualTo(Locale.forLanguageTag("en-gb"));
        assertThat(locales.get(2)).isEqualTo(Locale.ENGLISH);

        locales = RequestHeader.getLocaleList("fr");
        assertThat(locales.get(0)).isEqualTo(Locale.FRENCH);

        locales = RequestHeader.getLocaleList("en-ca,en;q=0.8,en-us;q=0.6,de-de;q=0.4,de;q=0.2");
        assertThat(locales.get(0)).isEqualTo(Locale.CANADA);
        assertThat(locales.get(1)).isEqualTo(Locale.ENGLISH);
        assertThat(locales.get(2)).isEqualTo(Locale.US);
        assertThat(locales.get(3)).isEqualTo(Locale.GERMANY);
        assertThat(locales.get(4)).isEqualTo(Locale.GERMAN);

        locales = RequestHeader.getLocaleList("en-ca,en-us;q=0.6 ,de;q=0.2, en;q=0.8, ,de-de;q=0.4");
        assertThat(locales.get(0)).isEqualTo(Locale.CANADA);
        assertThat(locales.get(1)).isEqualTo(Locale.ENGLISH);
        assertThat(locales.get(2)).isEqualTo(Locale.US);
        assertThat(locales.get(3)).isEqualTo(Locale.GERMANY);
        assertThat(locales.get(4)).isEqualTo(Locale.GERMAN);

        locales = RequestHeader.getLocaleList(null);
        assertThat(locales).isEmpty();

        locales = RequestHeader.getLocaleList("");
        assertThat(locales).isEmpty();

        locales = RequestHeader.getLocaleList("abc");
        assertThat(locales.get(0)).isEqualTo(new Locale("abc"));

        locales = RequestHeader.getLocaleList("123$4567");
        assertThat(locales).isEmpty();

        locales = RequestHeader.getLocaleList("en, en-US,en-cockney,i-cherokee,x-pig-latin");
        assertThat(locales.get(0)).isEqualTo(Locale.ENGLISH);
        assertThat(locales.get(1)).isEqualTo(Locale.US);
        assertThat(locales.get(2)).isEqualTo(new Locale("en", "", "cockney"));
        assertThat(locales.get(3)).isEqualTo(Locale.forLanguageTag("i-cherokee"));
        assertThat(locales.get(4)).isEqualTo(Locale.forLanguageTag("x-pig-latin"));
    }
}
