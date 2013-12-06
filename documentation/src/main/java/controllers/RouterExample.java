package controllers;

import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Router;

@Controller
public class RouterExample extends DefaultController {

    // tag::router[]
    @Requires
    private Router router;

    public void doSomethingWithTheRouter() {
        org.wisdom.api.router.Route route = router.getRouteFor(HttpMethod.GET, "/");
    }
    // end::router[]

    // tag::reverse[]
    @Route(method=HttpMethod.GET, uri="/reverse")
    public Result reverse() {
        // Get the url of another action method (without parameters)
        String all = router.getReverseRouteFor(PhotoController.class, "all");
        // Get the url of another action method, with id = 1
        String get = router.getReverseRouteFor(PhotoController.class, "get", "id", 1);

        return ok(all + "\n" + get);
    }
    // end::reverse[]

    // tag::redirect[]
    @Route(method=HttpMethod.GET, uri = "/reverse/redirect")
    public Result redirectToHello() {
        String url = router.getReverseRouteFor(Name.class, "index", "name", "wisdom");
        return redirect(url);
    }
    // end::redirect[]
}
