package org.ow2.chameleon.wisdom.controller.resources;

import com.google.common.collect.ImmutableList;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.wisdom.api.Controller;
import org.ow2.chameleon.wisdom.api.DefaultController;
import org.ow2.chameleon.wisdom.api.http.HttpMethod;
import org.ow2.chameleon.wisdom.api.http.Result;
import org.ow2.chameleon.wisdom.api.route.Route;
import org.ow2.chameleon.wisdom.api.route.RouteBuilder;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * A controller publishing the resources found in a folder and in bundles
 */
@Component
@Provides(specifications = Controller.class)
@Instantiate(name = "PublicResourceController")
public class ResourceController extends DefaultController {

    // The default instance handle the `assets` folder.
    private final File directory;
    private final BundleContext context;

    public ResourceController(BundleContext bc, @Property(value = "assets") String path) {
        directory = new File(path);
        this.context = bc;
    }

    @Override
    public List<Route> routes() {
        return ImmutableList.of(new RouteBuilder()
                .route(HttpMethod.GET)
                .on("/" + directory.getName() + "/{path+}")
                .to(this, "serve"));
    }

    public Result serve() {
        File file = new File(directory, context().parameterFromPath("path"));
        if (!file.exists()) {
            return fromBundle(context().parameterFromPath("path"));
        } else {
            return ok(file);
        }

        //TODO add ETAG, and Cache configuration.
    }

    private Result fromBundle(String path) {
        Bundle[] bundles = context.getBundles();
        // Skip bundle 0;
        for (int i = 1; i <= bundles.length; i++) {
            URL url = bundles[i].getResource("/assets/" + path);
            if (url != null) {
                return ok(url);
            }
        }
        return notFound();
    }


}
