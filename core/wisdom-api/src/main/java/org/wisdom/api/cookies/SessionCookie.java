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
 * Session Cookie... Mostly an adaption of Play1's excellent cookie system that
 * in turn is based on the new client side rails cookies.
 */
public interface SessionCookie {

    /**
     * Configuration Key : Time until session expires.
     */
    public static final String SESSION_EXPIRE_TIME_SECOND = "application.session.expire_time_in_seconds";

    /**
     * Configuration Key : Send session cookie only back when content has changed.
     */
    public static final String SESSION_SEND_ONLY_IF_CHANGED = "application.session.send_only_if_changed";

    /**
     * Configuration Key :  Used to set the Secure flag if the cookie. Means Session will only be
     * transferred over Https.
     */
    public static final String SESSION_OVER_HTTPS_ONLY = "application.session.transferred_over_https_only";

    /**
     * Configuration Key :  Used to set the HttpOnly flag at the session cookie. On a supported
     * browser, an HttpOnly session cookie will be used only when transmitting
     * HTTP (or HTTPS) requests, thus restricting access from other, non-HTTP
     * APIs (such as JavaScript). This restriction mitigates but does not
     * eliminate the threat of session cookie theft via cross-site scripting
     * (XSS).
     */
    public static final String SESSION_HTTP_ONLY = "application.session.http_only";


    /**
     * Initializes the cookie. This method is called by the engine and reads the existing data.
     *
     * @param context the context
     */
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
     * Saves the session and writes the content to the session cookie.
     *
     * @param context the context
     * @param result  the result
     */
    void save(Context context, Result result);

    /**
     * Adds the given data to the session.
     *
     * @param key   the key
     * @param value the value
     */
    void put(String key, String value);

    /**
     * Returns the value of the key or null.
     *
     * @param key the key
     * @return the value associated with the key, {@literal null} if the key is not present in the current session.
     */
    String get(String key);

    /**
     * Removes the value of the key and returns the value or null.
     *
     * @param key the key
     * @return the value that is now removed from the session, {@literal null} if no value were associated with the key.
     */
    String remove(String key);

    /**
     * Clears the data.
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
