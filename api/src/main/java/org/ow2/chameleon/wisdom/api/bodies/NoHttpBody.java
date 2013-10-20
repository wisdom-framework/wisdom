package org.ow2.chameleon.wisdom.api.bodies;

import org.ow2.chameleon.wisdom.api.http.Context;
import org.ow2.chameleon.wisdom.api.http.Renderable;
import org.ow2.chameleon.wisdom.api.http.Result;

import java.util.Map;

/**
 * Empty body.
 */
public class NoHttpBody implements Renderable {

    @Override
    public void render(Context context, Result result, Map<String, Object> params) throws Exception {
        // Empty on purpose... there is no body.
    }

}
