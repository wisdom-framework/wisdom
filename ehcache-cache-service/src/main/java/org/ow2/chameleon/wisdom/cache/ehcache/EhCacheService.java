package org.ow2.chameleon.wisdom.cache.ehcache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.felix.ipojo.annotations.*;
import org.joda.time.Duration;
import org.ow2.chameleon.wisdom.api.cache.Cache;
import org.ow2.chameleon.wisdom.api.configuration.ApplicationConfiguration;

import java.io.File;
import java.net.URL;

/**
 * An implementation of the cache service based on EhCache.
 */
@Component(immediate = true)
@Provides
@Instantiate
public class EhCacheService implements Cache {

    public static final String CUSTOM_CONFIGURATION = "ehcache.xml";
    public static final String INTERNAL_CONFIGURATION = "org/ow2/chameleon/wisdom/cache/ehcache/ehcache-default.xml";
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
            manager.addCache("wisdom");
            cache = manager.getCache("wisdom");
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }


    }

    @Invalidate
    public void stop() {
        manager.removeCache("wisdom");
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
