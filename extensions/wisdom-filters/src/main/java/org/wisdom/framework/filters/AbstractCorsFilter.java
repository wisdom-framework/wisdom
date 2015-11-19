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
package org.wisdom.framework.filters;

import com.google.common.base.Joiner;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.api.interception.Filter;
import org.wisdom.api.interception.RequestContext;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.Router;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import static org.wisdom.api.http.HeaderNames.*;

/**
 * A filter to support CORS (Cross-origin resource sharing).
 * Wisdom provides a configuration based implementation, but you can extend this class directly to cusotmize the CORS
 * support.
 * CORS is defined by the W3C as a recommendation : http://www.w3.org/TR/cors/
 */
public abstract class AbstractCorsFilter implements Filter {

    private final Router router;

    /**
     * Creates an {@link org.wisdom.framework.filters.AbstractCorsFilter} instance.
     *
     * @param router the router
     */
    public AbstractCorsFilter(Router router) {
        this.router = router;
    }

    /**
     * Interception method.
     * It checks whether or not the request requires CORS support or not. It also checks whether the requests is allowed
     * or not.
     *
     * @param route   the router
     * @param context the filter context
     * @return the result, containing the CORS headers as defined in the recommendation
     * @throws Exception if the result cannot be handled correctly.
     */
    public Result call(Route route, RequestContext context) throws Exception {
        // Is CORS required?
        String originHeader = context.request().getHeader(ORIGIN);

        if (originHeader != null) {
            originHeader = originHeader.toLowerCase();
        }

        // If not Preflight
        if (route.getHttpMethod() != HttpMethod.OPTIONS) {
            return retrieveAndReturnResult(context, originHeader);
        }
        // OPTIONS route exists, don't use filter! (might manually implement
        // CORS?)
        if (!route.isUnbound()) {
            return context.proceed();
        }

        // Try "Preflight"

        // Find existing methods for other routes
        Collection<Route> routes = router.getRoutes();
        List<String> methods = new ArrayList<>(4); // expect POST PUT GET DELETE
        for (Route r : routes) {
            if (r.matches(r.getHttpMethod(), route.getUrl())) {
                methods.add(r.getHttpMethod().name());
            }
        }

        // If there's none, proceed to 404
        if (methods.isEmpty()) {
            return context.proceed();
        }

        String requestMethod = context.request().getHeader(ACCESS_CONTROL_REQUEST_METHOD);

        // If it's not a CORS request, just proceed!
        if (originHeader == null || requestMethod == null) {
            return context.proceed();
        }

        Result res = Results.ok(); // setup result

        if (!methods.contains(requestMethod.toUpperCase())) {
            res = Results.unauthorized("No such method for this route");
        }

        Integer maxAge = getMaxAge();
        if (maxAge != null) {
            res = res.with(ACCESS_CONTROL_MAX_AGE, String.valueOf(maxAge));
        }

        // Otherwise we should be return OK with the appropriate headers.

        String exposedHeaders = getExposedHeadersHeader();
        String allowedHosts = getAllowedHostsHeader(originHeader);

        String allowedMethods = Joiner.on(", ").join(methods);

        Result result = res.with(ACCESS_CONTROL_ALLOW_ORIGIN, allowedHosts)
                .with(ACCESS_CONTROL_ALLOW_METHODS, allowedMethods).with(ACCESS_CONTROL_ALLOW_HEADERS, exposedHeaders);
        if (getAllowCredentials()) {
            result = result.with(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        }

        return result;
    }

    protected Result retrieveAndReturnResult(RequestContext context, String originHeader) throws Exception {
        Result result = context.proceed();

        // Is it actually a CORS request?
        if (originHeader != null) {
            String allowedHosts = getAllowedHostsHeader(originHeader);
            result = result.with(ACCESS_CONTROL_ALLOW_ORIGIN, allowedHosts);
            if (getAllowCredentials()) {
                result = result.with(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            }
            if (!getExposedHeaders().isEmpty()) {
                result = result.with(ACCESS_CONTROL_EXPOSE_HEADERS, getExposedHeadersHeader());
            }
        }

        return result;
    }

    private String getExposedHeadersHeader() {
        return Joiner.on(", ").join(getExposedHeaders());
    }

    private String getAllowedHostsHeader(final String origin) {
        final List<String> allowedHosts = getAllowedHosts();
        //If wildcard is used, only return the request supplied origin
        if(getAllowCredentials() && allowedHosts.contains("*")){
            return origin;
        }else {
            return Joiner.on(", ").join(getAllowedHosts());
        }
    }

    /**
     * By default intercepts all requests. It is highly recommended to override this method.
     *
     * @return {@code .*}
     */
    public Pattern uri() {
        return Pattern.compile(".*");
    }

    /**
     * The filter priority, 0 by default (closest to the action method, but before the interceptors)
     *
     * @return the filter priority
     */
    public int priority() {
        return 0;
    }

    /**
     * Gets the list of exposed headers.
     *
     * @return the list of exposed headers
     */
    public abstract List<String> getExposedHeaders();

    /**
     * Gets the list of allowed hosts
     *
     * @return the list of host
     */
    public abstract List<String> getAllowedHosts();

    /**
     * Checks whether the server allow credentials.
     *
     * @return {@code true} if the server allows credentials
     */
    public abstract boolean getAllowCredentials();

    /**
     * Gets the max-age of the result (cache configuration).
     *
     * @return the max age.
     */
    public abstract Integer getMaxAge();

}
