package org.ow2.chameleon.wisdom.api.bodies;

import org.ow2.chameleon.wisdom.api.http.Context;
import org.ow2.chameleon.wisdom.api.http.MimeTypes;
import org.ow2.chameleon.wisdom.api.http.Renderable;
import org.ow2.chameleon.wisdom.api.http.Result;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

/**
 * A renderable object taking an URL as parameter.
 */
public class RenderableURL implements Renderable<URL> {

    private final URL url;
    private final boolean mustBeChunked;

    public RenderableURL(URL url, boolean mustBeChunked) {
        this.url = url;
        this.mustBeChunked = mustBeChunked;
    }

    public RenderableURL(URL url) {
        this(url, false);
    }

    @Override
    public InputStream render(Context context, Result result) throws Exception {
        return url.openStream();
    }

    @Override
    public long length() {
        return -1; // Unknown.
    }

    @Override
    public String mimetype() {
        return MimeTypes.getMimeTypeForFile(url);
    }

    @Override
    public URL content() {
        return url;
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
