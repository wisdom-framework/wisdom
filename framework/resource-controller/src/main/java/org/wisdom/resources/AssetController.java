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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
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
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

/**
 * A controller publishing the resources found in a folder and in bundles.
 */
@Component(immediate = true)
@Provides
public class AssetController extends DefaultController implements AssetProvider {

    /**
     * The default instance handle the `assets` folder.
     */
    private final File directory;
    private final BundleContext context;
    private final boolean manageAssetsFromBundles;
    @Requires
    ApplicationConfiguration configuration;
    @Requires
    Crypto crypto;



    /**
     * Creates an instance of the asset controller.
     * @param bc the bundle context
     * @param path the path of the directory containing external asset ("assets" by default).
     */
    public AssetController(BundleContext bc,
                           @Property(mandatory = true) String path,
                           @Property(value = "false") boolean manageAssetsFromBundles) {
        this.directory = new File(configuration.getBaseDir(), path);
        this.context = bc;
        this.manageAssetsFromBundles = manageAssetsFromBundles;
    }

    /**
     * @return the 'serve' routes.
     */
    @Override
    public List<Route> routes() {
        return ImmutableList.of(new RouteBuilder()
                .route(HttpMethod.GET)
                .on("/" + directory.getName() + "/{path+}")
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
        if (asset == null  && manageAssetsFromBundles) {
            asset = getAssetFromBundle(path);
        }

        if (asset != null) {
            return CacheUtils.fromAsset(context(), asset, configuration);
        }
        return notFound();
    }

    private Asset<URL> getAssetFromBundle(String path) {
        Bundle[] bundles = context.getBundles();
        // Skip bundle 0
        for (int i = 1; i < bundles.length; i++) {
            URL url = bundles[i].getResource("/assets/" + path);
            if (url != null) {
                return new DefaultAsset<>("/assets/" + path, url, bundles[i].getSymbolicName(),
                        bundles[i].getLastModified(),
                        CacheUtils.computeEtag(bundles[i].getLastModified(), configuration, crypto));
            }
        }
        return null; // NO FOUND;
    }

    private Asset<File> getAssetFromFS(String path) {
        File file = new File(directory, path);
        if (!file.exists()) {
            return null;
        }
        return new DefaultAsset<>("/assets/", file, file.getAbsolutePath(), file.lastModified(),
                CacheUtils.computeEtag(file.lastModified(), configuration, crypto));
    }

    /**
     * @return the list of provided assets.
     */
    @Override
    public Collection<Asset<?>> assets() {
        HashMap<String, Asset<?>> map = new HashMap<>();
        if (directory.isDirectory()) {
            // First insert the FS assets
            // For this iterate over the file present on the file system.
            Collection<File> files = FileUtils.listFiles(directory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
            for (File file : files) {
                if (file.getName().startsWith(".")) {
                    // Skip file starting with .
                    continue;
                }
                String path = "/assets" + file.getAbsolutePath().substring(directory.getAbsolutePath().length());
                // TODO Do we really need computing the ETAG here ?
                map.put(path, new DefaultAsset<>(path, file, file.getAbsolutePath(), file.lastModified(), null));
            }
        }

        if (! manageAssetsFromBundles) {
            return map.values();
        }

        // No add the bundle things.
        Bundle[] bundles = context.getBundles();
        // Skip bundle 0
        for (int i = 1; i < bundles.length; i++) {
            URL root = bundles[i].getEntry("/assets");
            Enumeration<URL> urls = bundles[i].findEntries("/assets/", "*", true);

            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    String path = url.toExternalForm().substring(root.toExternalForm().length());
                    if (path.startsWith("/")) {
                        path = "/assets" + path;
                    } else {
                        path = "/assets/" + path;
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
