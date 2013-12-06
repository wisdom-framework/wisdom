package controllers;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

@Controller
public class Simple extends DefaultController {

    @Route(method= HttpMethod.GET, uri = "/works")
    public Result index() {
        return ok("It works");
    }
}
