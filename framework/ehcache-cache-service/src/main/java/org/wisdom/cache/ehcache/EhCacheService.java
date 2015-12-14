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
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.wisdom.api.cache.Cache;
import org.wisdom.api.configuration.ApplicationConfiguration;

import java.io.File;
import java.net.URL;
import java.util.Hashtable;

/**
 * An implementation of the cache service based on EhCache.
 */
@Component(immediate = true)
@Instantiate
public class EhCacheService implements Cache {

    private static final String WISDOM_KEY = "wisdom";

    /**
     * The custom configuration path.
     * To customize the ehcache configuration, creates the 'ehcache.xml' file in the 'conf' directory of the Wisdom
     * server, or in the 'src/main/configuration' directory of your project.
     */
    public static final String CUSTOM_CONFIGURATION = "conf/ehcache.xml";

    /**
     * The path to the internal configuration.
     */
    public static final String INTERNAL_CONFIGURATION = "org/wisdom/cache/ehcache/ehcache-default.xml";

    private net.sf.ehcache.Cache cache;
    private CacheManager manager;

    @Requires
    ApplicationConfiguration configuration;

    @Context
    BundleContext context;
    ServiceRegistration<Cache> registration;


    /**
     * Creates the EhCache-based implementation of the Cache Service.
     */
    @Validate
    public void start() {
        Boolean enabled = configuration.getBooleanWithDefault("ehcache.enabled", true);
        if (!enabled) {
            return;
        }

        File config = new File(configuration.getBaseDir(), CUSTOM_CONFIGURATION);
        final ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            if (config.isFile()) {
                manager = CacheManager.create(config.getAbsolutePath());
            } else {
                URL url = EhCacheService.class.getClassLoader().getResource(INTERNAL_CONFIGURATION);
                if (url != null) {
                    manager = CacheManager.create(url);
                } else {
                    throw new ExceptionInInitializerError("Cannot instantiate EhCache, " +
                            "cannot load " + INTERNAL_CONFIGURATION + " file");
                }
            }
            manager.addCache(WISDOM_KEY);
            cache = manager.getCache(WISDOM_KEY);

            registration = context.registerService(Cache.class, this, new Hashtable<String, Object>());
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }


    }


    /**
     * Cleans up everything.
     */
    @Invalidate
    public void stop() {
        if (registration != null) {
            registration.unregister();
            registration = null;
        }
        if (manager != null) {
            manager.removeCache(WISDOM_KEY);
        }
    }

    /**
     * Adds an entry in the cache.
     *
     * @param key        Item key.
     * @param value      Item value.
     * @param expiration Expiration time in seconds (0 second means eternity).
     */
    @Override
    public void set(String key, Object value, int expiration) {
        Element element = new Element(key, value);
        if (expiration == 0) {
            element.setEternal(true);
        }
        element.setTimeToLive(expiration);
        cache.put(element);
    }

    /**
     * Adds an entry in the cache.
     *
     * @param key        Item key.
     * @param value      Item value.
     * @param expiration Expiration time.
     */
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

    /**
     * Gets an entry from the cache.
     *
     * @param key Item key.
     * @return the stored object, {@literal null} if none of expired.
     */
    @Override
    public Object get(String key) {
        Element element = cache.get(key);
        if (element != null) {
            return element.getObjectValue();
        }
        return null;
    }

    /**
     * Removes an object from the cache.
     *
     * @param key Item key
     * @return {@literal true} if the object was removed, {@literal false} otherwise.
     */
    @Override
    public boolean remove(String key) {
        return cache.remove(key);
    }
}
