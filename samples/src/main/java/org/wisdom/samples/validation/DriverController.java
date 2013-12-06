package org.wisdom.samples.validation;

import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Body;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.View;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.templates.Template;
import org.wisdom.samples.validation.model.Car;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import java.io.IOException;
import java.util.Set;

/**
 * An hello world controller.
 */
@Controller
public class DriverController extends DefaultController {

    @View("validation/validation")
    private Template index;
    @Requires
    private Validator validator;

    /**
     * Displays the result (manual check).
     */
    @Route(method = HttpMethod.POST, uri = "samples/validation")
    public Result check(@Body Car car) throws IOException {
        Set<ConstraintViolation<Car>> violations = validator.validate(car);
        if (!violations.isEmpty()) {
            return badRequest(violations).json();
        } else {
            return ok();
        }
    }

    /**
     * Displays the result (automatic check).
     */
    @Route(method = HttpMethod.POST, uri = "samples/auto-validation")
    public Result auto(@Valid @Body Car car) throws IOException {
        return ok();
    }

    /**
     * Displays the index page with lots of form items.
     */
    @Route(method = HttpMethod.GET, uri = "samples/validation")
    public Result index() throws IOException {
        return ok(render(index, "signedBy", "clement"));
    }

}
