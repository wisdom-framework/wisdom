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
import com.google.common.base.Charsets;
import org.slf4j.LoggerFactory;
import org.wisdom.api.http.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * A renderable object providing a JSONP response.
 * The JSONP renders a JavaScript call of a JSON object.<br>
 * Example of use, provided the following action method:
 * <pre>
 *   public Result myService(String callback) {
 *     JsonNode json = ...
 *     return ok(callback, json);
 *   }
 * </pre>
 * And the following request:
 * <pre>
 *   GET  /my-service?callback=foo
 * </pre>
 * The response will have content type "text/javascript" and will look like the following:
 * <pre>
 *   foo({...});
 * </pre>
 */
public class RenderableJsonP implements Renderable<String> {

    private static final ObjectWriter OBJECT_WRITER = new ObjectMapper().writer().withDefaultPrettyPrinter();

    private final JsonNode node;
    private final String padding;
    private byte[] rendered;

    /**
     * Creates a new {@link RenderableJsonP} instance.
     *
     * @param padding the padding
     * @param node    the json node
     */
    public RenderableJsonP(String padding, JsonNode node) {
        this.padding = padding;
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
            rendered = (padding + "(" + OBJECT_WRITER.writeValueAsString(node) + ");").getBytes(Charsets.UTF_8);
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
                LoggerFactory.getLogger(RenderableJsonP.class).warn("Cannot render JSON object {}", node, e);
                return -1;
            }
        }
        return rendered.length;
    }

    @Override
    public String mimetype() {
        return MimeTypes.JAVASCRIPT;
    }

    @Override
    public String content() {
        try {
            return padding + "(" + OBJECT_WRITER.writeValueAsString(node) + ")";
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot render jsonp content", e);
        }
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
