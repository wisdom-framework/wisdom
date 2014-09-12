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
package controllers.interceptor;

import org.apache.felix.ipojo.annotations.Requires;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.HttpParameter;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.content.Json;
import org.wisdom.api.cookies.SessionCookie;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

import java.net.URL;


@Controller
@MyCustomAnnotation("hello wisdom")
public class MyInterceptorController extends DefaultController {

    @Requires
    Json json;

    @Route(method = HttpMethod.GET, uri = "/interception/my")
    public Result get(@HttpParameter("url") URL url, @HttpParameter SessionCookie session) {
        return ok(json.newObject().put("url", url.toExternalForm()).put("message", session.get("data")));
    }
}
