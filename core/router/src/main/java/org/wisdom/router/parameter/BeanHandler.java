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
package org.wisdom.router.parameter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.content.ParameterFactories;
import org.wisdom.api.http.Context;
import org.wisdom.api.router.parameters.ActionParameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the {@link org.wisdom.api.annotations.BeanParameter} annotated parameters.
 */
public class BeanHandler implements RouteParameterHandler {
    private static final java.lang.String SETTER_PREFIX = "set";

    private static final Logger LOGGER = LoggerFactory.getLogger(BeanHandler.class);

    /**
     * Creates the parameter's value.
     *
     * @param argument the argument
     * @param context  the current HTTP context
     * @param engine   the converter
     * @return the created object
     */
    @Override
    public Object create(ActionParameter argument, Context context, ParameterFactories engine) {
        Object object = createNewInstance(argument.getRawType(), context, engine);

        for (Method method : argument.getRawType().getMethods()) {
            if (method.getName().startsWith(SETTER_PREFIX)) {
                if (method.getParameterTypes().length != 1) {
                    LOGGER.warn("The class {} has a setter method called {} but with too many parameters to be " +
                                    "injected with the 'BeanParameter' annotation", argument.getRawType().getName(),
                            method.getName());
                    continue;
                }

                // Only 1 parameter
                Annotation[] annotation = method.getParameterAnnotations()[0];
                Class<?> typesOfParameter = method.getParameterTypes()[0];
                Type genericTypeOfParameter = method.getGenericParameterTypes()[0];
                ActionParameter parameter = ActionParameter.from(method, annotation, typesOfParameter,
                        genericTypeOfParameter);
                // An exception is thrown if we can't build the parameter object.
                Object value = Bindings.create(parameter, context, engine);
                if (value != null) {
                    inject(object, method, value);
                }
            }
        }
        return object;
    }

    private void inject(Object object, Method method, Object value) {
        try {
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            method.invoke(object, value);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Cannot inject the value " + value + " in the method " + method
                    .getName() + " from " + object.getClass().getName(), e);
        } catch (InvocationTargetException e) { //NOSONAR
            throw new IllegalArgumentException("Cannot inject the value " + value + " in the method " + method
                    .getName() + " from " + object.getClass().getName(), e.getTargetException());
        }
    }

    public static List<ActionParameter> buildActionParameterList(Constructor cst) {
        List<ActionParameter> arguments = new ArrayList<>();
        Annotation[][] annotations = cst.getParameterAnnotations();
        Class<?>[] typesOfParameters = cst.getParameterTypes();
        Type[] genericTypeOfParameters = cst.getGenericParameterTypes();
        for (int i = 0; i < annotations.length; i++) {
            arguments.add(ActionParameter.from(cst, annotations[i], typesOfParameters[i],
                    genericTypeOfParameters[i]));
        }
        return arguments;
    }


    private Object createNewInstance(Class<?> rawType, Context context, ParameterFactories engine) {
        try {
            // If we have an empty constructor use it.
            Constructor<?> cst = getNoArgConstructor(rawType);
            if (cst != null) {
                return cst.newInstance();
            }

            // Try to get a constructor with annotated parameters.
            cst = findConstructor(rawType);
            if (cst == null) {
                throw new IllegalArgumentException("Cannot build an instance of '" + rawType.getName() + "', " +
                        "cannot find a suitable constructor.");
            }

            List<ActionParameter> parameters = buildActionParameterList(cst);
            Object[] values = new Object[parameters.size()];
            for (int i = 0; i < parameters.size(); i++) {
                ActionParameter argument = parameters.get(i);
                values[i] = Bindings.create(argument, context, engine);
            }

            return cst.newInstance(values);

        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("Cannot build an instance of '" + rawType.getName(), e);
        } catch (InvocationTargetException e) { //NOSONAR
            throw new IllegalArgumentException("Cannot build an instance of '" + rawType.getName(), e.getTargetException());
        }
    }

    private Constructor<?> findConstructor(Class<?> rawType) {
        for (Constructor constructor : rawType.getConstructors()) {
            Annotation[][] annotations = constructor.getParameterAnnotations();
            // Just check that all parameters are annotated, a more in-depth check is done during the creation of the
            // actual parameter.
            for (Annotation[] param : annotations) {
                if (param.length == 0) {
                    // Not suitable.
                    continue;
                }
                return constructor;
            }
        }
        return null;
    }

    private static Constructor<?> getNoArgConstructor(Class<?> rawType) {
        try {
            return rawType.getConstructor();
        } catch (NoSuchMethodException e) { // NOSONAR
            // Ignore it.
        }
        return null;
    }
}

