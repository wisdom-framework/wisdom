package org.wisdom.resources;

import com.google.common.collect.ImmutableList;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.crypto.Crypto;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.HeaderNames;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.RouteBuilder;
import org.wisdom.api.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.List;

/**
 * A controller publishing the resources found in a folder and in bundles
 */
@Component
@Provides(specifications = Controller.class)
@Instantiate(name = "PublicResourceController")
public class ResourceController extends DefaultController {

    /**
     * Value to set max age in header. E.g. Cache-Control:max-age=XXXXXX
     */
    public final static String HTTP_CACHE_CONTROL_MAX_AGE = "http.cache_control_max_age";
    /**
     * Default value for Cache-Control http header when not set in application.conf
     */
    public final static String HTTP_CACHE_CONTROL_DEFAULT = "3600";
    /**
     * Enable / disable etag E.g. ETag:"f0680fd3"
     */
    public final static String HTTP_USE_ETAG = "http.useETag";
    /**
     * Default value / etag enabled by default.
     */
    public final static boolean HTTP_USE_ETAG_DEFAULT = true;

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
            return fromFile(file);
        }
    }

    private Result fromFile(File file) {
        long lastModified = file.lastModified();
        String etag = computeEtag(lastModified);
        if (!isModified(context(), lastModified, etag)) {
            return new Result(NOT_MODIFIED);
        } else {
            Result result = ok(file);
            addCacheControlAndEtagToResult(result, etag);
            return result;
        }
    }

    public boolean isModified(Context context, long lastModified, String etag) {
        // First check etag. Important, if there is an If-None-Match header, we MUST not check the
        // If-Modified-Since header, regardless of whether If-None-Match matches or not. This is in
        // accordance with section 14.26 of RFC2616.

        final String browserEtag = context.header(HeaderNames.IF_NONE_MATCH);
        if (browserEtag != null) {
            // We check the given etag against the given one.
            // If the given one is null, that means that etags are disabled.
            return !browserEtag.equals(etag);
        }

        // IF_NONE_MATCH not set, check IF_MODIFIED_SINCE
        final String ifModifiedSince = context.header(HeaderNames.IF_MODIFIED_SINCE);

        if (ifModifiedSince != null && lastModified > 0 && !ifModifiedSince.isEmpty()) {
            try {
                Date browserDate = DateUtil.parseHttpDateFormat(ifModifiedSince);
                if (browserDate.getTime() >= lastModified) {
                    return false;
                }
            } catch (IllegalArgumentException ex) {
                LOGGER.error("Cannot parse the data value from the " + IF_MODIFIED_SINCE + " value (" +
                        ifModifiedSince + ")", ex);
                return false;
            }
            return true;
        }
        return true;
    }

    public long getLastModified(File file) {
        if (file.isFile()) {
            return file.lastModified();
        } else {
            return 0L;
        }
    }

    public long getLastModified(Bundle bundle) {
        return bundle.getLastModified();
    }

    /**
     * Computes the ETAG value based on the last modification date passed as parameter
     *
     * @param lastModification the last modification (must be valid)
     * @return the encoded etag
     */
    public String computeEtag(long lastModification) {
        boolean useEtag = configuration.getBooleanWithDefault(HTTP_USE_ETAG, HTTP_USE_ETAG_DEFAULT);
        if (!useEtag) {
            return null;
        }
        String raw = Long.toString(lastModification);
        return crypto.hexSHA1(raw);
    }

    public void addCacheControlAndEtagToResult(Result result, String etag) {
        String maxAge = configuration.getWithDefault(HTTP_CACHE_CONTROL_MAX_AGE,
                HTTP_CACHE_CONTROL_DEFAULT);

        if ("0".equals(maxAge)) {
            result.with(HeaderNames.CACHE_CONTROL, "no-cache");
        } else {
            result.with(HeaderNames.CACHE_CONTROL, "max-age=" + maxAge);
        }

        // Use etag on demand:
        boolean useEtag = configuration.getBooleanWithDefault(HTTP_USE_ETAG, HTTP_USE_ETAG_DEFAULT);

        if (useEtag) {
            result.with(HeaderNames.ETAG, etag);
        }
    }

    private Result fromBundle(String path) {
        Bundle[] bundles = context.getBundles();
        // Skip bundle 0
        for (int i = 1; i < bundles.length; i++) {
            URL url = bundles[i].getResource("/assets/" + path);
            if (url != null) {
                long lastModified = getLastModified(bundles[i]);
                String etag = computeEtag(lastModified);
                if (!isModified(context(), lastModified, etag)) {
                    return new Result(NOT_MODIFIED);
                } else {
                    Result result = ok(url);
                    addCacheControlAndEtagToResult(result, etag);
                    return result;
                }
            }
        }
        return notFound();
    }

}
