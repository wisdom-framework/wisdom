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
package org.wisdom.test.parents;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Results;
import org.wisdom.api.http.Status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

/**
 * Check the basic behavior of the Wisdom Test class.
 */
public class WisdomTestTest {

    WisdomTest test = new WisdomTest();

    @Test
    public void testStatus() throws Exception {
        Action.ActionResult result = new Action.ActionResult(Results.ok(), null);
        assertThat(test.status(result)).isEqualTo(Status.OK);

        result = new Action.ActionResult(Results.notFound(), null);
        assertThat(test.status(result)).isEqualTo(Status.NOT_FOUND);

        result = new Action.ActionResult(Results.forbidden(), null);
        assertThat(test.status(result)).isEqualTo(Status.FORBIDDEN);
    }

    @Test
    public void testContentType() throws Exception {
        Action.ActionResult result = new Action.ActionResult(Results.ok(), null);
        // Not set.
        assertThat(test.contentType(result)).isNull();

        result = new Action.ActionResult(Results.ok().html(), null);
        assertThat(test.contentType(result)).isEqualTo(MimeTypes.HTML);

        result = new Action.ActionResult(Results.ok().json(), null);
        assertThat(test.contentType(result)).isEqualTo(MimeTypes.JSON);

        result = new Action.ActionResult(Results.ok().as("acme/acme"), null);
        assertThat(test.contentType(result)).isEqualTo("acme/acme");
    }

    @Test
    public void testJson() throws Exception {
        // Regular Json node
        final String json = "{\"a\":\"1\",\"property\":\"default value\"," +
                "\"bundle\":\"org.acme.demo.solution\"}";
        ObjectNode node = WisdomTest.MAPPER.readValue(json, ObjectNode.class);
        Action.ActionResult result = new Action.ActionResult(Results.ok(node), null);
        assertThat(test.json(result).toString()).isEqualTo(json);

        Map.Entry<String, Integer> entry = new HashMap.SimpleEntry<>("key", 1);
        result = new Action.ActionResult(Results.ok(entry).json(), null);
        assertThat(test.json(result).get("key").asInt()).isEqualTo(1);

        try {
            result = new Action.ActionResult(Results.ok("<h1>Hello</h1>"), null);
            test.json(result);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertThat(e.getCause()).isInstanceOf(ClassCastException.class);
        }
    }

    @Test
    public void testJsonarray() throws Exception {
        // Regular Json node
        String json = "[\"1\",\"2\"]";
        ArrayNode node = WisdomTest.MAPPER.readValue(json, ArrayNode.class);
        Action.ActionResult result = new Action.ActionResult(Results.ok(node), null);
        assertThat(test.jsonarray(result).size()).isEqualTo(2);
        assertThat(test.jsonarray(result).get(0).asInt()).isEqualTo(1);

        List<String> list = ImmutableList.of("a", "b");
        result = new Action.ActionResult(Results.ok(list).json(), null);
        assertThat(test.jsonarray(result).size()).isEqualTo(2);
        assertThat(test.jsonarray(result).get(0).asText()).isEqualTo("a");

        try {
            result = new Action.ActionResult(Results.ok("<h1>Hello</h1>"), null);
            test.jsonarray(result);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertThat(e.getCause()).isInstanceOf(ClassCastException.class);
        }

        try {
            json = "{\"a\":\"1\",\"property\":\"default value\"," +
                    "\"bundle\":\"org.acme.demo.solution\"}";
            result = new Action.ActionResult(Results.ok(json), null);
            test.jsonarray(result);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            assertThat(e.getCause()).isInstanceOf(ClassCastException.class);
        }
    }

    @Test
    public void testToString() throws Exception {
        Action.ActionResult result = new Action.ActionResult(Results.ok("<h1>Hello</h1>"), null);
        assertThat(test.toString(result)).isEqualTo("<h1>Hello</h1>");

        String json = "{\"a\":\"1\",\"property\":\"default value\"," +
                "\"bundle\":\"org.acme.demo.solution\"}";
        ObjectNode node = WisdomTest.MAPPER.readValue(json, ObjectNode.class);
        result = new Action.ActionResult(Results.ok(node), null);
        assertThat(test.toString(result)).isEqualTo(json);
    }

    @Test
    public void testToBytes() throws Exception {
        Action.ActionResult result = new Action.ActionResult(Results.ok("<h1>Hello</h1>"), null);
        assertThat(test.toBytes(result)).isNotEmpty();

        String json = "{\"a\":\"1\",\"property\":\"default value\"," +
                "\"bundle\":\"org.acme.demo.solution\"}";
        ObjectNode node = WisdomTest.MAPPER.readValue(json, ObjectNode.class);
        result = new Action.ActionResult(Results.ok(node), null);
        assertThat(test.toBytes(result)).isNotEmpty();
    }
}
