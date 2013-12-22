package org.wisdom.api.interceptor;

import org.wisdom.api.http.Context;
import org.wisdom.api.http.Request;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Route;

import java.util.*;

/**
 * the interception context let access interceptor to the current HTTP context, and other interceptors.
 */
public class InterceptionContext {


    private final Route route;

    private final LinkedList<Interceptor> chain;
    private final LinkedHashMap<Interceptor, Object> interceptors;
    private final Object[] parameters;
    private ListIterator<Interceptor> iterator;

    private final Map<String, Object> data = new HashMap<>();

    /**
     * Creates a new Interception Context. Instances should only be created by the router.
     * @param route the intercepted route
     * @param interceptors the set of interceptors and their configuration
     * @param parameters the route parameters
     */
    public InterceptionContext(Route route, LinkedHashMap<Interceptor, Object> interceptors, Object[] parameters) {
        this.route = route;
        this.interceptors = interceptors;
        this.parameters = Arrays.copyOf(parameters, parameters.length);

        chain = new LinkedList<>();

        if (interceptors != null) {
            chain.addAll(interceptors.keySet());
        }

        // Add the route invocation
        final RouterInvokerInterceptor invoker = new RouterInvokerInterceptor();
        chain.add(invoker);
    }

    /**
     * Retrieves the configuration annotation for the given interceptor.
     * @param interceptor the interceptor
     * @return the configuration, {@code null} if not found
     */
    private Object getConfigurationForInterceptor(Interceptor interceptor) {
        return interceptors.get(interceptor);
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
        Interceptor interceptor = iterator.next();
        Object configuration = getConfigurationForInterceptor(interceptor);
        return interceptor.call(this, configuration);
    }

    public Context context() {
        return Context.context.get();
    }

    public Request request() {
        return context().request();
    }

    public Route route() {
        return route;
    }

    /**
     * Access to the data shared by interceptors.
     * It let interceptors sharing data. All modification will be seen by the other interceptors
     * @return the data
     */
    public Map<String, Object> data() {
        return data;
    }

    /**
     * The end (actually middle) of the chain. This interceptor is a fake calling the action method.
     * It does not call {@link InterceptionContext#proceed()}.
     */
    private class RouterInvokerInterceptor extends Interceptor<Void> {

        @Override
        public Result call(Void configuration, InterceptionContext context) throws Throwable {
            return (Result) route.getControllerMethod().invoke(route.getControllerObject(), parameters);
        }

        @Override
        public Class annotation() {
            return null;
        }
    }
}
