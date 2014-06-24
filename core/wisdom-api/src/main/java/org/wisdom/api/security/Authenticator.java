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
package org.wisdom.api.security;


import org.wisdom.api.http.Context;
import org.wisdom.api.http.Result;

/**
 * Authentication service.
 * <p>
 * Providers of this service will be called every time an action annotated with the {@link org.wisdom.api.security
 * .Authenticated} annotation is accessed.
 * <p>
 * Generally, only one provider is exposed. However, when several providers are there,
 * the {@link org.wisdom.api.security.Authenticated} annotation can specify which implementation of the service to
 * use (using the value returned by {@link #getName()}.
 */
public interface Authenticator {

    /**
     * @return the name of the authenticator used to select the authenticator service in the
     * {@link org.wisdom.api.security.Authenticated} annotation. Must not be {@literal null}.
     */
    String getName();

    /**
     * Retrieves the username from the HTTP context; the default is to read from the session cookie.
     *
     * @param context the context
     * @return {@literal null} if the user is not authenticated, the user name otherwise.
     */
    String getUserName(Context context);

    /**
     * Generates an alternative result if the user is not authenticated. It should be a '401 Not Authorized' response.
     *
     * @param context the context
     * @return the result.
     */
    Result onUnauthorized(Context context);

}
