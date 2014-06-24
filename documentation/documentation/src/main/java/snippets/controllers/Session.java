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
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

@Controller
public class Session extends DefaultController {

    // tag::session[]
    @Route(method= HttpMethod.GET, uri = "/session")
    public Result readSession() {
        String user = session("connected");
        if(user != null) {
            return ok("Hello " + user);
        } else {
            return unauthorized("Oops, you are not connected");
        }
    }
    // end::session[]

    // tag::login[]
    @Route(method= HttpMethod.GET, uri = "/session/login")
    public Result login() {
        String user = session("connected");
        if(user != null) {
            return ok("Already connected");
        } else {
            session("connected", "wisdom");
            return readSession();
        }
    }
    // end::login[]

    // tag::logout[]
    @Route(method= HttpMethod.GET, uri = "/session/logout")
    public Result logout() {
        session().remove("connected");
        return ok("You have been logged out");
    }
    // end::logout[]

    // tag::clear[]
    @Route(method= HttpMethod.GET, uri = "/session/clear")
    public Result clear() {
        session().clear();
        return ok("You have been logged out");
    }
    // end::clear[]

    // tag::flash[]
    @Route(method= HttpMethod.GET, uri = "/session/flash")
    public Result welcome() {
        String message = flash("success");
        if(message == null) {
            message = "Welcome!";
            flash("success", message);
        }
        return ok(message);
    }
    // end::flash[]
}
