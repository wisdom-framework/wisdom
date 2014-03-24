package org.wisdom.api.interception;

import org.wisdom.api.http.Context;
import org.wisdom.api.http.Request;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.api.router.Route;

import java.util.*;
import java.util.regex.Pattern;

/**
 * The request context let access interceptor to the current HTTP context, filters and interceptors.
 * This class is responsible of the request resolution, and so hold the filter chain.
 */
public class RequestContext {

    /**
     * The invoked route.
     */
    private final Route route;

    /**
     * The filter chain.
     */
    private final List<Filter> chain;

    /**
     * The map storing the configuration for interceptors.
     */
    private final Map<Interceptor<?>, Object> interceptors;

    /**
     * The action parameters.
     */
    private final Object[] parameters;

    private ListIterator<Filter> iterator;

    private final Map<String, Object> data = new HashMap<String, Object>();

    /**
     * Creates a new Interception Context. Instances should only be created by the router.
     * @param route the intercepted route
     * @param chain the interception chain containing filters and interceptors.
     * @param interceptors the set of interceptors and their configuration
     */
    public RequestContext(Route route, List<Filter> chain, Map<Interceptor<?>, Object> interceptors,
                          Object[] parameters) {
        this.route = route;
        this.interceptors = interceptors;

        this.chain = new LinkedList<>(chain);
        this.parameters = Arrays.copyOf(parameters, parameters.length);

        // Add the action invocation
        final ActionInvoker invoker = new ActionInvoker();
        this.chain.add(invoker);
    }

    /**
     * Retrieves the configuration annotation for the given interceptor.
     * @param interceptor the interceptor
     * @return the configuration, {@code null} if not found
     */
    <A> A getConfigurationForInterceptor(Interceptor<A> interceptor) {
        return (A) interceptors.get(interceptor);
    }

    /**
     * Calls the next interceptors.
     * @return the result from the next interceptor
     * @throws Throwable
     */
    public Result proceed() throws Throwable {
        if (iterator == null) {
            iterator = chain.listIterator();
        }
        if (! iterator.hasNext()) {
            throw new IllegalStateException("Reached the end of the chain without result.");
        }
        Filter filter = iterator.next();
        return filter.call(route, this);
    }

    public Context context() {
        return Context.CONTEXT.get();
    }

    public Request request() {
        return context().request();
    }

    public Route route() {
        return route;
    }

    /**
     * Access to the data shared by filters and interceptors.
     * It let filters and interceptors sharing data. All modifications will be seen by the other participant of the
     * chain.
     * @return the data
     */
    public Map<String, Object> data() {
        return data;
    }

    /**
     * The end (actually middle) of the chain. This interceptor is a fake calling the action method.
     * It does not call {@link RequestContext#proceed()}.
     */
    private class ActionInvoker implements Filter {

        @Override
        public Result call(Route route, RequestContext context) throws Throwable {
            if (RequestContext.this.route.isUnbound()) {
                return Results.notFound();
            } else {
                return (Result) RequestContext.this.route.getControllerMethod().invoke(
                        RequestContext.this.route.getControllerObject(), parameters);
            }
        }

        @Override
        public Pattern uri() {
            // Not meaningful here.
            return null;
        }

        @Override
        public int priority() {
            // Anyway, we're the last.
            return -1;
        }
    }
}
