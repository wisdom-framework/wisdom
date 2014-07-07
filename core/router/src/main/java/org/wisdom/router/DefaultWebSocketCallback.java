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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.wisdom.api.Controller;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.router.RouteUtils;
import org.wisdom.api.router.parameters.ActionParameter;
import org.wisdom.api.router.parameters.Source;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Common logic shared by all web socket callbacks.
 */
public class DefaultWebSocketCallback {

    private final Controller controller;
    private final Method method;
    private final Pattern regex;
    private final ImmutableList<String> parameterNames;
    protected final WebSocketRouter router;
    protected List<ActionParameter> arguments;

    /**
     * Creates the callback object.
     *
     * @param controller the controller
     * @param method     the method to call
     * @param uri        the listened uri indicating when the callback need to be called, it can use the Wisdom's URI syntax
     *                   to specify dynamic parts.
     * @param router     the web socket router
     */
    public DefaultWebSocketCallback(Controller controller, Method method, String uri, WebSocketRouter router) {
        this.router = router;
        this.controller = controller;
        this.method = method;
        this.regex = Pattern.compile(RouteUtils.convertRawUriToRegex(uri));
        this.parameterNames = ImmutableList.copyOf(RouteUtils.extractParameters(uri));
    }

    /**
     * @return the controller.
     */
    public Controller getController() {
        return controller;
    }

    /**
     * @return the method.
     */
    public Method getMethod() {
        return method;
    }

    /**
     * @return the computed URI regular expression.
     */
    public Pattern getRegex() {
        return regex;
    }

    /**
     * Creates the list of parameter for the given method. WebSocket callbacks can only use {@link org.wisdom.api
     * .annotations.Parameter}. {@link org.wisdom.router.OnMessageWebSocketCallback} instances also support the
     * {@link org.wisdom.api.annotations.Body} annotation to retrieve the payload.
     * <p>
     * If a method's parameter is not annotated, this method fails.
     *
     * @param method the method
     * @return the list of parameter
     */
    public List<ActionParameter> buildArguments(Method method) {
        List<ActionParameter> args = new ArrayList<>();
        Annotation[][] annotations = method.getParameterAnnotations();
        Class<?>[] typesOfParameters = method.getParameterTypes();
        Type[] genericTypeOfParameters = method.getGenericParameterTypes();
        for (int i = 0; i < annotations.length; i++) {
            boolean sourceDetected = false;
            for (int j = 0; !sourceDetected && j < annotations[i].length; j++) {
                Annotation annotation = annotations[i][j];
                if (annotation instanceof Parameter) {
                    Parameter parameter = (Parameter) annotation;
                    args.add(new ActionParameter(parameter.value(),
                            Source.PARAMETER, typesOfParameters[i], genericTypeOfParameters[i]));
                    sourceDetected = true;
                }
            }
            if (!sourceDetected) {
                // All parameters must have been annotated.
                WebSocketRouter.getLogger().error("The method {} has a parameter without annotations indicating " +
                                " the injected data. Only @Parameter annotations are supported in web sockets callbacks.",
                        method.getName()
                );
                return Collections.emptyList();
            }
        }
        return args;
    }

    /**
     * Checks whether the given url matches the computed URI regular expression.
     *
     * @param url the url
     * @return {@code true} if the url matches, {@code false} otherwise
     */
    public boolean matches(String url) {
        return regex.matcher(url).matches();
    }

    /**
     * Checks that the callback is well-formed.
     *
     * @return {@code true} if the callback is well-formed, {@code false} otherwise.
     */
    public boolean check() {
        if (!method.getReturnType().equals(Void.TYPE)) {
            WebSocketRouter.getLogger().error("The method {} annotated with a web socket callback is not well-formed. " +
                            "These methods receive only parameter annotated with @Parameter and do not return anything",
                    method.getName()
            );
            return false;
        }

        List<ActionParameter> localArguments = buildArguments(method);
        if (localArguments == null) {
            return false;
        } else {
            this.arguments = localArguments;
            return true;
        }
    }

    /**
     * Gets the map of parameter (name - value).
     *
     * @param uri the uri
     * @return the map of parameter
     */
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

    /**
     * Invokes the callback.
     *
     * @param uri     the uri
     * @param client  the client identifier (the one having sent the message)
     * @param content the payload of the message
     * @throws InvocationTargetException when the callback throws an exception
     * @throws IllegalAccessException    when the callback cannot be called
     */
    public void invoke(String uri, String client, byte[] content) throws
            InvocationTargetException,
            IllegalAccessException {
        Map<String, String> values = getPathParametersEncoded(uri);
        Object[] parameters = new Object[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            ActionParameter argument = arguments.get(i);
            if (argument.getSource() == Source.PARAMETER) {
                if (argument.getName().equals("client") && argument.getRawType().equals(String.class)) {
                    parameters[i] = client;
                } else {
                    parameters[i] = router.converter().convertValue(values.get(argument.getName()),
                            argument.getRawType(), argument.getGenericType(), argument.getDefaultValue());
                }
            } else {
                // Body
                parameters[i] = transform(argument, content);
            }
        }
        getMethod().invoke(getController(), parameters);
    }

    private Object transform(ActionParameter parameter, byte[] content) {
        String data = new String(content, Charset.defaultCharset());
        try {
            return router.converter().convertValue(data, parameter.getRawType(), parameter.getGenericType(), null);
        } catch (IllegalArgumentException | NoSuchElementException e) { //NOSONAR
            // The NoSuchElementException is thrown when there are no suitable converter,
            // while the IllegalArgumentException is thrown when the conversion fails. In both case,
            // the conversion failed.
        }

        // For all the other cases, we need a binder, however, we have no idea about the type of message,
        // for now we suppose it's json.
        return router.engine().getBodyParserEngineForContentType(MimeTypes.JSON).invoke(content, parameter.getRawType());
    }
}
