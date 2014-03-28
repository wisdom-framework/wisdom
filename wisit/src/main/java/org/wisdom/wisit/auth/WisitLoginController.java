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
package org.wisdom.wisit.auth;

import org.apache.felix.ipojo.annotations.*;
import org.wisdom.api.Controller;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Body;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.http.Result;

import java.util.UUID;

import static org.wisdom.api.http.HttpMethod.GET;
import static org.wisdom.api.http.HttpMethod.POST;

/**
 * Wisdom Terminal login controller.
 *
 * It use cookies-session in order to handle the authentication.
 * The user credentials can be set from the application.conf using:
 * WISIT_USER and WISIT_PASS property.
 *
 * The default credentials are admin/admin.
 *
 * The routes are the following:
 *
 * POST -d '{user: <user>, pass: <pass>}' /wisit/login
 * GET                                    /wisit/logout
 *
 * @author Jonathan M. Bardin
 */
@Component
@Provides(specifications = {Controller.class,WisitAuthService.class})
@Instantiate
public class WisitLoginController extends DefaultController implements WisitAuthService {

    private static final String ADMIN = "admin";
    private static final String SESSION_KEY_WISIT_USER = "wisit-user";
    
    /**
     * Default wisitUser account for the wisdom terminal.
     */
    private final Credential wisitUser = new Credential(ADMIN,ADMIN);

    /**
     * ApplicationConfiguration service (application.conf), use to retrieve wisit username and password.
     */
    @Requires
    private ApplicationConfiguration conf;

    @Validate
    private void start(){
        //Get the credentials from the application.conf

        String username = conf.get(SESSION_KEY_WISIT_USER);
        String pass = conf.get(WISIT_PASS);

        if(username != null){
            wisitUser.setUser(username);
        }

        if(pass != null){
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
    @Route(method = POST,uri = "/wisit/login")
    public Result login(@Body Credential credential) {
        if(credential.getUser() == null || credential.getPass() == null){
            return badRequest();
        }

        if(!credential.equals(wisitUser)){
            return unauthorized();
        }

        session().put(SESSION_KEY_WISIT_USER, credential.getUser());
        return ok(UUID.randomUUID().toString());
    }

    /**
     * Logout the user, destroy its session.
     * @return OK
     */
    @Route(method = GET,uri = "/wisit/logout")
    public Result logout(){
        session().clear();
        return ok();
    }

    /**
     * #WisitAuthService implementation#
     *
     * @return True is the user has a valid authenticated session, false otherwise.
     */
    public boolean isAuthorised(){
        return wisitUser.getUser().equals(session().get(SESSION_KEY_WISIT_USER));
    }
}
