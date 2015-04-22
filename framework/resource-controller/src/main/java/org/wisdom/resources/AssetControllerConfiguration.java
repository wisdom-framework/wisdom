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

import org.apache.felix.ipojo.configuration.Configuration;
import org.apache.felix.ipojo.configuration.Instance;

/**
 * Declares the instance of {@link org.wisdom.resources.AssetController} handling the {@link assets} directory and assets from bundles.
 */
@Configuration
public class AssetControllerConfiguration {

    /**
     * The asset controller instance configuration.
     * By default is is looking the external assets in the `assets` directory and manage embedded assets from the
     * `/assets` directory (from bundles).
     */
    @SuppressWarnings("UnusedDeclaration")
    public static Instance instance = Instance.instance()
            .of(AssetController.class)
            .named("PublicAssetController")
            .with("path").setto("assets")
            .with("manageAssetsFromBundles").setto(true);

}
