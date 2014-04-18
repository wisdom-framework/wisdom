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
 * A controller publishing the resources found in a folder and in bundles.
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

    private Result fromBundle(String path) {
        Bundle[] bundles = context.getBundles();
        // Skip bundle 0
        for (int i = 1; i < bundles.length; i++) {
            URL url = bundles[i].getResource("/assets/" + path);
            if (url != null) {
                return CacheUtils.fromBundle(bundles[i], url, context(), configuration, crypto);
            }
        }
        return notFound();
    }

}
