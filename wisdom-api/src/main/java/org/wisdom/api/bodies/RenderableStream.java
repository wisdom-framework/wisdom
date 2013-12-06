package org.wisdom.api.bodies;

import org.wisdom.api.http.Context;
import org.wisdom.api.http.Renderable;
import org.wisdom.api.http.Result;

import java.io.InputStream;

/**
 * A renderable object taking an Input Stream as parameter.
 */
public class RenderableStream implements Renderable<InputStream> {

    private final InputStream stream;
    private final boolean mustBeChunked;

    public RenderableStream(InputStream stream, boolean mustBeChunked) {
        this.stream = stream;
        this.mustBeChunked = mustBeChunked;
    }

    public RenderableStream(InputStream stream) {
        this(stream, true);
    }

    @Override
    public InputStream render(Context context, Result result) throws Exception {
        return stream;
    }

    @Override
    public long length() {
        return -1; // Unknown.
    }

    @Override
    public String mimetype() {
        return null;
    }

    @Override
    public InputStream content() {
        return stream;
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
        return mustBeChunked;
    }

}
