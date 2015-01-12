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
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.FormParameter;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

@Controller
@Path("/attributes")
public class Attributes extends DefaultController {

    // tag::attributes[]
    @Route(method= HttpMethod.POST, uri="/")
    public Result post(@FormParameter("id") String id, @FormParameter("name") String name) {
        // The values of id and names are computed the request attributes.
        return ok(id + " - " + name);
    }
    // end::attributes[]

    @Route(method= HttpMethod.POST, uri="/dump")
    public Result dump() {
        StringBuilder buffer = new StringBuilder();
        for (String key : context().form().keySet()) {
            buffer.append(key).append(" : ").append(context().form().get(key)).append("\n");
        }
        return ok(buffer.toString());
    }
}
