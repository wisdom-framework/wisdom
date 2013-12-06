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
 */
public interface FlashCookie {

    void init(Context context);

    void save(Context context, Result result);

    void put(String key, String value);

    void put(String key, Object value);

    //TODO Remove this method
    void now(String key, String value);

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

    void discard(String key);

    void discard();

    void keep(String key);

    void keep();

    String get(String key);

    boolean remove(String key);

    void clearCurrentFlashCookieData();

    boolean contains(String key);

    String toString();

    Map<String, String> getCurrentFlashCookieData();

    Map<String, String> getOutgoingFlashCookieData();
}
