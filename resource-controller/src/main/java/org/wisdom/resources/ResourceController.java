package org.wisdom.resources;

import com.google.common.collect.ImmutableList;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.crypto.Crypto;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.RouteBuilder;

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

    /**
     * The default instance handle the `assets` folder.
     */
    private final File directory;
    private final BundleContext context;
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceController.class);
    @Requires
    ApplicationConfiguration configuration;
    @Requires
    Crypto crypto;


    public ResourceController(BundleContext bc, @Property(value = "assets") String path) {
        directory = new File(configuration.getBaseDir(), path);
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
            return CacheUtils.fromFile(file, context(), configuration, crypto);
        }
    }


    public long getLastModified(Bundle bundle) {
        return bundle.getLastModified();
    }

    private Result fromBundle(String path) {
        Bundle[] bundles = context.getBundles();
        // Skip bundle 0
        for (int i = 1; i < bundles.length; i++) {
            URL url = bundles[i].getResource("/assets/" + path);
            if (url != null) {
                long lastModified = getLastModified(bundles[i]);
                String etag = CacheUtils.computeEtag(lastModified, configuration, crypto);
                if (!CacheUtils.isModified(context(), lastModified, etag)) {
                    return new Result(NOT_MODIFIED);
                } else {
                    Result result = ok(url);
                    CacheUtils.addCacheControlAndEtagToResult(result, etag, configuration);
                    return result;
                }
            }
        }
        return notFound();
    }

}
