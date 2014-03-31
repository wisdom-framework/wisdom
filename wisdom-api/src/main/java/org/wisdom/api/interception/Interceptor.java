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
package org.wisdom.api.interception;


import org.wisdom.api.http.Result;
import org.wisdom.api.router.Route;

import java.util.regex.Pattern;

/**
 * Class extended by interceptors.
 * An interceptor <em>intercepts</em> request and can customize the input and output of the request. When several
 * interceptors are used on a single invocation, a chain is built. Interceptor must call the
 * {@link RequestContext#proceed()} method to call the next interceptor.
 * @param <A> the type of the annotation used to configure the interceptor.
 */
public abstract class Interceptor<A> implements Filter {

    /**
     * The interception method. The method should call {@link RequestContext#proceed()} to call the next
     * interception. Without this call it cuts the chain.
     * @param configuration the interception configuration
     * @param context the interception context
     * @return the result
     * @throws Exception if anything bad happen
     */
    public abstract Result call(A configuration, RequestContext context) throws Exception;

    /**
     * Gets the annotation class configuring the current interceptor.
     * @return the annotation
     */
    public abstract Class<A> annotation();

    /**
     * The interception method. The method should call {@link org.wisdom.api.interception.RequestContext#proceed()}
     * to call the next interceptor. Without this call it cuts the chain.
     *
     * @param route the route
     * @param context the filter context
     * @return the result
     * @throws Exception if anything bad happen
     */
    @Override
    public final Result call(Route route, RequestContext context) throws Exception {
        return call(context.getConfigurationForInterceptor(this), context);
    }

    /**
     * Interceptors are attached to controller. Uri does not matter.
     * @return {@literal null}
     */
    @Override
    public Pattern uri() {
        return null;
    }

    /**
     * Interceptors are attached to controller. Order is defined using the annotation order.
     * @return {@literal -1}
     */
    @Override
    public int priority() {
        return -1;
    }
}
