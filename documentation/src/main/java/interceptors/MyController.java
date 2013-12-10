package interceptors;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

// tag::class[]
@Controller
@Logged(duration = true)
public class MyController extends DefaultController {
// end::class[]

    // tag::method[]
    @Route(method= HttpMethod.GET, uri = "/intercepted")
    @Logged(duration = true)
    public Result action() {
        //...
        return ok();
    }
    // end::method[]

}
