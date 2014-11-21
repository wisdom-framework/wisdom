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
package org.wisdom.framework.filters.test;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.PathParameter;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

@Controller
@Path("/corsTests/")
public class CorsTestController extends DefaultController {

    @Route(method = HttpMethod.POST, uri = "post")
    public Result postRoute() {
        return ok();
    }
    
    @Route(method = HttpMethod.POST, uri = "dynamic/{id}")
    public Result postDynamicRoute(@PathParameter("id") String id) {
        return ok();
    }

    @Route(method = HttpMethod.GET, uri = "get")
    public Result getRoute() {
        return ok();
    }

    @Route(method = HttpMethod.PUT, uri = "put")
    public Result putRoute() {
        return ok();
    }

    @Route(method = HttpMethod.POST, uri = "postPutGet")
    public Result postRoute2() {
        return ok();
    }

    @Route(method = HttpMethod.GET, uri = "postPutGet")
    public Result getRoute2() {
        return ok();
    }

    @Route(method = HttpMethod.PUT, uri = "postPutGet")
    public Result putRoute2() {
        return ok();
    }

}
