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
package org.wisdom.framework.csrf;

import org.apache.felix.ipojo.annotations.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.annotations.Service;
import org.wisdom.api.configuration.Configuration;
import org.wisdom.api.cookies.Cookie;
import org.wisdom.api.crypto.Crypto;
import org.wisdom.api.http.*;
import org.wisdom.framework.csrf.api.CSRFErrorHandler;
import org.wisdom.framework.csrf.api.CSRFService;

import java.util.List;

/**
 * Implementation of the {@link org.wisdom.framework.csrf.api.CSRFService}. This implementation requires a
 * configuration:
 * <code><pre>
 * csrf {
 *     token {
 *         name = the name of the token, it's the (form) field or parameter containing the token
 *         sign = whether or not tokens need to be signed, enabled by default
 *     }
 *     cookie {
 *         name = the optional name of the cookie, if not set it uses the session
 *         domain = the optional domain
 *         path = the path, / by default
 *         secure = is the cookie secure or not, default to true
 *     }
 * }
 * </pre></code>
 */
@Service
public class CSRFServiceImpl implements CSRFService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSRFService.class.getName());
    public static final String CSRF_TOKEN_HEADER = "X-XSRF-TOKEN";
    public static final String NO_CHECK_HEADER_VALUE = "no-check";
    public static final String AJAX_HEADER = "X-Requested-With";

    /**
     * The crypto service. Public for testing purpose.
     */
    @Requires
    public Crypto crypto;

    /**
     * The CSRF configuration. Public for testing purpose.
     */
    @Requires(filter = "(configuration.path=csrf)")
    public Configuration configuration;

    /**
     * The CSRF Error Handler. Public for testing purpose.
     */
    @Requires(optional = true, defaultimplementation = DefaultCSRFErrorHandler.class)
    public CSRFErrorHandler handler;

    /**
     * Extracts the token from the request. This implementation checks in the request data, then in the CORS cookie
     * if any, and finally in the session cookie. If the token is signed, it resigns it to avoid the BREACH
     * vulnerability.
     *
     * @param context the context
     * @return the extract token, {@code null} if no token.
     */
    @Override
    public String extractTokenFromRequest(Context context) {
        // First check the tags, this is where tokens are added if it's added to the current request
        // In that case, the request scope contains the token
        String token = (String) context.request().data().get(TOKEN_KEY);
        if (token == null && getCookieName() != null) {
            // Search in a cookie
            Cookie cookie = context.cookie(getCookieName());
            if (cookie != null) {
                token = cookie.value();
            }
        }
        if (token == null) {
            // Check in the session
            token = context.session().get(getTokenName());
        }

        if (token != null && isSignedToken()) {
            // Extract the signed token, and then resign it. This makes the token random per request, preventing
            // the BREACH vulnerability
            return crypto.signToken(crypto.extractSignedToken(token));
        } else {
            return token;
        }
    }

    /**
     * Checks whether or not the request is valid (not by passed, valid token).
     *
     * @param context the context
     * @return {@code true} if the request if valid, {@code false} otherwise.
     */
    @Override
    public boolean isValidRequest(Context context) {
        // Check if we are executing an unsafe method
        if (!isUnsafe(context)) {
            return true;
        }

        if (checkCsrfBypass(context)) {
            LOGGER.debug("Bypassing CSRF check for {} {}", context.route().getHttpMethod(), context.route().getUrl());
            return true;
        } else {
            String tokenFromRequest = extractTokenFromRequest(context);
            if (tokenFromRequest == null) {
                LOGGER.error("CSRF Check failed because there is no token in the incoming request headers");
                return false;
            }
            String tokenFromContent = extractTokenFromContent(context);
            if (tokenFromContent == null) {
                LOGGER.error("CSRF Check failed because we are unable to find a token in the incoming request query " +
                        "string or body");
                return false;
            }

            if (compareTokens(tokenFromRequest, tokenFromContent)) {
                return true;
            } else {
                LOGGER.error("CSRF Check failed because the given token is invalid");
                return false;
            }
        }
    }

    private boolean isUnsafe(Context context) {
        return context.route().getHttpMethod() == HttpMethod.POST && UNSAFE_CONTENT_TYPES.contains(context.request()
                .contentType());
    }

    /**
     * Methods adding the CORS token to the given result. The effect depends on the configuration.
     *
     * @param context  the context
     * @param newToken the new token
     * @param result   the result that is enhanced
     * @return the updated result
     */
    @Override
    public Result addTokenToResult(Context context, String newToken, Result result) {
        if (isCached(context)) {
            LOGGER.debug("Not adding token to a cached result");
            return result;
        }

        LOGGER.debug("Adding token to result");
        if (getCookieName() != null) {
            Cookie cookie = context.cookie(getCookieName());
            if (cookie != null) {
                return result.with(Cookie.builder(cookie).setValue(newToken).build());
            } else {
                // Create a new cookie
                return result.with(Cookie.cookie(getCookieName(), newToken)
                        .setSecure(isSecureCookie())
                        .setPath(getCookiePath())
                        .setDomain(getCookieDomain())
                        .setMaxAge(3600)
                        .build());
            }
        } else {
            // Session
            context.session().put(getTokenName(), newToken);
            return result;
        }
    }

    private boolean isCached(Context context) {
        String cacheControl = context.request().getHeader(HeaderNames.CACHE_CONTROL);
        return cacheControl != null && !cacheControl.contains(HeaderNames.NOCACHE_VALUE);
    }

    private boolean checkCsrfBypass(Context context) {
        // Check whether the CSRF Header has the no-check value
        // Since injecting arbitrary header values is not possible with a CSRF attack, the presence of this header
        // indicates that this is not a CSRF attack
        if (context.header(CSRF_TOKEN_HEADER) != null
                && NO_CHECK_HEADER_VALUE.equalsIgnoreCase(context.header(CSRF_TOKEN_HEADER))) {
            return true;
        }
        // Check that 'X-Requested-With' header is defined
        // AJAX requests are not CSRF attacks either because they are restricted to same origin policy
        return context.header(AJAX_HEADER) != null;

    }

    /**
     * Compares to token.
     *
     * @param a the first token
     * @param b the second token
     * @return {@code true} if the token are equal, {@code false} otherwise
     */
    @Override
    public boolean compareTokens(String a, String b) {
        if (isSignedToken()) {
            return crypto.compareSignedTokens(a, b);
        } else {
            return crypto.constantTimeEquals(a, b);
        }
    }

    /**
     * Extracts the token from the content of the request. This implementation checks inside the headers, and inside
     * form body.
     *
     * @param context the context
     * @return the extracted token, {@code null} if not found.
     */
    @Override
    public String extractTokenFromContent(Context context) {
        // check query String
        String token = context.request().parameter(getTokenName());

        if (token == null) {
            // Check a specified header
            token = context.header(CSRF_TOKEN_HEADER);
        }

        // If still not found, check in body
        if (token == null) {
            if (context.request().contentType().startsWith(MimeTypes.FORM)
                    || context.request().contentType().startsWith(MimeTypes.MULTIPART)) {
                List<String> list = context.form().get(getTokenName());
                if (list != null && !list.isEmpty()) {
                    return list.get(0);
                }
            }
        }
        return token;
    }

    /**
     * Clears the token from the request
     *
     * @param context the context
     * @param msg     the error message
     * @return the result
     */
    @Override
    public Result clearTokenIfInvalid(Context context, String msg) {
        Result error = handler.onError(context, msg);
        final String cookieName = getCookieName();
        if (cookieName != null) {
            Cookie cookie = context.cookie(cookieName);
            if (cookie != null) {
                return error.without(cookieName);
            }
        } else {
            context.session().remove(getTokenName());
        }
        return error;
    }

    private boolean isSignedToken() {
        return configuration.getBooleanWithDefault("token.sign", true);
    }

    private boolean isSecureCookie() {
        return configuration.getBooleanWithDefault("cookie.secure", true);
    }

    public String getTokenName() {
        return configuration.getWithDefault("token.name", "csrfToken");
    }

    @Override
    public String getCurrentToken(Context context) {
        return (String) context.request().data().get(TOKEN_KEY);
    }

    private String getCookiePath() {
        return configuration.getWithDefault("cookie.path", "/");
    }

    private String getCookieDomain() {
        return configuration.getWithDefault("cookie.domain", null);
    }

    @Override
    public String generateToken(Context context) {
        String newToken;
        if (isSignedToken()) {
            newToken = crypto.generateSignedToken();
        } else {
            newToken = crypto.generateToken();
        }
        // Add the new token to the request scope
        context.request().data().put(TOKEN_KEY, newToken);
        return newToken;
    }

    @Override
    public boolean eligibleForCSRF(Context context) {
        // If the request isn't accepting HTML, then it won't be rendering a form, so there's no point in generating a
        // CSRF token for it.
        final HttpMethod method = context.route().getHttpMethod();
        return
                // NO POST here, because, POST would mean another request has been done beforehand to retrieve the
                // first form.
                ((method == HttpMethod.GET) || method == HttpMethod.HEAD)
                        && (context.request().accepts(MimeTypes.HTML) || context.request().accepts("application/xml+xhtml"));
    }

    private String getCookieName() {
        return configuration.get("cookie.name");
    }
}
