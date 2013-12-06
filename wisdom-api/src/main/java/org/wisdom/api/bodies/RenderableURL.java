package org.wisdom.api.bodies;

import org.wisdom.api.http.Context;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Renderable;
import org.wisdom.api.http.Result;

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
        this(url, true);
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
        return mustBeChunked;
    }

}
