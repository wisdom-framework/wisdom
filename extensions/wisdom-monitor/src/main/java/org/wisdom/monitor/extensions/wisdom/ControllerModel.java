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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.architecture.HandlerDescription;
import org.apache.felix.ipojo.architecture.InstanceDescription;
import org.apache.felix.ipojo.handlers.dependency.DependencyDescription;
import org.apache.felix.ipojo.handlers.dependency.DependencyHandlerDescription;
import org.apache.felix.ipojo.util.DependencyModel;
import org.wisdom.api.Controller;
import org.wisdom.api.content.Json;
import org.wisdom.api.model.Crud;
import org.wisdom.api.router.Route;
import org.wisdom.api.router.Router;
import org.wisdom.api.templates.Template;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple class building a json representation for a controller.
 */
public class ControllerModel {

    /**
     * The regex to extract the template name from the generated filter.
     */
    public static final Pattern TEMPLATE_FILTER_PATTERN = Pattern.compile("\\(name=(.*)\\)");

    /**
     * The regex to extract the entity name from the generated filter.
     */
    public static final Pattern MODEL_FILTER_PATTERN = Pattern.compile("\\(entity\\.classname=(.*)\\)");

    /**
     * Creates the Json representation for an invalid controller.
     *
     * @param description the instance's description
     * @param json        the json service
     * @return the json representation
     */
    public static JsonNode from(InstanceDescription description, Json json) {
        ObjectNode node = json.newObject();
        node.put("classname", description.getComponentDescription().getName())
                .put("invalid", description.getState() == ComponentInstance.INVALID)
                .put("reason", extractInvalidityReason(description));
        return node;
    }

    /**
     * Creates the Json representation for a valid (exposed) controller.
     *
     * @param controller the controller
     * @param router     the router
     * @param json       the json service
     * @return the json representation
     */
    public static JsonNode from(Controller controller, Router router, Json json) {
        ObjectNode node = json.newObject();
        node.put("classname", controller.getClass().getName())
                .put("invalid", false)
                .put("routes", getRoutes(controller, router, json));
        return node;
    }

    private static ArrayNode getRoutes(Controller controller, Router router, Json json) {
        ArrayNode array = json.newArray();
        for (Route route : router.getRoutes()) {
            if (route.getControllerClass().equals(controller.getClass())) {
                array.add(RouteModel.from(route, json));
            }
        }
        return array;
    }

    private static String extractInvalidityReason(InstanceDescription description) {
        // As it generally comes from the @Requires, let's have a look
        DependencyHandlerDescription deps = (DependencyHandlerDescription) description.getHandlerDescription("org.apache.felix.ipojo:requires");
        if (deps != null && !deps.isValid()) {
            for (DependencyDescription dd : deps.getDependencies()) {
                if (dd.getState() == DependencyModel.UNRESOLVED) {

                    if (dd.getSpecification().equals(Template.class.getName())) {
                        // View case
                        return "Missing template : " + extractTemplateName(dd.getFilter());
                    } else if (dd.getSpecification().equals(Crud.class.getName())) {
                        // Crud case
                        return "Missing model : " + extractModelName(dd.getFilter());
                    }

                    // General case
                    return "Missing service : " + dd.getSpecification();
                }
            }
        }

        // It's not the dependency handler
        for (HandlerDescription hd : description.getHandlers()) {
            if (!hd.isValid()) {
                return "Invalid handler " + hd.getHandlerName();
            }
        }

        return "";
    }

    /**
     * Extracts the template name from the given LDAP filter.
     * The LDAP filter is structure as established in the WisdomViewVisitor.
     *
     * @param filter the filter
     * @return the extracted template name
     */
    private static String extractTemplateName(String filter) {
        Matcher matcher = TEMPLATE_FILTER_PATTERN.matcher(filter);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            return "Unknown template";
        }
    }

    /**
     * Extracts the model name from the given LDAP filter.
     * The LDAP filter is structure as established in the WisdomModelVisitor.
     *
     * @param filter the filter
     * @return the extracted template name
     */
    private static String extractModelName(String filter) {
        Matcher matcher = MODEL_FILTER_PATTERN.matcher(filter);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            return "Unknown model";
        }
    }


}
