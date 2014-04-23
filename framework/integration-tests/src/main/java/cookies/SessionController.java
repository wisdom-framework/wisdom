package cookies;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.cookies.Cookie;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

@Controller
public class SessionController extends DefaultController {

    @Route(method = HttpMethod.GET, uri = "/session")
    public Result populate() {
        if (session().get("blah") == null) {
            session("blah", "42");
        } else {
            session().remove("blah");
        }
        session("foo", "bar");
        session("baz", "bah");
        return ok("Hello");
    }

    @Route(method = HttpMethod.GET, uri = "/session/clear")
    public Result clear() {
        session().clear();
        return ok("Hello");
    }


    @Route(method = HttpMethod.GET, uri = "/session/cookie")
    public Result useAnotherCookie() {
        session("foo", "bar");
        return ok("Hello").with(Cookie.cookie("toto", "titi").build());
    }

    @Route(method = HttpMethod.GET, uri = "/session/cookie/clear")
    public Result clearCookie() {
        return ok("Hello").without("toto");
    }
}
