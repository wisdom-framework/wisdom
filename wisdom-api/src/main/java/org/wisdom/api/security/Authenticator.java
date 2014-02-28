package org.wisdom.api.security;


import org.wisdom.api.http.Context;
import org.wisdom.api.http.Result;

public interface Authenticator {

    /**
     * Retrieves the username from the HTTP context; the default is to read from the session cookie.
     * @param context the context
     * @return {@literal null} if the user is not authenticated, the user name otherwise.
     */
    String getUserName(Context context);

    /**
     * Generates an alternative result if the user is not authenticated. It should be a '401 Not Authorized' response.
     * @param context the context
     * @return the result.
     */
    Result onUnauthorized(Context context);

}
