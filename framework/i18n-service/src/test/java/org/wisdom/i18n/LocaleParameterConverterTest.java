/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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

import org.junit.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;


public class LocaleParameterConverterTest {

    LocaleParameterConverter converter = new LocaleParameterConverter();

    @Test
    public void testFromString() throws Exception {
        Locale locale = converter.fromString("fr");
        assertThat(locale).isEqualTo(Locale.FRENCH);

        locale = converter.fromString("fr-FR");
        assertThat(locale).isEqualTo(Locale.FRANCE);

        locale = converter.fromString("fr_CA");
        assertThat(locale).isEqualTo(Locale.CANADA_FRENCH);

        locale = converter.fromString("");
        assertThat(locale).isEqualTo(new Locale("", "",""));

        locale = converter.fromString(null);
        assertThat(locale).isEqualTo(new Locale("", "",""));
    }

    @Test
    public void testGetType() throws Exception {
        assertThat(converter.getType()).isEqualTo(Locale.class);
    }
}