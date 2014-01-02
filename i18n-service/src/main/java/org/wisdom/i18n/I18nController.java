package org.wisdom.i18n;

import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Result;
import org.wisdom.api.i18n.InternationalizationService;

import java.util.Map;

/**
 * A controller allowing clients to retrieve the internationalized messages.
 */
@Controller
public class I18nController extends DefaultController {


    @Requires
    InternationalizationService service;

    @Route(method= HttpMethod.GET, uri = "i18n/{key}")
    public Result getMessage(@Parameter("key") String key) {
        String message = service.get(context().request().languages(), key);
        if (message != null) {
            return ok(message).as(MimeTypes.TEXT);
        } else {
            return notFound("No message for " + key).as(MimeTypes.TEXT);
        }
    }

    @Route(method= HttpMethod.GET, uri = "i18n")
    public Result getMessages() {
        Map<String, String> messages = service.getAllMessages(context().request().languages());
        if (messages != null) {
            return ok(messages).json();
        } else {
            return notFound().json();
        }
    }

}
