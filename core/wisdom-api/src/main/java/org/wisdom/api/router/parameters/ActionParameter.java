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
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Formal parameter metadata on action method and callbacks.
 */
public class ActionParameter {

    /**
     * Stores the annotation bindings.
     */
    private static final Map<Class<? extends Annotation>, ParameterFactory> BINDINGS = new HashMap<>();

    /**
     * Register the bindings.
     */
    static {
        BINDINGS.put(Parameter.class, new ParameterFactory<Parameter>() {
            /**
             * Creates a 'parameter' action parameter.
             *
             * @param parameter   the annotation
             * @param rawClass    the class of the formal parameter in the action method
             * @param genericType metadata on generics if any
             * @return the action parameter.
             */
            @Override
            public ActionParameter build(Parameter parameter, Class rawClass,
                                         Type genericType) {
                return new ActionParameter(parameter.value(),
                        Source.PARAMETER, rawClass, genericType);
            }
        });

        BINDINGS.put(PathParameter.class, new ParameterFactory<PathParameter>() {
            /**
             * Creates a 'path' action parameter.
             *
             * @param parameter   the annotation
             * @param rawClass    the class of the formal parameter in the action method
             * @param genericType metadata on generics if any
             * @return the action parameter.
             */
            @Override
            public ActionParameter build(PathParameter parameter, Class rawClass,
                                         Type genericType) {
                return new ActionParameter(parameter.value(),
                        Source.PATH, rawClass, genericType);
            }
        });

        BINDINGS.put(QueryParameter.class, new ParameterFactory<QueryParameter>() {
            /**
             * Creates a 'query' action parameter.
             *
             * @param parameter   the annotation
             * @param rawClass    the class of the formal parameter in the action method
             * @param genericType metadata on generics if any
             * @return the action parameter.
             */
            @Override
            public ActionParameter build(QueryParameter parameter, Class rawClass,
                                         Type genericType) {
                return new ActionParameter(parameter.value(),
                        Source.QUERY, rawClass, genericType);
            }
        });

        BINDINGS.put(Attribute.class, new ParameterFactory<Attribute>() {
            /**
             * Creates a 'attribute' action parameter. This binding is deprecated.
             *
             * @param parameter   the annotation
             * @param rawClass    the class of the formal parameter in the action method
             * @param genericType metadata on generics if any
             * @return the action parameter.
             */
            @Override
            public ActionParameter build(Attribute parameter, Class rawClass,
                                         Type genericType) {
                return new ActionParameter(parameter.value(),
                        Source.FORM, rawClass, genericType);
            }
        });

        BINDINGS.put(FormParameter.class, new ParameterFactory<FormParameter>() {
            /**
             * Creates a 'form' action parameter.
             *
             * @param parameter   the annotation
             * @param rawClass    the class of the formal parameter in the action method
             * @param genericType metadata on generics if any
             * @return the action parameter.
             */
            @Override
            public ActionParameter build(FormParameter parameter, Class rawClass,
                                         Type genericType) {
                return new ActionParameter(parameter.value(),
                        Source.FORM, rawClass, genericType);
            }
        });

        BINDINGS.put(Body.class, new ParameterFactory<Body>() {
            /**
             * Creates a 'body' action parameter.
             *
             * @param parameter   the annotation
             * @param rawClass    the class of the formal parameter in the action method
             * @param genericType metadata on generics if any
             * @return the action parameter.
             */
            @Override
            public ActionParameter build(Body parameter, Class rawClass,
                                         Type genericType) {
                return new ActionParameter(null,
                        Source.BODY, rawClass, genericType);
            }
        });

        BINDINGS.put(HttpParameter.class, new ParameterFactory<HttpParameter>() {
            /**
             * Creates a 'Http' action parameter.
             *
             * @param parameter   the annotation
             * @param rawClass    the class of the formal parameter in the action method
             * @param genericType metadata on generics if any
             * @return the action parameter.
             */
            @Override
            public ActionParameter build(HttpParameter parameter, Class rawClass,
                                         Type genericType) {
                return new ActionParameter(parameter.value(),
                        Source.HTTP, rawClass, genericType);
            }
        });

        BINDINGS.put(BeanParameter.class, new ParameterFactory<BeanParameter>() {
            /**
             * Creates a 'bean' action parameter.
             *
             * @param parameter   the annotation
             * @param rawClass    the class of the formal parameter in the action method
             * @param genericType metadata on generics if any
             * @return the action parameter.
             */
            @Override
            public ActionParameter build(BeanParameter parameter, Class rawClass,
                                         Type genericType) {
                return new ActionParameter(null,
                        Source.BEAN, rawClass, genericType);
            }
        });
    }

    private final String name;
    private final Source source;
    private final Class<?> rawType;
    private final Type genericType;
    private String defaultValue;


    /**
     * Creates a new action parameter instance from the given parameter. Action Parameter contain the metadata of a
     * specific method or constructor parameter to identify the injected data.
     *
     * @param member      the constructor or method having the analyzed parameter.
     * @param annotations the parameter's annotations
     * @param rawType     the type of the parameter
     * @param genericType the generic type of the parameter
     * @return the built action parameter
     */
    public static ActionParameter from(Member member, Annotation[] annotations, Class<?> rawType,
                                       Type genericType) {
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
            throw new IllegalArgumentException("The member (Constructor or method) of " + member.getDeclaringClass()
                    .getName() + "." + member.getName() +
                    " has a parameter without annotations indicating the injected data");
        }

        parameter.setDefaultValue(defaultValue);

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
     * @param name        the name
     * @param source      the source
     * @param rawType     the type
     * @param genericType the generic type
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
