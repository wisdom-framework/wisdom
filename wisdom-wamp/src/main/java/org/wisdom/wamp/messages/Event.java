package org.wisdom.wamp.messages;

import com.fasterxml.jackson.databind.JsonNode;
import org.wisdom.wamp.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * The WAMP EVENT message.
 * http://wamp.ws/spec/#event_message
 */
public class Event extends Message {

    private final String topic;
    private final JsonNode payload;

    public Event( String topic, JsonNode payload) {
        this.topic = topic;
        this.payload = payload;
    }

    @Override
    public MessageType getType() {
        return MessageType.EVENT;
    }

    @Override
    public List<Object> toList() {
        List<Object> res = new ArrayList<>();
        res.add(getType().code());
        res.add(topic);
        res.add(payload);
        return res;
    }

}
