package org.ow2.chameleon.wisdom.api.bodies;

import com.google.common.collect.ImmutableList;
import org.ow2.chameleon.wisdom.api.http.Context;
import org.ow2.chameleon.wisdom.api.http.MimeTypes;
import org.ow2.chameleon.wisdom.api.http.Renderable;
import org.ow2.chameleon.wisdom.api.http.Result;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;

/**
 * A renderable object taking an URL as parameter.
 */
public class RenderableURL implements Renderable {

    private final URL url;
    private final String type;

    public RenderableURL(URL url) {
        this.url = url;
        this.type = MimeTypes.getMimeTypeForFile(url);
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
        return type;
    }

}
