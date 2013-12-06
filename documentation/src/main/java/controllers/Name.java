package controllers;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

@Controller
public class Name extends DefaultController {

    @Route(method= HttpMethod.GET, uri = "/hello/{name}")
    public Result index(@Parameter("name") String name) {
        return ok("Hello " + name);
    }
}
