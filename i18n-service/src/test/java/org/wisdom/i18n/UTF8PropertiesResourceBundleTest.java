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
package org.wisdom.i18n;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
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
