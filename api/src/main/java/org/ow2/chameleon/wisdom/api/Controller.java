package org.ow2.chameleon.wisdom.api;

import org.ow2.chameleon.wisdom.api.route.Route;

import java.util.List;

/**
 * Controller interface.
 * Every component willing to provide <em>actions</em> must publish the controller service.
 * The router implementation is bound to all controllers and dispatches the request.
 */
public interface Controller {

    /**
     * Gets the list of routes offered by this controller.
     * This list is ordered, meaning that the first routes are evaluated before the others. As soon as the router is
     * finding a route matching the request, it delegates the request to the target action.
     * @return the list of routes.
     */
    public List<Route> routes();
}
