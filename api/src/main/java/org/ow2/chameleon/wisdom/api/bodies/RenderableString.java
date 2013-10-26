package org.ow2.chameleon.wisdom.api.bodies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.IOUtils;
import org.ow2.chameleon.wisdom.api.http.Context;
import org.ow2.chameleon.wisdom.api.http.Renderable;
import org.ow2.chameleon.wisdom.api.http.Result;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * A renderable object taking a String as parameter.
 */
public class RenderableString implements Renderable {

    //TODO Support encoding

    private final String rendered;

    public RenderableString(String object) {
        rendered = object;
    }

    public RenderableString(StringBuilder object) {
        rendered = object.toString();
    }

    public RenderableString(StringBuffer object) {
        rendered = object.toString();
    }

    public RenderableString(Object object) {
        rendered = object.toString();
    }

    @Override
    public InputStream render(Context context, Result result) throws Exception {
        return new ByteArrayInputStream(rendered.getBytes());
    }

    @Override
    public long length() {
        return rendered.length();
    }
}
