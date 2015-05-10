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
package controller;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.http.FileItem;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

/**
 * A controller using query, path and HTTP parameters
 */
@Controller
public class ParameterizedController extends DefaultController {

    @Route(method = HttpMethod.GET, uri = "/params/1")
    public Result actionWithQueryParameter(@FormParameter("hello") String h) {
        return ok();
    }

    @Route(method = HttpMethod.GET, uri = "/params/2/{hello}")
    public Result actionWithPathParameter(@PathParameter("hello") String h) {
        return ok();
    }

    @Route(method = HttpMethod.GET, uri = "/params/3/{hello}")
    public Result actionWithPathAndFormParameters(@PathParameter("hello") String h,
                                                  @FormParameter("name") @DefaultValue("wisdom") String n) {
        return ok();
    }

    @Route(method = HttpMethod.POST, uri = "/params/4")
    public Result actionWithBody(@Body String body) {
        return ok();
    }

    @Route(method = HttpMethod.POST, uri = "/params/5")
    public Result actionWithFileItem(@FormParameter("file") FileItem fileItem) {
        return ok();
    }


}
