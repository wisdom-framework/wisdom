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
package org.wisdom.api.cookies;

import org.wisdom.api.http.Context;
import org.wisdom.api.http.Result;

import java.util.Map;

/**
 * Flash scope: A client side cookie that can be used to transfer information
 * from one request to another.
 * 
 * Stuff in a flash cookie gets deleted after the next request.
 * 
 * Please note also that flash cookies are not signed.
 *
 * This class uses 'current' for the retrieve flash values and 'outgoing' for the data write back.
 */
public interface FlashCookie {

    /**
     * The key used to store the success message.
     */
    static final String FLASH_SUCCESS = "flash_success";

    /**
     * The key used to store the error message.
     */
    static final String FLASH_ERROR = "flash_error";

    /**
     * Initializes the cookie.
     * It reads the content of the flash cookie (current values).
     * @param context the context
     */
    void init(Context context);

    /**
     * Writes the flash content to the cookie (outgoing values).
     * @param context the context
     * @param result the result
     */
    void save(Context context, Result result);

    /**
     * Sets the given key to the given value.
     * @param key the key
     * @param value the value
     */
    void put(String key, String value);

    /**
     * Sets the given key to the given value.
     * @param key the key
     * @param value the value
     */
    void put(String key, Object value);

    /**
     * Sets the error flash cookie value.
     * Usually accessible via ${flash_error} in html template engine.
     * 
     * @param value The i18n key used to retrieve value of that message
     *        OR an already translated message that will be displayed right away.
     */
    void error(String value);

    /**
     * Sets the success flash cookie value.
     * Usually accessible via ${flash_success} in html template engine.
     * 
     * @param value The i18n key used to retrieve value of that message
     *        OR an already translated message that will be displayed right away.
     */
    void success(String value);

    /**
     * Removes the given key from the outgoing data.
     * @param key the key
     */
    void discard(String key);

    /**
     * Clears the outgoing values.
     */
    void discard();

    /**
     * Transfers the given key/value to the next response.
     * @param key the key
     */
    void keep(String key);

    /**
     * Transfers the whole flash cookie to the next response.
     */
    void keep();

    /**
     * Gets the current value.
     * @param key the key
     * @return the current value, {@literal null} if not set
     */
    String get(String key);

    /**
     * Removes the given key of the current data.
     * @param key the key
     * @return {@literal true} if the entry was removed, {@literal false} otherwise
     */
    boolean remove(String key);

    /**
     * Clear the current data.
     */
    void clearCurrentFlashCookieData();

    /**
     * Checks whether the current data contains the given key.
     * @param key the key
     * @return {@literal true} if the current data contains the given key, {@literal false} otherwise
     */
    boolean contains(String key);

    /**
     * Gets the current data.
     * @return the current data
     */
    Map<String, String> getCurrentFlashCookieData();

    /**
     * Gets the outgoing data.
     * @return the outgoing data
     */
    Map<String, String> getOutgoingFlashCookieData();
}
