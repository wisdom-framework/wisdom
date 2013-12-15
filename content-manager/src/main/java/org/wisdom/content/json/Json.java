package org.wisdom.content.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.felix.ipojo.annotations.*;
import org.wisdom.api.content.JacksonModuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A component managing Json.
 */
@Component(immediate = true)
@Provides
@Instantiate
public class Json implements JacksonModuleRepository {

    private static final Object LOCK = new Object();
    private static ObjectMapper MAPPER;
    private static Logger LOGGER = LoggerFactory.getLogger(Json.class);

    private List<Module> modules = new ArrayList<>();

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    /**
     * Convert an object to JsonNode.
     *
     * @param data Value to convert in Json.
     */
    public static JsonNode toJson(final Object data) {
        synchronized (LOCK) {
            try {
                return MAPPER.valueToTree(data);
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
    public static <A> A fromJson(JsonNode json, Class<A> clazz) {
        synchronized (LOCK) {
            try {
                return MAPPER.treeToValue(json, clazz);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Convert a JsonNode to its string representation.
     */
    public static String stringify(JsonNode json) {
        return json.toString();
    }

    /**
     * Parse a String representing a json, and return it as a JsonNode.
     */
    public static JsonNode parse(String src) {
        synchronized (LOCK) {
            try {
                return MAPPER.readValue(src, JsonNode.class);
            } catch (Exception t) {
                throw new RuntimeException(t);
            }
        }
    }

    @Validate
    public void validate() {
        LOGGER.info("Starting JSON management");
        MAPPER = new ObjectMapper();
    }

    @Invalidate
    public void invalidate() {
        MAPPER = null;
    }

    @Override
    public void register(Module module) {
        LOGGER.info("Adding JSON module " + module.getModuleName());
        synchronized (LOCK) {
            modules.add(module);
            rebuildMapper();
        }
    }

    private void rebuildMapper() {
        MAPPER = new ObjectMapper();
        for (Module module : modules) {
            MAPPER.registerModule(module);
        }
    }

    @Override
    public void unregister(Module module) {
        LOGGER.info("Removing JSON module " + module.getModuleName());
        synchronized (LOCK) {
            if (modules.contains(module)) {
                rebuildMapper();
            }
        }
    }

}
