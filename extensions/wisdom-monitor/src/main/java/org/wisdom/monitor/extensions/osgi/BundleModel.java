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
package org.wisdom.monitor.extensions.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.util.*;

/**
 * Bundle Model used from the template.
 * Instances are very ephemera on purpose to avoid leaks.
 */
public class BundleModel {

    /**
     * Creates a bundle model from the given bundle.
     *
     * @param bundle the bundle.
     */
    public BundleModel(Bundle bundle) {
        this.bundle = bundle;
    }

    /**
     * Creates the list of bundle models based on the bundle currently deployed.
     *
     * @param context the bundle context.
     * @return the list of models
     */
    public static List<BundleModel> bundles(BundleContext context) {
        List<BundleModel> bundles = new ArrayList<>();
        for (Bundle bundle : context.getBundles()) {
            bundles.add(new BundleModel(bundle));
        }
        return bundles;
    }

    /**
     * The represented bundle.
     */
    private final Bundle bundle;

    /**
     * @return the bundle state.
     */
    public String getState() {
        return BundleStates.from(bundle);
    }

    /**
     * @return the bundle id.
     */
    public long getId() {
        return bundle.getBundleId();
    }

    /**
     * @return the display name of the bundle.
     */
    public String getName() {
        return bundle.getSymbolicName() + " - " + bundle.getVersion();
    }

    /**
     * @return the bundle's headers.
     */
    public Map<String, String> getHeaders() {
        Map<String, String> map = new TreeMap<>();
        Enumeration<String> enumeration = bundle.getHeaders().keys();
        while (enumeration.hasMoreElements()) {
            String key = enumeration.nextElement();
            map.put(key, bundle.getHeaders().get(key));
        }
        return map;
    }
}
