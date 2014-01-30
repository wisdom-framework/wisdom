package org.wisdom.wamp.messages;

import com.fasterxml.jackson.databind.JsonNode;
import org.wisdom.api.content.Json;

import java.util.List;

/**
 * Common logic shared by all WAMP messages.
 */
public abstract class Message {

    public abstract MessageType getType();

    public abstract List<Object> toList();

    public String toString() {
        return this.toList().toString();
    }

    public JsonNode toJson(Json mapper) {
        return mapper.toJson(toList());
    }
}

