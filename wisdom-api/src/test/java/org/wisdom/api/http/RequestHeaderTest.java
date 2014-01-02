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

        Locale[] locales = RequestHeader.getLocaleList("da, en-gb;q=0.8, en;q=0.7");
        assertThat(locales[0]).isEqualTo(Locale.forLanguageTag("da"));
        assertThat(locales[1]).isEqualTo(Locale.forLanguageTag("en-gb"));
        assertThat(locales[2]).isEqualTo(Locale.ENGLISH);

        locales = RequestHeader.getLocaleList("fr");
        assertThat(locales[0]).isEqualTo(Locale.FRENCH);

        locales = RequestHeader.getLocaleList("en-ca,en;q=0.8,en-us;q=0.6,de-de;q=0.4,de;q=0.2");
        assertThat(locales[0]).isEqualTo(Locale.CANADA);
        assertThat(locales[1]).isEqualTo(Locale.ENGLISH);
        assertThat(locales[2]).isEqualTo(Locale.US);
        assertThat(locales[3]).isEqualTo(Locale.GERMANY);
        assertThat(locales[4]).isEqualTo(Locale.GERMAN);

        locales = RequestHeader.getLocaleList("en-ca,en-us;q=0.6 ,de;q=0.2, en;q=0.8, ,de-de;q=0.4");
        assertThat(locales[0]).isEqualTo(Locale.CANADA);
        assertThat(locales[1]).isEqualTo(Locale.ENGLISH);
        assertThat(locales[2]).isEqualTo(Locale.US);
        assertThat(locales[3]).isEqualTo(Locale.GERMANY);
        assertThat(locales[4]).isEqualTo(Locale.GERMAN);

        locales = RequestHeader.getLocaleList(null);
        assertThat(locales).isEmpty();

        locales = RequestHeader.getLocaleList("");
        assertThat(locales).isEmpty();

        locales = RequestHeader.getLocaleList("abc");
        assertThat(locales[0]).isEqualTo(new Locale("abc"));

        locales = RequestHeader.getLocaleList("123$4567");
        assertThat(locales).isEmpty();

        locales = RequestHeader.getLocaleList("en, en-US,en-cockney,i-cherokee,x-pig-latin");
        assertThat(locales[0]).isEqualTo(Locale.ENGLISH);
        assertThat(locales[1]).isEqualTo(Locale.US);
        assertThat(locales[2]).isEqualTo(new Locale("en", "", "cockney"));
        assertThat(locales[3]).isEqualTo(Locale.forLanguageTag("i-cherokee"));
        assertThat(locales[4]).isEqualTo(Locale.forLanguageTag("x-pig-latin"));

        // That's the configuration of my browser.
        locales = RequestHeader.getLocaleList("en-US,en;q=0.8,de;q=0.6,fr;q=0.4");
        assertThat(locales[0]).isEqualTo(Locale.US);
        assertThat(locales[1]).isEqualTo(Locale.ENGLISH);
        assertThat(locales[2]).isEqualTo(Locale.GERMAN);
        assertThat(locales[3]).isEqualTo(Locale.FRENCH);
    }
}
