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

import org.wisdom.api.http.Context;
import org.wisdom.api.http.Result;

/**
 * Service interface to expose if you need to customize the result when an CSRF error is detected. By default it
 * return a {@code FORBIDDEN} result. Notice that only one provide of this service can be used.
 */
public interface CSRFErrorHandler {

    /**
     * Called when a CSRF error is detected. This callback let you customize the result to return to the client. By
     * default, a {@code FORBIDDEN} result is returned
     *
     * @param context the current context
     * @param reason  the error message
     * @return the result to return to the client
     */
    public Result onError(Context context, String reason);

}
