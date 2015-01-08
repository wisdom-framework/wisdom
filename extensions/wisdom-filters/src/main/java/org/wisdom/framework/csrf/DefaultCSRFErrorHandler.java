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

import org.wisdom.api.http.Context;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.framework.csrf.api.CSRFErrorHandler;

/**
 * The default implementation of {@link org.wisdom.framework.csrf.api.CSRFErrorHandler}.
 */
public class DefaultCSRFErrorHandler implements CSRFErrorHandler {

    /**
     * Returns a {@code FORBIDDEN} result.
     *
     * @param context the current context
     * @param reason  the error message
     * @return a {@code FORBIDDEN} result
     */
    @Override
    public Result onError(Context context, String reason) {
        return Results.forbidden(reason);
    }
}
