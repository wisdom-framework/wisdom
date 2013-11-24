package controllers;

import org.ow2.chameleon.wisdom.api.DefaultController;
import org.ow2.chameleon.wisdom.api.annotations.Controller;
import org.ow2.chameleon.wisdom.api.annotations.Route;
import org.ow2.chameleon.wisdom.api.http.HttpMethod;
import org.ow2.chameleon.wisdom.api.http.Result;

@Controller
public class Simple extends DefaultController {

    @Route(method= HttpMethod.GET, uri = "/works")
    public Result index() {
        return ok("It works");
    }
}
