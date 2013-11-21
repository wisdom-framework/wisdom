package controllers;

import org.ow2.chameleon.wisdom.api.DefaultController;
import org.ow2.chameleon.wisdom.api.annotations.Controller;
import org.ow2.chameleon.wisdom.api.annotations.Route;
import org.ow2.chameleon.wisdom.api.http.HttpMethod;
import org.ow2.chameleon.wisdom.api.http.Result;

@Controller
public class Redirect extends DefaultController {

    // tag::redirect[]
    @Route(method= HttpMethod.GET, uri="/redirect")
    public Result redirectToIndex() {
        return redirect("/");
    }
    // end::redirect[]

    // tag::temporary-redirect[]
    @Route(method= HttpMethod.GET, uri="/tmp")
    public Result redirectToHello() {
        return redirectTemporary("/hello/wisdom");
    }
    // end::temporary-redirect[]

}
