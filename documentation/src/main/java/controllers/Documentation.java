package controllers;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

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
