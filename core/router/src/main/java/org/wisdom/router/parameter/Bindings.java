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

import org.slf4j.LoggerFactory;
import org.wisdom.api.content.ParameterFactories;
import org.wisdom.api.http.Context;
import org.wisdom.api.router.parameters.ActionParameter;
import org.wisdom.api.router.parameters.Source;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Annotation handler's entry point. Notice that the handler are not parsing the annotation but {@link org.wisdom.api
 * .router.RouteUtils.Argument} and the selection is made on the {@link org.wisdom.api.router.parameters.Source}.
 */
public class Bindings {

    private static final Map<Source, RouteParameterHandler> BINDINGS = new HashMap<>(); //NOSONAR

    static {
        bind(Source.BODY, new BodyHandler());
        bind(Source.PARAMETER, new ParameterHandler());
        bind(Source.QUERY, new ParameterHandler());
        bind(Source.PATH, new ParameterHandler());
        bind(Source.FORM, new AttributeHandler());
        bind(Source.HTTP, new HttpHandler());
        bind(Source.BEAN, new BeanHandler());
    }

    /**
     * Associates the given source to the given handler.
     *
     * @param source  the source, must not be {@code null}
     * @param handler the handler, must not be {@code null}
     */
    public static void bind(Source source, RouteParameterHandler handler) {
        if (BINDINGS.containsKey(source)) {
            LoggerFactory.getLogger(Bindings.class).warn("Replacing a route parameter binding for {} by {}",
                    source.name(), handler);
        }
        BINDINGS.put(source, handler);
    }

    /**
     * Creates the value to be injected.
     *
     * @param argument the argument
     * @param context  the context
     * @param engine   the engine
     * @return the created object
     */
    public static Object create(ActionParameter argument, Context context,
                                ParameterFactories engine) {
        RouteParameterHandler handler = BINDINGS.get(argument.getSource());
        if (handler != null) {
            return handler.create(argument, context, engine);
        } else {
            LoggerFactory.getLogger(Bindings.class).warn("Unsupported route parameter in method : {}",
                    argument.getSource().name());
            return null;
        }
    }

    /**
     * Checks whether or not the given type is a multiple.
     *
     * @param rawType the type
     * @return {@code true} if the type is an aggregate type
     */
    public static boolean supportMultipleValues(Class<?> rawType) {
        return rawType.isArray() || Collection.class.isAssignableFrom(rawType);
    }
}
