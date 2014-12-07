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

import com.google.common.base.Strings;
import org.wisdom.api.content.ParameterFactories;
import org.wisdom.api.cookies.Cookie;
import org.wisdom.api.cookies.FlashCookie;
import org.wisdom.api.cookies.SessionCookie;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.Request;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.parameters.ActionParameter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * The handler managing the {@link org.wisdom.api.router.parameters.Source#HTTP} source. It looks up is made by the
 * parameter's type and if ambiguous by a hint given as parameter:
 * <ol>
 * <li>if the parameter's type is {@link org.wisdom.api.http.Context}, the HTTP context is injected</li>
 * <li>if the parameter's type is {@link org.wisdom.api.http.Request}, the HTTP Request is injected</li>
 * <li>if the parameter's type is {@link org.wisdom.api.cookies.Cookie}, the Cookie corresponding to the argument's
 * name is injected.</li>
 * <li>if the parameter's type is {@link org.wisdom.api.cookies.SessionCookie}, the Session Cookie is injected.</li>
 * <li>if the parameter's type is {@link org.wisdom.api.cookies.FlashCookie}, the Flash Cookie is injected.</li>
 * <li>if the parameter's type is {@link org.wisdom.api.router.Route}, the route is injected.</li>
 * <li>if the parameter's type is {@link java.io.Reader}, the reader on the request body is injected.</li>
 * <li>otherwise, the value is looked up in the request scope and in the HTTP Headers. First, we look up for the
 * value in the request scope, and return it as it is. If not, we look up in the HTTP Header. The value is
 * converted to the parameter's type using the converter engine. Both case requires a {@code String} parameter
 * indicating either the key (for the request scope) or the header name (for headers).</li>
 * </ol>
 * <p>
 * The type is inferred from the argument type.
 * <p>
 * This annotation is retrieved and analyzed at runtime (by the router).
 */
public class HttpHandler implements RouteParameterHandler {
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
        if (argument.getRawType().equals(Context.class)) {
            return context;
        }

        // Check whether we have a ParameterFactory that can handle the creation.
        Set<Class> handled = engine.getTypesHandledByFactories();
        if (handled.contains(argument.getRawType())) {
            return engine.newInstance(context, argument.getRawType());
        }

        if (argument.getRawType().equals(Request.class)) {
            return context.request();
        }

        if (argument.getRawType().equals(SessionCookie.class)) {
            return context.session();
        }

        if (argument.getRawType().equals(FlashCookie.class)) {
            return context.flash();
        }

        if (argument.getRawType().equals(Cookie.class)) {
            if (!Strings.isNullOrEmpty(argument.getName())) {
                return context.cookie(argument.getName());
            } else {
                throw new IllegalArgumentException("Missing cookie's name set in the @HttpParameter annotation");
            }
        }

        if (argument.getRawType().equals(Route.class)) {
            return context.route();
        }

        if (argument.getRawType().equals(BufferedReader.class) || argument.getRawType().equals(Reader.class)) {
            try {
                return context.reader();
            } catch (IOException e) {
                throw new IllegalArgumentException("Cannot inject the reader object in the @HttpParameter injected " +
                        "parameter", e);
            }
        }

        // Ran out of possibilities based on the type, check for the request scope and then HTTP headers
        if (Strings.isNullOrEmpty(argument.getName())) {
            throw new IllegalArgumentException("Cannot inject the value of a HTTP header and request scope value in " +
                    "the @HttpParameter - header's name not defined");
        } else {
            final Object requestScopeValue = context.request().data().get(argument.getName());
            if (requestScopeValue != null) {
                return requestScopeValue;
            } else {
                if (context.header(argument.getName()) != null || argument.getDefaultValue() != null) {
                    return engine.convertValues(context.headers(argument.getName()), argument.getRawType(),
                            argument.getGenericType(), argument.getDefaultValue());
                } else {
                    // No value.
                    if (List.class.isAssignableFrom(argument.getRawType())) {
                        return Collections.emptyList();
                    }
                    if (Set.class.isAssignableFrom(argument.getRawType())) {
                        return Collections.emptySet();
                    }
                    if (Collections.class.isAssignableFrom(argument.getRawType())) {
                        return Collections.emptyList();
                    }
                    return null;
                }
            }
        }

    }
}
