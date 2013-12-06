package org.wisdom.api.http;

import java.io.InputStream;

/**
 * Allows customized the rendering process.
 */
public interface Renderable<T> {

    InputStream render(Context context, Result result) throws Exception;

    long length();

    String mimetype();

    T content();

    boolean requireSerializer();

    void setSerializedForm(String serialized);

    boolean mustBeChunked();
}
