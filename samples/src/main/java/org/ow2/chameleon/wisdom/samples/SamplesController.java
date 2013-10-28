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
import org.ow2.chameleon.wisdom.api.route.Router;
import org.ow2.chameleon.wisdom.api.templates.Template;
import org.ow2.chameleon.wisdom.samples.file.FileController;
import org.ow2.chameleon.wisdom.samples.hello.HelloController;

import java.util.List;
import java.util.Map;

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
                        router.getReverseRouteFor(FileController.class, "index"))
        );

        return ok(index.render(ImmutableMap.<String, Object>of(
                "samples", samples)));
    }

}
