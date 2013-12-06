package controllers;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Body;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

@Controller
public class BodyWrap extends DefaultController {

    @Route(method= HttpMethod.POST, uri = "/wrap")
    public Result index(@Body MyData data) {
        return ok(data.toString());
    }
}
