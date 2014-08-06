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


public class ConstructorBasedConverterTest {


    @Test
    public void testGetIfEligible() throws Exception {
       assertThat(ConstructorBasedConverter.getIfEligible(Person.class)).isNotNull();
       assertThat(ConstructorBasedConverter.getIfEligible(Object.class)).isNull();
    }

    @Test
    public void testFromString() throws Exception {
       assertThat(ConstructorBasedConverter.getIfEligible(Person.class).fromString("wisdom").name).isEqualTo("wisdom");
    }

    @Test
    public void testGetType() throws Exception {
        assertThat(ConstructorBasedConverter.getIfEligible(Person.class).getType()).isEqualTo(Person.class);
    }
}