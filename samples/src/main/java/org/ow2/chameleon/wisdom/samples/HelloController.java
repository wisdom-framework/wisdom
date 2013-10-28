package org.ow2.chameleon.wisdom.samples;

import com.google.common.collect.ImmutableMap;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.ow2.chameleon.wisdom.api.Controller;
import org.ow2.chameleon.wisdom.api.DefaultController;
import org.ow2.chameleon.wisdom.api.annotations.Route;
import org.ow2.chameleon.wisdom.api.http.HttpMethod;
import org.ow2.chameleon.wisdom.api.http.Result;
import org.ow2.chameleon.wisdom.api.route.Router;
import org.ow2.chameleon.wisdom.api.templates.Template;

/**
 * An hello world controller.
 */
@Component
@Provides(specifications = Controller.class)
@Instantiate
public class HelloController extends DefaultController {

    @Requires(filter = "(name=index)")
    private Template index;
    @Requires
    private Router router;

//    @Requires(filter = "(name=hello/hello)")
//    private Template hello;

    @Route(method = HttpMethod.GET, uri = "/hello/result")
    public Result hello() {
        MyForm form = context().body(MyForm.class);
        System.out.println(form);
        return ok("Hello " + form.name);
    }

    /**
     * Displays the index page of the hello app.
     */
    @Route(method = HttpMethod.GET, uri = "/hello")
    public Result index() {
        return ok(index.render(
                ImmutableMap.<String, Object>of(
                        "signedBy", "clement",
                        "formURL", router.getReverseRouteFor(this, "hello"))));
    }

}
