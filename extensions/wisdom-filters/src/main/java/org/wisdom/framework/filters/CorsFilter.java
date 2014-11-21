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

import static org.wisdom.api.http.HeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS;
import static org.wisdom.api.http.HeaderNames.ACCESS_CONTROL_ALLOW_HEADERS;
import static org.wisdom.api.http.HeaderNames.ACCESS_CONTROL_ALLOW_METHODS;
import static org.wisdom.api.http.HeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN;
import static org.wisdom.api.http.HeaderNames.ACCESS_CONTROL_EXPOSE_HEADERS;
import static org.wisdom.api.http.HeaderNames.ACCESS_CONTROL_MAX_AGE;
import static org.wisdom.api.http.HeaderNames.ACCESS_CONTROL_REQUEST_METHOD;
import static org.wisdom.api.http.HeaderNames.ORIGIN;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.api.interception.Filter;
import org.wisdom.api.interception.RequestContext;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.Router;

import com.google.common.base.Joiner;

public abstract class CorsFilter implements Filter {

    private final Router router;

    public CorsFilter(Router router) {
        this.router = router;
    }

    public Result call(Route route, RequestContext context) throws Exception {
        // Is CORS required?
        String originHeader = context.request().getHeader(ORIGIN);

        if (originHeader != null) {
            originHeader = originHeader.toLowerCase();
        }

        // If not Preflight
        if (route.getHttpMethod() != HttpMethod.OPTIONS) {

            Result result = context.proceed();

            // Is it actually a CORS request?
            if (originHeader != null) {
                String allowedHosts = getAllowedHostsHeader();
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
        // OPTIONS route exists, don't use filter! (might manually implement
        // CORS?)
        if (!route.isUnbound()) {
            return context.proceed();
        }

        String requestMethod = context.request().getHeader(ACCESS_CONTROL_REQUEST_METHOD);

        // We should notify the invalid CORS Preflight request
        if (originHeader == null || requestMethod == null) {
            return Results.unauthorized("Invalid CORS request");
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
        String allowedHosts = getAllowedHostsHeader();

        String allowedMethods = Joiner.on(", ").join(methods);

        Result result = res.with(ACCESS_CONTROL_ALLOW_ORIGIN, allowedHosts)
                .with(ACCESS_CONTROL_ALLOW_METHODS, allowedMethods).with(ACCESS_CONTROL_ALLOW_HEADERS, exposedHeaders);
        if (getAllowCredentials()) {
            result = result.with(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        }

        return result;
    }

    private String getExposedHeadersHeader() {
        return Joiner.on(", ").join(getExposedHeaders());
    }

    private String getAllowedHostsHeader() {
        return Joiner.on(", ").join(getAllowedHosts());
    }

    public Pattern uri() {
        return Pattern.compile(".*");
    }

    public int priority() {
        return 0;
    }

    public abstract List<String> getExposedHeaders();

    public abstract List<String> getAllowedHosts();

    public abstract boolean getAllowCredentials();

    public abstract Integer getMaxAge();

}
