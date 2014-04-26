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

import com.google.common.collect.ImmutableList;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.RouteBuilder;

import java.util.List;

@Controller
public class Routes extends DefaultController {

    public Result action() {
        return ok("I've done something");
    }

    // tag::routes[]
    @Override
    public List<Route> routes() {
        return ImmutableList.of(
                new RouteBuilder().route(HttpMethod.GET).on("/routes").to(this, "action"),
                new RouteBuilder().route(HttpMethod.POST).on("/routes").to(this, "action")
        );
    }
    // end::routes[]
}
