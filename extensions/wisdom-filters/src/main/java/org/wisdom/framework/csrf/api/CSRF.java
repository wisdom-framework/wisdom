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
package org.wisdom.framework.csrf.api;

import org.wisdom.api.annotations.Interception;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that provides CSRF protection. Annotated action methods are only executed if the CSRF check has been
 * passed before. If the pass fails, a {@link org.wisdom.framework.csrf.api.CSRFErrorHandler} is called.
 * <p>
 * Notice that the CSRF token changes on each request.
 * <p>
 * The token can be injected using: {@code @HttpParameter(CSRFAdd.CSRF_TOKEN) String token}.
 */
@Interception
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CSRF {

    /**
     * The key where the generated token is stored in the request scope.
     */
    public static final String CSRF_TOKEN = CSRFService.TOKEN_KEY;
}
