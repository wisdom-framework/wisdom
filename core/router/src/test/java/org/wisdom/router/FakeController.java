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
package org.wisdom.router;

import org.wisdom.api.DefaultController;
import org.wisdom.api.bodies.NoHttpBody;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.api.router.Route;

import java.util.Collections;
import java.util.List;

/**
 * A fake controller.
 */
public class FakeController extends DefaultController {

    private List<Route> routes = Collections.emptyList();

    public FakeController() {

    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }

    public Result bar() {
        return Results.ok(new Object());
    }

    public Result foo() {
        return status(Result.CREATED);
    }



    @Override
    public List<Route> routes() {
        return routes;
    }
}
