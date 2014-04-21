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
package controllers;

import com.google.common.collect.ImmutableList;
import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.router.RouteBuilder;

import java.util.Collections;
import java.util.List;

@Controller
public class Documentation extends DefaultController {

    @Route(method = HttpMethod.GET, uri = "/favicon.ico")
    public Result getFavicon(){
        return redirect("/assets/images/favicon.png");
    }

    @Requires
    ApplicationConfiguration configuration;

    @Route(method = HttpMethod.GET, uri = "/documentation")
    public Result doc() {
        return redirect("/assets/index.html");
    }

    /**
     * Default implementation of the routes method.
     *
     * @return an empty list. The router must also check for the {@link org.wisdom.api.annotations
     * .Route} annotations.
     */
    @Override
    public List<org.wisdom.api.router.Route> routes() {
        // If documentation.standalone is not set to false, register a route on /
        if (configuration.getBooleanWithDefault("documentation.standalone", true)) {
            return ImmutableList.of(new RouteBuilder().route(HttpMethod.GET).on("/").to(this, "doc"));
        } else {
            return Collections.emptyList();
        }
    }
}
