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
package org.wisdom.api.asset;

import java.util.Collection;

/**
 * Assets defines a layer to manipulate and retrieve asset currently available in the system.
 */
public interface Assets {

    /**
     * Gets the path to retrieve the asset identified by its file name.
     *
     * @param path the path
     * @return the path to retrieve the asset or {@literal null} if not found. If there are several matches,
     * return the first one.
     */
    Asset assetAt(String path);

    /**
     * @return the list of all assets currently available. This lookup is done on demand,
     * ignoring cached value. So it can be very expensive.
     */
    Collection<Asset<?>> assets();

    /**
     * Retrieve the list of all asset currently available on the platform.
     *
     * @param useCache whether or not we can returned a cached version of the result. This cache may contain
     *                 invalidated data or may not contain all available assets.
     * @return the list of assets
     */
    Collection<Asset<?>> assets(boolean useCache);

}
