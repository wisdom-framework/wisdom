/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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
package controllers.negotiation;

import com.google.common.collect.ImmutableMap;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Body;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Path;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

import java.util.Map;

/**
 * A controller with `accepts` and `produces` attribute
 */
@Controller
@Path("/negotiation")
public class NegotiatedController extends DefaultController {

    Map<String, String> map = ImmutableMap.of("hello", "wisdom");

    @Route(method= HttpMethod.GET, uri="/", produces = "application/xml")
    public Result asXML() {
        return ok(map).xml();
    }

    @Route(method= HttpMethod.GET, uri="/", produces = "application/json")
    public Result asJson() {
        return ok(map).json();
    }

    @Route(method= HttpMethod.POST, uri="/consume",
            accepts = "application/xml")
    public Result fromXML(@Body Data form) {
        return ok(form).xml();
    }

    @Route(method= HttpMethod.POST, uri="/consume",
            accepts = "application/json")
    public Result fromJson(@Body Data form) {
        return ok(form).json();
    }

    @Route(method= HttpMethod.POST, uri="/consprod",
            accepts = "application/xml",
            produces = "application/xml")
    public Result fromXMLAsXml(@Body Data form) {
        return ok(form).xml();
    }

    @Route(method= HttpMethod.POST, uri="/consprod",
            accepts = "application/json",
            produces = "application/json")
    public Result fromJsonAsJson(@Body Data form) {
        return ok(form).json();
    }


}
