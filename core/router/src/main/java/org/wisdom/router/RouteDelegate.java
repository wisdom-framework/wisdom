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
package org.wisdom.router;

import com.google.common.base.Preconditions;
import com.google.common.net.MediaType;
import org.wisdom.api.Controller;
import org.wisdom.api.annotations.Interception;
import org.wisdom.api.http.*;
import org.wisdom.api.interception.Filter;
import org.wisdom.api.interception.Interceptor;
import org.wisdom.api.interception.RequestContext;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.parameters.ActionParameter;
import org.wisdom.router.parameter.Bindings;

import javax.validation.Constraint;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Delegated route used for interception purpose.
 */
public class RouteDelegate extends Route {

    private final Route route;
    private final RequestRouter router;
    private final boolean mustValidate;
    private final Map<String, Object> interceptors;

    /**
     * Creates a new instance of {@link org.wisdom.router.RouteDelegate}.
     *
     * @param router the router
     * @param route  the delegate / wrapped route
     */
    public RouteDelegate(RequestRouter router, Route route) {
        this.route = route;
        this.router = router;
        if (!route.isUnbound()) {
            this.mustValidate = detectValidationRequirement(route.getControllerMethod());
            this.interceptors = extractInterceptors();
        } else {
            this.mustValidate = false;
            this.interceptors = Collections.emptyMap();
        }
    }

    private Map<String, Object> extractInterceptors() {
        Map<String, Object> map = new LinkedHashMap<>();
        Annotation[] classAnnotations = route.getControllerClass().getAnnotations();
        for (Annotation annotation : classAnnotations) {
            if (annotation.annotationType().isAnnotationPresent(Interception.class)) {
                // Interceptor detected.
                map.put(annotation.annotationType().getName(), annotation);
            }
        }
        // Check the method
        Annotation[] methodAnnotations = route.getControllerMethod().getAnnotations();
        for (Annotation annotation : methodAnnotations) {
            if (annotation.annotationType().isAnnotationPresent(Interception.class)) {
                // Interceptor detected.
                map.put(annotation.annotationType().getName(), annotation);
            }
        }

        return map;
    }

    private static boolean detectValidationRequirement(Method method) {
        Annotation[][] annotations = method.getParameterAnnotations();
        for (Annotation[] array : annotations) {
            for (Annotation annotation : array) {
                if (isConstraint(annotation)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines whether the given annotation is a 'constraint' or not.
     * It just checks if the annotation has the {@link Constraint} annotation on it or if the annotation is the {@link
     * Valid} annotation.
     *
     * @param annotation the annotation to check
     * @return {@code true} if the given annotation is a constraint
     */
    private static boolean isConstraint(Annotation annotation) {
        return annotation.annotationType().isAnnotationPresent(Constraint.class)
                || annotation.annotationType().equals(Valid.class);
    }

    @Override
    public String getUrl() {
        return route.getUrl();
    }

    @Override
    public HttpMethod getHttpMethod() {
        return route.getHttpMethod();
    }

    @Override
    public Class<? extends Controller> getControllerClass() {
        return route.getControllerClass();
    }

    @Override
    public Method getControllerMethod() {
        return route.getControllerMethod();
    }

    @Override
    public boolean matches(HttpMethod method, String uri) {
        return route.matches(method, uri);
    }

    @Override
    public boolean matches(String method, String uri) {
        return route.matches(method, uri);
    }

    @Override
    public Map<String, String> getPathParametersEncoded(String uri) {
        return route.getPathParametersEncoded(uri);
    }

    @Override
    public int isCompliantWithRequestContentType(Request request) {
        return route.isCompliantWithRequestContentType(request);
    }

    @Override
    public Route accepting(String... types) {
        return route.accepting(types);
    }

    @Override
    public Route accepts(String... types) {
        return route.accepts(types);
    }

    @Override
    public Set<MediaType> getAcceptedMediaTypes() {
        return route.getAcceptedMediaTypes();
    }

    @Override
    public Set<MediaType> getProducedMediaTypes() {
        return route.getProducedMediaTypes();
    }

    /**
     * Gets the HTTP Status to return for this unbound route. This method is meaningful only if the route is unbound
     * (and so cannot be served).
     *
     * @return {@link Status#NOT_FOUND} when there are no action method to handle the route,
     * {@link Status#UNSUPPORTED_MEDIA_TYPE} when the request content cannot be accepted.
     */
    @Override
    public int getUnboundStatus() {
        return route.getUnboundStatus();
    }

    @Override
    public Route produces(String... types) {
        return route.produces(types);
    }

    @Override
    public Route producing(String... provide) {
        return route.producing(provide);
    }

    @Override
    public boolean isCompliantWithRequestAccept(Request request) {
        return route.isCompliantWithRequestAccept(request);
    }

    @Override
    public Controller getControllerObject() {
        return route.getControllerObject();
    }

    @Override
    public List<ActionParameter> getArguments() {
        return route.getArguments();
    }

    @Override
    public Result invoke() throws Exception {
        Context context = Context.CONTEXT.get();
        Preconditions.checkNotNull(context);

        // Build chain if needed.
        // We get an immutable copy of the set.
        Set<Filter> filters = router.getFilters();
        // Interceptors will be handled after filters.
        List<Filter> chain = filters.stream()
                .filter(filter -> !(filter instanceof Interceptor)  && filter.uri().matcher(route.getUrl()).matches())
                .collect(Collectors.toList());

        Map<Interceptor<?>, Object> itcpConfiguration = new LinkedHashMap<>();
        if (!interceptors.isEmpty()) {
            for (Map.Entry<String, Object> entry : interceptors.entrySet()) {
                final Interceptor<?> interceptor = getInterceptorForAnnotation(entry.getKey());
                if (interceptor == null) {
                    return Results.badRequest("Missing interceptor handling " + entry.getKey());
                }
                itcpConfiguration.put(interceptor, entry.getValue());
                chain.add(interceptor);
            }
        }

        // Ready to call the action.
        Filter endOfChain = new EndOfChainInvoker();
        RequestContext ctx = new RequestContext(this, chain, itcpConfiguration, null, endOfChain);
        return ctx.proceed();
    }

    private Interceptor<?> getInterceptorForAnnotation(String className) {
        List<Interceptor<?>> localInterceptors = router.getInterceptors();
        if (localInterceptors == null) {
            return null;
        }
        for (Interceptor<?> interceptor : localInterceptors) {
            if (interceptor.annotation().getName().equals(className)) {
                return interceptor;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return route.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o instanceof Route) {  //NOSONAR we want to check for Route too.
            return route.equals(o);
        } else {
            return o.equals(this);
        }
    }

    @Override
    public int hashCode() {
        return route.hashCode();
    }

    @Override
    public boolean isUnbound() {
        return route.isUnbound();
    }

    private class EndOfChainInvoker implements Filter {
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
            if (isUnbound()) {
                return new Result().status(route.getUnboundStatus()).noContentIfNone();
            } else {

                // The interceptor and filter may have change some values, compute the parameters.
                final List<ActionParameter> arguments = getArguments();
                Object[] parameters = new Object[arguments.size()];
                for (int i = 0; i < arguments.size(); i++) {
                    ActionParameter argument = arguments.get(i);
                    parameters[i] = Bindings.create(argument, context.context(),
                            router.getParameterConverterEngine());
                }

                // Validate if needed.
                if (mustValidate) {
                    Validator validator = router.getValidator();
                    if (validator != null) {
                        Set<ConstraintViolation<Controller>> violations =
                                validator.forExecutables().validateParameters(getControllerObject(), getControllerMethod(),
                                        parameters);

                        if (!violations.isEmpty()) {
                            return Results.badRequest(violations).json();
                        }
                    }
                }

                // Sets the parameters.
                context.setParameters(parameters);

                // Invoke the action method.
                final Result result = (Result) getControllerMethod().invoke(
                        getControllerObject(), parameters);

                // Manage the VARY header if the route has a 'consume' set:
                if (! result.getHeaders().containsKey(HeaderNames.VARY)) {
                    String headers = null;
                    if (! getAcceptedMediaTypes().isEmpty()) {
                        headers = HeaderNames.CONTENT_TYPE;
                    }
                    if (! getProducedMediaTypes().isEmpty()) {
                        if (headers == null) {
                            headers = HeaderNames.ACCEPT;
                        } else {
                            headers += ", " + HeaderNames.ACCEPT;
                        }
                    }
                    if (headers != null) {
                        result.with(HeaderNames.VARY, headers);
                    }
                }

                // Manage produced types
                final Set<MediaType> mediaTypes = route.getProducedMediaTypes();
                if (mediaTypes.isEmpty()  || result.getContentType() != null
                        || result.getRenderable() != null  && result.getRenderable().mimetype() != null) {
                    return result;
                }

                // check whether we can set the produced media type
                if (mediaTypes.size() == 1) {
                    // Only one
                    result.as(mediaTypes.iterator().next().toString());
                }
                // Else we cannot do anything.

                return result;

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

