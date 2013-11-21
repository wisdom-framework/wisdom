package controllers;

import org.ow2.chameleon.wisdom.api.DefaultController;
import org.ow2.chameleon.wisdom.api.annotations.*;
import org.ow2.chameleon.wisdom.api.http.HttpMethod;
import org.ow2.chameleon.wisdom.api.http.Result;

@Controller
@Path("/attributes")
public class Attributes extends DefaultController {

    // tag::attributes[]
    @Route(method= HttpMethod.POST, uri="/")
    public Result post(@Attribute("id") String id, @Attribute("name") String name) {
        // The values of id and names are computed the request attributes.
        return ok(id + " - " + name);
    }
    // end::attributes[]

    @Route(method= HttpMethod.POST, uri="/dump")
    public Result dump() {
        StringBuilder buffer = new StringBuilder();
        for (String key : context().attributes().keySet()) {
            buffer.append(key).append(" : ").append(context().attributes().get(key)).append("\n");
        }
        return ok(buffer.toString());
    }
}
