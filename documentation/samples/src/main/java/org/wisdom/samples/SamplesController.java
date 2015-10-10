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
package org.wisdom.samples;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.View;
import org.wisdom.api.asset.Assets;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Router;
import org.wisdom.api.templates.Template;
import org.wisdom.samples.ajax.TodoListController;
import org.wisdom.samples.async.SimpleAsyncController;
import org.wisdom.samples.file.FileController;
import org.wisdom.samples.hello.HelloController;
import org.wisdom.samples.interceptors.Logged;
import org.wisdom.samples.session.SessionController;
import org.wisdom.samples.validation.DriverController;

import java.util.List;

/**
 * A controller listing all samples.
 */
@Controller
@Logged(duration = true)
public class SamplesController extends DefaultController {

    /**
     * The name of the index method.
     */
    public static final String INDEX = "index";

    @View("samples/samples")
    private Template index;

    @Requires
    private Router router;

    @Route(method = HttpMethod.GET, uri = "/samples")
    public Result index() {
        logger().info("Building the sample page");
        List<Sample> samples = ImmutableList.of(
                new Sample("hello", "a simple example using templates and forms",
                        router.getReverseRouteFor(HelloController.class, INDEX)),
                new Sample("file upload", "a simple file server demonstrating file uploads and flash",
                        router.getReverseRouteFor(FileController.class, INDEX)),
                new Sample("todo list", "a todo list manager demonstrating ajax calls",
                        router.getReverseRouteFor(TodoListController.class, INDEX)),
                new Sample("simple async", "an example of async result (the page appears 10 seconds later)",
                        router.getReverseRouteFor(SimpleAsyncController.class, "heavyComputation", "name", "wisdom")),
                new Sample("session", "an example a session usage",
                        router.getReverseRouteFor(SessionController.class, INDEX)),
                new Sample("validation", "user input validation using Bean Validation",
                        router.getReverseRouteFor(DriverController.class, INDEX)),
                new Sample("web socket", "web socket example", "/assets/websocket.html"),
//                new Sample("SockJS", "SockJS example", "/assets/sockjs.html"),
                new Sample("Bean Mapping", "Creation of a validated bean", "/bean?q2=wisdom"),
                new Sample("Filters", "Redirect to a custom 404 page when the page is not found", "/samples/missing"),
                new Sample("Custom JSON serializer", "Customize the serialization to have the first name as key",
                        "/team"),
                new Sample("Authentication check", "Unauthenticated access", "/security/secret"),
                new Sample("Authentication check", "Authenticated access", "/security/secret?username=admin"),
                new Sample("Error rendering", "Check the error template", "/error"),
                new Sample("Client Side Internationalization", "Example using two different libraries",
                        "/assets/i18n.html")
        );

        return ok(render(index,
                ImmutableMap.<String, Object>of("samples", samples)));
    }

    @Requires
    Assets assets;

    @Route(method = HttpMethod.GET, uri = "/samples/assets")
    public Result assets() {
        return ok(assets.assets()).json();
    }

}
