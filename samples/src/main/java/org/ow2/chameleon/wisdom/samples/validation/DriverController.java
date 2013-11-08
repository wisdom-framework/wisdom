package org.ow2.chameleon.wisdom.samples.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.io.IOUtils;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.ow2.chameleon.wisdom.api.Controller;
import org.ow2.chameleon.wisdom.api.DefaultController;
import org.ow2.chameleon.wisdom.api.annotations.Body;
import org.ow2.chameleon.wisdom.api.annotations.Route;
import org.ow2.chameleon.wisdom.api.http.HttpMethod;
import org.ow2.chameleon.wisdom.api.http.Result;
import org.ow2.chameleon.wisdom.api.templates.Template;
import org.ow2.chameleon.wisdom.samples.validation.model.Car;
import org.ow2.chameleon.wisdom.samples.validation.model.Driver;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.IOException;
import java.util.Set;

/**
 * An hello world controller.
 */
@Component
@Provides(specifications = Controller.class)
@Instantiate
public class DriverController extends DefaultController {

    @Requires(filter = "(name=validation/validation)")
    private Template index;

    @Requires
    private Validator validator;

    /**
     * Displays the result.
     */
    @Route(method = HttpMethod.POST, uri = "samples/validation")
    public Result check(@Body Car car) throws IOException {
        Set<ConstraintViolation<Car>> violations = validator.validate(car);
        if (! violations.isEmpty()) {
            return badRequest(violations).json();
        } else {
            return ok();
        }
    }

    /**
     * Displays the index page with lots of form items.
     */
    @Route(method = HttpMethod.GET, uri = "samples/validation")
    public Result index() throws IOException {
        return ok(render(index, "signedBy", "clement"));
    }

}
