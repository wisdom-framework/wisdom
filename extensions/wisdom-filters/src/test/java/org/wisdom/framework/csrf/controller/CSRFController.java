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
package org.wisdom.framework.csrf.controller;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.templates.Template;
import org.wisdom.framework.csrf.api.CSRF;
import org.wisdom.framework.csrf.api.AddCSRFToken;

@Controller
public class CSRFController extends DefaultController {

    @View("csrf")
    Template template;

    @View("dialect")
    Template templateWithDialect;

    public CSRFController() {
        logger().info("Starting controller");
    }

    @Route(method = HttpMethod.GET, uri = "/csrf")
    @AddCSRFToken
    public Result getPage(@HttpParameter(AddCSRFToken.CSRF_TOKEN) String token) {
        if (token == null) {
            return internalServerError("Token expected " + context().request().data());
        }
        return ok(render(template, "token", token));
    }

    @Route(method = HttpMethod.GET, uri = "/csrf/dialect")
    @AddCSRFToken
    public Result getPageUsingDialect() {
        return ok(render(templateWithDialect));
    }


    @Route(method = HttpMethod.POST, uri = "/csrf")
    @CSRF
    public Result submitted(@FormParameter("key") String key) {
        if (key == null) {
            return internalServerError("Key expected");
        }
        return ok(key);
    }
}
