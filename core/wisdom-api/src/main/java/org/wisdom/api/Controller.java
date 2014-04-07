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
package org.wisdom.api;

import org.wisdom.api.router.Route;

import java.util.List;

/**
 * Controller interface.
 * Every component willing to provide <em>actions</em> must publish the controller service.
 * The router implementation is bound to all controllers and dispatches the request.
 */
public interface Controller {

    /**
     * Gets the list of routes offered by this controller.
     * This list is ordered, meaning that the first routes are evaluated before the others. As soon as the router is
     * finding a route matching the request, it delegates the request to the target action.
     * @return the list of routes.
     */
    public List<Route> routes();
}
