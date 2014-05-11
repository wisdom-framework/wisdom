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
package org.wisdom.content.converters;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class CharacterConverterTest {

    CharacterConverter converter = CharacterConverter.INSTANCE;

    @Test
    public void testFromString() throws Exception {
        assertThat(converter.fromString("a")).isEqualTo('a');
    }

    @Test(expected = NullPointerException.class)
    public void testWithNull() throws Exception {
        converter.fromString(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithEmptyString() throws Exception {
        converter.fromString("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithLongString() throws Exception {
        converter.fromString("ab");
    }

    @Test
    public void testGetType() throws Exception {
        assertThat(converter.getType()).isEqualTo(Character.class);
    }
}