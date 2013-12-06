package org.wisdom.api.cache;

import org.joda.time.Duration;

/**
 * Interface of the case service.
 */
public interface Cache {

    /**
     * Sets a value into the cache.
     *
     * @param key Item key.
     * @param value Item value.
     * @param expiration Expiration time in seconds (0 second means eternity).
     */
    public void set(String key, Object value, int expiration);

    /**
     * Sets a value into the cache.
     *
     * @param key Item key.
     * @param value Item value.
     * @param expiration Expiration time.
     */
    public void set(String key, Object value, Duration expiration);

    /**
     * Retrieves a value from the cache.
     *
     * @param key Item key.
     * @return the cached value, {@literal null} if not cached.
     */
    public Object get(String key);

    /**
     * Removes a value from the cache.
     *
     * @param key Item key
     * @return {@literal true} if the value was effectively removed form the cache, {@literal false} otherwise.
     */
    public boolean remove(String key);
}
