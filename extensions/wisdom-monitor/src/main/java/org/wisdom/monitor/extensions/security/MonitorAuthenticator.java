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
package org.wisdom.monitor.extensions.security;

import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.api.security.Authenticator;

/**
 * The authenticator used to authorized users on the monitor.
 */
@Component
@Provides
@Instantiate
public class MonitorAuthenticator implements Authenticator {

    @Requires
    ApplicationConfiguration configuration;

    public static final Logger LOGGER = LoggerFactory.getLogger(MonitorAuthenticator.class);
    private String username;
    private Boolean enabled;

    /**
     * Fails if the username and password are not set in the application configuration.
     */
    @Validate
    public void ensureCredentials() {
        username = configuration.getOrDie("monitor.auth.username");
        enabled = configuration.getBooleanWithDefault("monitor.auth.enabled", true);
    }


    /**
     * @return "Monitor-Authenticator".
     */
    @Override
    public String getName() {
        return "Monitor-Authenticator";
    }

    /**
     * Retrieves the username from the HTTP context.
     * It reads the 'wisdom.monitor.username' in the session, and checks it is equal to the username set in the
     * application configuration.
     *
     * @param context the context
     * @return {@literal null} if the user is not authenticated, the user name otherwise.
     */
    @Override
    public String getUserName(Context context) {
        if (!enabled) {
            // Fake user.
            return "admin";
        }

        String user = context.session().get("wisdom.monitor.username");
        if (user != null && user.equals(username)) {
            return user;
        }

        return null;
    }


    /**
     * Generates an alternative result if the user is not authenticated. It should be a '401 Not Authorized' response.
     *
     * @param context the context
     * @return the result.
     */
    @Override
    public Result onUnauthorized(Context context) {
        return Results.redirect("/monitor/login");
    }
}
