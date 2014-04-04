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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.content.JacksonModuleRepository;
import org.wisdom.api.content.Json;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A component managing Json.
 * <p/>
 * This class manages JSON module dynamically, and recreates a JSON Mapper every time a module arrives or leaves.
 */
@Component(immediate = true)
@Provides
@Instantiate
public class JsonSingleton implements JacksonModuleRepository, Json {

    /**
     * An object used as lock.
     */
    private final Object lock = new Object();

    /**
     * The current object mapper.
     */
    private ObjectMapper mapper;

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonSingleton.class);

    /**
     * The current list of registered modules.
     */
    private List<Module> modules = new ArrayList<>();

    /**
     * Gets the current mapper.
     *
     * @return the mapper.
     */
    public ObjectMapper mapper() {
        synchronized (lock) {
            return mapper;
        }
    }

    /**
     * Converts an object to JsonNode.
     *
     * @param data Value to convert in Json.
     * @return the resulting JSON Node
     * @throws java.lang.RuntimeException if the JSON Node cannot be created
     */
    public JsonNode toJson(final Object data) {
        synchronized (lock) {
            try {
                return mapper.valueToTree(data);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Converts a JsonNode to a Java value.
     *
     * @param json  Json value to convert.
     * @param clazz Expected Java value type.
     * @return the created object
     * @throws java.lang.RuntimeException if the object cannot be created
     */
    public <A> A fromJson(JsonNode json, Class<A> clazz) {
        synchronized (lock) {
            try {
                return mapper.treeToValue(json, clazz);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Converts a Json String to a Java value.
     *
     * @param json  Json string to convert.
     * @param clazz Expected Java value type.
     * @return the created object
     * @throws java.lang.RuntimeException if the object cannot be created
     */
    public <A> A fromJson(String json, Class<A> clazz) {
        synchronized (lock) {
            try {
                JsonNode node = mapper.readTree(json);
                return mapper.treeToValue(node, clazz);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Converts a JsonNode to its string representation.
     * This implementation use a `pretty printer`.
     *
     * @param json the json node
     * @return the String representation of the given Json Object
     * @throws java.lang.RuntimeException if the String form cannot be created
     */
    public String stringify(JsonNode json) {
        try {
            return mapper().writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot stringify the input json node", e);
        }
    }

    /**
     * Parses a String representing a json, and return it as a JsonNode.
     *
     * @param src the JSON String
     * @return the Json Node
     * @throws java.lang.RuntimeException if the given string is not a valid JSON String
     */
    public JsonNode parse(String src) {
        synchronized (lock) {
            try {
                return mapper.readValue(src, JsonNode.class);
            } catch (Exception t) {
                throw new RuntimeException(t);
            }
        }
    }

    /**
     * Parses a stream representing a json, and return it as a JsonNode.
     * The stream is <strong>not</strong> closed by the method.
     *
     * @param stream the JSON stream
     * @return the JSON node
     * @throws java.lang.RuntimeException if the given stream is not a valid JSON String
     */
    public JsonNode parse(InputStream stream) {
        synchronized (lock) {
            try {
                return mapper.readValue(stream, JsonNode.class);
            } catch (Exception t) {
                throw new RuntimeException(t);
            }
        }
    }

    /**
     * Creates a new JSON Object.
     *
     * @return the new Object Node.
     */
    @Override
    public ObjectNode newObject() {
        return mapper().createObjectNode();
    }

    /**
     * Creates a new JSON Array.
     *
     * @return the new Array Node.
     */
    @Override
    public ArrayNode newArray() {
        return mapper().createArrayNode();
    }

    /**
     * Starts the JSON support.
     * An empty mapper is created.
     */
    @Validate
    public void validate() {
        LOGGER.info("Starting JSON support service");
        setMapper(new ObjectMapper());
    }

    /**
     * Sets the object mapper.
     *
     * @param mapper the object mapper to use
     */
    private void setMapper(ObjectMapper mapper) {
        synchronized (lock) {
            this.mapper = mapper;
            if (mapper != null) {
                this.mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            }
        }
    }

    /**
     * Stops the JSON management.
     * Releases the current mapper.
     */
    @Invalidate
    public void invalidate() {
        setMapper(null);
    }

    /**
     * Registers a new JSON Module.
     *
     * @param module the module to register
     */
    @Override
    public void register(Module module) {
        LOGGER.info("Adding JSON module " + module.getModuleName());
        synchronized (lock) {
            modules.add(module);
            rebuildMapper();
        }
    }

    private void rebuildMapper() {
        mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        for (Module module : modules) {
            mapper.registerModule(module);
        }
    }

    /**
     * Un-registers a JSON Module.
     *
     * @param module the module
     */
    @Override
    public void unregister(Module module) {
        LOGGER.info("Removing JSON module " + module.getModuleName());
        synchronized (lock) {
            if (modules.contains(module)) {
                rebuildMapper();
            }
        }
    }

}
