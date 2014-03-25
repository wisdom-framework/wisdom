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

