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
package org.wisdom.samples.hello;

import com.google.common.collect.ImmutableMap;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Router;
import org.wisdom.api.templates.Template;

/**
 * An hello world controller.
 */
@Controller
@Path("samples/hello")
public class HelloController extends DefaultController {

    @View("hello/index")
    private Template index;

    @Requires
    private Router router;

    @View("hello/hello")
    private Template hello;

    /**
     * Displays the result.
     */
    @Route(method = HttpMethod.POST, uri = "/result")
    public Result hello(@Body MyForm form) {
        return ok(render(hello,
                ImmutableMap.<String, Object>of("form", form)));
    }

    /**
     * Displays the index page of the hello app.
     */
    @Route(method = HttpMethod.GET, uri = "/")
    public Result index() {
        return ok(render(index, "signedBy", "wisdom"));
    }

}
