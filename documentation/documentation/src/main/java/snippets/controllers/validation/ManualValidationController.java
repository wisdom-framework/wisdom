/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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
// tag::controller[]
package snippets.controllers.validation;

import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Body;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@Controller
public class ManualValidationController extends DefaultController {

    @Requires
    Validator validator;

    @Route(method = HttpMethod.POST, uri = "/validation/bean/manual")
    public Result actionWithValidatedBody(
            @Body User user
    ) {
        final Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (violations.isEmpty()) {
            return ok();
        } else {
            return badRequest("Oh no, this is not right: "
                    + violations.iterator().next().getMessage());
        }
    }

    public static class User {
        @NotNull
        @Size(min = 4, max = 8)
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
// end::controller[]
