package org.wisdom.api.cookies;

import org.wisdom.api.http.Context;
import org.wisdom.api.http.Result;

import java.util.Map;

/**
 * Session Cookie... Mostly an adaption of Play1's excellent cookie system that
 * in turn is based on the new client side rails cookies.
 */
public interface SessionCookie {

    public final class ConfigurationKeys {
        /**
         * Time until session expires.
         */
        public static final String SESSION_EXPIRE_TIME_SECOND = "application.session.expire_time_in_seconds";

        /**
         * Send session cookie only back when content has changed.
         */
        public static final String SESSION_SEND_ONLY_IF_CHANGED = "application.session.send_only_if_changed";

        /**
         * Used to set the Secure flag if the cookie. Means Session will only be
         * transferred over Https.
         */
        public static final String SESSION_OVER_HTTPS_ONLY = "application.session.transferred_over_https_only";

        /**
         * Used to set the HttpOnly flag at the session cookie. On a supported
         * browser, an HttpOnly session cookie will be used only when transmitting
         * HTTP (or HTTPS) requests, thus restricting access from other, non-HTTP
         * APIs (such as JavaScript). This restriction mitigates but does not
         * eliminate the threat of session cookie theft via cross-site scripting
         * (XSS).
         */
        public static final String SESSION_HTTP_ONLY = "application.session.http_only";
    }

    void init(Context context);

    /**
     * @return id of a session.
     */
    String getId();

    /**
     * @return complete content of session.
     */
    Map<String, String> getData();

    /**
     * @return an authenticity token or generates a new one.
     */
    String getAuthenticityToken();

    void save(Context context, Result result);

    void put(String key, String value);

    /**
     * Returns the value of the key or null.
     *
     * @param key
     * @return
     */
    String get(String key);

    /**
     * Removes the value of the key and returns the value or null.
     *
     * @param key
     * @return
     */
    String remove(String key);

    /**
     * Clear the data.
     */
    void clear();

    /**
     * Returns true if the session is empty, e.g. does not contain anything else
     * than the timestamp key.
     *
     * @return {@literal true} if the session cookie is empty, {@literal false} otherwise
     */
    boolean isEmpty();

}
