package sample;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

/**
 * Your first Wisdom Controller.
 */
@Component
@Provides
@Instantiate
public class SampleController extends DefaultController {

    @Route(method = HttpMethod.GET, uri = "/")
    public Result hello() {
        return ok("hello Wisdom !");
    }
}
