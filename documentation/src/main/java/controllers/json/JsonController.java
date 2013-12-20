package controllers.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

@Controller
public class JsonController extends DefaultController {

    @Route(method= HttpMethod.GET, uri = "/json")
    public Result produce() {
        Person p = new Person("clement", 32);
        return ok(p).json();
    }

    @Route(method= HttpMethod.POST, uri = "/json")
    public Result consume() {
        System.out.println("Body: " + context().body());
        JsonNode content = context().body(JsonNode.class);
        return ok(content.toString());
    }

}
