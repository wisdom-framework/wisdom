package org.wisdom.router;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.wisdom.api.Controller;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.router.RouteUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Common logic shared by all callbacks.
 */
public class DefaultWebSocketCallback {

    private final Controller controller;
	private final Method method;
    private final Pattern regex;
    private final ImmutableList<String> parameterNames;
    protected List<RouteUtils.Argument> arguments;

    public DefaultWebSocketCallback(Controller controller, Method method, String uri) {
        this.controller = controller;
        this.method = method;
        this.regex = Pattern.compile(RouteUtils.convertRawUriToRegex(uri));
        this.parameterNames = ImmutableList.copyOf(RouteUtils.extractParameters(uri));
    }

    public Controller getController() {
		return controller;
	}

	public Method getMethod() {
		return method;
	}

	public Pattern getRegex() {
		return regex;
	}
    
    public List<RouteUtils.Argument> buildArguments(Method method) {
        List<RouteUtils.Argument> args = new ArrayList<>();
        Annotation[][] annotations = method.getParameterAnnotations();
        Class<?>[] typesOfParameters = method.getParameterTypes();
        for (int i = 0; i < annotations.length; i++) {
            boolean sourceDetected = false;
            for (int j = 0; !sourceDetected && j < annotations[i].length; j++) {
                Annotation annotation = annotations[i][j];
                if (annotation instanceof Parameter) {
                    Parameter parameter = (Parameter) annotation;
                    args.add(new RouteUtils.Argument(parameter.value(),
                            RouteUtils.Source.PARAMETER, typesOfParameters[i]));
                    sourceDetected = true;
                }
            }
            if (!sourceDetected) {
                // All parameters must have been annotated.
                WebSocketRouter.logger.error("The method {} has a parameter without annotations indicating " +
                        " the injected data. Only @Parameter annotations are supported in web sockets callbacks.",
                        method.getName());
                return new ArrayList<>();
            }
        }
        return args;
    }

    public boolean matches(String url) {
        return regex.matcher(url).matches();
    }

    public boolean check() {
        if (!method.getReturnType().equals(Void.TYPE)) {
            WebSocketRouter.logger.error("The method {} annotated with a web socket callback is not well-formed. " +
                    "These methods receive only parameter annotated with @Parameter and do not return anything",
                    method.getName());
            return false;
        }

        List<RouteUtils.Argument> localArguments = buildArguments(method);
        if (localArguments == null) {
            return false;
        } else {
            this.arguments = localArguments;
            return true;
        }
    }

    public Map<String, String> getPathParametersEncoded(String uri) {
        Map<String, String> map = Maps.newHashMap();
        Matcher m = regex.matcher(uri);
        if (m.matches()) {
            for (int i = 1; i < m.groupCount() + 1; i++) {
                map.put(parameterNames.get(i - 1), m.group(i));
            }
        }
        return map;
    }

    public void invoke(String uri) throws InvocationTargetException, IllegalAccessException {
        Map<String, String> values = getPathParametersEncoded(uri);
        Object[] parameters = new Object[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            RouteUtils.Argument argument = arguments.get(i);
            parameters[i] = RouteUtils.getParameter(argument, values);
        }
        method.invoke(controller, parameters);
    }
}
