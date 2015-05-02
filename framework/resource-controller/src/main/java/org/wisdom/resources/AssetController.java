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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.net.URL;
import java.util.*;

/**
 * A controller publishing the resources found in a folder and in bundles.
 */
@Component(immediate = true)
@Provides
public class AssetController extends DefaultController implements AssetProvider {

    public static final Logger LOGGER = LoggerFactory.getLogger(AssetController.class);
    /**
     * The default instance handle the `assets` folder.
     */
    private final File directory;

    @Context
    private BundleContext context;

    private final boolean manageAssetsFromBundles;
    private final String pathInBundles;
    private final String root;

    @Requires
    ApplicationConfiguration configuration;
    @Requires
    Crypto crypto;

    /**
     * Constructor used for testing purpose only.
     *
     * @param configuration           the configuration service
     * @param crypto                  the crypto service
     * @param bc                      the bundle context
     * @param path                    the external FS path
     * @param manageAssetsFromBundles whether or not it should handle embedded assets
     * @param pathInBundles           the path in the bundle if enabled
     * @param url                     the root url where assets are served.
     */
    public AssetController(
            ApplicationConfiguration configuration,
            Crypto crypto,
            BundleContext bc,
            String path,
            boolean manageAssetsFromBundles,
            String pathInBundles,
            String url) {

        this.configuration = configuration;
        this.crypto = crypto;
        this.context = bc;

        if (!Strings.isNullOrEmpty(path)) {
            this.directory = new File(configuration.getBaseDir(), path); //NOSONAR - injected service.
        } else {
            this.directory = null;
        }
        this.manageAssetsFromBundles = manageAssetsFromBundles;
        this.pathInBundles = computePathInBundle(pathInBundles);
        this.root = computeRoot(url);
    }

    /**
     * Creates an instance of the asset controller. This constructor is used by iPOJO.
     *
     * @param path                    the path of the directory containing external asset.
     * @param manageAssetsFromBundles do we handle the assets contained in bundles
     * @param pathInBundles           the path in the bundle
     * @param url                     the root url
     */
    public AssetController(@Property(name = "path", value = "") String path,
                           @Property(name = "manageAssetsFromBundles", value = "false") boolean manageAssetsFromBundles,
                           @Property(name = "pathInBundles", value = "/assets/") String pathInBundles,
                           @Property(name = "url", value = "/assets") String url) {
        if (!Strings.isNullOrEmpty(path)) {
            this.directory = new File(configuration.getBaseDir(), path); //NOSONAR - injected service.
        } else {
            this.directory = null;
        }
        this.manageAssetsFromBundles = manageAssetsFromBundles;
        this.pathInBundles = computePathInBundle(pathInBundles);
        this.root = computeRoot(url);

        if (manageAssetsFromBundles) {
            LOGGER.info("Serving assets from bundles ({}) on {}",
                    pathInBundles, root);
        }
        LOGGER.info("Serving assets from file system ({}) on {}",
                path, root);
    }

    private String computeRoot(String url) {
        if (url != null) {
            if (!url.startsWith("/")) {
                throw new IllegalArgumentException("The `url` property must start with `/`");
            }
            return url;
        } else {
            return "/assets";
        }
    }

    protected String computePathInBundle(String pathInBundles) {
        if (manageAssetsFromBundles && !Strings.isNullOrEmpty(pathInBundles)) {
            if (!pathInBundles.startsWith("/") || !pathInBundles.endsWith("/")) {
                throw new IllegalArgumentException("The `pathInBundles` property must start and end with `/`");
            }
            return pathInBundles;
        } else {
            return "/assets/";
        }
    }

    /**
     * @return the 'serve' routes.
     */
    @Override
    public List<Route> routes() {
        return ImmutableList.of(new RouteBuilder()
                .route(HttpMethod.GET)
                .on(root + "/{path+}")
                .to(this, "serve"));
    }

    /**
     * @return the result serving the asset.
     */
    public Result serve() {
        String path = context().parameterFromPath("path");
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        Asset<?> asset = getAssetFromFS(path);
        if (asset == null && manageAssetsFromBundles) {
            asset = getAssetFromBundle(path);
        }

        if (asset != null) {
            return CacheUtils.fromAsset(context(), asset, configuration);
        }
        return notFound();
    }

    private Asset<URL> getAssetFromBundle(String path) {
        Bundle[] bundles = context.getBundles();
        // Skip bundle 0 as it cannot contain assets
        for (int i = 1; i < bundles.length; i++) {
            URL url = bundles[i].getResource(pathInBundles + path);
            if (url != null) {
                return new DefaultAsset<>(root + "/" + path, url, bundles[i].getSymbolicName(),
                        bundles[i].getLastModified(),
                        CacheUtils.computeEtag(bundles[i].getLastModified(), configuration, crypto));
            }
        }
        return null; // Asset not found, just returning null.
    }

    private Asset<File> getAssetFromFS(String path) {
        if (directory == null) {
            return null;
        }
        File file = new File(directory, path);
        if (!file.exists()) {
            return null;
        }
        return new DefaultAsset<>(root + "/" + path, file, file.getAbsolutePath(), file.lastModified(),
                CacheUtils.computeEtag(file.lastModified(), configuration, crypto));
    }

    /**
     * @return the list of provided assets.
     */
    @Override
    public Collection<Asset<?>> assets() {
        Map<String, Asset<?>> map = new HashMap<>();
        if (directory != null && directory.isDirectory()) {
            // First insert the FS assets
            // For this iterate over the file present on the file system.
            Collection<File> files = FileUtils.listFiles(directory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
            for (File file : files) {
                if (file.getName().startsWith(".")) {
                    // Skip file starting with . - there are hidden.
                    continue;
                }
                if (file.getName().startsWith(".")) {
                    // Skip file starting with . - there are hidden.
                    continue;
                }

                // The path is computed as follows:
                // root (ending with /) and the path of the file relative to the directory. As these path may contain
                // \ on Windows we replace them by /.
                String path = root
                        + file.getAbsolutePath().substring(directory.getAbsolutePath().length()).replace("\\", "/");
                // TODO Do we really need computing the ETAG here ?
                map.put(path, new DefaultAsset<>(path, file, file.getAbsolutePath(), file.lastModified(), null));
            }
        }

        if (!manageAssetsFromBundles) {
            return map.values();
        }

        // No add the bundle things.
        Bundle[] bundles = context.getBundles();
        // Skip bundle 0
        for (int i = 1; i < bundles.length; i++) {
            // Remove the last "/" - we are sure to have one.
            URL root = bundles[i].getEntry(pathInBundles.substring(0, pathInBundles.length() - 1));
            Enumeration<URL> urls = bundles[i].findEntries(pathInBundles, "*", true);

            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    String path = url.toExternalForm().substring(root.toExternalForm().length());
                    if (path.startsWith("/")) {
                        path = this.root + path;
                    } else {
                        path = this.root + "/" + path;
                    }
                    if (!map.containsKey(path)) {
                        // We should not replace assets overridden by files.
                        map.put(path, new DefaultAsset<>(path, url, url.toExternalForm(),
                                bundles[i].getLastModified(), null));
                    }
                }
            }
        }

        return map.values();
    }

    /**
     * Retrieves an asset.
     *
     * @param path the asset path
     * @return the Asset object, or {@literal null} if the current provider can't serve this asset.
     */
    @Override
    public Asset<?> assetAt(String path) {
        Asset<?> asset = getAssetFromFS(path);
        if (asset == null) {
            asset = getAssetFromBundle(path);
        }
        return asset;
    }
}
