/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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
package org.wisdom.framework.csrf;

import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.annotations.Service;
import org.wisdom.api.http.Result;
import org.wisdom.api.interception.Interceptor;
import org.wisdom.api.interception.RequestContext;
import org.wisdom.framework.csrf.api.CSRF;
import org.wisdom.framework.csrf.api.CSRFService;

@Service(Interceptor.class)
public class CSRFInterceptor extends Interceptor<CSRF> {

    @Requires
    public
    CSRFService csrf;

    /**
     * The interception method. The method should call {@link org.wisdom.api.interception.RequestContext#proceed()} to call the next
     * interception. Without this call it cuts the chain.
     *
     * @param configuration the interception configuration
     * @param context       the interception context
     * @return the result
     * @throws Exception if anything bad happen
     */
    @Override
    public Result call(CSRF configuration, RequestContext context) throws Exception {
        if (csrf.isValidRequest(context.context())) {
            String token = csrf.generateToken(context.context());
            return csrf.addTokenToResult(context.context(), token, context.proceed());
        } else {
            // CSRF Error
            return csrf.clearTokenIfInvalid(context.context(), "CSRF check failed - invalid or missing token");
        }
    }

    /**
     * Gets the annotation class configuring the current interceptor.
     *
     * @return the annotation
     */
    @Override
    public Class<CSRF> annotation() {
        return CSRF.class;
    }
}
