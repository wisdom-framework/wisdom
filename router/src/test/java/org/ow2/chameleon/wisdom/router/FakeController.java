package org.ow2.chameleon.wisdom.router;

import org.ow2.chameleon.wisdom.api.DefaultController;
import org.ow2.chameleon.wisdom.api.http.Result;
import org.ow2.chameleon.wisdom.api.router.Route;

import java.util.Collections;
import java.util.List;

/**
 * A fake controller.
 */
public class FakeController extends DefaultController {

    private List<Route> routes = Collections.emptyList();

    public FakeController() {

    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }

    public Result foo() {
        return null;
    }



    @Override
    public List<Route> routes() {
        return routes;
    }
}
