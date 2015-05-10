
package controllers;

import aQute.bnd.annotation.ConsumerType;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.annotations.QueryParameter;
import org.wisdom.api.http.*;

import javax.enterprise.inject.Produces;

import static org.wisdom.api.http.HttpMethod.*;

/**
 * A good old fake controller.
 *
 * @version 1.3
 */
@Controller
@Path("/hello")
public class FakeController extends DefaultController{

    @Route(method = OPTIONS,uri = "/tricky")
    public Result tricky(){
        return ok("Hello ");
    }

    @Route(method = PUT,uri = "/trickyNotAChild")
    public Result trickyNotAChild(){
        return ok("Hello ");
    }

    public void shouldBeignoreCauseItsnotAroute(){

    }
}
