
package controllers;

import aQute.bnd.annotation.ConsumerType;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.annotations.QueryParameter;
import org.wisdom.api.http.*;

import javax.enterprise.inject.Produces;

import static org.wisdom.api.http.HttpMethod.*;

/**
 * A controller using weird hierarchy
 *
 * @version 1.3
 */
@Controller
@Path("/hello")
public class FooBarBaz extends DefaultController{

    @Route(method = GET, uri = "/foo")
    public Result foo(){
        return ok("Hello ");
    }

    @Route(method = GET, uri = "/foo/bar")
    public Result foo(){
        return ok("Hello ");
    }

    @Route(method = GET, uri = "/foo/xxx/zzz")
    public Result foo(){
        return ok("Hello ");
    }
}
