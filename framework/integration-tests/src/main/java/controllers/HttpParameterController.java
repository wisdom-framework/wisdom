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

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.HttpParameter;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.cookies.SessionCookie;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Request;
import org.wisdom.api.http.Result;

@Controller
public class HttpParameterController extends DefaultController {

    @Route(method = HttpMethod.GET, uri = "/parameter/http")
    public Result http(@HttpParameter Context context, @HttpParameter Request request,
                       @HttpParameter("header") String header, @HttpParameter SessionCookie session,
                       @HttpParameter Stuff stuff) {
        StringBuilder buffer = new StringBuilder();
        if (context != null) {
            buffer.append("OK");
        } else {
            buffer.append("KO");
        }

        if (request != null) {
            buffer.append("OK");
        } else {
            buffer.append("KO");
        }

        if (header != null) {
            buffer.append("OK");
        } else {
            buffer.append("KO");
        }

        if (session != null) {
            buffer.append("OK");
        } else {
            buffer.append("KO");
        }

        buffer.append(stuff.message);

        return ok(buffer.toString());
    }

}
