package org.ow2.chameleon.wisdom.api;

import org.ow2.chameleon.wisdom.api.route.Route;

import java.util.Collections;
import java.util.List;

/**
 * Controller interface.
 * Every component willing to provide <em>actions</em> must publish the controller service.
 * The router implementation is bound to all controllers and dispatches the request.
 */
public interface Controller {

    /**
     * Gets the list of routes offered by this controller.
     * @return the list of routes.
     */
    public List<Route> routes();
}
