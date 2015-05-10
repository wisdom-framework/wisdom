
package fake;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.Result;

import static org.wisdom.api.http.HttpMethod.GET;

@Controller
public class FakeControllerNoJDoc extends DefaultController{
    @Route(method = GET,uri = "/")
    public Result hello(){
        return ok();
    }
}
