package org.ow2.chameleon.wisdom.api.bodies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import org.apache.commons.io.IOUtils;
import org.ow2.chameleon.wisdom.api.http.Context;
import org.ow2.chameleon.wisdom.api.http.MimeTypes;
import org.ow2.chameleon.wisdom.api.http.Renderable;
import org.ow2.chameleon.wisdom.api.http.Result;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * A renderable object taking a String as parameter.
 */
public class RenderableString implements Renderable<String> {

    //TODO Support encoding

    private final String rendered;
    private final String type;

    public RenderableString(String object) {
        this(object, null);
    }

    public RenderableString(StringBuilder object) {
        this(object.toString(), null);
    }

    public RenderableString(StringBuffer object) {
        this(object.toString(), null);
    }

    public RenderableString(Object object) {
        this(object.toString(), null);
    }

    public RenderableString(Object object, String type) {
        this(object.toString(), type);
    }

    public RenderableString(StringWriter object) {
        this(object.toString(), null);
    }

    public RenderableString(String object, String type) {
        rendered = object;
        this.type = type;
    }

    @Override
    public InputStream render(Context context, Result result) throws Exception {
        return new ByteArrayInputStream(rendered.getBytes());
    }

    @Override
    public long length() {
        return rendered.length();
    }

    @Override
    public String mimetype() {
        if (type == null) {
            return MimeTypes.HTML;
        } else {
            return type;
        }
    }

    @Override
    public String content() {
        return rendered;
    }

    @Override
    public boolean requireSerializer() {
        return false;
    }

    @Override
    public void setSerializedForm(String serialized) {
        // Nothing because serialization is not supported for this renderable class.
    }

    public final static List<String> CAN_BE_HANDLED_AS_STRING = ImmutableList.of(
            MimeTypes.CSS,
            MimeTypes.HTML,
            MimeTypes.JAVASCRIPT,
            MimeTypes.TEXT
    );

    public static boolean canBeHandledAsString(String contentType) {
        return CAN_BE_HANDLED_AS_STRING.contains(contentType);
    }
}
