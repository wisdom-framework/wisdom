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
 */
@Component(immediate = true)
@Provides
@Instantiate
public class JsonSingleton implements JacksonModuleRepository, Json {

    private final Object lock = new Object();
    private ObjectMapper mapper;
    private static Logger LOGGER = LoggerFactory.getLogger(JsonSingleton.class);

    private List<Module> modules = new ArrayList<>();

    public ObjectMapper mapper() {
        synchronized (lock) {
            return mapper;
        }
    }

    /**
     * Convert an object to JsonNode.
     *
     * @param data Value to convert in Json.
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
     * Convert a JsonNode to a Java value
     *
     * @param json  Json value to convert.
     * @param clazz Expected Java value type.
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
     * Convert a Json String to a Java value
     *
     * @param json  Json string to convert.
     * @param clazz Expected Java value type.
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
     * Convert a JsonNode to its string representation.
     */
    public String stringify(JsonNode json) {
        try {
            return mapper().writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot stringify the input json node", e);
        }
    }

    /**
     * Parse a String representing a json, and return it as a JsonNode.
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
     * Parse a stream representing a json, and return it as a JsonNode.
     * The stream is <strong>not</strong> closed by the method.
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

    @Override
    public ObjectNode newObject() {
        return mapper().createObjectNode();
    }

    @Override
    public ArrayNode newArray() {
        return mapper().createArrayNode();
    }

    @Validate
    public void validate() {
        LOGGER.info("Starting JSON support service");
        setMapper(new ObjectMapper());
    }

    /**
     * Sets the object mapper.
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

    @Invalidate
    public void invalidate() {
        setMapper(null);
    }

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
