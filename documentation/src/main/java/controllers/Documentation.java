package controllers;

import org.ow2.chameleon.wisdom.api.DefaultController;
import org.ow2.chameleon.wisdom.api.annotations.Controller;
import org.ow2.chameleon.wisdom.api.annotations.Route;
import org.ow2.chameleon.wisdom.api.http.HttpMethod;
import org.ow2.chameleon.wisdom.api.http.Result;

@Controller
public class Documentation extends DefaultController {

    @Route(method = HttpMethod.GET, uri = "/documentation")
    public Result doc() {
        return redirect("/assets/index.html");
    }

    @Route(method = HttpMethod.GET, uri = "/")
    public Result index() {
        return redirect("/assets/index.html");
    }
}
