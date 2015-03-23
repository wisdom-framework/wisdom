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

import org.wisdom.api.http.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to declare the route of an action method.
 * <p/>
 * This annotation is retrieved and analyzed at runtime (by the router).
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Route {

    /**
     * The method of the route.
     */
    HttpMethod method();

    /**
     * The route's uri, with placeholders. Here are some examples:
     * <code>
     *     <pre>
     *         /foo/{id}
     *         /foo/{id}/{name}
     *         /foo/{id<[0-9]+>}
     *         /foo/{path*}
     *     </pre>
     * </code>
     */
    String uri();

    /**
     * The list of mime types accepted by the action method. By default, it accepts all content. Specified mime types
     * can use wildcards such as `text/*`.
     */
    String[] accepts() default {};

    /**
     * The list of mime types produced by the action method. Specified mime types cannot use wildcards.
     */
    String[] produces() default {};

}
