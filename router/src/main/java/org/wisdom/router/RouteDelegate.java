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
import org.wisdom.api.Controller;
import org.wisdom.api.annotations.Interception;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.api.interception.Filter;
import org.wisdom.api.interception.Interceptor;
import org.wisdom.api.interception.RequestContext;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.RouteUtils;

import javax.validation.Constraint;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Delegated route used for interception purpose.
 */
public class RouteDelegate extends Route {

    private final Route route;
    private final RequestRouter router;
    private final boolean mustValidate;
    private final Map<String, Object> interceptors;

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
        if (route != null) {
            return route.getControllerMethod();
        } else {
            return null;
        }
    }

    @Override
    public boolean matches(HttpMethod method, String uri) {
        return route.matches(method, uri);
    }

    @Override
    public boolean matches(String httpMethod, String uri) {
        return route.matches(httpMethod, uri);
    }

    @Override
    public Map<String, String> getPathParametersEncoded(String uri) {
        return route.getPathParametersEncoded(uri);
    }

    @Override
    public Controller getControllerObject() {
        return route.getControllerObject();
    }

    @Override
    public List<RouteUtils.Argument> getArguments() {
        return route.getArguments();
    }

    @Override
    public Result invoke() throws Throwable {
        Context context = Context.CONTEXT.get();
        Preconditions.checkNotNull(context);
        List<RouteUtils.Argument> arguments = route.getArguments();
        Object[] parameters = new Object[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            RouteUtils.Argument argument = arguments.get(i);
            switch (argument.getSource()) {
                case PARAMETER:
                    parameters[i] = RouteUtils.getParameter(argument, context);
                    break;
                case BODY:
                    parameters[i] = context.body(argument.getType());
                    break;
                case ATTRIBUTE:
                    parameters[i] = RouteUtils.getAttribute(argument, context);
                    break;
                default:
                    break;
            }
        }

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


        // Build chain if needed.
        Set<Filter> filters = router.getFilters();
        List<Filter> chain = new ArrayList<Filter>();
        for (Filter filter : filters) {
            if (!(filter instanceof Interceptor)) {
                // Interceptors will be handled after filters.
                if (filter.uri().matcher(route.getUrl()).matches()) {
                    chain.add(filter);
                }
            }
        }

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
        RequestContext ctx = new RequestContext(this, chain, itcpConfiguration, parameters);
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
        } else if (o instanceof Route) {
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
}
