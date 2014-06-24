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


public class BooleanConverterTest {

    private BooleanConverter converter = BooleanConverter.INSTANCE;

    @Test
    public void testYesNo() throws Exception {
        assertThat(converter.fromString("yes")).isTrue();
        assertThat(converter.fromString("YeS")).isTrue();
        assertThat(converter.fromString("no")).isFalse();
        assertThat(converter.fromString("nO")).isFalse();
    }

    @Test
    public void testOnOff() throws Exception {
        assertThat(converter.fromString("on")).isTrue();
        assertThat(converter.fromString("ON")).isTrue();
        assertThat(converter.fromString("off")).isFalse();
        assertThat(converter.fromString("oFf")).isFalse();
    }

    @Test
    public void testTrueFalse() throws Exception {
        assertThat(converter.fromString("true")).isTrue();
        assertThat(converter.fromString("TruE")).isTrue();
        assertThat(converter.fromString("fALse")).isFalse();
        assertThat(converter.fromString("false")).isFalse();
    }

    @Test
    public void testNumbers() throws Exception {
        assertThat(converter.fromString("1")).isTrue();
        assertThat(converter.fromString("2")).isFalse();
        assertThat(converter.fromString("0")).isFalse();
    }

    @Test
    public void testWithNullAndEmptyString() throws Exception {
        assertThat(converter.fromString(null)).isFalse();
        assertThat(converter.fromString("")).isFalse();
    }

    @Test
    public void testWithRandomString() throws Exception {
        assertThat(converter.fromString("aaaa")).isFalse();
        assertThat(converter.fromString("welcome true")).isFalse();
        assertThat(converter.fromString("true welcome")).isFalse();
    }

    @Test
    public void testGetType() throws Exception {
        assertThat(converter.getType()).isEqualTo(Boolean.class);
    }
}