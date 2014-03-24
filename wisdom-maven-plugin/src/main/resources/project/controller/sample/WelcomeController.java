package sample;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.View;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.templates.Template;

/**
 * Your first Wisdom Controller.
 */
@Controller
public class WelcomeController extends DefaultController {

    @View("welcome")
    private Template welcome;

    @Route(method = HttpMethod.GET, uri = "/")
    public Result welcome() {
        return ok(render(welcome));
    }

}
