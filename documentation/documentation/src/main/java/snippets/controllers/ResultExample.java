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
package snippets.controllers;

import org.wisdom.api.DefaultController;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;
import org.wisdom.api.templates.Template;

import java.util.Collections;
import java.util.List;

public class ResultExample extends DefaultController {

    //@View("template")
    Template template;

    public void example() {
        List<String> errors = Collections.emptyList();
        // tag::results[]
        Result ok = ok("Hello wisdom!");
        Result page = ok(render(template));
        Result notFound = notFound();
        Result pageNotFound = notFound("<h1>Page not found</h1>").as(MimeTypes.HTML);
        Result badRequest = badRequest(render(template, "error", errors));
        Result oops = internalServerError("Oops");
        Result exception = internalServerError(new NullPointerException("Cannot be null"));
        Result anyStatus = status(488).render("Strange response").as(MimeTypes.TEXT);
        // end::results[]
    }

}
