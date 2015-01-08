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
import org.wisdom.framework.csrf.api.AddCSRFToken;
import org.wisdom.framework.csrf.api.CSRFService;

/**
 * Interceptor managing actions using {@link org.wisdom.framework.csrf.api.AddCSRFToken}. It adds a CSRF token to the
 * result. However, it does not check for the incoming CSRF token, use the {@link org.wisdom.framework.csrf.api.CSRF}
 * annotation for this purpose.
 */
@Service(Interceptor.class)
public class AddCSRFTokenInterceptor extends Interceptor<AddCSRFToken> {

    @Requires
    public
    CSRFService csrf;

    /**
     * Injects a CSRF token into the result if the request is eligible.
     *
     * @param configuration the interception configuration
     * @param context       the interception context
     * @return the result with the token if a token was added
     * @throws Exception if anything bad happen
     */
    @Override
    public Result call(AddCSRFToken configuration, RequestContext context) throws Exception {
        if (csrf.eligibleForCSRF(context.context())
                && (csrf.extractTokenFromRequest(context.context()) == null || configuration.newTokenOnEachRequest())) {
            // Generate a new token, it will be stored in the request scope.
            String token = csrf.generateToken(context.context());
            // Add it to the response
            return csrf.addTokenToResult(context.context(), token, context.proceed());
        } else {
            return context.proceed();
        }
    }

    /**
     * Gets the annotation class configuring the current interceptor.
     *
     * @return CSRFAdd
     */
    @Override
    public Class<AddCSRFToken> annotation() {
        return AddCSRFToken.class;
    }
}
