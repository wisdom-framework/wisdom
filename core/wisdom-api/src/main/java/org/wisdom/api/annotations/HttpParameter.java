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
package org.wisdom.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to inject a HTTP parameter. The @HttpParameter annotation is designed to ease the injection of HTTP
 * related attributes in action methods. Lookup is made by the parameter's type and if ambiguous by a hint given as
 * parameter:
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
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpParameter {

    /**
     * Value required only for cookies and headers.
     */
    String value() default "";

}
