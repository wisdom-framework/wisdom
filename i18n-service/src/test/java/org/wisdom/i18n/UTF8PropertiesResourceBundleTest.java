package org.wisdom.i18n;

import com.google.common.collect.Iterators;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check the UTF-8 support.
 */
public class UTF8PropertiesResourceBundleTest {


    @Test
    public void testUTF8() throws IOException {
        File properties = new File("target/test-classes/utf-8.properties");
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(properties);
            UTF8PropertiesResourceBundle bundle = new UTF8PropertiesResourceBundle(fis);
            String japanese = (String) bundle.handleGetObject("japanese");
            String russian = (String) bundle.handleGetObject("russian");
            String polish = (String) bundle.handleGetObject("polish");
            String german = (String) bundle.handleGetObject("german");
            String unit = (String) bundle.handleGetObject("unit");

            assertThat(japanese).isEqualTo("てすと");
            assertThat(russian).isEqualTo("Я Б Г Д Ж Й");
            assertThat(polish).isEqualTo("Ł Ą Ż Ę Ć Ń Ś Ź");
            assertThat(german).isEqualTo("Ä ä Ü ü ß");
            assertThat(unit).isEqualTo("℃ å");

            assertThat(bundle.handleGetObject("do_not_exist")).isNull();
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }

    @Test
    public void testKeys() throws IOException {
        File properties = new File("target/test-classes/utf-8.properties");
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(properties);
            UTF8PropertiesResourceBundle bundle = new UTF8PropertiesResourceBundle(fis);
            assertThat(bundle.keySet()).containsOnly(
                    "japanese",
                    "russian",
                    "polish",
                    "german",
                    "unit"
            );
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }
}
