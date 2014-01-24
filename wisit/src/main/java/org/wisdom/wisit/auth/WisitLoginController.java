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

    /**
     * Default wisitUser account for the wisdom terminal.
     */
    private final Credential wisitUser = new Credential("admin","admin");

    /**
     * ApplicationConfiguration service (application.conf), use to retrieve wisit username and password.
     */
    @Requires
    private ApplicationConfiguration conf;

    @Validate
    private void start(){
        //Get the credentials from the application.conf

        String username = conf.get(WISIT_USER);
        String pass = conf.get(WISIT_PASS);

        if(username != null){
            wisitUser.setUser(username);
        }

        if(pass != null){
            wisitUser.setPass(pass);
        }
    }

    @Route(method = POST,uri = "/wisit/login")
    public Result login(@Body Credential credential) {
        if(credential.getUser() == null || credential.getPass() == null){
            return badRequest();
        }

        if(!credential.equals(wisitUser)){
            return unauthorized();
        }

        session().put("wisit-user", credential.getUser());
        return ok(UUID.randomUUID().toString());
    }


    @Route(method = GET,uri = "/wisit/logout")
    public Result logout(){
        session().clear();
        return ok();
    }

    public boolean isAuthorised(){
        return wisitUser.getUser().equals(session().get("wisit-user"));
    }
}
