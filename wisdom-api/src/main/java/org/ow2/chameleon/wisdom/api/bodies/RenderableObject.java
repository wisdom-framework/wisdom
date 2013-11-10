package org.ow2.chameleon.wisdom.api.bodies;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import org.ow2.chameleon.wisdom.api.http.Context;
import org.ow2.chameleon.wisdom.api.http.MimeTypes;
import org.ow2.chameleon.wisdom.api.http.Renderable;
import org.ow2.chameleon.wisdom.api.http.Result;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

/**
 * Render any object, but it requires serialization.
 */
public class RenderableObject implements Renderable<Object> {

    private final Object object;
    private String serialized;

    public RenderableObject(Object o) {
        this.object = o;
    }

    @Override
    public InputStream render(Context context, Result result) throws Exception {
        if (serialized == null) {
            throw new Exception("Serialization required before rendering");
        }
        return new ByteArrayInputStream(serialized.getBytes(Charsets.UTF_8));
    }

    @Override
    public void setSerializedForm(String serialized) {
        this.serialized = serialized;
    }

    @Override
    public long length() {
        return -1; // Unknown
    }

    @Override
    public String mimetype() {
        return null; // Unknown !
    }

    @Override
    public Object content() {
        return object;
    }

    @Override
    public boolean requireSerializer() {
        return true;
    }
}
