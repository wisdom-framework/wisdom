package org.wisdom.samples;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.samples.interceptors.Logged;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.View;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Router;
import org.wisdom.api.templates.Template;
import org.wisdom.samples.ajax.TodoListController;
import org.wisdom.samples.async.SimpleAsyncController;
import org.wisdom.samples.file.FileController;
import org.wisdom.samples.hello.HelloController;
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
        List<Sample> samples = ImmutableList.<Sample>of(
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
                new Sample("validation", "user input validation",
                        router.getReverseRouteFor(DriverController.class, INDEX)),
                new Sample("web socket", "web socket", "/assets/websocket.html"),
                new Sample("WAMP", "Wamp Web Socket subprotocol", "/assets/wamp.html")
        );

        return ok(render(index,
                ImmutableMap.<String, Object>of("samples", samples)));
    }

}
