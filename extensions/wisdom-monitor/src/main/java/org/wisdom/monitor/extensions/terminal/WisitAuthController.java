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
package org.wisdom.monitor.extensions.terminal;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Body;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.Result;
import org.wisdom.api.security.Authenticator;

import java.util.UUID;

import static org.wisdom.api.http.HttpMethod.GET;
import static org.wisdom.api.http.HttpMethod.POST;

/**
 * Simple Authenticator used for the Wisit Terminal.
 *
 * @author <a href="mailto:jbardin@tech-arts.com">Jonathan M. Bardin</a>
 */
@Component
@Path("/monitor/terminal")
public class WisitAuthController extends DefaultController implements Authenticator {
    public static final String ADMIN = "admin";
    public static final String SESSION_KEY_WISIT_USER = "wisit-user";
    public static final String CONF_KEY_WISIT_PASS = "wisit.pass";

    /**
     * ApplicationConfiguration service (application.conf), use to retrieve wisit username and password.
     */
    @Requires
    private ApplicationConfiguration conf;

    /**
     * Default wisit user account for the wisdom terminal.
     */
    private final Credential wisitUser = new Credential(ADMIN, ADMIN);

    @Override
    public String getUserName(Context context) {
        return context.session().get(SESSION_KEY_WISIT_USER);
    }

    @Override
    public Result onUnauthorized(Context context) {
        return unauthorized();
    }

    @Validate
    public void start() {
        //Get the credentials from the application.conf
        String username = conf.get(SESSION_KEY_WISIT_USER);
        String pass = conf.get(CONF_KEY_WISIT_PASS);

        if (username != null) {
            wisitUser.setUser(username);
        }

        if (pass != null) {
            wisitUser.setPass(pass);
        }
    }

    /**
     * Login an user from it's Credential. It creates a session
     * for the successfully authenticated user.
     *
     * @param credential The Credential used in order to authenticate the user.
     * @return OK and the user session key if <code>credential</code> is valid.
     */
    @Route(method = POST, uri = "/login")
    public Result login(@Body Credential credential) {
        if (credential.getUser() == null || credential.getPass() == null) {
            return badRequest();
        }

        if (!credential.equals(wisitUser)) {
            return unauthorized();
        }

        session().put(SESSION_KEY_WISIT_USER, credential.getUser());
        return ok(UUID.randomUUID().toString());
    }

    /**
     * Logout the user, destroy its session.
     *
     * @return OK
     */
    @Route(method = GET, uri = "/logout")
    public Result logout() {
        session().clear();
        return ok();
    }
}
