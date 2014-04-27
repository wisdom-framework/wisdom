package controllers;

import org.hibernate.validator.constraints.Email;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

import javax.validation.constraints.NotNull;

/**
 * Check validation.
 */
@Controller
public class ValidationController extends DefaultController {

    @Route(method = HttpMethod.GET, uri = "/validation/auto")
    public Result automatic(@Parameter("email") @NotNull @Email String email) {
        return ok(email);
    }
}
