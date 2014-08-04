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
package controllers.template;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.View;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.templates.Template;

@Controller
@Path("/templates/")
public class TemplateController extends DefaultController {

    @View("htmlEscaping")
    Template escaping;

    @View("static")
    Template staticMethodAccess;

    @Route(method = HttpMethod.GET, uri = "escaping")
    public Result escaping() {
        String maliciousJavascript = "<script>alert('Hello! <>&\"'');</script>";
        return ok(render(escaping, "maliciousJavascript", maliciousJavascript));
    }

    @Route(method = HttpMethod.GET, uri = "static")
    public Result staticMethodAccess() {
        return ok(render(staticMethodAccess));
    }
}
