package org.wisdom.router;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.wisdom.api.Controller;
import org.wisdom.api.annotations.Body;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.content.ContentEngine;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.router.RouteUtils;

/**
 * the on receive callback is a bit different as we need to handle the wrapping of the received message.
 */
public class OnMessageWebSocketCallback extends DefaultWebSocketCallback {

    public OnMessageWebSocketCallback(Controller controller, Method method, String uri) {
        super(controller, method, uri);
    }

    @Override
    public List<RouteUtils.Argument> buildArguments(Method method) {
        List<RouteUtils.Argument> arguments = new ArrayList<>();
        Annotation[][] annotations = method.getParameterAnnotations();
        Class<?>[] typesOfParameters = method.getParameterTypes();
        for (int i = 0; i < annotations.length; i++) {
            boolean sourceDetected = false;
            for (int j = 0; !sourceDetected && j < annotations[i].length; j++) {
                Annotation annotation = annotations[i][j];
                if (annotation instanceof Parameter) {
                    Parameter parameter = (Parameter) annotation;
                    arguments.add(new RouteUtils.Argument(parameter.value(),
                            RouteUtils.Source.PARAMETER, typesOfParameters[i]));
                    sourceDetected = true;
                }
                if (annotation instanceof Body) {
                    arguments.add(new RouteUtils.Argument(null,
                            RouteUtils.Source.BODY, typesOfParameters[i]));
                    sourceDetected = true;
                }
            }
            if (!sourceDetected) {
                // All parameters must have been annotated.
                WebSocketRouter.getLogger().error("The method {} has a parameter without annotations indicating " +
                        " the injected data. Only @Parameter and @Body annotations are supported in web sockets " +
                        "callbacks receiving events",
                        method.getName());
                return null;
            }
        }
        return arguments;
    }

    public void invoke(String uri, String client, byte[] content, ContentEngine engine) throws
            InvocationTargetException,
            IllegalAccessException {
        Map<String, String> values = getPathParametersEncoded(uri);
        Object[] parameters = new Object[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            RouteUtils.Argument argument = arguments.get(i);
            if (argument.getSource() == RouteUtils.Source.PARAMETER) {
                if (argument.getName().equals("client")  && argument.getType().equals(String.class)) {
                    parameters[i] = client;
                } else {
                    parameters[i] = RouteUtils.getParameter(argument, values);
                }
            } else {
                // Body
                parameters[i] = transform(argument.getType(), content, engine);
            }
        }
        getMethod().invoke(getController(), parameters);
    }

    private Object transform(Class<?> type, byte[] content, ContentEngine engine) {
        if (type.equals(String.class)) {
            return new String(content, Charset.defaultCharset());
        }
        if (type.equals(Integer.class)) {
            // Parse as string, wrap as boolean.
            String s = new String(content, Charset.defaultCharset());
            return Integer.parseInt(s);
        }
        if (type.equals(Boolean.class)) {
            // Parse as string, wrap as boolean.
            String s = new String(content, Charset.defaultCharset());
            return Boolean.parseBoolean(s);
        }
        // Byte Array
        if (type.isArray()  && type.getComponentType().equals(Byte.TYPE)) {
            return content;
        }
        // For all the other cases, we need a binder, however, we have no idea about the type of message,
        // for now we suppose it's json.
        return engine.getBodyParserEngineForContentType(MimeTypes.JSON).invoke(content, type);
    }
}
