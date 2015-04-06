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
package org.wisdom.content.serializers;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.content.ContentSerializer;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Renderable;

/**
 * Renders JSON content.
 */
@Component
@Instantiate
@Provides
public class JSONSerializer implements ContentSerializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSONSerializer.class);

    @Requires
    private Json json;

    @Override
    public String getContentType() {
        return MimeTypes.JSON;
    }

    @Override
    public void serialize(Renderable<?> renderable) {
        JsonNode node = json.toJson(renderable.content());
        if (node == null) {
            LOGGER.error("Cannot serialize result - cannot create a JSON Node from the response content");
            renderable.setSerializedForm("");
        } else {
            renderable.setSerializedForm(node.toString());
        }
    }
}
