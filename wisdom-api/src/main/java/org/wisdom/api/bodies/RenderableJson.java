package org.wisdom.api.bodies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.LoggerFactory;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Renderable;
import org.wisdom.api.http.Result;

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
    public InputStream render(Context context, Result result) throws Exception {
        if (rendered == null) {
            _render();
        }
        return new ByteArrayInputStream(rendered);
    }

    private void _render() throws JsonProcessingException {
        rendered = OBJECT_WRITER.writeValueAsBytes(node);
    }

    @Override
    public long length() {
        if (rendered == null) {
            try {
                _render();
            } catch (JsonProcessingException e) {  //NOSONAR
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
