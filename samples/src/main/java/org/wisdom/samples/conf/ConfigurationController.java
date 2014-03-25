package org.wisdom.samples.conf;

import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

@Controller
public class ConfigurationController extends DefaultController {

    @Requires
    ApplicationConfiguration configuration;

    @Route(method= HttpMethod.GET, uri = "/conf")
    public Result get() {
        return ok(configuration.asMap()).json();
    }
}
