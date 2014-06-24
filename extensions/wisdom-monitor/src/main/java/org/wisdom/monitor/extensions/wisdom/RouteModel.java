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

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.wisdom.api.content.Json;
import org.wisdom.api.router.Route;

/**
 * A simple class building a json representation for a route.
 */
public class RouteModel {

    /**
     * Creates the JSON representation of the given route.
     * @param route the route
     * @param json the JSON service
     * @return the json representation
     */
    public static ObjectNode from(Route route, Json json) {
        return json.newObject().put("url", route.getUrl())
                .put("controller", route.getControllerClass().getName())
                .put("method", route.getControllerMethod().getName())
                .put("http_method", route.getHttpMethod().toString());
    }
}
