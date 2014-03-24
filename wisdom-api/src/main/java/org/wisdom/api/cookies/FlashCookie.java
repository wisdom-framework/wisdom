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
     * Usually accessible via ${flash.error} in html template engine.
     * 
     * @param value The i18n key used to retrieve value of that message
     *        OR an already translated message that will be displayed right away.
     */
    void error(String value);

    /**
     * Sets the success flash cookie value.
     * Usually accessible via ${flash.success} in html template engine.
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
