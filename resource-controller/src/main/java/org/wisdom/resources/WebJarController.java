package org.wisdom.resources;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.TreeMultimap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * A controller serving WebJars.
 * WebJars (http://www.webjars.org) are jar files embedding web resources.
 * <p/>
 * The Wisdom Maven plugin copies these files to 'assets/libs', so this controller just load the requested
 * resources from this place. contained resources are served from:
 * <ol>
 * <li>/libs/libraryname-version/path</li>
 * <li>/libs/libraryname/path</li>
 * <li>/libs/path</li>
 * </ol>
 */
@Component
@Provides(specifications = Controller.class)
@Instantiate(name = "WebJarResourceController")
public class WebJarController extends DefaultController {

    /**
     * The default instance handle the `assets/libs` folder.
     */
    private final File directory;

    private static final Logger LOGGER = LoggerFactory.getLogger(WebJarController.class);

    private TreeMultimap<String, File> index = TreeMultimap.create();
    private List<WebJarLib> libs = new ArrayList<>();

    @Requires
    Crypto crypto;

    @Requires
    ApplicationConfiguration configuration;

    public WebJarController(@Property(value = "assets/libs") String path) {
        directory = new File(configuration.getBaseDir(), path);

        // Build index from files
        for (File dir : FileUtils.listFilesAndDirs(directory, FalseFileFilter.INSTANCE, TrueFileFilter.TRUE)) {
            String library = dir.getName();
            for (File ver : FileUtils.listFilesAndDirs(directory, FalseFileFilter.INSTANCE, TrueFileFilter.TRUE)) {
                String version = ver.getName();
                WebJarLib lib = new WebJarLib(library, version, ver);
                libs.add(lib);
                populateIndexForLibrary(lib);
            }
        }

        LOGGER.info("{} libraries embedded within web jars detected", libs.size());
        LOGGER.info("WebJar index built - {} files indexed", index.size());
    }

    private void populateIndexForLibrary(WebJarLib lib) {
        LOGGER.debug("Indexing files for WebJar library {}-{}", lib.name, lib.version);
        for (File file : FileUtils.listFiles(lib.root, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
            if (!file.isDirectory()) {
                index.put(file.getAbsolutePath().substring(lib.root.getAbsolutePath().length() + 1), file);
            }
        }
    }

    @Override
    public List<Route> routes() {
        return ImmutableList.of(
                new RouteBuilder()
                        .route(HttpMethod.GET)
                        .on("/" + "libs" + "/{path+}")
                        .to(this, "serve")
        );
    }


    public Result serve() {
        String path = context().parameterFromPath("path");

        Collection<File> files = index.get(path);

        if (files.size() == 1) {
            // Perfect ! only one match
            return fromFile(files.iterator().next());
        } else if (files.size() > 1) {
            // Several candidates
            LOGGER.warn("Several candidates to match '{}' : {} - returning the first match", path, files);
            return fromFile(files.iterator().next());
        } else {
            // No direct match, try complete path
            File full = new File(directory, path);
            if (full.exists()) {
                // We have a full path (name/version/path)
                return fromFile(full);
            } else {
                // The version may have been omitted.
                // Try to extract the library name
                if (path.contains("/")) {
                    String library = path.substring(0, path.indexOf("/"));
                    String stripped = path.substring(path.indexOf("/") + 1);
                    File file = getFileFromLibrary(library, stripped);
                    if (file == null) {
                        return notFound();
                    } else {
                        return fromFile(file);
                    }
                } else {
                    return notFound();
                }
            }
        }
    }

    private File getFileFromLibrary(String library, String stripped) {
        for (WebJarLib lib : libs) {
            // We are sure that stripped does not contains the version, because it would have been catch by the full
            // path check, so stripped is the path within the module.
            if (lib.name.equals(library) && lib.contains(stripped)) {
                return lib.get(stripped);
            }
        }
        return null;
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

    /**
     * Computes the ETAG value based on the last modification date passed as parameter
     *
     * @param lastModification the last modification (must be valid)
     * @return the encoded etag
     */
    public String computeEtag(long lastModification) {
        boolean useEtag = configuration.getBooleanWithDefault(ResourceController.HTTP_USE_ETAG, ResourceController.HTTP_USE_ETAG_DEFAULT);
        if (!useEtag) {
            return null;
        }
        String raw = Long.toString(lastModification);
        return crypto.hexSHA1(raw);
    }

    public void addCacheControlAndEtagToResult(Result result, String etag) {
        String maxAge = configuration.getWithDefault(ResourceController.HTTP_CACHE_CONTROL_MAX_AGE,
                ResourceController.HTTP_CACHE_CONTROL_DEFAULT);

        if ("0".equals(maxAge)) {
            result.with(HeaderNames.CACHE_CONTROL, "no-cache");
        } else {
            result.with(HeaderNames.CACHE_CONTROL, "max-age=" + maxAge);
        }

        // Use etag on demand:
        boolean useEtag = configuration.getBooleanWithDefault(ResourceController.HTTP_USE_ETAG, ResourceController.HTTP_USE_ETAG_DEFAULT);

        if (useEtag) {
            result.with(HeaderNames.ETAG, etag);
        }
    }

    private class WebJarLib {

        final File root;
        final String name;
        final String version;


        private WebJarLib(String name, String version, File root) {
            this.root = root;
            this.name = name;
            this.version = version;
        }

        public boolean contains(String path) {
            return new File(root, path).isFile();
        }

        public File get(String path) {
            return new File(root, path);
        }
    }

}
