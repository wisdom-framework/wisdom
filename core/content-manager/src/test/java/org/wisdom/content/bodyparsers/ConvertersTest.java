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
package org.wisdom.content.bodyparsers;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConvertersTest {

    @Test
    public void testConvert() throws Exception {
        String aString = "aString";
        String str = "1024";
        String doubleStr = "1024.24";
        String boolStr = "true";
        String byteStr = "89";
        char charStr = 'x';
        String emptyString = "";

        assertEquals(Integer.valueOf(str), Converters.convert(str, int.class));
        assertEquals(Integer.valueOf(str), Converters.convert(str, Integer.class));
        assertEquals(Long.valueOf(str), Converters.convert(str, long.class));
        assertEquals(Float.valueOf(doubleStr), Converters.convert(doubleStr, float.class));
        assertEquals(Float.valueOf(doubleStr), Converters.convert(doubleStr, Float.class));
        assertEquals(Double.valueOf(doubleStr), Converters.convert(doubleStr, double.class));
        assertEquals(Double.valueOf(doubleStr), Converters.convert(doubleStr, Double.class));
        assertEquals(Boolean.valueOf(boolStr), Converters.convert(boolStr, boolean.class));
        assertEquals(Boolean.valueOf(boolStr), Converters.convert(boolStr, Boolean.class));
        assertEquals(Byte.valueOf(byteStr), Converters.convert(byteStr, byte.class));
        assertEquals(Byte.valueOf(byteStr), Converters.convert(byteStr, Byte.class));
        assertEquals(Character.valueOf(charStr), Converters.convert(String.valueOf(charStr), char.class));
        assertEquals(Character.valueOf(charStr), Converters.convert(String.valueOf(charStr), Character.class));
        assertEquals(null, Converters.convert(String.valueOf(emptyString), char.class));
        assertEquals(null, Converters.convert(String.valueOf(emptyString), Character.class));

        assertEquals(null, Converters.convert(String.valueOf(aString), StringBuffer.class));
        assertEquals(null, Converters.convert(String.valueOf(aString), Integer.class));

        assertEquals(aString, Converters.convert(String.valueOf(aString), String.class));
    }
}