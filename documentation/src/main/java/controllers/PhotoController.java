package controllers;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

@Controller
@Path("/photo")
public class PhotoController extends DefaultController {

    @Route(method= HttpMethod.GET, uri="/")
    public Result all() {
        return ok();
    }

    @Route(method= HttpMethod.POST, uri="/")
    public Result upload() {
        return ok();
    }

    @Route(method= HttpMethod.GET, uri="/{id}")
    public Result get(@Parameter("id") String id) {
        return ok();
    }

    // ...
}
