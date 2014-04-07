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
package org.wisdom.content.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wisdom.api.content.Json;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the Json Service implementation.
 */
public class JsonSingletonTest {

    JsonSingleton json = new JsonSingleton();

    @Before
    public void setUp() {
        json.validate();
    }

    @After
    public void tearDown() {
        json.invalidate();
    }

    @Test
    public void testMapper() throws Exception {
        assertThat(json.mapper()).isNotNull();
    }

    @Test
    public void testToJson() throws Exception {
        String message = "a message";
        JsonNode node = json.toJson(message);
        assertThat(node.asText()).isEqualTo(message);

        String[] array = new String[] { "a", "b", "c"};
        node = json.toJson(array);
        assertThat(node.isArray());
        assertThat(node.get(1).asText()).isEqualTo("b");

        node = json.toJson(Arrays.asList(array));
        assertThat(node.isArray());
        assertThat(node.get(1).asText()).isEqualTo("b");

        Data data = new Data();
        data.age = 32;
        data.name = "clement";
        data.messages = Arrays.asList(array);
        node = json.toJson(data);
        assertThat(node.isObject());
        assertThat(node.get("age").asInt()).isEqualTo(data.age);
        assertThat(node.get("name").asText()).isEqualTo(data.name);
        assertThat(node.get("messages").isArray());
        assertThat(node.get("messages").get(1).asText()).isEqualTo(data.messages.get(1));
    }

    @Test
    public void testFromJson() throws Exception {
        String test = "{\"age\":32,\"messages\":[\"msg 1\",\"msg 2\",\"msg 3\"],\"name\":\"clement\"}";
        Data data = json.fromJson(test, Data.class);
        assertThat(data.age).isEqualTo(32);
        assertThat(data.name).isEqualTo("clement");
        assertThat(data.messages.get(1)).isEqualTo("msg 2");
    }

    @Test
    public void testSerializationAndDeserialization() throws Exception {
        Data data = new Data();
        data.age = 32;
        data.name = "clement";
        data.messages = Arrays.asList("msg 1", "msg 2", "msg 3");

        JsonNode node =  json.toJson(data);
        Data data2 = json.fromJson(node, data.getClass());

        assertThat(data2.age).isEqualTo(data.age);
        assertThat(data2.name).isEqualTo(data.name);
        assertThat(data2.messages.get(1)).isEqualTo(data.messages.get(1));
    }

    @Test
    public void testStringify() throws Exception {
        Data data = new Data();
        data.age = 32;
        data.name = "clement";
        data.messages = Arrays.asList("msg 1", "msg 2", "msg 3");

        JsonNode node =  json.toJson(data);

        final String stringified = json.stringify(node);
        assertThat(stringified).contains("\"age\"");
        assertThat(stringified).contains("32,");
        assertThat(stringified).contains("clement");
    }

    @Test
    public void testParse() throws Exception {
        String test = "{\"age\":32,\"messages\":[\"msg 1\",\"msg 2\",\"msg 3\"],\"name\":\"clement\"}";
        JsonNode node = json.parse(test);
        assertThat(node.isObject());
        assertThat(node.get("age").asInt()).isEqualTo(32);
        assertThat(node.get("name").asText()).isEqualTo("clement");
        assertThat(node.get("messages").isArray());
        assertThat(node.get("messages").get(1).asText()).isEqualTo("msg 2");
    }

    @Test
    public void testParseStream() throws Exception {
        String test = "{\"age\":32,\"messages\":[\"msg 1\",\"msg 2\",\"msg 3\"],\"name\":\"clement\"}";
        InputStream stream = IOUtils.toInputStream(test);
        JsonNode node = json.parse(stream);
        assertThat(node.isObject());
        assertThat(node.get("age").asInt()).isEqualTo(32);
        assertThat(node.get("name").asText()).isEqualTo("clement");
        assertThat(node.get("messages").isArray());
        assertThat(node.get("messages").get(1).asText()).isEqualTo("msg 2");
    }

    @Test
    public void testNewObject() throws Exception {
        assertThat(json.newObject()).isNotNull();
    }

    @Test
    public void testNewArray() throws Exception {
        assertThat(json.newArray()).isNotNull();
    }
}
