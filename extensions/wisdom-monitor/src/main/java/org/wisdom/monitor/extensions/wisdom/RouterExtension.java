package org.wisdom.monitor.extensions.wisdom;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.felix.ipojo.annotations.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.View;
import org.wisdom.api.content.Json;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.Router;
import org.wisdom.api.security.Authenticated;
import org.wisdom.api.templates.Template;
import org.wisdom.monitor.extensions.security.MonitorAuthenticator;
import org.wisdom.monitor.service.MonitorExtension;

/**
 * Provide information about available routes.
 */
@Controller
@Authenticated(MonitorAuthenticator.class)
public class RouterExtension extends DefaultController implements MonitorExtension {
    private static final Logger LOGGER = LoggerFactory.getLogger(RouterExtension.class);

    @View("monitor/routes")
    Template template;

    @Requires
    Router router;

    @Requires
    Json json;

    @Route(method = HttpMethod.GET, uri = "/monitor/routes")
    public Result index() {
        return ok(render(template));
    }

    @Route(method = HttpMethod.GET, uri = "/monitor/routes/routes")
    public Result getRoutes() {
        ArrayNode array = json.newArray();
        for (org.wisdom.api.router.Route route : router.getRoutes()) {
            array.add(
                    json
                            .newObject()
                            .put("url", route.getUrl())
                            .put("controller", route.getControllerClass().getName())
                            .put("method", route.getControllerMethod().getName())
                            .put("http_method", route.getHttpMethod().toString())
            );
        }
        return ok(array);
    }

    @Override
    public String label() {
        return "Routes";
    }

    @Override
    public String url() {
        return "/monitor/routes";
    }

    @Override
    public String category() {
        return "Wisdom";
    }
}
