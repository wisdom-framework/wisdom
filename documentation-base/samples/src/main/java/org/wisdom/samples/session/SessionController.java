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
package org.wisdom.samples.session;

import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.View;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Router;
import org.wisdom.api.templates.Template;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

/**
 * A simple controller to demonstrate sessions.
 */
@Controller
public class SessionController extends DefaultController {

    @View("session/session")
    public Template index;

    @Requires
    public Router router;

    @Route(method = HttpMethod.GET, uri = "/session")
    public Result index() {
        System.out.println(session().getData());
        Map<String, String> data = session().getData();
        return ok(render(index, "data", data));
    }

    /**
     * Action called to clear the session
     */
    @Route(method = HttpMethod.POST, uri = "/session/clear")
    public Result clear() {
        session().clear();
        return redirect(router.getReverseRouteFor(this, "index"));
    }

    /**
     * Action called to populate the session
     */
    @Route(method = HttpMethod.POST, uri = "/session/populate")
    public Result populate() {
        session().put("createdBy", "wisdom");
        session().put("at", DateFormat.getDateTimeInstance().format(new Date()));
        return redirect(router.getReverseRouteFor(this, "index"));
    }

}
