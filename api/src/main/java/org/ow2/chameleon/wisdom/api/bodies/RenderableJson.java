package org.ow2.chameleon.wisdom.api.bodies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import org.ow2.chameleon.wisdom.api.http.Context;
import org.ow2.chameleon.wisdom.api.http.MimeTypes;
import org.ow2.chameleon.wisdom.api.http.Renderable;
import org.ow2.chameleon.wisdom.api.http.Result;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * A renderable object taking an ObjectNode as parameter.
 */
public class RenderableJson implements Renderable {

    public static ObjectMapper mapper = new ObjectMapper();

    // TODO Can this be a singleton (is it thread safe ?)
    private ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();

    private final String rendered;

    public RenderableJson(ObjectNode node) {
        String temp;
        try {
            temp = ow.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            temp =null;
        }
        rendered = temp;
    }

    public RenderableJson(Object object) {
        String temp;
        try {
            temp = ow.writeValueAsString(object);
        } catch (JsonProcessingException e1) {
            e1.printStackTrace();
            temp = null;
        }
        rendered = temp;
    }

    @Override
    public InputStream render(Context context, Result result) throws Exception {
        return new ByteArrayInputStream(rendered.getBytes(Charsets.UTF_8));
    }

    @Override
    public long length() {
        return rendered.length();
    }

    @Override
    public String mimetype() {
        return MimeTypes.JSON;
    }

}
