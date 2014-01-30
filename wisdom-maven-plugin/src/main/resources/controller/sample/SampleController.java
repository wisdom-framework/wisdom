package sample;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

/**
 * Your first Wisdom Controller.
 */
@Controller
public class SampleController extends DefaultController {

    @Route(method = HttpMethod.GET, uri = "/")
    public Result hello() {
        return ok("hello Wisdom !");
    }
}
