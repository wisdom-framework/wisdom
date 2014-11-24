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
package controllers.filters;

import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

import javax.validation.constraints.NotNull;


@Controller
public class MyFilteredController extends DefaultController {

    @Requires
    Json json;

    @Route(method = HttpMethod.GET, uri = "/filter/dummy")
    public Result get(@HttpParameter("key") @NotNull String value,
                      @FormParameter("field") @DefaultValue("") String field,
                      @HttpParameter("X-Foo") @DefaultValue("") String foo
    ) {
        logger().info("Received headers {}", context().headers());
        logger().info("Foo {}", foo);

        return ok(
                json.newObject()
                        .put("key", value)
                        .put("field", field)
                        .put("foo", foo));
    }
}
