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

    @Route(method = HttpMethod.GET, uri = "/security/secret")
    @Authenticated
    public Result secret() {
        return ok("This is a secret... " + context().request().username());
    }

    @Route(method = HttpMethod.GET, uri = "/security")
    public Result notSecret() {
        String name = context().parameter("username");
        if (name == null) {
            name = "anonymous";
        }
        return ok("Hello " + name);
    }
}
