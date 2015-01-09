/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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
package org.wisdom.framework.csrf.api;

import com.google.common.collect.ImmutableList;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;

import java.util.List;

/**
 * The CSRF service used by the interceptors. You can use this service directly if you want to implement your own check.
 */
public interface CSRFService {

    /**
     * The key used to store the token in the request scope.
     */
    public final String TOKEN_KEY = "CSRF_TOKEN";

    /**
     * Unsafe content types are content types that requires a CSRF check.
     */
    public final List<String> UNSAFE_CONTENT_TYPES =
            ImmutableList.of(MimeTypes.TEXT, MimeTypes.FORM, MimeTypes.MULTIPART);

    /**
     * Extracts the token from the incoming request. It checks the session, the CSRF cookie (if enabled) and a HTTP
     * header.
     *
     * @param context the context
     * @return the extracted token, {@code null} if not found
     */
    public String extractTokenFromRequest(Context context);

    /**
     * Extracts the token from the incoming request content (query and body). It extracts the token from the query
     * string and if the body is a form, from it. If the body is not a form, {@code null} is returned.
     *
     * @param context the context
     * @return the extracted token, {@code null} if not found
     */
    public String extractTokenFromContent(Context context);

    /**
     * Computes the result to return to the client when a CSRF error is detected. It also clear existing tokens.
     * Implementation calls the {@link org.wisdom.framework.csrf.api.CSRFErrorHandler} service if available.
     *
     * @param context the context
     * @param msg     the error message
     * @return the result.
     */
    public Result clearTokenIfInvalid(Context context, String msg);

    /**
     * Generates a CSRF token.
     *
     * @param context the current HTTP context
     * @return the generated token, signed or not depending of the current configuration
     */
    public String generateToken(Context context);

    /**
     * Checks whether the current request is valid according to the CSRF check.
     *
     * @param context the context
     * @return {@code true} if the request is valid (CSRF check passed), {@code false} otherwise
     */
    public boolean isValidRequest(Context context);

    /**
     * Adds a token to the given result.
     *
     * @param context  the context
     * @param newToken the new token
     * @param result   the result that is enhanced
     * @return the new result
     */
    public Result addTokenToResult(Context context, String newToken, Result result);

    /**
     * Compares token, and checks that they are equals. Behavior depends whether or not tokens are signed.
     *
     * @param a the first token
     * @param b the second token
     * @return {@code true} if the token are equal, {@code false} otherwise
     */
    public boolean compareTokens(String a, String b);

    /**
     * Checks whether or not the incoming request result should have a token injected or not.
     *
     * @param context the context
     * @return {@code true} if the result should contain a CSRF token
     */
    public boolean eligibleForCSRF(Context context);

    /**
     * Gets the name of the token.
     * @return the configured name of the token
     */
    public String getTokenName();

    /**
     * Gets the token that has been generated for the current request.
     * @param context the current request
     * @return the token, {@code null} if there are no token generated.
     */
    String getCurrentToken(Context context);
}
