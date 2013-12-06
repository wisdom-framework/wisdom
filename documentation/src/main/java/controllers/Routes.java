package controllers;

import com.google.common.collect.ImmutableList;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.RouteBuilder;

import java.util.List;

@Controller
public class Routes extends DefaultController {

    public Result action() {
        return ok("I've done something");
    }

    // tag::routes[]
    @Override
    public List<Route> routes() {
        return ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/routes").to(this, "action"),
                new RouteBuilder().route(HttpMethod.POST).on("/routes").to(this, "action")
        );
    }
    // end::routes[]
}
