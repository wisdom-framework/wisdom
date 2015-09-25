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
package org.wisdom.resources;

import com.google.common.collect.ImmutableList;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;
import org.wisdom.api.asset.Asset;
import org.wisdom.api.asset.AssetProvider;
import org.wisdom.api.asset.DefaultAsset;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.crypto.Crypto;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.RouteBuilder;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A controller serving WebJars.
 * WebJars (http://www.webjars.org) are jar files embedding web resources.
 * <p>
 * The Wisdom Maven plugin copies these files to 'assets/libs' and from bundles.Web Jar resources are served
 * from:
 * <ol>
 * <li>/libs/libraryname/version/path</li>
 * <li>/libs/libraryname/path</li>
 * <li>/libs/path</li>
 * </ol>
 */
@Component(immediate = true)
@Provides(specifications = {Controller.class, AssetProvider.class})
@Instantiate(name = "WebJarResourceController")
public class WebJarController extends DefaultController implements BundleTrackerCustomizer<List<BundleWebJarLib>>,
        AssetProvider {

    /**
     * A regex checking the the given path is the root of a Web Jar Lib.
     */
    public static final Pattern WEBJAR_ROOT_REGEX = Pattern.compile(".*META-INF/resources/webjars/([^/]+)/([^/]+)/");

    /**
     * The path containing the web libraries in a bundle.
     */
    public static final String WEBJAR_LOCATION = "META-INF/resources/webjars/";

    /**
     * A regex extracting the library name and version from Zip Entry names.
     */
    public static final Pattern WEBJAR_REGEX = Pattern.compile(".*META-INF/resources/webjars/([^/]+)/([^/]+)/.*");

    /**
     * The RegEx pattern to identify the shape of the url.
     */
    static Pattern PATTERN = Pattern.compile("([^/]+)(/([^/]+))?/(.*)");

    /**
     * The instance of deployer.
     */
    private final WebJarDeployer deployer;

    /**
     * The default instance handle the `assets/libs` folder.
     */
    private final File directory;

    private final BundleTracker<List<BundleWebJarLib>> tracker;

    Set<WebJarLib> libraries = new TreeSet<>(new Comparator<WebJarLib>() {
        @Override
        public int compare(WebJarLib o1, WebJarLib o2) {
            if (o1 instanceof FileWebJarLib && o2 instanceof BundleWebJarLib) {
                return -1;
            }
            if (o1 instanceof BundleWebJarLib && o2 instanceof FileWebJarLib) {
                return 1;
            }
            return o1.toString().compareTo(o2.toString());
        }
    });

    @Requires
    Crypto crypto;

    @Requires
    ApplicationConfiguration configuration;

    /**
     * Constructor used for testing purpose only.
     *
     * @param crypto        the crypto service
     * @param configuration the configuration
     * @param path          the path (relative to the configuration's base dir)
     */
    WebJarController(Crypto crypto, ApplicationConfiguration configuration, String path) {
        this.crypto = crypto;
        this.configuration = configuration;
        directory = new File(configuration.getBaseDir(), path);  //NOSONAR Injected field
        tracker = null;
        deployer = null;
        start();
    }

    /**
     * Creates the controller serving resources embedded in WebJars.
     *
     * @param context the bundle context
     * @param path    the path (relative to the configuration's base dir) in which exploded webjars are
     */
    public WebJarController(@Context BundleContext context, @Property(value = "assets/libs",
            name = "path") String path) {
        directory = new File(configuration.getBaseDir(), path); //NOSONAR Injected field
        tracker = new BundleTracker<>(context, Bundle.ACTIVE, this);
        deployer = new WebJarDeployer(context, this);
    }

    /**
     * Starts the controllers.
     */
    @Validate
    public void start() {
        if (directory.isDirectory()) {
            buildFileIndex();
        }
        if (tracker != null) {
            tracker.open();
        }
        if (deployer != null) {
            deployer.start();
        }
    }

    /**
     * Stops the controllers.
     */
    @Invalidate
    public void stop() {
        if (deployer != null) {
            deployer.stop();
        }
        if (tracker != null) {
            tracker.close();
        }
        libraries.clear();
    }

    private void buildFileIndex() {
        if (directory.listFiles() == null) {
            // Empty.
            return;
        }

        FileFilter isDirectory = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        };
        File[] names = directory.listFiles(isDirectory);

        if (names == null) {
            // names is null if directory does not denote a valid file.
            return;
        }

        // Build index from files
        synchronized (this) {
            for (File dir : names) {
                String library = dir.getName();
                File[] versions = dir.listFiles(isDirectory);
                if (versions == null) {
                    // versions is null if dir does not denote a valid file.
                    continue;
                }
                for (File ver : versions) {
                    String version = ver.getName();
                    FileWebJarLib lib = new FileWebJarLib(library, version, ver);
                    logger().info("Exploded web jar libraries detected : {}", lib);
                    libraries.add(lib);
                }
            }
        }

    }

    int indexSize() {
        int count = 0;
        for (WebJarLib lib : libs()) {
            count += lib.names().size();
        }
        return count;
    }

    synchronized List<WebJarLib> libs() {
        return new ArrayList<>(libraries);
    }

    private List<WebJarLib> findLibsContaining(String path) {
        List<WebJarLib> list = new ArrayList<>();
        for (WebJarLib lib : libs()) {
            if (lib.contains(path)) {
                list.add(lib);
            }
        }
        return list;
    }

    private List<WebJarLib> find(String name) {
        List<WebJarLib> list = new ArrayList<>();
        for (WebJarLib lib : libs()) {
            if (lib.name.equals(name)) {
                list.add(lib);
            }
        }
        return list;
    }

    /**
     * @return the router serving the assets embedded in WebJars.
     */
    @Override
    public List<Route> routes() {
        return ImmutableList.of(
                new RouteBuilder()
                        .route(HttpMethod.GET)
                        .on("/" + "libs" + "/{path+}")
                        .to(this, "serve")
        );
    }

    /**
     * @return the asset embedded in a web jar.
     */
    public Result serve() {
        String path = context().parameterFromPath("path");
        if (path == null) {
            logger().error("Cannot server Web Jar resource : no path");
            return badRequest();
        }

        Asset<?> asset = assetAt(path);
        if (asset == null) {
            return notFound();
        }

        return CacheUtils.fromAsset(context(), asset, configuration);
    }

    private WebJarLib find(String name, String version) {
        for (WebJarLib lib : libs()) {
            if (lib.name.equals(name) && lib.version.equals(version)) {
                return lib;
            }
        }
        return null;
    }

    /**
     * A bundle just arrived (and / or just becomes ACTIVE). We need to check if it contains 'webjar libraries'.
     *
     * @param bundle      the bundle
     * @param bundleEvent the event
     * @return the list of webjar found in the bundle, empty if none.
     */
    @Override
    public synchronized List<BundleWebJarLib> addingBundle(Bundle bundle, BundleEvent bundleEvent) {
        Enumeration<URL> e = bundle.findEntries(WEBJAR_LOCATION, "*", true);
        if (e == null) {
            // No match
            return Collections.emptyList();
        }
        List<BundleWebJarLib> list = new ArrayList<>();
        while (e.hasMoreElements()) {
            String path = e.nextElement().getPath();
            if (path.endsWith("/")) {
                Matcher matcher = WEBJAR_ROOT_REGEX.matcher(path);
                if (matcher.matches()) {
                    String name = matcher.group(1);
                    String version = matcher.group(2);
                    final BundleWebJarLib lib = new BundleWebJarLib(name, version, bundle);
                    logger().info("Web Jar library ({}) found in {} [{}]", lib,
                            bundle.getSymbolicName(),
                            bundle.getBundleId());
                    list.add(lib);
                }
            }
        }

        addWebJarLibs(list);

        return list;
    }

    /**
     * Adds the given set of {@link WebJarLib} to the managed libraries.
     * @param list the set to add
     */
    public void addWebJarLibs(Collection<? extends WebJarLib> list) {
        synchronized (this) {
            libraries.addAll(list);
        }
    }

    /**
     * A bundle is updated.
     *
     * @param bundle      the bundle
     * @param bundleEvent the event
     * @param webJarLibs  the webjars that were embedded in the previous version of the bundle.
     */
    @Override
    public void modifiedBundle(Bundle bundle, BundleEvent bundleEvent, List<BundleWebJarLib> webJarLibs) {
        // Remove all WebJars from the given bundle, and then read tem.
        synchronized (this) {
            removedBundle(bundle, bundleEvent, webJarLibs);
            addingBundle(bundle, bundleEvent);
        }
    }

    /**
     * A bundle is removed.
     *
     * @param bundle      the bundle
     * @param bundleEvent the event
     * @param webJarLibs  the webjars that were embedded in the bundle.
     */
    @Override
    public void removedBundle(Bundle bundle, BundleEvent bundleEvent, List<BundleWebJarLib> webJarLibs) {
        removeWebJarLibs(webJarLibs);
    }

    public void removeWebJarLibs(Collection<? extends WebJarLib> webJarLibs) {
        synchronized (this) {
            libraries.removeAll(webJarLibs);
        }
    }

    /**
     * @return the list of provided assets.
     */
    @Override
    public Collection<Asset<?>> assets() {
        List<Asset<?>> assets = new ArrayList<>();
        for (WebJarLib lib : libraries) {
            for (String path : lib.names()) {
                if (path.endsWith("/") || path.startsWith(".")) {
                    continue;
                }
                String url = "/libs/" + lib.name + "/" + lib.version + "/" + path;
                DefaultAsset<?> asset = new DefaultAsset<>(url, lib.get(path),
                        lib.toString(),
                        lib.lastModified(),
                        null);
                assets.add(asset);
            }
        }
        return assets;
    }

    /**
     * Retrieves an asset.
     *
     * @param path the asset path
     * @return the Asset object, or {@literal null} if the current provider can't serve this asset.
     */
    @Override
    public Asset<?> assetAt(String path) {
        List<WebJarLib> candidates = findLibsContaining(path);

        if (candidates.size() == 1) {
            // Perfect ! only one match
            return new DefaultAsset<>(
                    "/libs/" + candidates.get(0).name + "/" + candidates.get(0).version + "/" + path,
                    candidates.get(0).get(path),
                    candidates.get(0).toString(),
                    candidates.get(0).lastModified(),
                    CacheUtils.computeEtag(candidates.get(0).lastModified(), configuration, crypto)
            );
        } else if (candidates.size() > 1) {
            // Several candidates
            logger().warn("{} WebJars provide '{}' - returning the one from {}-{}", candidates.size(), path,
                    candidates.get(0).name, candidates.get(0).version);
            return new DefaultAsset<>(
                    "/libs/" + candidates.get(0).name + "/" + candidates.get(0).version + "/" + path,
                    candidates.get(0).get(path),
                    candidates.get(0).toString(),
                    candidates.get(0).lastModified(),
                    CacheUtils.computeEtag(candidates.get(0).lastModified(), configuration, crypto)
            );
        } else {
            Matcher matcher = PATTERN.matcher(path);
            if (!matcher.matches()) {
                // It should have been handled by the path match.
                return null;
            }

            final String name = matcher.group(1);
            final String version = matcher.group(3);
            if (version != null) {
                String rel = matcher.group(4);
                // We have a name and a version
                // Try to find the matching library
                WebJarLib lib = find(name, version);
                if (lib != null) {
                    return new DefaultAsset<>(
                            rel,
                            lib.get(rel),
                            lib.toString(),
                            lib.lastModified(),
                            CacheUtils.computeEtag(lib.lastModified(), configuration, crypto)
                    );
                }
                // If not found, it may be because the version is not really the version but a segment of the path.
            }
            // If we reach this point it means that the name/version lookup has failed, try without the version
            String rel = matcher.group(4);
            if (version != null) {
                // We have a group 3
                rel = version + "/" + rel;
            }

            List<WebJarLib> libs = find(name);
            if (libs.size() == 1) {
                // Only on library has the given name
                if (libs.get(0).contains(rel)) {
                    WebJarLib lib = libs.get(0);
                    return new DefaultAsset<>(
                            "/libs/" + lib.name + "/" + lib.version + "/" + rel,
                            lib.get(rel),
                            lib.toString(),
                            lib.lastModified(),
                            CacheUtils.computeEtag(lib.lastModified(), configuration, crypto)
                    );
                }
            } else if (libs.size() > 1) {
                // Several candidates
                WebJarLib higher = null;
                ComparableVersion higherVersion = null;
                for (WebJarLib lib: libs) {
                    if (lib.contains(rel)) {
                        if(higher == null) {
                            higher = lib;
                            higherVersion = new ComparableVersion(higher.version);
                        } else {
                            ComparableVersion newVersion = new ComparableVersion(lib.version);
                            if(newVersion.compareTo(higherVersion) > 0) {
                                higher = lib;
                                higherVersion = new ComparableVersion(higher.version);
                            }
                        }
                    }
                }
                if(higher != null) {
                    logger().warn("{} WebJars match the request '{}' - returning the resource from {}-{}",
                            libs.size(), path, higher.name, higher.version);
                    return new DefaultAsset<>(
                            "/libs/" + higher.name + "/" + higher.version + "/" + rel,
                            higher.get(rel),
                            higher.toString(),
                            higher.lastModified(),
                            CacheUtils.computeEtag(higher.lastModified(), configuration, crypto)
                    );
                }

            }

            return null;
        }
    }
}
