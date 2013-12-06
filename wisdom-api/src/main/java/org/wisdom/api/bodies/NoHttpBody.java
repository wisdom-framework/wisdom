package org.wisdom.api.bodies;

import org.wisdom.api.http.Context;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Renderable;
import org.wisdom.api.http.Result;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Empty body.
 */
public class NoHttpBody implements Renderable<Void> {

    public static final byte[] EMPTY = new byte[0];

    @Override
    public InputStream render(Context context, Result result) throws Exception {
        return new ByteArrayInputStream(EMPTY);
    }

    @Override
    public long length() {
        return 0;
    }

    @Override
    public String mimetype() {
        return MimeTypes.TEXT;
    }

    @Override
    public Void content() {
        return null;
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
