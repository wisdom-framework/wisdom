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
package org.wisdom.monitor.extensions.wisdom;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.View;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Router;
import org.wisdom.api.security.Authenticated;
import org.wisdom.api.templates.Template;
import org.wisdom.monitor.service.MonitorExtension;

/**
 * Provide information about available routes.
 */
@Controller
@Authenticated("Monitor-Authenticator")
public class RouterExtension extends DefaultController implements MonitorExtension {

    @View("monitor/routes")
    Template template;

    @Requires
    Router router;

    @Requires
    Json json;

    @Route(method = HttpMethod.GET, uri = "/monitor/routes")
    public Result index() {
        return ok(render(template));
    }

    @Route(method = HttpMethod.GET, uri = "/monitor/routes/routes")
    public Result getRoutes() {
        ArrayNode array = json.newArray();
        for (org.wisdom.api.router.Route route : router.getRoutes()) {
            array.add(
                    RouteModel.from(route, json)
            );
        }
        return ok(array);
    }

    @Override
    public String label() {
        return "Routes";
    }

    @Override
    public String url() {
        return "/monitor/routes";
    }

    @Override
    public String category() {
        return "Wisdom";
    }
}
