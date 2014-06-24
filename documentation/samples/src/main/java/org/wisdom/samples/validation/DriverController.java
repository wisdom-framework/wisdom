/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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
    @Route(method = HttpMethod.POST, uri = "samples/manual-validation")
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
