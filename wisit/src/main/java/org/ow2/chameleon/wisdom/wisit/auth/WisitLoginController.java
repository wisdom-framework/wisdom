package org.ow2.chameleon.wisdom.wisit.auth;

import org.apache.felix.ipojo.annotations.*;
import org.ow2.chameleon.wisdom.api.Controller;
import org.ow2.chameleon.wisdom.api.DefaultController;
import org.ow2.chameleon.wisdom.api.annotations.Body;
import org.ow2.chameleon.wisdom.api.annotations.Route;
import org.ow2.chameleon.wisdom.api.configuration.ApplicationConfiguration;
import org.ow2.chameleon.wisdom.api.http.Result;

import java.util.UUID;

import static org.ow2.chameleon.wisdom.api.http.HttpMethod.GET;
import static org.ow2.chameleon.wisdom.api.http.HttpMethod.POST;

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
            wisitUser.user=username;
        }

        if(pass != null){
            wisitUser.pass=pass;
        }
    }

    @Route(method = POST,uri = "/wisit/login")
    public Result login(@Body Credential credential) {
        if(credential.user == null || credential.pass == null){
            return badRequest();
        }

        if(!credential.equals(wisitUser)){
            return unauthorized();
        }

        session().put("wisit-user", credential.user);
        return ok(UUID.randomUUID().toString());
    }


    @Route(method = GET,uri = "/wisit/logout")
    public Result logout(){
        session().clear();
        return ok();
    }

    public boolean isAuthorised(){
        return wisitUser.user.equals(session().get("wisit-user"));
    }
}
