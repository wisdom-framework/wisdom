package org.wisdom.samples.security;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.security.Authenticated;

/**
 *
 */
@Controller
public class SecretKeeperController extends DefaultController {

    @Route(method= HttpMethod.GET, uri="/security/secret")
    @Authenticated
    public Result secret() {
        return ok("This is a secret... " + context().request().username());
    }

    @Route(method= HttpMethod.GET, uri="/security")
    public Result notSecret() {
        String name = context().session().get("username");
        if (name == null) {
            name = "anonymous";
        }
        return ok("Hello " + name);
    }
}
