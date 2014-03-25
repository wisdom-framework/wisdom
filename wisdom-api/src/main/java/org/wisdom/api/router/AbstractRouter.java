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
package org.wisdom.api.router;

import com.google.common.collect.ImmutableMap;
import org.wisdom.api.Controller;
import org.wisdom.api.http.HttpMethod;

import java.util.Map;

/**
 * A default implementation of the router interface.
 */
public abstract class AbstractRouter implements Router {


    @Override
    public Route getRouteFor(String method, String uri) {
        return getRouteFor(HttpMethod.from(method), uri);
    }

    @Override
    public String getReverseRouteFor(Class<? extends Controller> clazz, String method, Map<String,
            Object> params) throws RoutingException {
        return getReverseRouteFor(clazz.getName(), method, params);
    }

    @Override
    public String getReverseRouteFor(String className, String method) {
        return getReverseRouteFor(className, method, null);
    }

    @Override
    public String getReverseRouteFor(Controller controller, String method, Map<String, Object> params) {
        return getReverseRouteFor(controller.getClass(), method, params);
    }

    @Override
    public String getReverseRouteFor(Class<? extends Controller> clazz, String method) {
        return getReverseRouteFor(clazz, method, null);
    }

    @Override
    public String getReverseRouteFor(Controller controller, String method) {
        return getReverseRouteFor(controller.getClass(), method, null);
    }

    @Override
    public String getReverseRouteFor(Controller controller, String method, String var1, Object val1) {
        return getReverseRouteFor(controller, method, ImmutableMap.<String, Object>of(var1, val1));
    }

    @Override
    public String getReverseRouteFor(Controller controller, String method, String var1, Object val1, String var2, Object val2) {
        return getReverseRouteFor(controller, method, ImmutableMap.<String, Object>of(var1, val1, var2, val2));
    }

    @Override
    public String getReverseRouteFor(Controller controller, String method, String var1, Object val1, String var2,
                                     Object val2, String var3, Object val3) {
        return getReverseRouteFor(controller, method, ImmutableMap.<String, Object>of(var1, val1, var2, val2, var3,
                val3));
    }

    @Override
    public String getReverseRouteFor(Controller controller, String method, String var1, Object val1, String var2, Object val2, String var3, Object val3, String var4, Object val4) {
        return getReverseRouteFor(controller, method, ImmutableMap.<String, Object>of(var1, val1, var2, val2, var3,
                val3, var4, val4));
    }

    @Override
    public String getReverseRouteFor(Controller controller, String method, String var1, Object val1, String var2, Object val2, String var3, Object val3, String var4, Object val4, String var5, Object val5) {
        return getReverseRouteFor(controller, method, ImmutableMap.<String, Object>of(var1, val1, var2, val2, var3,
                val3, var4, val4, var5, val5));
    }

    @Override
    public String getReverseRouteFor(Class<? extends Controller> clazz, String method, String var1, Object val1) {
        return getReverseRouteFor(clazz, method, ImmutableMap.<String, Object>of(var1, val1));
    }

    @Override
    public String getReverseRouteFor(Class<? extends Controller> clazz, String method, String var1, Object val1, String var2, Object val2) {
        return getReverseRouteFor(clazz, method, ImmutableMap.<String, Object>of(var1, val1, var2, val2));
    }

    @Override
    public String getReverseRouteFor(Class<? extends Controller> clazz, String method, String var1, Object val1, String var2,
                                     Object val2, String var3, Object val3) {
        return getReverseRouteFor(clazz, method, ImmutableMap.<String, Object>of(var1, val1, var2, val2, var3,
                val3));
    }

    @Override
    public String getReverseRouteFor(Class<? extends Controller> clazz, String method, String var1, Object val1, String var2, Object val2, String var3, Object val3, String var4, Object val4) {
        return getReverseRouteFor(clazz, method, ImmutableMap.<String, Object>of(var1, val1, var2, val2, var3,
                val3, var4, val4));
    }

    @Override
    public String getReverseRouteFor(Class<? extends Controller> clazz, String method, String var1, Object val1, String var2, Object val2, String var3, Object val3, String var4, Object val4, String var5, Object val5) {
        return getReverseRouteFor(clazz, method, ImmutableMap.<String, Object>of(var1, val1, var2, val2, var3,
                val3, var4, val4, var5, val5));
    }
}
