package org.ow2.chameleon.wisdom.api.bodies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.ow2.chameleon.wisdom.api.http.Context;
import org.ow2.chameleon.wisdom.api.http.MimeTypes;
import org.ow2.chameleon.wisdom.api.http.Renderable;
import org.ow2.chameleon.wisdom.api.http.Result;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * A renderable object taking an ObjectNode as parameter.
 */
public class RenderableJson implements Renderable<ObjectNode> {

    public static ObjectMapper mapper = new ObjectMapper();
    private static ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
    private final ObjectNode node;
    private byte[] rendered;

    public RenderableJson(ObjectNode node) {
        this.node = node;
    }

    @Override
    public InputStream render(Context context, Result result) throws Exception {
        if (result.getContentType() == null) {
            result.as(MimeTypes.JSON);
        }
        if (rendered == null) {
            _render();
        }
        return new ByteArrayInputStream(rendered);
    }

    private void _render() throws JsonProcessingException {
        rendered = ow.writeValueAsBytes(node);
    }

    @Override
    public long length() {
        if (rendered == null) {
            try {
                _render();
            } catch (JsonProcessingException e) {
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
