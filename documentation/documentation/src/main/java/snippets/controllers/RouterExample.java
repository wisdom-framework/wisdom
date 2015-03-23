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

import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Body;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Router;
import snippets.controllers.websockets.Data;

@Controller
public class RouterExample extends DefaultController {

    Object map;

    // tag::router[]
    @Requires
    private Router router;

    public void doSomethingWithTheRouter() {
        org.wisdom.api.router.Route route = router.getRouteFor(HttpMethod.GET, "/");
    }
    // end::router[]

    // tag::reverse[]
    @Route(method=HttpMethod.GET, uri="/reverse")
    public Result reverse() {
        // Get the url of another action method (without parameters)
        String all = router.getReverseRouteFor(PhotoController.class, "all");
        // Get the url of another action method, with id = 1
        String get = router.getReverseRouteFor(PhotoController.class, "get", "id", 1);

        return ok(all + "\n" + get);
    }
    // end::reverse[]

    // tag::redirect[]
    @Route(method=HttpMethod.GET, uri = "/reverse/redirect")
    public Result redirectToHello() {
        String url = router.getReverseRouteFor(Name.class, "index", "name", "wisdom");
        return redirect(url);
    }
    // end::redirect[]

    // tag::negotiationBasedOnProduces[]
    @Route(method= HttpMethod.GET, uri="/", produces = "application/xml")
    public Result asXML() {
        return ok(map).xml();
    }

    @Route(method= HttpMethod.GET, uri="/", produces = "application/json")
    public Result asJson() {
        return ok(map).json();
    }
    // end::negotiationBasedOnProduces[]

    // tag::negotiationBasedOnAccepts[]
    @Route(method= HttpMethod.POST, uri="/consume",
            accepts = "application/xml")
    public Result fromXML(@Body Data form) {
        return ok(form).xml();
    }

    @Route(method= HttpMethod.POST, uri="/consume",
            accepts = "application/json")
    public Result fromJson(@Body Data form) {
        return ok(form).json();
    }
    // end::negotiationBasedOnAccepts[]

    // tag::negotiationBasedOnAcceptsAndProduces[]
    @Route(method= HttpMethod.POST, uri="/consprod",
            accepts = {"application/json", "application/xml"},
            produces = "application/json")
    public Result fromJsonAsJson(@Body Data form) {
        return ok(form).json();
    }
    // end::negotiationBasedOnAcceptsAndProduces[]
}
