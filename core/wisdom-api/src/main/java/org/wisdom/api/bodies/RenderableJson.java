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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.LoggerFactory;
import org.wisdom.api.http.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * A renderable object taking an ObjectNode as parameter.
 */
public class RenderableJson implements Renderable<ObjectNode> {

    private static ObjectWriter OBJECT_WRITER = new ObjectMapper().writer().withDefaultPrettyPrinter();

    private final ObjectNode node;
    private byte[] rendered;

    public RenderableJson(ObjectNode node) {
        this.node = node;
    }

    @Override
    public InputStream render(Context context, Result result) throws RenderableException {
        if (rendered == null) {
            _render();
        }
        return new ByteArrayInputStream(rendered);
    }

    private void _render() throws RenderableException {
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
                _render();
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
    public ObjectNode content() {
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
