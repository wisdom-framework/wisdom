package org.wisdom.samples.hello;

import com.google.common.collect.ImmutableMap;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Router;
import org.wisdom.api.templates.Template;

/**
 * An hello world controller.
 */
@Controller
@Path("samples/hello")
public class HelloController extends DefaultController {

    @View("hello/index")
    private Template index;

    @Requires
    private Router router;

    @View("hello/hello")
    private Template hello;

    /**
     * Displays the result.
     */
    @Route(method = HttpMethod.POST, uri = "/result")
    public Result hello(@Body MyForm form) {
        return ok(render(hello,
                ImmutableMap.<String, Object>of("form", form)));
    }

    /**
     * Displays the index page of the hello app.
     */
    @Route(method = HttpMethod.GET, uri = "/")
    public Result index() {
        return ok(render(index, "signedBy", "wisdom"));
    }

}
