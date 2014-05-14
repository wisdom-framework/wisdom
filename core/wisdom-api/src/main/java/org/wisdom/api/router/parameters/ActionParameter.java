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
package org.wisdom.api.router.parameters;

import org.wisdom.api.annotations.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Formal parameter metadata on action method and callbacks.
 */
public class ActionParameter {

    private static final Map<Class<? extends Annotation>, ParameterFactory> BINDINGS = new HashMap<>();

    static {
        BINDINGS.put(Parameter.class, new ParameterFactory<Parameter>() {
            @Override
            public ActionParameter build(Parameter parameter, Class rawClass,
                                         Type genericType) throws IllegalArgumentException {
                return new ActionParameter(parameter.value(),
                        Source.PARAMETER, rawClass, genericType);
            }
        });

        BINDINGS.put(PathParameter.class, new ParameterFactory<Parameter>() {
            @Override
            public ActionParameter build(Parameter parameter, Class rawClass,
                                         Type genericType) throws IllegalArgumentException {
                return new ActionParameter(parameter.value(),
                        Source.PATH, rawClass, genericType);
            }
        });

        BINDINGS.put(QueryParameter.class, new ParameterFactory<Parameter>() {
            @Override
            public ActionParameter build(Parameter parameter, Class rawClass,
                                         Type genericType) throws IllegalArgumentException {
                return new ActionParameter(parameter.value(),
                        Source.QUERY, rawClass, genericType);
            }
        });

        BINDINGS.put(Attribute.class, new ParameterFactory<Attribute>() {
            @Override
            public ActionParameter build(Attribute parameter, Class rawClass,
                                         Type genericType) throws IllegalArgumentException {
                return new ActionParameter(parameter.value(),
                        Source.FORM, rawClass, genericType);
            }
        });

        BINDINGS.put(FormParameter.class, new ParameterFactory<FormParameter>() {
            @Override
            public ActionParameter build(FormParameter parameter, Class rawClass,
                                         Type genericType) throws IllegalArgumentException {
                return new ActionParameter(parameter.value(),
                        Source.FORM, rawClass, genericType);
            }
        });

        BINDINGS.put(Body.class, new ParameterFactory<Body>() {
            @Override
            public ActionParameter build(Body parameter, Class rawClass,
                                         Type genericType) throws IllegalArgumentException {
                return new ActionParameter(null,
                        Source.BODY, rawClass, genericType);
            }
        });
    }

    private final String name;
    private final Source source;
    private final Class<?> rawType;
    private final Type genericType;
    private String defaultValue;

    public static ActionParameter from(Method method, Annotation[] annotations, Class rawType, Type genericType) {
        ActionParameter parameter = null;
        String defaultValue = null;
        for (Annotation annotation : annotations) {
            if (annotation instanceof DefaultValue) {
                defaultValue = ((DefaultValue) annotation).value();
            } else {
                ParameterFactory factory = BINDINGS.get(annotation.annotationType());
                if (factory != null) {
                    parameter = factory.build(annotation, rawType, genericType);
                }
            }
        }

        if (parameter == null) {
            // All parameters must have been annotated.
            throw new RuntimeException("The method " + method.getDeclaringClass().getName() + "." + method.getName() +
                    " has a parameter  without  annotations indicating the injected data");
        } else {
            parameter.setDefaultValue(defaultValue);
        }

        return parameter;
    }

    /**
     * Creates a new Argument.
     *
     * @param name    the name
     * @param source  the source
     * @param rawType the type
     */
    public ActionParameter(String name, Source source, Class<?> rawType) {
        this(name, source, rawType, null);
    }

    /**
     * Creates a new Argument.
     *
     * @param name    the name
     * @param source  the source
     * @param rawType the type
     */
    public ActionParameter(String name, Source source, Class<?> rawType, Type genericType) {
        this.name = name;
        this.source = source;
        this.rawType = rawType;
        this.genericType = genericType;
    }

    /**
     * Sets the default value.
     *
     * @param dv the default value
     */
    public void setDefaultValue(String dv) {
        defaultValue = dv;
    }

    /**
     * @return the argument's name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the argument's source.
     */
    public Source getSource() {
        return source;
    }

    /**
     * @return the argument's type (main class).
     */
    public Class<?> getRawType() {
        return rawType;
    }

    /**
     * @return information on generics if any.
     */
    public Type getGenericType() {
        return genericType;
    }

    /**
     * @return the default value if any.
     */
    public String getDefaultValue() {
        return defaultValue;
    }
}
