package org.ow2.chameleon.wisdom.samples;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.ow2.chameleon.wisdom.api.Controller;
import org.ow2.chameleon.wisdom.api.http.HttpMethod;
import org.ow2.chameleon.wisdom.api.http.Result;
import org.ow2.chameleon.wisdom.api.route.Route;
import org.ow2.chameleon.wisdom.api.route.RouteBuilder;
import org.ow2.chameleon.wisdom.api.route.Router;
import org.ow2.chameleon.wisdom.api.templates.Template;

import java.util.List;

/**
 * An hello world controller.
 */
@Component
@Provides(specifications = Controller.class)
@Instantiate
public class HelloController extends Controller {

    @Requires(filter = "(name=index)")
    private Template index;

    @Requires
    private Router router;

//    @Requires(filter = "(name=hello/hello)")
//    private Template hello;

    public List<Route> routes() {
        return ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/hello").to(this, "index"),
                new RouteBuilder().route(HttpMethod.POST).on("/hello/simple").to(this, "hello"),
                new RouteBuilder().route(HttpMethod.GET).on("/hello/user").to(this, "withUserInQuery"),
                new RouteBuilder().route(HttpMethod.GET).on("/hello/json").to(this, "helloAsJson"),
                new RouteBuilder().route(HttpMethod.GET).on("/hello/{user}").to(this, "withUserInPath")
        );
    }


    public Result hello() {
        MyForm form = context().body(MyForm.class);
        System.out.println(form);
        return ok("Hello " + form.name);
    }

    public Result withUserInQuery() {
        String username = context().parameter("user", "john doe");
        return ok("hello " + username);
    }

    public Result withUserInPath() {
        String username = context().parameterFromPath("user");
        return ok("hello " + username);
    }

    public Result helloAsJson() {
        String username = context().parameter("user", "john doe");
        return ok(ImmutableMap.of("message", "hello", "user", username)).json();
    }

    /**
     * Displays the index page of the hello app.
     * @return
     */
    public Result index() {
        return ok(index.render(
                ImmutableMap.<String, Object>of(
                        "signedBy", "clement",
                        "formURL", router.getReverseRouteFor(this, "hello"))));
    }

}
