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
package org.wisdom.wamp.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.wisdom.api.content.Json;

import java.io.IOException;
import java.io.InputStream;

/**
 * An implementation of the Json Service instantiable to be usable is tests.
 */
public class JsonService implements Json {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Gets the current mapper.
     *
     * @return the mapper
     */
    @Override
    public ObjectMapper mapper() {
        return MAPPER;
    }

    /**
     * Maps the given object to a JsonNode.
     * In addition to the default Jackson transformation, serializer dynamically added to the Json support are used.
     *
     * @param data the data to transform to json
     * @return the resulting json node
     */
    @Override
    public JsonNode toJson(Object data) {
        return MAPPER.valueToTree(data);
    }

    /**
     * Builds a new instance of the given class <em>clazz</em> from the given Json object.
     *
     * @param json  the json node
     * @param clazz the class of the instance to construct
     * @return an instance of the class.
     */
    @Override
    public <A> A fromJson(JsonNode json, Class<A> clazz) {
        try {
            return MAPPER.treeToValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Builds a new instance of the given class <em>clazz</em> from the given Json string.
     *
     * @param json  the json string
     * @param clazz the class of the instance to construct
     * @return an instance of the class.
     */
    @Override
    public <A> A fromJson(String json, Class<A> clazz) {
        try {
            JsonNode node = MAPPER.readTree(json);
            return MAPPER.treeToValue(node, clazz);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @param json
     * @return
     */
    @Override
    public String stringify(JsonNode json) {
        try {
            return mapper().writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public JsonNode parse(String src) {
        try {
            return MAPPER.readValue(src, JsonNode.class);
        } catch (Exception t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public JsonNode parse(InputStream stream) {
        try {
            return MAPPER.readValue(stream, JsonNode.class);
        } catch (Exception t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public ObjectNode newObject() {
        return MAPPER.createObjectNode();
    }

    @Override
    public ArrayNode newArray() {
        return MAPPER.createArrayNode();
    }
}
