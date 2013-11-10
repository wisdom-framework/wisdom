package org.ow2.chameleon.wisdom.samples;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.ow2.chameleon.wisdom.api.Controller;
import org.ow2.chameleon.wisdom.api.DefaultController;
import org.ow2.chameleon.wisdom.api.annotations.Route;
import org.ow2.chameleon.wisdom.api.http.HttpMethod;
import org.ow2.chameleon.wisdom.api.http.Result;
import org.ow2.chameleon.wisdom.api.router.Router;
import org.ow2.chameleon.wisdom.api.templates.Template;
import org.ow2.chameleon.wisdom.samples.ajax.TodoListController;
import org.ow2.chameleon.wisdom.samples.async.SimpleAsyncController;
import org.ow2.chameleon.wisdom.samples.file.FileController;
import org.ow2.chameleon.wisdom.samples.hello.HelloController;
import org.ow2.chameleon.wisdom.samples.validation.DriverController;

import java.util.List;

/**
 * A controller listing all samples.
 */
@Component
@Provides(specifications = Controller.class)
@Instantiate
public class SamplesController extends DefaultController {

    @Requires(filter = "(name=samples/samples)")
    private Template index;

    @Requires
    private Router router;

    @Route(method= HttpMethod.GET, uri = "/samples")
    public Result index() {
        List<Sample> samples = ImmutableList.<Sample>of(
                new Sample("hello", "a simple example using templates and forms",
                        router.getReverseRouteFor(HelloController.class, "index")),
                new Sample("file upload", "a simple file server demonstrating file uploads and flash",
                        router.getReverseRouteFor(FileController.class, "index")),
                new Sample("todo list", "a todo list manager demonstrating ajax calls",
                        router.getReverseRouteFor(TodoListController.class, "index")),
                new Sample("simple async", "an example of async result",
                        router.getReverseRouteFor(SimpleAsyncController.class, "heavyComputation", "name", "wisdom")),
                new Sample("validation", "input validation",
                        router.getReverseRouteFor(DriverController.class, "index"))
        );

        return ok(render(index,
                ImmutableMap.<String, Object>of("samples", samples)));
    }

}
