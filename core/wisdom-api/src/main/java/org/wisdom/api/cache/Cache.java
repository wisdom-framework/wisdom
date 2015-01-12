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
package org.wisdom.api.cache;

import org.joda.time.Duration;

/**
 * Interface of the case service.
 */
public interface Cache {

    /**
     * Sets a value into the cache.
     *
     * @param key        Item key.
     * @param value      Item value.
     * @param expiration Expiration time in seconds (0 second means eternity).
     * @param <T> the type of the value.
     */
    public <T> void set(String key, T value, int expiration);

    /**
     * Sets a value into the cache.
     *
     * @param key        Item key.
     * @param value      Item value.
     * @param expiration Expiration time.
     * @param <T> the type of the value.
     */
    public <T> void set(String key, T value, Duration expiration);

    /**
     * Retrieves a value from the cache.
     *
     * @param key Item key.
     * @param <T> the expected type of result.
     * @return the cached value, {@literal null} if not cached.
     */
    public <T> T get(String key);

    /**
     * Removes a value from the cache.
     *
     * @param key Item key
     * @return {@literal true} if the value was effectively removed form the cache, {@literal false} otherwise.
     */
    public boolean remove(String key);
}
