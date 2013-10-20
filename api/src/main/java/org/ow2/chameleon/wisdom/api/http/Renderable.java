package org.ow2.chameleon.wisdom.api.http;

import java.util.Map;

/**
 * Allows customized the rendering process.
 */
public interface Renderable {

    void render(Context context, Result result, Map<String, Object> params) throws Exception;
}
