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
package org.wisdom.cache.ehcache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.felix.ipojo.annotations.*;
import org.joda.time.Duration;
import org.wisdom.api.cache.Cache;
import org.wisdom.api.configuration.ApplicationConfiguration;

import java.io.File;
import java.net.URL;

/**
 * An implementation of the cache service based on EhCache.
 */
@Component(immediate = true)
@Provides
@Instantiate
public class EhCacheService implements Cache {
    
    private static final String WISDOM_KEY = "wisdom";

    public static final String CUSTOM_CONFIGURATION = "ehcache.xml";
    public static final String INTERNAL_CONFIGURATION = "org/wisdom/cache/ehcache/ehcache-default.xml";
    private net.sf.ehcache.Cache cache;
    private CacheManager manager;

    public EhCacheService(@Requires ApplicationConfiguration configuration) {
        File config = new File(configuration.getBaseDir(), CUSTOM_CONFIGURATION);

        if (config.isFile()) {
            manager = CacheManager.create(config.getAbsolutePath());
        } else {
            URL url = EhCacheService.class.getClassLoader().getResource(INTERNAL_CONFIGURATION);
            if (url != null) {
                final ClassLoader original = Thread.currentThread().getContextClassLoader();
                try {
                    Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
                    manager = CacheManager.create(url);
                } finally {
                    Thread.currentThread().setContextClassLoader(original);
                }
            } else {
                throw new ExceptionInInitializerError("Cannot instantiate EhCache, " +
                        "cannot load " + INTERNAL_CONFIGURATION + " file");
            }
        }

        final ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            manager.addCache(WISDOM_KEY);
            cache = manager.getCache(WISDOM_KEY);
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }


    }

    @Invalidate
    public void stop() {
        manager.removeCache(WISDOM_KEY);
    }

    @Override
    public void set(String key, Object value, int expiration) {
        Element element = new Element(key, value);
        if (expiration == 0) {
            element.setEternal(true);
        }
        element.setTimeToLive(expiration);
        cache.put(element);
    }

    @Override
    public void set(String key, Object value, Duration expiration) {
        Element element = new Element(key, value);
        if (expiration == null) {
            element.setEternal(true);
        } else {
            element.setTimeToLive((int) expiration.getStandardSeconds());
        }
        cache.put(element);
    }

    @Override
    public Object get(String key) {
        Element element = cache.get(key);
        if (element != null) {
            return element.getObjectValue();
        }
        return null;
    }

    @Override
    public boolean remove(String key) {
        return cache.remove(key);
    }
}
