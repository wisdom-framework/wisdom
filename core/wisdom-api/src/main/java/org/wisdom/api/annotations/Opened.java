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
 * Specifies a callback notified when a web socket is opened.
 * Like other web socket callback a special parameter is provided to retrieve the client's id. To retrieve it, use the
 * parameter named 'client': <code>@Parameter("client") String client</code>.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Opened {

    /**
     * The uri of the web sockets. As for route this uri can take contain placeholders:
     * <code>
     *     <pre>
     *         /foo/{id}
     *         /foo/{id}/{name}
     *         /foo/{id<[0-9]+>}
     *         /foo/{path*}
     *     </pre>
     * </code>
     * As a consequence, the identify callbacks can be notified for several web sockets. The method has to track the
     * open / close events to detect when all sockets are closed.
     */
    String value();


}
