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
