package org.ow2.chameleon.wisdom.api.cookies;

import org.ow2.chameleon.wisdom.api.http.Context;
import org.ow2.chameleon.wisdom.api.http.Result;

import java.util.Map;

/**
 * Session Cookie... Mostly an adaption of Play1's excellent cookie system that
 * in turn is based on the new client side rails cookies.
 */
public interface SessionCookie {

    /**
     * Time until session expires.
     */
    final String sessionExpireTimeInSeconds = "application.session.expire_time_in_seconds";

    /**
     * Send session cookie only back when content has changed.
     */
    final String sessionSendOnlyIfChanged = "application.session.send_only_if_changed";

    /**
     * Used to set the Secure flag if the cookie. Means Session will only be
     * transferrd over Https.
     */
    final String sessionTransferredOverHttpsOnly = "application.session.transferred_over_https_only";

    /**
     * Used to set the HttpOnly flag at the session cookie. On a supported
     * browser, an HttpOnly session cookie will be used only when transmitting
     * HTTP (or HTTPS) requests, thus restricting access from other, non-HTTP
     * APIs (such as JavaScript). This restriction mitigates but does not
     * eliminate the threat of session cookie theft via cross-site scripting
     * (XSS).
     */
    final String sessionHttpOnly = "application.session.http_only";

    /**
     * Prefix used for all Wisdom cookies.
     * Make sure you set the prefix in your application.conf file.
     * */
    final String applicationCookiePrefix = "application.cookie.prefix";


    public void init(Context context);

    /**
     * @return id of a session.
     */
    public String getId();

    /**
     * @return complete content of session.
     */
    public Map<String, String> getData();

    /**
     * @return an authenticity token or generates a new one.
     */
    public String getAuthenticityToken();

    public void save(Context context, Result result);

    public void put(String key, String value);

    /**
     * Returns the value of the key or null.
     *
     * @param key
     * @return
     */
    public String get(String key);

    /**
     * Removes the value of the key and returns the value or null.
     *
     * @param key
     * @return
     */
    public String remove(String key);

    public void clear();

    /**
     * Returns true if the session is empty, e.g. does not contain anything else
     * than the timestamp key.
     */
    public boolean isEmpty();

}
