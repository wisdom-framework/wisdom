package org.ow2.chameleon.wisdom.wisit.auth;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.ow2.chameleon.wisdom.api.Controller;
import org.ow2.chameleon.wisdom.api.DefaultController;
import org.ow2.chameleon.wisdom.api.annotations.Body;
import org.ow2.chameleon.wisdom.api.annotations.Route;
import org.ow2.chameleon.wisdom.api.cookies.SessionCookie;
import org.ow2.chameleon.wisdom.api.http.Result;
import org.ow2.chameleon.wisdom.wisit.auth.WisitAuth;
import org.ow2.chameleon.wisdom.wisit.auth.WisitAuthService;

import java.util.UUID;

import static org.ow2.chameleon.wisdom.api.http.HttpMethod.GET;
import static org.ow2.chameleon.wisdom.api.http.HttpMethod.POST;

/**
 * Created with IntelliJ IDEA.
 * User: barjo
 * Date: 11/28/13
 * Time: 5:17 PM
 * To change this template use File | Settings | File Templates.
 */
@Component
@Provides(specifications = {Controller.class,WisitAuthService.class})
@Instantiate
public class WisitLoginController extends DefaultController implements WisitAuthService {

    @Route(method = POST,uri = "/wisit/login")
    public Result login(@Body WisitAuth auth) {
        if(auth.user == null || auth.pass == null){
            return badRequest();
        }

        if(!auth.user.equals("admin") || !auth.pass.equals("admin")){
            return unauthorized();
        }

        session().put("user",auth.user);

        return ok(UUID.randomUUID().toString());
    }


    @Route(method = GET,uri = "/wisit/logout")
    public Result logout(){
        session().clear();
        return ok();
    }

    public boolean isAuthorised(){
        String user = session().getData().get("user");

        if(user == null || !user.equals("admin")){
            return false;
        }

        return true;
    }
}
