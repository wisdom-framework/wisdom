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
package snippets.controllers.security;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.api.security.Authenticator;

// tag::authenticator[]
@Component
@Provides
@Instantiate
public class MyAuthenticator implements Authenticator {

    @Override
    public String getName() {
        return "my-authenticator";
    }

    @Override
    public String getUserName(Context context) {
        if (context.session().get("username") != null) {
            return context.session().get("username");
        }
        String username = context.parameter("username");
        if (username != null  && username.equals("admin")) {
            context.session().put("username", "admin");
            return "admin";
        } else {
            return null;
        }
    }

    @Override
    public Result onUnauthorized(Context context) {
         return Results.unauthorized("Your are not authenticated !");
    }
}
// end::authenticator[]
