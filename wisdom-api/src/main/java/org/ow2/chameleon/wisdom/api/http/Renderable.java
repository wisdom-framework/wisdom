package org.ow2.chameleon.wisdom.api.http;

import java.io.InputStream;
import java.util.Map;

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
}
