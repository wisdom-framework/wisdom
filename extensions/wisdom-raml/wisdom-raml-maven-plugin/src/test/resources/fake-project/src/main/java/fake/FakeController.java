
package fake;

import aQute.bnd.annotation.ConsumerType;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.*;
import org.wisdom.api.annotations.QueryParameter;
import org.wisdom.api.http.*;

import javax.enterprise.inject.Produces;
import javax.validation.constraints.NotNull;

import static org.wisdom.api.http.HttpMethod.*;

/**
 * A good old fake controller.
 *
 * @version 1.3
 */
@Controller
@Path("/hello")
public class FakeController extends DefaultController{

    /**
     * Super secret.
     */
    private String secret;

    @Route(method = GET,uri = "", produces = "text/plain")
    public Result hello(){
        return ok("Hello princess");
    }

    @Route(method = GET,uri = "/login",produces = "text/html")
    public Result login(){
        return ok();
    }

    @Route(method = POST,uri = "/login",accepts = "application/x-www-form-urlencoded",produces = "text/html")
    public Result login(@FormParameter("email") final String email, @FormParameter("pass") final String pass){
        return ok();
    }

    @Route(method = GET,uri = "/{name}")
    public Result hello(@Parameter(value="name") String name){
        return ok("Hello "+name);
    }

    @Route(method = GET,uri = "/{name}")
    public Result hello(@Parameter(value="name") String name){
        return ok("Hello "+name);
    }

    /**
     * This method print some stuff esse!
     */
    @Route(method = GET,uri = "/{name}/french")
    public Result helloFrench(@Parameter(value="name") String name){
        return ok("Bonjour "+name);
    }

    @Route(method = GET,uri = "/{name}/spanish")
    public Result helloSpanish(@Parameter(value="name") String name){
        return ok("Ola "+name);
    }

    @Route(method = GET,uri = "/{name}/spanish/filter")
    public Result helloSpanishFilter(@Parameter(value="name") String name,@QueryParameter("filter") String filter ){
        return ok("Ola "+name);
    }

    @Route(method = GET,uri = "/carembar/{pepper}")
    public Result carembar(@Parameter(value="pepper") String name, @QueryParameter(value="lang") @DefaultValue("english") String lang){
        return ok("Hello "+name);
    }

    /**
     * Just a post, you know ;)
     *
     * @body.sample  {lang : 'english', name: 'john'}
     *
     * @param name
     * @param lang
     * @return
     */
    @Route(method = POST,uri = "/carembar/{pepper}",accepts = {"text/json","text/xml"},produces = {"text/plain","text/json"})
    public Result carembarPOST(@Parameter(value="pepper") String name, @Body String lang){
        return ok("Hello "+name);
    }


    @Route(method = OPTIONS,uri = "/tricky")
    public Result tricky(){
        return ok("Hello ");
    }

    @Route(method = PUT,uri = "/trickyNotAChild")
    public Result trickyNotAChild(){
        return ok("Hello ");
    }

    @Route(method = GET,uri = "/mandatory")
    public Result mandatory(@NotNull @QueryParameter("hero") String hero){
        return ok("yes ");
    }

    public void shouldBeignoreCauseItsnotAroute(){

    }
}
