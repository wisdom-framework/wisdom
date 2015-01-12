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
package snippets.controllers.validation;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Controller
public class I18NValidationController extends DefaultController {
    // tag::controller[]
    @Route(method = HttpMethod.GET, uri = "/validation/i18n")
    public Result actionWithValidatedParams(
            @NotNull(message = "{id_not_set}") @Parameter("id") String id,
            @Size(min = 4, max = 8, message = "{name_not_fit}") @Parameter("name") String name) {
        return ok();
    }
    // end::controller[]
}
