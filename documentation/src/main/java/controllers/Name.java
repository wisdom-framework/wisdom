package controllers;

import org.ow2.chameleon.wisdom.api.DefaultController;
import org.ow2.chameleon.wisdom.api.annotations.Controller;
import org.ow2.chameleon.wisdom.api.annotations.Parameter;
import org.ow2.chameleon.wisdom.api.annotations.Route;
import org.ow2.chameleon.wisdom.api.http.HttpMethod;
import org.ow2.chameleon.wisdom.api.http.Result;

@Controller
public class Name extends DefaultController {

    @Route(method= HttpMethod.GET, uri = "/hello/{name}")
    public Result index(@Parameter("name") String name) {
        return ok("Hello " + name);
    }
}
