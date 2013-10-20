package org.ow2.chameleon.wisdom.controller.resources;

import com.google.common.collect.ImmutableList;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.ow2.chameleon.wisdom.api.Controller;
import org.ow2.chameleon.wisdom.api.http.HttpMethod;
import org.ow2.chameleon.wisdom.api.http.Result;
import org.ow2.chameleon.wisdom.api.route.Route;
import org.ow2.chameleon.wisdom.api.route.RouteBuilder;

import java.io.File;
import java.util.List;

/**
 * A controller publishing the resources found in a folder.
 */
@Component
@Provides
@Instantiate(name = "PublicResourceController") // The default instance handle the `public` folder.
public class ResourceController extends Controller {

    private final File directory;

    public ResourceController(@Property String path) {
        directory = new File(path);
    }

    @Override
    public List<Route> routes() {
        return ImmutableList.of(new RouteBuilder().route(HttpMethod.GET).on("/" + directory.getName()).with(this,
                "serve"));
    }

    public Result serve() {
        File file = new File(directory, context().request().path());
        if (! file.exists()) {
            return notFound();
        } else {
            return ok(file);
        }
    }


}
