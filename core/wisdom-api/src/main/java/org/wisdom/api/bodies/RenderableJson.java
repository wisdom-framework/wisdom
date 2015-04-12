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
package org.wisdom.api.bodies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.LoggerFactory;
import org.wisdom.api.http.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * A renderable object taking an ObjectNode as parameter.
 */
public class RenderableJson implements Renderable<JsonNode> {

    private static ObjectWriter OBJECT_WRITER = new ObjectMapper().writer().withDefaultPrettyPrinter();

    private final JsonNode node;
    private byte[] rendered;

    /**
     * Creates a {@link RenderableJson} from the given json node.
     *
     * @param node the json node
     */
    public RenderableJson(JsonNode node) {
        this.node = node;
    }

    @Override
    public InputStream render(Context context, Result result) throws RenderableException {
        if (rendered == null) {
            render();
        }
        return new ByteArrayInputStream(rendered);
    }

    /**
     * Renders the JSON object as a byte array. Be aware that this method does not use the {@link org.wisdom.api
     * .content.Json} service.
     *
     * @throws RenderableException if the node cannot be rendered
     */
    private void render() throws RenderableException {
        try {
            rendered = OBJECT_WRITER.writeValueAsBytes(node);
        } catch (JsonProcessingException e) {
            throw new RenderableException("cannot write the JSON form of " + node, e);
        }
    }

    @Override
    public long length() {
        if (rendered == null) {
            try {
                render();
            } catch (RenderableException e) {  //NOSONAR
                LoggerFactory.getLogger(RenderableJson.class).warn("Cannot render JSON object {}", node, e);
                return -1;
            }
        }
        return rendered.length;
    }

    @Override
    public String mimetype() {
        return MimeTypes.JSON;
    }

    @Override
    public JsonNode content() {
        return node;
    }

    @Override
    public boolean requireSerializer() {
        return false;
    }

    @Override
    public void setSerializedForm(String serialized) {
        // Nothing because serialization is not supported for this renderable class.
    }

    @Override
    public boolean mustBeChunked() {
        return false;
    }

}
