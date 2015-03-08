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
package org.wisdom.api.interception;

import org.wisdom.api.http.Context;
import org.wisdom.api.http.Request;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.api.router.Route;

import java.lang.reflect.InvocationTargetException;
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
    private Object[] parameters;

    /**
     * The iterator to retrieve the ordered set of filters.
     */
    private ListIterator<Filter> iterator;

    /**
     * Creates a new Interception Context. Instances should only be created by the router.
     *
     * @param route        the intercepted route
     * @param chain        the ordered interception chain containing filters and interceptors.
     * @param interceptors the set of interceptors and their configuration
     * @param parameters   the parameters (can be {@code null} if not computed yet)
     */
    public RequestContext(Route route, List<Filter> chain, Map<Interceptor<?>, Object> interceptors,
                          Object[] parameters, Filter endOfChainInvoker) {
        this.route = route;
        this.interceptors = interceptors;

        this.chain = new LinkedList<>(chain);
        if (parameters != null) {
            this.parameters = Arrays.copyOf(parameters, parameters.length);
        }

        // Add the action invocation
        if (endOfChainInvoker == null) {
            endOfChainInvoker = new ActionInvoker();
        }
        this.chain.add(endOfChainInvoker);
    }

    /**
     * Sets or Updates the parameters of the action method. This method must not be called by filters or interceptors.
     * This method is called once the parameter values are computed, this can happen after the creation of the {@link
     * org.wisdom.api.interception.RequestContext} instance. This method should only be called by the router instance.
     * @param parameters the parameters
     * @return the current {@link org.wisdom.api.interception.RequestContext}
     */
    public RequestContext setParameters(Object[] parameters) {
        this.parameters = parameters;
        return this;
    }

    /**
     * Retrieves the configuration annotation for the given interceptor.
     *
     * @param interceptor the interceptor
     * @return the configuration, {@code null} if not found
     */
    <A> A getConfigurationForInterceptor(Interceptor<A> interceptor) {
        return (A) interceptors.get(interceptor);
    }

    /**
     * Calls the next interceptors.
     *
     * @return the result from the next interceptor
     * @throws java.lang.Exception if the invocation fails.
     */
    public Result proceed() throws Exception {
        if (iterator == null) {
            iterator = chain.listIterator();
        }
        if (!iterator.hasNext()) {
            throw new IllegalStateException("Reached the end of the chain without result.");
        }
        Filter filter = iterator.next();
        return filter.call(route, this);
    }

    /**
     * @return the HTTP context.
     */
    public Context context() {
        return Context.CONTEXT.get();
    }

    /**
     * @return the incoming request.
     */
    public Request request() {
        return context().request();
    }

    /**
     * @return the invoked route.
     */
    public Route route() {
        return route;
    }

    /**
     * Access to the data stored in the request's scope.
     * It lets filters and interceptors sharing data. All modifications will be seen by the other participants of the
     * chain, action methods and templates.
     *
     * @return the data
     */
    public Map<String, Object> data() {
        return request().data();
    }

    /**
     * The end (actually middle) of the chain. This interceptor is a fake calling the action method.
     * It does not call {@link RequestContext#proceed()}.
     */
    private class ActionInvoker implements Filter {

        /**
         * We are the end of the chain, so we call the action method.
         * If the route is unbound, there are no action method, a {@literal 404 - NOT FOUND} result is returned.
         *
         * @param route   the intercepted route
         * @param context the filter context
         * @return the result of the action method, {@literal 404 - NOT FOUND} for unbound routes.
         * @throws java.lang.reflect.InvocationTargetException if the action method throws an exception
         * @throws java.lang.IllegalAccessException            if the action method cannot be called
         */
        @Override
        public Result call(Route route, RequestContext context) throws InvocationTargetException, IllegalAccessException {
            if (RequestContext.this.route.isUnbound()) {
                return Results.notFound();
            } else {
                return (Result) RequestContext.this.route.getControllerMethod().invoke(
                        RequestContext.this.route.getControllerObject(), parameters);
            }
        }

        /**
         * @return {@literal null} as it's meaningless here.
         */
        @Override
        public Pattern uri() {
            // Not meaningful here.
            return null;
        }

        /**
         * @return {@literal -1} as it's meaningless here.
         */
        @Override
        public int priority() {
            // Anyway, we're the last.
            return -1;
        }
    }
}
