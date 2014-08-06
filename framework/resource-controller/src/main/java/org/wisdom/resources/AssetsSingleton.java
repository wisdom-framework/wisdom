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

import com.google.common.collect.ImmutableMap;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.asset.Asset;
import org.wisdom.api.asset.AssetProvider;
import org.wisdom.api.asset.Assets;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.http.*;
import org.wisdom.api.templates.Template;

import java.util.*;

/**
 * Implementation of the main asset control point.
 */
@Component
@Provides
@Instantiate
public class AssetsSingleton extends DefaultController implements Assets {

    private Collection<Asset<?>> cache = new ArrayList<>();

    @Requires(optional = true)
    AssetProvider[] providers;

    @Requires
    ApplicationConfiguration configuration;

    @Requires(filter = "(name=assets/list)")
    Template template;

    /**
     * Serves the asset list page, or a JSON form depending on the {@literal ACCEPT} header.
     * @return the page, the json form or a bad request. Bad request are returned in "PROD" mode.
     */
    @Route(method = HttpMethod.GET, uri = "/assets")
    public Result index() {
        if (configuration.isProd()) {
            // Dumping assets is not enabled in PROD mode,
            // returning a bad request result
            return badRequest("Sorry, no asset dump in PROD mode.");
        }

        if (cache.isEmpty()  || NOCACHE_VALUE.equalsIgnoreCase(context().header(CACHE_CONTROL))) {
            // Refresh the cache.
            all();
        }

        return Negotiation.accept(ImmutableMap.of(
                MimeTypes.HTML, ok(render(template, "assets", cache)),
                MimeTypes.JSON, ok(cache).json()
        ));
    }

    private List<Asset<?>> all() {
        List<Asset<?>> assets = new ArrayList<>();
        for (AssetProvider p : providers) {
            assets.addAll(p.assets());
        }
        Collections.sort(assets, new Comparator<Asset<?>>() {
            @Override
            public int compare(Asset<?> o1, Asset<?> o2) {
                return o1.getPath().compareTo(o2.getPath());
            }
        });
        // Update the cache.
        cache = assets;
        return assets;
    }

    /**
     * Gets the path to retrieve the asset identified by its file name.
     *
     * @param path the path
     * @return the path to retrieve the asset or {@literal null} if not found. If there are several matches,
     * return the first one.
     */
    @Override
    public Asset assetAt(String path) {
        // The simplest implementation is to delegate to the provider and see if they return something.
        for (AssetProvider provider : providers) {
            Asset asset = provider.assetAt(path);
            if (asset != null) {
                return asset;
            }
        }
        return null;
    }

    /**
     * @return the list of all assets currently available. This lookup is done on demand,
     * ignoring cached value. So it can be very expensive.
     */
    @Override
    public Collection<Asset<?>> assets() {
        return new ArrayList<>(all());
    }

    /**
     * Retrieve the list of all asset currently available on the platform.
     *
     * @param useCache whether or not we can returned a cached version of the result. This cache may contain
     *                 invalidated data or may not contain all available assets.
     * @return the list of assets
     */
    @Override
    public Collection<Asset<?>> assets(boolean useCache) {
        if (useCache && !cache.isEmpty()) {
            return new ArrayList<>(cache);
        }
        return assets();

    }
}
