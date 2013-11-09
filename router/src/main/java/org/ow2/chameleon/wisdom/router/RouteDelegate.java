package org.ow2.chameleon.wisdom.router;

import com.google.common.base.Preconditions;
import org.ow2.chameleon.wisdom.api.Controller;
import org.ow2.chameleon.wisdom.api.http.Context;
import org.ow2.chameleon.wisdom.api.http.HttpMethod;
import org.ow2.chameleon.wisdom.api.http.Result;
import org.ow2.chameleon.wisdom.api.http.Results;
import org.ow2.chameleon.wisdom.api.router.Route;
import org.ow2.chameleon.wisdom.api.router.RouteUtils;

import javax.validation.Constraint;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Delegated route used for interception purpose.
 */
public class RouteDelegate extends Route {

    private final Route route;
    private final RouterImpl router;
    private final boolean mustValidate;

    public RouteDelegate(RouterImpl router, Route route) {
        this.route = route;
        this.router = router;
        this.mustValidate = detectValidationRequirement(route.getControllerMethod());
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
        Context context = Context.context.get();
        Preconditions.checkNotNull(context);
        List<RouteUtils.Argument> arguments = route.getArguments();
        Object[] parameters = new Object[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            RouteUtils.Argument argument = arguments.get(i);
            switch (argument.source) {
                case PARAMETER:
                    parameters[i] = RouteUtils.getParameter(argument, context);
                    break;
                case BODY:
                    parameters[i] = context.body(argument.type);
                    break;
                case ATTRIBUTE:
                    parameters[i] = RouteUtils.getAttribute(argument, context);
                    break;
            }
        }

        //TODO Validation here !
        if (mustValidate) {
            Validator validator = router.getValidator();
            if (validator != null) {
                Set<ConstraintViolation<Controller>> violations =
                                validator.forExecutables().validateParameters(getControllerObject(), getControllerMethod(),
                                        parameters);

                if (!violations.isEmpty()) {
                    // TODO Improve output here
                    return Results.badRequest(violations).json();
                }
            }
        }


        return (Result) getControllerMethod().invoke(getControllerObject(), parameters);
    }

    @Override
    public String toString() {
        return route.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Route) {
            return route.equals(o);
        } else {
            return o.equals(this);
        }
    }

    @Override
    public int hashCode() {
        return route.hashCode();
    }
}
