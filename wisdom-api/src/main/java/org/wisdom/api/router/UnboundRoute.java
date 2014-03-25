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

import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a route without controller and method.
 * It generally results in a 404 response.
 */
public class UnboundRoute extends Route {

    private final List<RouteUtils.Argument> arguments;

    /**
     * Main constructor.
     *
     * @param httpMethod the method
     * @param uri        the uri
     */
    public UnboundRoute(HttpMethod httpMethod,
                        String uri) {
        super(httpMethod, uri, null, null);
        this.arguments = Collections.emptyList();
    }

    public boolean matches(HttpMethod method, String uri) {
        return false;
    }

    public boolean matches(String httpMethod, String uri) {
        return false;
    }

    public Map<String, String> getPathParametersEncoded(String uri) {
        return Collections.emptyMap();
    }

    public Result invoke() throws Throwable {
        return Results.notFound();
    }

    public List<RouteUtils.Argument> getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return "{"
                + getHttpMethod() + " " + getUrl() + " => "
                + "UNBOUND"
                + "}";
    }

    @Override
    public boolean isUnbound() {
        return true;
    }
}
