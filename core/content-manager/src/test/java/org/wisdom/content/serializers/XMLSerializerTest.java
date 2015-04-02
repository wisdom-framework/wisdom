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
package org.wisdom.content.serializers;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.wisdom.api.bodies.RenderableObject;
import org.wisdom.api.http.RenderableException;
import org.wisdom.content.jackson.JacksonSingleton;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks XML Serializer.
 */
public class XMLSerializerTest {

    XMLSerializer serializer = new XMLSerializer();


    @Before
    public void setUp() {
        final JacksonSingleton singleton = new JacksonSingleton();
        serializer.xml = singleton;
        singleton.validate();
    }

    @Test
    public void testSerialization() throws RenderableException, IOException {
        Data data = new Data();
        data.name = "wisdom";
        data.age = 2;
        RenderableObject object = new RenderableObject(data);
        serializer.serialize(object);
        String result = IOUtils.toString(object.render(null, null));
        assertThat(result)
                .contains("<Data")
                .contains("</Data>")
                .contains("<name>wisdom</name>")
                .contains("<age>2</age>");
    }

    @Test
    public void testWithNull() throws RenderableException, IOException {
        RenderableObject object = new RenderableObject(null);
        serializer.serialize(object);
        String result = IOUtils.toString(object.render(null, null));
        assertThat(result).isEmpty();
    }

    private class Data {
        String name;
        int age;

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}