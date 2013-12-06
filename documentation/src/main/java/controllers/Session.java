package controllers;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

@Controller
public class Session extends DefaultController {

    // tag::session[]
    @Route(method= HttpMethod.GET, uri = "/session")
    public Result readSession() {
        String user = session("connected");
        if(user != null) {
            return ok("Hello " + user);
        } else {
            return unauthorized("Oops, you are not connected");
        }
    }
    // end::session[]

    // tag::login[]
    @Route(method= HttpMethod.GET, uri = "/session/login")
    public Result login() {
        String user = session("connected");
        if(user != null) {
            return ok("Already connected");
        } else {
            session("connected", "wisdom");
            return readSession();
        }
    }
    // end::login[]

    // tag::logout[]
    @Route(method= HttpMethod.GET, uri = "/session/logout")
    public Result logout() {
        session().remove("connected");
        return ok("You have been logged out");
    }
    // end::logout[]

    // tag::clear[]
    @Route(method= HttpMethod.GET, uri = "/session/clear")
    public Result clear() {
        session().clear();
        return ok("You have been logged out");
    }
    // end::clear[]

    // tag::flash[]
    @Route(method= HttpMethod.GET, uri = "/session/flash")
    public Result welcome() {
        String message = flash("success");
        if(message == null) {
            message = "Welcome!";
            flash("success", message);
        }
        return ok(message);
    }
    // end::flash[]
}
