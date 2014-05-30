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
package org.wisdom.monitor.extensions.wisdom;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.architecture.Architecture;
import org.apache.felix.ipojo.architecture.InstanceDescription;
import org.apache.felix.ipojo.handlers.providedservice.ProvidedServiceDescription;
import org.apache.felix.ipojo.handlers.providedservice.ProvidedServiceHandlerDescription;
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
import org.wisdom.monitor.service.MonitorExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provide information about controller.
 * It also inspect invalid controller to determine the reason of the invalidity.
 */
@Controller
@Authenticated("Monitor-Authenticator")
public class ControllerExtension extends DefaultController implements MonitorExtension {

    @View("monitor/controllers")
    Template template;

    @Requires
    org.wisdom.api.Controller[] controllers;

    @Requires
    Router router;

    @Requires
    Json json;

    @Requires
    Architecture[] architectures;

    /**
     * @return the extension's main page.
     */
    @Route(method = HttpMethod.GET, uri = "/monitor/controllers")
    public Result index() {
        return ok(render(template));
    }

    /**
     * @return the JSON structure read by the HTML page.
     */
    @Route(method = HttpMethod.GET, uri = "/monitor/controllers/controllers")
    public Result getControllers() {
        ObjectNode node = json.newObject();

        ArrayNode array = json.newArray();
        for (org.wisdom.api.Controller controller : controllers) {
            array.add(ControllerModel.from(controller, router, json));
        }

        for (InstanceDescription description : getInvalidControllers()) {
            array.add(ControllerModel.from(description, json));
        }

        node.put("controllers", array);
        node.put("invalid", getInvalidControllers().size());

        return ok(node);
    }

    private List<InstanceDescription> getInvalidControllers() {
        List<InstanceDescription> invalid = new ArrayList<>();
        for (Architecture architecture : architectures) {
            final InstanceDescription description = architecture.getInstanceDescription();
            // Is it invalid ?
            if (description.getState() == ComponentInstance.INVALID) {
                // Is it a controller
                ProvidedServiceHandlerDescription hd = (ProvidedServiceHandlerDescription)
                        description.getHandlerDescription("org.apache.felix.ipojo:provides");
                if (hd != null) {
                    for (ProvidedServiceDescription psd : hd.getProvidedServices()) {
                        if (Arrays.asList(psd.getServiceSpecifications()).contains(org.wisdom.api.Controller.class
                                .getName())) {
                            invalid.add(description);
                        }
                    }
                }
            }
        }
        return invalid;
    }

    /**
     * @return {@literal Controllers}.
     */
    @Override
    public String label() {
        return "Controllers";
    }

    /**
     * @return {@literal /monitor/controllers}.
     */
    @Override
    public String url() {
        return "/monitor/controllers";
    }

    /**
     * @return {@literal Wisdom}.
     */
    @Override
    public String category() {
        return "Wisdom";
    }
}
