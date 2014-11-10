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
 * A filter implementation to extend to create a transparent proxy to a specific location.
 */
public class ProxyFilter implements Filter {


    private static final Set<String> HOP_HEADERS = new HashSet<>();

    static {
        HOP_HEADERS.add("connection");
        HOP_HEADERS.add("keep-alive");
        HOP_HEADERS.add("proxy-authorization");
        HOP_HEADERS.add("proxy-authenticate");
        HOP_HEADERS.add("proxy-connection");
        HOP_HEADERS.add("transfer-encoding");
        HOP_HEADERS.add("te");
        HOP_HEADERS.add("trailer");
        HOP_HEADERS.add("upgrade");
    }

    protected Logger logger;
    private HttpClient client;
    private String proxyTo;
    private String prefix;


    public ProxyFilter() {
        logger = createLogger();
        client = createHttpClient();
        proxyTo = getProxyTo();
        prefix = getPrefix();

        if (proxyTo == null) {
            throw new IllegalStateException("The 'proxyTo' parameter is required");
        }

        if (prefix == null) {
            prefix = "";
        }

    }

    public HttpClient getClient() {
        return client;
    }

    private HttpClient createHttpClient() {
        return newHttpClient();
    }

    protected HttpClient newHttpClient() {
        return HttpClients.custom()
                // Do not manage redirection.
                .setRedirectStrategy(new DefaultRedirectStrategy() {
                    @Override
                    protected boolean isRedirectable(String method) {
                        return followRedirect(method);
                    }
                })
                .setDefaultCookieStore(new BasicCookieStore() {
                    @Override
                    public synchronized List<Cookie> getCookies() {
                        return Collections.emptyList();
                    }
                })
                .build();
    }

    protected boolean followRedirect(String method) {
        return false;
    }


    protected Logger createLogger() {
        return LoggerFactory.getLogger(ProxyFilter.class.getName() + "-" + uri().toString());
    }


    /**
     * The interception method. The method should call {@link org.wisdom.api.interception.RequestContext#proceed()}
     * to call the next interceptor. Without this call it cuts the chain.
     *
     * @param route
     * @param context the filter context
     * @return the result
     * @throws Exception if anything bad happen
     */
    @Override
    public Result call(final Route route, final RequestContext context) throws Exception {
        return new AsyncResult(new Callable<Result>() {
            @Override
            public Result call() throws Exception {
                URI rewrittenURI = rewriteURI(context.request());
                logger.debug("Proxy request - rewriting {} to {}", context.request().uri(), rewrittenURI);
                if (rewrittenURI == null) {
                    return onRewriteFailed(context);
                }

                BasicHttpEntityEnclosingRequest request
                        = new BasicHttpEntityEnclosingRequest(context.request().method(), rewrittenURI.toString());
                // Any header listed by the Connection header must be removed:
                // http://tools.ietf.org/html/rfc7230#section-6.1.
                Set<String> hopHeaders = new HashSet<>();
                List<String> connectionHeaders = context.request().headers().get(HeaderNames.CONNECTION);
                for (String s : connectionHeaders) {
                    for (String entry : Splitter.on(",").omitEmptyStrings().trimResults().splitToList(s)) {
                        hopHeaders.add(entry.toLowerCase(Locale.ENGLISH));
                    }
                }

                boolean hasContent = context.request().contentType() != null;
                final String host = getHost();
                Multimap<String, String> headers = ArrayListMultimap.create();
                for (Map.Entry<String, List<String>> entry : context.request().headers().entrySet()) {
                    String name = entry.getKey();
                    if (HeaderNames.TRANSFER_ENCODING.equalsIgnoreCase(name)) {
                        hasContent = true;
                    }
                    if (host != null && HeaderNames.HOST.equalsIgnoreCase(name)) {
                        continue;
                    }
                    // Remove hop-by-hop headers.
                    String lower = name.toLowerCase(Locale.ENGLISH);
                    if (HOP_HEADERS.contains(lower) || hopHeaders.contains(lower)) {
                        continue;
                    }

                    for (String v : entry.getValue()) {
                        headers.put(name, v);
                    }
                }

                // Force the Host header if configured
                headers.removeAll(HeaderNames.HOST);
                if (host != null) {
                    headers.put(HeaderNames.HOST, host);
                    headers.put("X-Forwarded-Server", host);
                } else {
                    // Set of the URI one
                    headers.put("X-Forwarded-Server", rewrittenURI.getHost());
                }

                // Add proxy headers
                if (getVia() != null) {
                    headers.put(HeaderNames.VIA, "http/1.1 " + getVia());
                }
                headers.put("X-Forwarded-For", context.request().remoteAddress());
                if (host != null) {
                    headers.put("X-Forwarded-Host", host);
                }

                updateHeaders(context, headers);
                for (Map.Entry<String, String> s : headers.entries()) {
                    request.addHeader(s.getKey(), s.getValue());
                }
                // Remove content-length as it is computed by the HTTP client.
                request.removeHeaders(HeaderNames.CONTENT_LENGTH);

                if (hasContent) {
                    ByteArrayEntity entity = new ByteArrayEntity(context.context().raw(),
                            ContentType.create(context.request().contentMimeType(), context.request().contentCharset()));
                    request.setEntity(entity);
                }

                HttpResponse response = client.execute(new HttpHost(rewrittenURI.getHost(), rewrittenURI.getPort()), request);
                return onResult(toResult(response));
            }
        });

    }

    protected void updateHeaders(RequestContext context, Multimap<String, String> headers) {
        // Do nothing by default.
    }

    private Result toResult(HttpResponse response) throws IOException {
        Result result = new Result(response.getStatusLine().getStatusCode());
        // Copy headers
        for (Header h : response.getAllHeaders()) {
            result.with(h.getName(), h.getValue());
        }

        // Copy content
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            result.render(new RenderableStream(entity.getContent()));

        }

        return result;
    }

    protected Result onRewriteFailed(RequestContext context) {
        return Results.internalServerError("Cannot proxy request - failed to compute destination");
    }

    protected Result onResult(Result result) {
        return result;
    }

    protected URI rewriteURI(Request request) throws URISyntaxException {
        String path = request.path();
        if (!path.startsWith(prefix)) {
            return null;
        }

        StringBuilder uri = new StringBuilder(proxyTo);
        if (proxyTo.endsWith("/")) {
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
     * Notice that the router are caching these patterns and so cannot changed.
     */
    @Override
    public Pattern uri() {
        return Pattern.compile("/proxy/.*");
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

    protected String getHost() {
        Configuration configuration = getConfiguration();
        if (configuration == null) {
            return null;
        } else {
            return configuration.get("host");
        }
    }

    protected String getProxyTo() {
        Configuration configuration = getConfiguration();
        if (configuration == null) {
            return null;
        } else {
            return configuration.get("proxyTo");
        }
    }

    protected String getPrefix() {
        Configuration configuration = getConfiguration();
        if (configuration == null) {
            return "";
        } else {
            return configuration.get("prefix");
        }
    }

    protected String getVia() {
        Configuration configuration = getConfiguration();
        if (configuration == null) {
            return null;
        } else {
            return configuration.get("via");
        }
    }

    public Configuration getConfiguration() {
        return null;
    }
}
