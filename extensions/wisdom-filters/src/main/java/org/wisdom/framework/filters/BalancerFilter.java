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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import org.wisdom.api.configuration.Configuration;
import org.wisdom.api.http.HeaderNames;
import org.wisdom.api.http.Request;
import org.wisdom.api.http.Result;
import org.wisdom.api.interception.Filter;
import org.wisdom.api.interception.RequestContext;
import org.wisdom.api.router.Route;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A filter acting as a load balancer between {@link org.wisdom.framework.filters
 * .BalancerMember}. This implementation is made to fetch member from the service registry (and so are
 * dynamic), but you can change this behavior. This balancer supports sticky session and reverse routing.
 * However sticky session is limited by dynamism, and may not be enforced if the targeted member has
 * left. If no members are bound to the balancer, the request is just delegated to the next filter.
 * <p>
 * To create an instance of {@link org.wisdom.framework.filters.BalancerFilter}, you need to override this class and
 * declare it as a {@link org.wisdom.api.annotations.Service}. You can override most of its behavior. You have to
 * manage the binding and unbinding of {@link org.wisdom.framework.filters.BalancerMember}.
 */
public class BalancerFilter extends ProxyFilter implements Filter {

    /**
     * The set of headers for reverse proxy management.
     */
    private static final Set<String> REVERSE_PROXY_HEADERS = ImmutableSet.of(
            HeaderNames.LOCATION,
            HeaderNames.CONTENT_LOCATION
            // URI ?
    );

    /**
     * List of members.
     */
    private final List<BalancerMember> members = new ArrayList<>();
    /**
     * The name of the balancer.
     */
    private final String name;
    /**
     * Whether or not the balancer should support sticky session.
     */
    private final boolean stickySession;
    /**
     * Whether or not the balancer handle reverse proxy.
     */
    private final boolean proxyPassReverse;

    /**
     * Counter to return unique id.
     */
    private final AtomicLong counter = new AtomicLong();

    /**
     * Creates a {@link org.wisdom.framework.filters.BalancerFilter} instance. This instance requires that the {@link
     * BalancerFilter#getName()} method is implemented by the sub-class.
     */
    public BalancerFilter() {
        this.name = getName();
        this.stickySession = getStickySession();
        this.proxyPassReverse = getProxyPassReverse();
    }

    /**
     * Creates a {@link org.wisdom.framework.filters.BalancerFilter} instance. Configuration is taken from the given
     * configuration object.
     *
     * @param configuration the configuration object
     */
    public BalancerFilter(Configuration configuration) {
        super(configuration);
        this.name = getName();
        this.prefix = getPrefix();
        this.stickySession = getStickySession();
        this.proxyPassReverse = getProxyPassReverse();
    }

    /**
     * A default implementation of the {@link ProxyFilter#getProxyTo()} method returning an empty String.
     *
     * @return an empty String
     */
    @Override
    protected final String getProxyTo() {
        // Just there to not be null, and fail in the 'super' constructor.
        return "";
    }

    /**
     * Methods called on incoming request. If there are no members attached to this balancer, the request is
     * processed using {@link org.wisdom.api.interception.RequestContext#proceed()}. Otherwise, a member is selected
     * and the request is delegated.
     *
     * @param route   the route
     * @param context the filter context
     * @return the result
     * @throws Exception when the request cannot be handled correctly
     */
    @Override
    public Result call(Route route, RequestContext context) throws Exception {
        if (getMembers().isEmpty()) {
            return context.proceed();
        } else {
            return super.call(route, context);
        }
    }

    private synchronized List<BalancerMember> getMembers() {
        return new ArrayList<>(members);
    }

    /**
     * Compute the destination URI. It picks a member (enforcing the sticky session if enabled), and computes the URI.
     *
     * @param rc the request content
     * @return the new URI
     * @throws URISyntaxException if the URI cannot be computed
     */
    @Override
    public URI rewriteURI(RequestContext rc) throws URISyntaxException {
        Request request = rc.request();
        BalancerMember member = selectBalancerMember(rc);
        logger.debug("Selected {}", member.getName());
        String path = request.path();
        if (!path.startsWith(prefix)) {
            return null;
        }

        return computeDestinationURI(
                request,
                path,
                member.proxyTo(),
                prefix
        );
    }

    protected BalancerMember selectBalancerMember(RequestContext request) {
        BalancerMember member;
        if (stickySession) {
            String balancer = request.context().session().get("_balancer");
            if (balancer == null) {
                // URL lookup (query string).
                balancer = request.request().parameter("_balancer");
            }

            // A balancer hint was given.
            if (balancer != null) {
                member = getBalancerMember(balancer);
                if (member != null) {
                    // Member still around.
                    return member;
                }
            }
            // The member left, we can't ensure the sticky session.
            logger.warn("Cannot enforce sticky session policy for {} - the member ({}) has left", request.request().uri(), balancer);
        }

        synchronized (this) {
            int index = (int) (counter.getAndIncrement() % members.size());
            member = members.get(index);
            if (stickySession) {
                request.context().session().put("_balancer", member.getName());
            }
            return member;
        }
    }

    /**
     * Callback that can be overridden to customize the header ot the request. This method implements the reverse
     * routing. It updates URLs contained in the headers.
     *
     * @param context the request context
     * @param headers the current set of headers, that need to be modified
     */
    @Override
    public void updateHeaders(RequestContext context, Multimap<String, String> headers) {
        if (!proxyPassReverse) {
            return;
        }
        for (Map.Entry<String, String> h : new LinkedHashSet<>(headers.entries())) {
            if (REVERSE_PROXY_HEADERS.contains(h.getKey())) {
                URI location = URI.create(h.getValue()).normalize();
                if (location.isAbsolute() && isBackendLocation(location)) {
                    String initial = context.request().uri();
                    URI uri = URI.create(initial);
                    try {
                        URI newURI = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(),
                                location.getPath(), location.getQuery(), location.getFragment());
                        headers.remove(h.getKey(), h.getValue());
                        headers.put(h.getKey(), newURI.toString());
                    } catch (URISyntaxException e) {
                        logger.error("Cannot manipulate the header {} (value={}) to enforce reverse routing", h
                                .getKey(), h.getValue(), e);
                    }
                }
            }
        }
    }

    private boolean isBackendLocation(URI location) {
        for (BalancerMember member : getMembers()) {
            URI backendURI = URI.create(member.proxyTo()).normalize();
            if (backendURI.getHost().equals(location.getHost())
                    && backendURI.getScheme().equals(location.getScheme())
                    && backendURI.getPort() == location.getPort()) {
                return true;
            }
        }
        return false;
    }

    private BalancerMember getBalancerMember(String balancer) {
        for (BalancerMember member : getMembers()) {
            if (member.getName().equals(balancer)) {
                return member;
            }
        }
        return null;
    }

    /**
     * Gets the balancer name.
     *
     * @return the name
     */
    public String getName() {
        if (configuration == null) {
            throw new IllegalArgumentException("The balancer name must be set (either " +
                    "by overriding, or configuration)");
        } else {
            return configuration.getOrDie("name");
        }
    }

    /**
     * Checks whether or not the sticky session support is enabled (false by default).
     *
     * @return {@code true} when sticky sessions are enabled, {@code false} otherwise.
     */
    public boolean getStickySession() {
        if (configuration == null) {
            return false;
        } else {
            return configuration.getBooleanWithDefault("stickySession", false);
        }
    }

    /**
     * Checks whether or not the reverse routing support is enabled (false by default).
     *
     * @return {@code true} when reverse routing is enabled, {@code false} otherwise.
     */
    public boolean getProxyPassReverse() {
        if (configuration == null) {
            return false;
        } else {
            return configuration.getBooleanWithDefault("proxyPassReverse", false);
        }
    }

    /**
     * Adds a new member.
     *
     * @param member the member.
     */
    public synchronized void addMember(BalancerMember member) {
        if (member.getBalancerName().equals(name)) {
            logger.info("Adding balancer member '{}' to balancer '{}'", member.getName(), name);
            members.add(member);
        }
    }

    /**
     * Removes a member.
     *
     * @param member the member.
     */
    public synchronized void removeMember(BalancerMember member) {
        if (members.remove(member)) {
            logger.info("Removing balancer member '{}' from balancer '{}'", member.getName(), name);
        }
    }
}
