package controllers.content;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Body;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

@Controller
public class FormController extends DefaultController {

    public static class Form {
        // Form Parameter
        String name;
        int age;

        // Not necessary set
        byte[] content = new byte[0];

        // Parameter
        String id;
    }

    @Requires
    Json json;

    @Route(method = HttpMethod.POST, uri="/content/form/{id}")
    public Result form(@Body Form form) {
        ObjectNode node = json.newObject()
                .put("name", form.name)
                .put("age", form.age)
                .put("content", form.content.length)
                .put("id", form.id);
        return ok(json.newObject()
                .put("content-type", context().request().contentType())
                .set("content", node));
    }

}
