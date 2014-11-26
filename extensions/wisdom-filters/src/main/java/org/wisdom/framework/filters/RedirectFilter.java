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

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.bodies.RenderableStream;
import org.wisdom.api.configuration.Configuration;
import org.wisdom.api.http.*;
import org.wisdom.api.interception.Filter;
import org.wisdom.api.interception.RequestContext;
import org.wisdom.api.router.Route;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

/**
 * A filter implementation to extend to create return a {@code REDIRECT} result to a specific location.
 */
public class RedirectFilter implements Filter {

    private final Configuration configuration;

    protected Logger logger;
    private String redirectTo;
    private String prefix;

    /**
     * Default constructor, not configuration.
     */
    public RedirectFilter() {
        this(null);
    }

    /**
     * Constructor receiving a configuration.
     *
     * @param conf the configuration
     */
    public RedirectFilter(Configuration conf) {
        configuration = conf;
        logger = createLogger();
        redirectTo = getRedirectTo();
        prefix = getPrefix();

        if (redirectTo == null) {
            throw new IllegalStateException("The 'redirectTo' parameter is required");
        }

        if (prefix == null) {
            prefix = "";
        }
    }

    /**
     * Creates the logger instance used by this filter. It can be overridden to customize the logger instance.
     *
     * @return the logger
     */
    protected Logger createLogger() {
        return LoggerFactory.getLogger(RedirectFilter.class.getName() + "-" + uri().toString());
    }

    /**
     * The interception method. It just returns a {@code REDIRECT} result to the computed URI.
     *
     * @param route   the route
     * @param context the filter context
     * @return the result
     * @throws Exception if anything bad happen
     */
    @Override
    public Result call(final Route route, final RequestContext context) throws Exception {
        URI redirectedURI = rewriteURI(context.request());
        logger.debug("Redirecting request - rewriting {} to {}", context.request().uri(), redirectedURI);
        if (redirectedURI == null) {
            return onRewriteFailed(context);
        }
        return Results.redirect(redirectedURI.toString());
    }

    /**
     * Callback invokes when the URL rewrite fails. By default, it returns an internal error.
     *
     * @param context the request context
     * @return the result in case of rewrite failure, an internal error by default.
     */
    protected Result onRewriteFailed(RequestContext context) {
        return Results.internalServerError("Cannot redirect request - failed to compute destination");
    }

    /**
     * Computes the URI where the request need to be transferred.
     *
     * @param request the request
     * @return the URI
     * @throws java.net.URISyntaxException if the URI cannot be computed
     */
    public URI rewriteURI(Request request) throws URISyntaxException {
        String path = request.path();
        if (!path.startsWith(prefix)) {
            return null;
        }

        StringBuilder uri = new StringBuilder(redirectTo);
        if (redirectTo.endsWith("/")) {
            uri.setLength(uri.length() - 1);
        }
        String rest = path.substring(prefix.length());
        if (!rest.startsWith("/") && !rest.isEmpty()) {
            uri.append("/");
        }
        uri.append(rest);

        // Do we have a query String
        int index = request.uri().indexOf("?");
        String query = null;
        if (index != -1) {
            // Remove the ?
            query = request.uri().substring(index + 1);
        }

        if (!Strings.isNullOrEmpty(query)) {
            uri.append("?").append(query);
        }

        return URI.create(uri.toString()).normalize();
    }

    /**
     * Gets the Regex Pattern used to determine whether the route is handled by the filter or not.
     * Notice that the router are caching these patterns and so cannot be changed.
     */
    @Override
    public Pattern uri() {
        return Pattern.compile(getPrefix() + ".*");
    }

    /**
     * Gets the filter priority, determining the position of the filter in the filter chain. Filter with a high
     * priority are called first. Notice that the router are caching these priorities and so cannot changed.
     * <p>
     * It is heavily recommended to allow configuring the priority from the Application Configuration.
     *
     * @return the priority
     */
    @Override
    public int priority() {
        return 1000;
    }

    /**
     * Gets the destination of the redirection. By default, it returns the 'redirectTo' entry of the configuration
     * object. It can be overridden to return any value.
     *
     * @return the URL of the destination
     */
    protected String getRedirectTo() {
        if (configuration == null) {
            return null;
        } else {
            return configuration.get("redirectTo");
        }
    }

    /**
     * Gets the prefix of the redirect filter. By default, it returns the 'prefix' entry of the configuration object. It
     * can be overridden to return any value.
     *
     * @return the URL of the destination
     */
    protected String getPrefix() {
        if (configuration == null) {
            return "";
        } else {
            return configuration.get("prefix");
        }
    }
}
