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

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.crypto.Crypto;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.Result;

import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

/**
 * Represents and serves a web jar library embedded in a bundle.
 */
class BundleWebJarLib extends WebJarLib {

    public final Bundle bundle;

    public static final Logger LOGGER = LoggerFactory.getLogger(BundleWebJarLib.class);

    private Map<String, URL> index = new TreeMap<>();

    BundleWebJarLib(String name, String version, Bundle bundle) {
        super(name, version);
        this.bundle = bundle;
        index();
    }

    @Override
    public Collection<String> names() {
        return index.keySet();
    }

    public Result get(String path, Context context, ApplicationConfiguration configuration, Crypto crypto) {
        URL url = index.get(path);
        return CacheUtils.fromBundle(bundle, url, context, configuration, crypto);
    }

    @Override
    public Object get(String path) {
        return index.get(path);
    }

    @Override
    public long lastModified() {
        return bundle.getLastModified();
    }

    public Collection<String> resources() {
        return index.keySet();
    }

    private void index() {
        LOGGER.debug("Indexing files for WebJar library {}-{} contained in bundle {} [{}]", name, version,
                bundle.getSymbolicName(), bundle.getBundleId());

        Enumeration<URL> urls = bundle.findEntries(WebJarController.WEBJAR_LOCATION + name + "/" + version, "*", true);

        String root = "/" + WebJarController.WEBJAR_LOCATION + name + "/" + version;
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            if (url.getPath().startsWith(root) && url.getPath().length() > root.length()) {
                String path = url.getPath().substring(root.length() + 1);
                index.put(path, url);
            }
        }
    }

    @Override
    public String toString() {
        return super.toString() + "[" + bundle.getBundleId() + "]";
    }
}
